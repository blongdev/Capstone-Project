package com.blongdev.sift;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthHelper;


public class AuthenticationActivity extends BaseActivity {

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

                }
            });

            mWebView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    if (url.contains("code=")) {
                        mReddit.runUserChallengeTask(url, getApplicationContext());
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                    }
                }
            });



    }



}
