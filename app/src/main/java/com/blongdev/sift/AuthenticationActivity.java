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
    Reddit mReddit;

    private static final String LOG_TAG = "Authentication Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mWebView = (WebView) findViewById(R.id.web_view);

        mReddit = Reddit.getInstance();

        final Credentials credentials = Credentials.installedApp(getString(R.string.client_id), getString(R.string.redirect_url));
            final OAuthHelper oAuth = mReddit.mRedditClient.getOAuthHelper();
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
                        mReddit.runUserChallengeTask(url, getApplicationContext());
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            });



//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }



}
