package com.blongdev.sift;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.blongdev.sift.database.SiftContract;
import com.fasterxml.jackson.databind.JsonNode;
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
import net.dean.jraw.models.Captcha;
import net.dean.jraw.models.Comment;
import net.dean.jraw.models.LoggedInAccount;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.models.Thing;
import net.dean.jraw.models.VoteDirection;
import net.dean.jraw.models.attr.Votable;


import java.net.URL;
import java.util.List;
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

    public static final String AUTHENTICATED = "com.blongdev.sift.AUTHENTICATED";


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
    //Account mAccount;


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

//    public void addGeneralAccount(Context context) {
//        CreateSyncAccount(context, GENERAL_ACCOUNT);
//    }

    public void refreshKey(Context context, OnRefreshCompleted callback) {
        //TODO make work with multiple accounts
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(SiftContract.Accounts.CONTENT_URI, null, null, null, null);
            if (cursor != null) {
                 if (cursor.moveToFirst()) {
                    mRefreshToken = cursor.getString(cursor.getColumnIndex(SiftContract.Accounts.COLUMN_REFRESH_KEY));
                    if (mRefreshToken != null && !mRefreshToken.isEmpty()) {
                        new RefreshTokenTask(callback, context).execute();
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

        runInitialSync(context);

    }

    private final class RefreshTokenTask extends AsyncTask<String, Void, OAuthData> {

        private OnRefreshCompleted mOnRefreshCompleted;
        private Context mContext;

        public RefreshTokenTask(OnRefreshCompleted activity, Context context) {
            mOnRefreshCompleted = activity;
            mContext = context;
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

            Intent refreshIntent = new Intent(AUTHENTICATED);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(refreshIntent);
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

            Intent refreshIntent = new Intent(AUTHENTICATED);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(refreshIntent);
        }
    }


    public void runInitialSync(Context context) {
        AccountManager accountManager = (AccountManager) context.getSystemService(context.ACCOUNT_SERVICE);
        Account account = accountManager.getAccountsByType(ACCOUNT_TYPE)[0];

        if (account != null) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            bundle.putBoolean(context.getString(R.string.initial_sync), true);
            ContentResolver.requestSync(account, SiftContract.AUTHORITY, bundle);
        }
    }


//    /**
//     * Create a new dummy account for the sync adapter
//     *
//     * @param context The application context
//     */
//    public static Account CreateSyncAccount(Context context, String accountName) {
//        // Create the account type and default account
//        Account newAccount = new Account(
//                accountName, ACCOUNT_TYPE);
//        // Get an instance of the Android account manager
//        AccountManager accountManager =
//                (AccountManager) context.getSystemService(context.ACCOUNT_SERVICE);
//        /*
//         * Add the account and account type, no password or user data
//         * If successful, return the Account object, otherwise report an error.
//         */
//        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
//            /*
//             * If you don't set android:syncable="true" in
//             * in your <provider> element in the manifest,
//             * then call context.setIsSyncable(account, AUTHORITY, 1)
//             * here.
//             */
//
//            ContentResolver.setIsSyncable(newAccount, SiftContract.AUTHORITY, 1);
//            ContentResolver.setSyncAutomatically(newAccount, SiftContract.AUTHORITY, true);
//            ContentResolver.addPeriodicSync(newAccount, SiftContract.AUTHORITY, Bundle.EMPTY, SYNC_INTERVAL);
//
//
//            return newAccount;
//
//        } else {
//            /*
//             * The account exists or some other error occurred. Log this, report it,
//             * or handle it internally.
//             */
//        }
//        return null;
//    }
//
//    public AsyncTask refreshToken() {
//        return new RefreshTokenTask().execute();
//    }

    public interface OnRefreshCompleted {
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
                Log.v("Reddit", count + " vote updated");

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

            Log.v("Reddit", "Subscribed");

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
            Log.v("Reddit", "Unsubscribed");

            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {

        }
    }

    public static void favoritePost(Context context, String serverId) {
        new FavoritePostTask(context, serverId).execute();
    }

    private static final class FavoritePostTask extends AsyncTask<String, Void, Void> {

        Context mContext;
        String mServerId;

        public FavoritePostTask(Context context, String serverId) {
            mContext = context;
            mServerId = serverId;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... params) {

            if (!instance.mRedditClient.isAuthenticated() || !instance.mRedditClient.hasActiveUserContext()) {
                return null;
            }

            Submission sub = instance.mRedditClient.getSubmission(mServerId);
            if (sub == null) {
                return null;
            }

            net.dean.jraw.managers.AccountManager accountManager = new net.dean.jraw.managers.AccountManager(instance.mRedditClient);
            try {
                accountManager.save(sub);

                ContentValues cv = new ContentValues();
                cv.put(SiftContract.Posts.COLUMN_SERVER_ID, sub.getId());
                cv.put(SiftContract.Posts.COLUMN_TITLE, sub.getTitle());
                cv.put(SiftContract.Posts.COLUMN_OWNER_USERNAME, sub.getAuthor());
                cv.put(SiftContract.Posts.COLUMN_SUBREDDIT_NAME, sub.getSubredditName());
                cv.put(SiftContract.Posts.COLUMN_POINTS, sub.getScore());
                cv.put(SiftContract.Posts.COLUMN_URL, sub.getUrl());
                cv.put(SiftContract.Posts.COLUMN_IMAGE_URL, getImageUrl(sub));
                cv.put(SiftContract.Posts.COLUMN_NUM_COMMENTS, sub.getCommentCount());
                cv.put(SiftContract.Posts.COLUMN_BODY, sub.getSelftext());
                cv.put(SiftContract.Posts.COLUMN_DOMAIN, sub.getDomain());
                cv.put(SiftContract.Posts.COLUMN_DATE_CREATED, sub.getCreatedUtc().getTime());
                cv.put(SiftContract.Posts.COLUMN_VOTE, sub.getVote().getValue());
                cv.put(SiftContract.Posts.COLUMN_FAVORITED, 1);
                Uri uri = mContext.getContentResolver().insert(SiftContract.Posts.CONTENT_URI, cv);
                long postId = ContentUris.parseId(uri);

                cv.clear();
                long accountId = Utilities.getAccountId(mContext, instance.mRedditClient);
                cv.put(SiftContract.Favorites.COLUMN_ACCOUNT_ID, accountId);
                cv.put(SiftContract.Favorites.COLUMN_POST_ID, postId);
                mContext.getContentResolver().insert(SiftContract.Favorites.CONTENT_URI, cv);

            } catch (ApiException e) {
                e.printStackTrace();
            }


            Log.v("Reddit", "Saved");

            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {

        }
    }

    public static void unfavoritePost(Context context, String serverId) {
        new UnfavoritePostTask(context, serverId).execute();
    }

    private static final class UnfavoritePostTask extends AsyncTask<String, Void, Void> {

        Context mContext;
        String mServerId;
        long mPostId;

        public UnfavoritePostTask(Context context, String serverId) {
            mContext = context;
            mServerId = serverId;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... params) {

            if (!instance.mRedditClient.isAuthenticated() || !instance.mRedditClient.hasActiveUserContext()) {
                return null;
            }

            Submission sub = instance.mRedditClient.getSubmission(mServerId);
            if (sub == null) {
                return null;
            }

            net.dean.jraw.managers.AccountManager accountManager = new net.dean.jraw.managers.AccountManager(instance.mRedditClient);
            try {
                accountManager.unsave(sub);

                long postId = Utilities.getSavedPostId(mContext, mServerId);

                if (postId > 0) {
                    String selection = SiftContract.Favorites.COLUMN_POST_ID + " = ?";
                    String[] selectionArgs = new String[]{String.valueOf(postId)};
                    int count = mContext.getContentResolver().delete(SiftContract.Favorites.CONTENT_URI, selection, selectionArgs);
                    Log.v("Reddit", count + " Unsaved");

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


    public static void commentOnPost(Context context, String serverId, String comment) {
        new CommentOnPostTask(context, serverId, comment).execute();
    }

    private static final class CommentOnPostTask extends AsyncTask<String, Void, Void> {

        Context mContext;
        String mServerId;
        String mComment;

        public CommentOnPostTask(Context context, String serverId, String comment) {
            mContext = context;
            mServerId = serverId;
            mComment =  comment;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... params) {

            if (!instance.mRedditClient.isAuthenticated() || !instance.mRedditClient.hasActiveUserContext()) {
                return null;
            }

            Submission sub = instance.mRedditClient.getSubmission(mServerId);
            if (sub == null) {
                return null;
            }

            net.dean.jraw.managers.AccountManager accountManager = new net.dean.jraw.managers.AccountManager(instance.mRedditClient);
            try {
                accountManager.reply(sub, mComment);
            } catch (ApiException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            Toast.makeText(mContext, mContext.getString(R.string.comment_posted), Toast.LENGTH_LONG).show();
        }
    }

    public static void replyToComment(Context context, Comment comment, String reply) {
        new ReplyToCommentTask(context, comment, reply).execute();
    }

    private static final class ReplyToCommentTask extends AsyncTask<String, Void, Void> {

        Context mContext;
        String mReply;
        Comment mComment;

        public ReplyToCommentTask(Context context, Comment comment, String reply) {
            mContext = context;
            mReply = reply;
            mComment =  comment;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... params) {

            if (!instance.mRedditClient.isAuthenticated() || !instance.mRedditClient.hasActiveUserContext()) {
                return null;
            }

            net.dean.jraw.managers.AccountManager accountManager = new net.dean.jraw.managers.AccountManager(instance.mRedditClient);
            try {
                accountManager.reply(mComment, mReply);

            } catch (ApiException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            Toast.makeText(mContext, mContext.getString(R.string.comment_posted), Toast.LENGTH_LONG).show();
        }
    }


    public static void addFriend(Context context, String username) {
        new AddFriendTask(context, username).execute();
    }

    private static final class AddFriendTask extends AsyncTask<String, Void, Void> {

        Context mContext;
        String mUsername;

        public AddFriendTask(Context context, String username) {
            mContext = context;
            mUsername = username;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... params) {

            if (!instance.mRedditClient.isAuthenticated() || !instance.mRedditClient.hasActiveUserContext()) {
                return null;
            }
            net.dean.jraw.models.Account user = instance.mRedditClient.getUser(mUsername);

            if (!user.isFriend()) {
                net.dean.jraw.managers.AccountManager accountManager = new net.dean.jraw.managers.AccountManager(instance.mRedditClient);
                accountManager.updateFriend(mUsername);
            }

            ContentValues cv = new ContentValues();
            cv.put(SiftContract.Users.COLUMN_SERVER_ID, user.getId());
            cv.put(SiftContract.Users.COLUMN_USERNAME, mUsername);
            Uri userUri = mContext.getContentResolver().insert(SiftContract.Users.CONTENT_URI, cv);
            long userId = ContentUris.parseId(userUri);
            cv.clear();

            //add friend
            long accountId = Utilities.getAccountId(mContext, instance.mRedditClient);
            cv.put(SiftContract.Friends.COLUMN_ACCOUNT_ID, accountId);
            cv.put(SiftContract.Friends.COLUMN_FRIEND_USER_ID, userId);
            mContext.getContentResolver().insert(SiftContract.Friends.CONTENT_URI, cv);

            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            Toast.makeText(mContext, mContext.getString(R.string.friend_added), Toast.LENGTH_LONG).show();
        }
    }


    public static void removeFriend(Context context, String username) {
        new RemoveFriendTask(context, username).execute();
    }

    private static final class RemoveFriendTask extends AsyncTask<String, Void, Void> {

        Context mContext;
        String mUsername;

        public RemoveFriendTask(Context context, String username) {
            mContext = context;
            mUsername = username;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... params) {

            if (!instance.mRedditClient.isAuthenticated() || !instance.mRedditClient.hasActiveUserContext()) {
                return null;
            }
            net.dean.jraw.models.Account user = instance.mRedditClient.getUser(mUsername);
            if (user.isFriend()) {
                net.dean.jraw.managers.AccountManager accountManager = new net.dean.jraw.managers.AccountManager(instance.mRedditClient);
                accountManager.deleteFriend(mUsername);
            }

            long userId = Utilities.getSavedUserId(mContext, mUsername);

            if (userId > 0) {
                String selection = SiftContract.Friends.COLUMN_FRIEND_USER_ID + " = ?";
                String[] selectionArgs = new String[]{String.valueOf(userId)};
                int count = mContext.getContentResolver().delete(SiftContract.Friends.CONTENT_URI, selection, selectionArgs);
                Log.v("Reddit", count + " Removed");

            }


            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            Toast.makeText(mContext, mContext.getString(R.string.friend_removed), Toast.LENGTH_LONG).show();
        }
    }

    public static void goToUser(Context context, String username) {
        new GoToUserTask(context, username).execute();
    }

    private static final class GoToUserTask extends AsyncTask<String, Void, Void> {

        Context mContext;
        String mUsername;
        boolean mUserFound = false;

        public GoToUserTask(Context context, String username) {
            mContext = context;
            mUsername = username;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... params) {
            if (!instance.mRedditClient.isAuthenticated()) {
                return null;
            }

            try {
                instance.mRedditClient.getUser(mUsername);
                mUserFound = true;
            } catch (NetworkException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            if (mUserFound) {
                Intent intent = new Intent(mContext, UserInfoActivity.class);
                intent.putExtra(mContext.getString(R.string.username), mUsername);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            } else {
                Toast.makeText(mContext, mContext.getString(R.string.user_not_found), Toast.LENGTH_LONG).show();
            }
        }
    }



    public static void goToSubreddit(Context context, String subreddit) {
        new GoToSubredditTask(context, subreddit).execute();
    }

    private static final class GoToSubredditTask extends AsyncTask<String, Void, Void> {

        Context mContext;
        String mSubreddit;
        boolean mSubredditFound = false;

        public GoToSubredditTask(Context context, String subreddit) {
            mContext = context;
            mSubreddit = subreddit;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... params) {
            if (!instance.mRedditClient.isAuthenticated()) {
                return null;
            }

            try {
                instance.mRedditClient.getSubreddit(mSubreddit);
                mSubredditFound = true;
            } catch (NetworkException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            if (mSubredditFound) {
                Intent intent = new Intent(mContext, SubredditActivity.class);
                intent.putExtra(mContext.getString(R.string.subreddit_name), mSubreddit);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            } else {
                Toast.makeText(mContext, mContext.getString(R.string.subreddit_not_found), Toast.LENGTH_LONG).show();
            }
        }
    }


    public static void textSubmission(Context context, String subreddit, String title, String text, Captcha captcha, String captchaAttempt) {
        new TextSubmissionTask(context, subreddit, title, text, captcha, captchaAttempt).execute();
    }

    private static final class TextSubmissionTask extends AsyncTask<String, Void, Void> {

        Context mContext;
        String mSubreddit;
        String mTitle;
        String mText;
        Captcha mCaptcha;
        String mCaptchaAttempt;
        boolean mSubmitted = false;

        public TextSubmissionTask(Context context, String subreddit, String title, String text, Captcha captcha, String captchaAttempt) {
            mContext = context;
            mSubreddit = subreddit;
            mTitle = title;
            mText = text;
            mCaptcha = captcha;
            mCaptchaAttempt = captchaAttempt;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... params) {
            if (!instance.mRedditClient.isAuthenticated()) {
                return null;
            }

            try {
                net.dean.jraw.managers.AccountManager accountManager = new net.dean.jraw.managers.AccountManager(instance.mRedditClient);
                net.dean.jraw.managers.AccountManager.SubmissionBuilder submission
                        = new net.dean.jraw.managers.AccountManager.SubmissionBuilder(mText, mSubreddit, mTitle);

                if (mCaptcha != null && !TextUtils.isEmpty(mCaptchaAttempt)) {
                    accountManager.submit(submission, mCaptcha, mCaptchaAttempt);
                } else {
                    accountManager.submit(submission);
                }
                mSubmitted = true;
            } catch (NetworkException | ApiException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            if (mSubmitted) {
                Toast.makeText(mContext, mContext.getString(R.string.submit_successful), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            } else {
                Toast.makeText(mContext, mContext.getString(R.string.submit_error), Toast.LENGTH_LONG).show();
            }
        }
    }

    public static void linkSubmission(Context context, String subreddit, String title, URL url, Captcha captcha, String captchaAttempt) {
        new LinkSubmissionTask(context, subreddit, title, url, captcha, captchaAttempt).execute();
    }

    private static final class LinkSubmissionTask extends AsyncTask<String, Void, Void> {

        Context mContext;
        String mSubreddit;
        String mTitle;
        URL mUrl;
        Captcha mCaptcha;
        String mCaptchaAttempt;
        boolean mSubmitted = false;

        public LinkSubmissionTask(Context context, String subreddit, String title, URL url, Captcha captcha, String captchaAttempt) {
            mContext = context;
            mSubreddit = subreddit;
            mTitle = title;
            mUrl = url;
            mCaptcha = captcha;
            mCaptchaAttempt = captchaAttempt;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... params) {
            if (!instance.mRedditClient.isAuthenticated()) {
                return null;
            }

            try {

                net.dean.jraw.managers.AccountManager accountManager = new net.dean.jraw.managers.AccountManager(instance.mRedditClient);
                net.dean.jraw.managers.AccountManager.SubmissionBuilder submission
                        = new net.dean.jraw.managers.AccountManager.SubmissionBuilder(mUrl, mSubreddit, mTitle);

                if (mCaptcha != null && !TextUtils.isEmpty(mCaptchaAttempt)) {
                    accountManager.submit(submission, mCaptcha, mCaptchaAttempt);
                } else {
                    accountManager.submit(submission);
                }
                mSubmitted = true;
            } catch (NetworkException | ApiException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            if (mSubmitted) {
                Toast.makeText(mContext, mContext.getString(R.string.submit_successful), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            } else {
                Toast.makeText(mContext, mContext.getString(R.string.submit_error), Toast.LENGTH_LONG).show();
            }
        }
    }


    public static String getImageUrl(Submission sub) {
        JsonNode data = sub.getDataNode();
        if (data != null) {
            JsonNode preview = data.findValue("preview");
            if (preview != null) {
                JsonNode images = preview.findValue("images");
                if (images != null) {
                    JsonNode source = images.findValue("source");
                    if (source != null) {
                        List<String> urls = source.findValuesAsText("url");
                        if (urls != null && urls.size() > 0) {
                            return urls.get(0);
                        }

                        //List<String> widths = source.findValuesAsText("width");
                        //List<String> heights = source.findValuesAsText("height");
                    }
                }
            }
        }

        return null;
    }
}
