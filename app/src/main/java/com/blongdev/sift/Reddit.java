package com.blongdev.sift;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.blongdev.sift.database.SiftContract;
import com.squareup.okhttp.internal.Util;

import net.dean.jraw.ApiException;
import net.dean.jraw.RedditClient;
import net.dean.jraw.http.LoggingMode;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.http.oauth.OAuthHelper;
import net.dean.jraw.models.Comment;
import net.dean.jraw.models.LoggedInAccount;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.models.Thing;
import net.dean.jraw.models.VoteDirection;
import net.dean.jraw.models.attr.Votable;

import java.util.UUID;

/**
 * Created by Brian on 3/21/2016.
 */
public class Reddit {

    private static final String REFRESH_KEY = "refreshKey";
    private static final String CLIENT_ID = "pFsQuM_0DQdv-g";
    private static final String REDIRECT_URL = "http://www.google.com";

    public static final String ACCOUNT_TYPE = "com.blongdev";
    public static final String GENERAL_ACCOUNT = "General";


    public static final long SECONDS_PER_MINUTE = 60L;
    public static final long SYNC_INTERVAL_IN_MINUTES = 60L;
    public static final long SYNC_INTERVAL = SYNC_INTERVAL_IN_MINUTES * SECONDS_PER_MINUTE;

    private static Reddit instance = new Reddit();
    public RedditClient mRedditClient;

    public UserAgent mUserAgent;
    public String mRefreshToken;
    public Credentials mCredentials;
    public OAuthHelper mOAuthHelper;
    public LoggedInAccount mMe;
    Account mAccount;


    private static final String LOG_TAG = "Reddit Singleton";


    public static Reddit getInstance() {
        return instance;
    }

    private Reddit() {
        mUserAgent = getUserAgent();
        mRedditClient = new RedditClient(mUserAgent);
        mCredentials = getCredentials();
        mOAuthHelper = mRedditClient.getOAuthHelper();

        if (mRedditClient.isAuthenticated()) {
            mMe = mRedditClient.me();
        }

        //TODO turn off logging for release builds
        mRedditClient.setLoggingMode(LoggingMode.ALWAYS);
        mRedditClient.setSaveResponseHistory(true);
    }

    public static UserAgent getUserAgent () {
        return UserAgent.of("Android", "com.blongdev.sift", BuildConfig.VERSION_NAME, "blongdev");
    }

    public static Credentials getCredentials() {
        return Credentials.installedApp(CLIENT_ID, REDIRECT_URL);
    }

    public void addGeneralAccount(Context context) {
        CreateSyncAccount(context, GENERAL_ACCOUNT);
    }

