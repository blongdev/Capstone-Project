package com.blongdev.sift;

import android.accounts.Account;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.blongdev.sift.database.SiftContract;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.http.oauth.OAuthHelper;
import net.dean.jraw.models.LoggedInAccount;

/**
 * Created by Brian on 3/21/2016.
 */
public class Reddit {

    private static final String REFRESH_KEY = "refreshKey";
    private static final String CLIENT_ID = "pFsQuM_0DQdv-g";
    private static final String REDIRECT_URL = "http://www.google.com";

    private static Reddit ourInstance = new Reddit();
    public RedditClient mRedditClient;
    public UserAgent mUserAgent;
    public String mRefreshToken;
    public Credentials mCredentials;
    public OAuthHelper mOAuthHelper;
    public LoggedInAccount mMe;

    private static final String LOG_TAG = "Reddit Singleton";


    public static Reddit getInstance() {
        return ourInstance;
    }

    private Reddit() {
        String versionName = BuildConfig.VERSION_NAME;
        mUserAgent = UserAgent.of("Android", "com.blongdev.sift", versionName, "toothkey");
        mRedditClient = new RedditClient(mUserAgent);
        mCredentials = Credentials.installedApp(CLIENT_ID, REDIRECT_URL);
        mOAuthHelper = mRedditClient.getOAuthHelper();

        if (mRedditClient.isAuthenticated()) {
            mMe = mRedditClient.me();
        }
    }

    public void refreshKey(Context context, OnRefreshCompleted callback) {
        //TODO make work with multiple accounts
        Cursor cursor = context.getContentResolver().query(SiftContract.Accounts.CONTENT_URI, null, null, null, null);
        if(cursor != null && cursor.moveToFirst()) {
            mRefreshToken = cursor.getString(cursor.getColumnIndex(SiftContract.Accounts.COLUMN_REFRESH_KEY));
            if (mRefreshToken != null && !mRefreshToken.isEmpty()) {
                new RefreshTokenTask(callback).execute();
                return;
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
        context.getContentResolver().insert(SiftContract.Accounts.CONTENT_URI, cv);



        cv.clear();
        //subreddits


        //

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
//
//    public AsyncTask refreshToken() {
//        return new RefreshTokenTask().execute();
//    }

    public interface OnRefreshCompleted{
        void onRefreshCompleted();
    }
}
