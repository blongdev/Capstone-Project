package com.blongdev.sift;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.http.oauth.OAuthHelper;

import java.net.URISyntaxException;
import java.net.URL;

public class AuthenticationActivity extends AppCompatActivity {

    WebView mWebView;
    RedditClient mRedditClient;

    private static final String LOG_TAG = "Authentication Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mWebView = (WebView) findViewById(R.id.web_view);

        String versionName = BuildConfig.VERSION_NAME;
        UserAgent myUserAgent = UserAgent.of("Android", "com.blongdev.sift", versionName, "toothkey");
        if (mRedditClient == null) {
            mRedditClient = new RedditClient(myUserAgent);
        }
        if (!mRedditClient.isAuthenticated()) {
            final Credentials credentials = Credentials.installedApp(getString(R.string.client_id), getString(R.string.redirect_url));
            final OAuthHelper oAuth = mRedditClient.getOAuthHelper();
            String[] scopes = new String[]{"identity", "edit", "flair", "history", "modconfig", "modflair",
                    "modlog", "modposts", "modwiki", "mysubreddits", "privatemessages", "read", "report",
                    "save", "submit", "subscribe", "vote", "wikiedit", "wikiread"};
            String url = oAuth.getAuthorizationUrl(credentials, true, scopes).toExternalForm();

            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();

            mWebView.loadUrl(url);

            mWebView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
//                activity.setProgress(newProgress * 1000);
//                    progressBar.setProgress(newProgress);
                }
            });

            mWebView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    if (url.contains("code=")) {
                        new UserChallengeTask(oAuth, credentials).execute(url);
                    }
                }
            });

        } else {
            Toast.makeText(getApplicationContext(), "Authenticated", Toast.LENGTH_SHORT).show();
        }



//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }


    private final class UserChallengeTask extends AsyncTask<String, Void, OAuthData> {

        private OAuthHelper mOAuthHelper;
        private Credentials mCredentials;

        public UserChallengeTask(OAuthHelper oAuthHelper, Credentials credentials) {
            Log.v(LOG_TAG, "UserChallengeTask()");
            mOAuthHelper = oAuthHelper;
            mCredentials = credentials;
        }

        @Override
        protected OAuthData doInBackground(String... params) {
            Log.v(LOG_TAG, "doInBackground()");
            Log.v(LOG_TAG, "params[0]: " + params[0]);
            try {
                OAuthData oAuthData =  mOAuthHelper.onUserChallenge(params[0], mCredentials);
                if (oAuthData != null) {
                    mRedditClient.authenticate(oAuthData);
                    Log.v(LOG_TAG, "Reddit client authentication: " + mRedditClient.isAuthenticated());
                    //TODO: Save refresh token:
                    String refreshToken = mRedditClient.getOAuthData().getRefreshToken();
                    Log.v(LOG_TAG, "Refresh Token: " + refreshToken);
                } else {
                    Log.e(LOG_TAG, "Passed in OAuthData was null");
                }
            } catch (IllegalStateException | NetworkException | OAuthException e) {
                // Handle me gracefully
                Log.e(LOG_TAG, "OAuth failed");
                Log.e(LOG_TAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(OAuthData oAuthData) {
            Log.v(LOG_TAG, "onPostExecute()");
            if (mRedditClient.isAuthenticated()) {
                Toast.makeText(getApplicationContext(), "AUTHENTICATED", Toast.LENGTH_LONG).show();
            }

        }
    }

}