    public void refreshKey(Context context, OnRefreshCompleted callback) {
        //TODO make work with multiple accounts
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(SiftContract.Accounts.CONTENT_URI, null, null, null, null);
            if (cursor != null) {
                 if (cursor.moveToFirst()) {
                    mRefreshToken = cursor.getString(cursor.getColumnIndex(SiftContract.Accounts.COLUMN_REFRESH_KEY));
                    if (mRefreshToken != null && !mRefreshToken.isEmpty()) {
                        new RefreshTokenTask(callback).execute();
                        return;
                    }
                 } else {
                    new GetUserlessTask(context, callback).execute();
                 }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

    }

    private final class UserChallengeTask extends AsyncTask<String, Void, OAuthData> {

        private Context mContext;

        public UserChallengeTask(Context context) {
            Log.v(LOG_TAG, "UserChallengeTask()");
            mContext = context;
        }

        @Override
        protected OAuthData doInBackground(String... params) {
            Log.v(LOG_TAG, "doInBackground()");
            Log.v(LOG_TAG, "params[0]: " + params[0]);
            try {
                OAuthData oAuthData =  mOAuthHelper.onUserChallenge(params[0], mCredentials);
                if (oAuthData != null) {
                    mRedditClient.authenticate(oAuthData);
                    addUser(mContext);

                } else {
                    Log.e(LOG_TAG, "Passed in OAuthData was null");
                }
            } catch (IllegalStateException | NetworkException | OAuthException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(OAuthData oAuthData) {
            Log.v(LOG_TAG, "onPostExecute()");
        }
    }

    public AsyncTask runUserChallengeTask(String url, Context context) {
        return new UserChallengeTask(context).execute(url);
    }

    public void addUser(Context context) {
        if (!mRedditClient.isAuthenticated()) {
            return;
        }

        mMe = mRedditClient.me();
        String username = mMe.getFullName();
        String serverId = mMe.getId();
        long date = mMe.getCreated().getTime();
        long commentKarma = mMe.getCommentKarma();
        long linkKarma = mMe.getLinkKarma();

        //user
        ContentValues cv = new ContentValues();
        cv.put(SiftContract.Users.COLUMN_USERNAME, username);
        cv.put(SiftContract.Users.COLUMN_DATE_CREATED, date);

        Uri userUri = context.getContentResolver().insert(SiftContract.Users.CONTENT_URI, cv);
        long userId = ContentUris.parseId(userUri);

        cv.clear();

        //account
        String refreshToken = mRedditClient.getOAuthData().getRefreshToken();

        cv.put(SiftContract.Accounts.COLUMN_REFRESH_KEY, refreshToken);
        cv.put(SiftContract.Accounts.COLUMN_USER_ID, userId);
        cv.put(SiftContract.Accounts.COLUMN_USERNAME, username);
        context.getContentResolver().insert(SiftContract.Accounts.CONTENT_URI, cv);

        cv.clear();
        //subreddits

        //add account for sync adapter
        mAccount = CreateSyncAccount(context, username);

    }

    private final class RefreshTokenTask extends AsyncTask<String, Void, OAuthData> {

        private OnRefreshCompleted mOnRefreshCompleted;

        public RefreshTokenTask(OnRefreshCompleted activity) {
            mOnRefreshCompleted = activity;
        }

        @Override
        protected OAuthData doInBackground(String... params) {
            if (!mRefreshToken.isEmpty()) {
                mOAuthHelper.setRefreshToken(mRefreshToken);
                try {
                    OAuthData finalData = mOAuthHelper.refreshToken(mCredentials);
                    mRedditClient.authenticate(finalData);
                    if (mRedditClient.isAuthenticated()) {
                        Log.v(LOG_TAG, "Authenticated");
                    }
                } catch (OAuthException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(OAuthData oAuthData) {
            Log.v(LOG_TAG, "onPostExecute()");
            mOnRefreshCompleted.onRefreshCompleted();
        }
    }



    private final class GetUserlessTask extends AsyncTask<String, Void, Void> {

        Context mContext;
        private OnRefreshCompleted mOnRefreshCompleted;

        public GetUserlessTask(Context context, OnRefreshCompleted activity) {
            mContext = context;
            mOnRefreshCompleted = activity;
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                //TODO use device id rather than random uuid
                String android_id = Settings.Secure.getString(mContext.getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                UUID uuid = UUID.randomUUID();
                Credentials credentials = Credentials.userlessApp(CLIENT_ID, uuid);
                OAuthData authData = mRedditClient.getOAuthHelper().easyAuth(credentials);
                mRedditClient.authenticate(authData);
                if (mRedditClient.isAuthenticated()) {
                    Log.v(LOG_TAG, "Authenticated");
                }
            } catch (OAuthException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            Log.v(LOG_TAG, "onPostExecute()");
            mOnRefreshCompleted.onRefreshCompleted();
        }
    }



    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    public static Account CreateSyncAccount(Context context, String accountName) {
        // Create the account type and default account
        Account newAccount = new Account(
                accountName, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(context.ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            ContentResolver.setIsSyncable(newAccount, SiftContract.AUTHORITY, 1);
            ContentResolver.setSyncAutomatically(newAccount, SiftContract.AUTHORITY, true);
            ContentResolver.addPeriodicSync(newAccount, SiftContract.AUTHORITY, Bundle.EMPTY, SYNC_INTERVAL);


            return newAccount;

        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
        }
        return null;
    }
//
//    public AsyncTask refreshToken() {
//        return new RefreshTokenTask().execute();
//    }

    public interface OnRefreshCompleted{
        void onRefreshCompleted();
        void restartActivity();
    }

    //TODO implement for multiple accounts
    public void removeAccounts (Context context, OnRefreshCompleted onRefreshCompleted) {
        //revoke access token
        if (mRedditClient.isAuthenticated() && mRedditClient.hasActiveUserContext()) {
            new RevokeTokenTask(context, onRefreshCompleted).execute();
        }
    }

    private final class RevokeTokenTask extends AsyncTask<String, Void, Void> {

        Context mContext;
        private OnRefreshCompleted mOnRefreshCompleted;

        public RevokeTokenTask(Context context, OnRefreshCompleted activity) {
            mContext = context;
            mOnRefreshCompleted = activity;
        }

        @Override
        protected Void doInBackground(String... params) {

            Cursor cursor = null;
            try {
                cursor = mContext.getContentResolver().query(SiftContract.Accounts.CONTENT_URI, null, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        String accountId = cursor.getString(cursor.getColumnIndex(SiftContract.Accounts._ID));
                        mRefreshToken = cursor.getString(cursor.getColumnIndex(SiftContract.Accounts.COLUMN_REFRESH_KEY));
                        if (mRefreshToken != null && !mRefreshToken.isEmpty()) {
                            //revoking tokens causing crash
                            //mOAuthHelper.revokeAccessToken(mCredentials);
                            //mRedditClient.deauthenticate();
                            String selection = SiftContract.Accounts._ID + " =?";
                            String[] selectionArgs = new String[]{accountId};
                            mContext.getContentResolver().delete(SiftContract.Accounts.CONTENT_URI, selection, selectionArgs);
                            instance = new Reddit();
                        }
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            Log.v(LOG_TAG, "onPostExecute()");
            mOnRefreshCompleted.restartActivity();
        }
    }

    public static void votePost(Context context, String serverId, int vote) {
        new VotePostTask(context, serverId, vote).execute();
    }

    private static final class VotePostTask extends AsyncTask<String, Void, Void> {

        Context mContext;
        String mServerId;
        int mVote;

        public VotePostTask(Context context, String serverId, int vote) {
            mContext = context;
            mServerId = serverId;
            mVote = vote;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... params) {
            try {

                if (!instance.mRedditClient.isAuthenticated() || !instance.mRedditClient.hasActiveUserContext()) {
                    return null;
                }

                Submission sub = instance.mRedditClient.getSubmission(mServerId);
                //TODO rather then not sending archived posts, hide vote arrows
                if (sub == null || sub.isArchived()) {
                    return null;
                }
                net.dean.jraw.managers.AccountManager accountManager = new net.dean.jraw.managers.AccountManager(instance.mRedditClient);

                switch (mVote) {
                    case SiftContract.Posts.NO_VOTE:
                        accountManager.vote(sub, VoteDirection.NO_VOTE);
                        break;
                    case SiftContract.Posts.UPVOTE:
                        accountManager.vote(sub, VoteDirection.UPVOTE);
                        break;
                    case SiftContract.Posts.DOWNVOTE:
                        accountManager.vote(sub, VoteDirection.DOWNVOTE);
                        break;
                }

                ContentValues cv = new ContentValues();
                cv.put(SiftContract.Posts.COLUMN_VOTE, mVote);
                String selection = SiftContract.Posts.COLUMN_SERVER_ID + " = ?";
                String[] selectionArgs = new String[]{String.valueOf(mServerId)};
                int count = mContext.getContentResolver().update(SiftContract.Posts.CONTENT_URI, cv, selection, selectionArgs);
                Log.v("PostSyncAdapter", count + " vote updated");

            } catch (ApiException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {

        }
    }

    public static void voteComment(Context context, Comment comment, int vote) {
        new VoteCommentTask(context, comment, vote).execute();
    }

    private static final class VoteCommentTask extends AsyncTask<String, Void, Void> {

        Context mContext;
        Comment mComment;
        int mVote;

        public VoteCommentTask(Context context, Comment comment, int vote) {
            mContext = context;
            mComment = comment;
            mVote = vote;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... params) {
            try {

                if (!instance.mRedditClient.isAuthenticated() || !instance.mRedditClient.hasActiveUserContext()) {
                    return null;
                }

                //TODO rather then not sending archived comments, hide vote arrows
                if (mComment == null || mComment.isArchived()) {
                    return null;
                }
                net.dean.jraw.managers.AccountManager accountManager = new net.dean.jraw.managers.AccountManager(instance.mRedditClient);

                switch (mVote) {
                    case SiftContract.Posts.NO_VOTE:
                        accountManager.vote(mComment, VoteDirection.NO_VOTE);
                        break;
                    case SiftContract.Posts.UPVOTE:
                        accountManager.vote(mComment, VoteDirection.UPVOTE);
                        break;
                    case SiftContract.Posts.DOWNVOTE:
                        accountManager.vote(mComment, VoteDirection.DOWNVOTE);
                        break;
                }

            } catch (ApiException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {

        }
    }

    public static void subscribe(Context context, String name) {
        new SubscribeTask(context, name).execute();
    }

    private static final class SubscribeTask extends AsyncTask<String, Void, Void> {

        Context mContext;
        String mName;

        public SubscribeTask(Context context, String name) {
            mContext = context;
            mName = name;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... params) {

            if (!instance.mRedditClient.isAuthenticated() || !instance.mRedditClient.hasActiveUserContext()) {
                return null;
            }

            Subreddit subreddit  = instance.mRedditClient.getSubreddit(mName);

            //TODO rather then not sending archived comments, hide vote arrows
            if (subreddit == null) {
                return null;
            }

            net.dean.jraw.managers.AccountManager accountManager = new net.dean.jraw.managers.AccountManager(instance.mRedditClient);
            accountManager.subscribe(subreddit);

            ContentValues cv = new ContentValues();
            long subredditId = Utilities.getSubredditId(mContext, subreddit.getId());
            if (subredditId <= 0) {
                cv.put(SiftContract.Subreddits.COLUMN_NAME, subreddit.getDisplayName());
                cv.put(SiftContract.Subreddits.COLUMN_SERVER_ID, subreddit.getId());
                Uri subredditUri = mContext.getContentResolver().insert(SiftContract.Subreddits.CONTENT_URI, cv);
                subredditId = ContentUris.parseId(subredditUri);
                cv.clear();
            }

            //add subscription
            long accountId = Utilities.getAccountId(mContext, instance.mRedditClient);
            if (accountId > 0) {
                cv.put(SiftContract.Subscriptions.COLUMN_ACCOUNT_ID, accountId);
                cv.put(SiftContract.Subscriptions.COLUMN_SUBREDDIT_ID, subredditId);
                mContext.getContentResolver().insert(SiftContract.Subscriptions.CONTENT_URI, cv);
                cv.clear();
            }

            Log.v("PostSyncAdapter", "Subscribed");

            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {

        }
    }

    public static void unsubscribe(Context context, String name) {
        new UnsubscribeTask(context, name).execute();
    }

    private static final class UnsubscribeTask extends AsyncTask<String, Void, Void> {

        Context mContext;
        String mName;

        public UnsubscribeTask(Context context, String name) {
            mContext = context;
            mName = name;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... params) {

            if (!instance.mRedditClient.isAuthenticated() || !instance.mRedditClient.hasActiveUserContext()) {
                return null;
            }

            Subreddit subreddit  = instance.mRedditClient.getSubreddit(mName);

            //TODO rather then not sending archived comments, hide vote arrows
            if (subreddit == null) {
                return null;
            }

            net.dean.jraw.managers.AccountManager accountManager = new net.dean.jraw.managers.AccountManager(instance.mRedditClient);
            accountManager.unsubscribe(subreddit);

            long subredditId = Utilities.getSubredditId(mContext, subreddit.getId());

            String selection = SiftContract.Subscriptions.COLUMN_SUBREDDIT_ID + " = ?";
            String[] selectionArgs = new String[]{String.valueOf(subredditId)};
            int count = mContext.getContentResolver().delete(SiftContract.Subscriptions.CONTENT_URI, selection, selectionArgs);
            Log.v("PostSyncAdapter", "Unsubscribed");

            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {

        }
    }
}
