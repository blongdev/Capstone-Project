package com.blongdev.sift;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class SubredditActivity extends BaseActivity {

    MenuItem mSubscribe;
    String mSubredditName;
    boolean mSubscribed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddit);

        //Change toolbar title to username
        Intent intent = getIntent();
        mSubredditName = intent.getStringExtra(getString(R.string.subreddit_name));
        if (!TextUtils.isEmpty(mSubredditName)) {
            ActionBar toolbar = getSupportActionBar();
            if (toolbar != null) {
                toolbar.setTitle(mSubredditName);
            }
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Utilities.loggedIn(getApplicationContext())) {
                    Toast.makeText(getApplicationContext(), getString(R.string.must_log_in), Toast.LENGTH_LONG).show();
                    return;
                }

                Intent intent = new Intent(getApplicationContext(), ComposePostActivity.class);
                intent.putExtra(getString(R.string.subreddit_name), mSubredditName);
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_subreddit, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        mSubscribe = (MenuItem) menu.findItem(R.id.subscribe);

        if (mReddit.mRedditClient.isAuthenticated() && mReddit.mRedditClient.hasActiveUserContext()) {
            if (!TextUtils.isEmpty(mSubredditName)) {
                long subscriptionId = Utilities.getSubscriptionId(getApplicationContext(), mSubredditName);
                if (subscriptionId > 0) {
                    mSubscribed = true;
                    mSubscribe.setIcon(R.drawable.ic_favorite_24dp);
                }
            }
        }

        mSubscribe.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (!Utilities.loggedIn(getApplicationContext())) {
                    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.must_log_in), Toast.LENGTH_LONG).show();
                    return false;
                }

                if (mSubscribed) {
                    mSubscribed = false;
                    mSubscribe.setIcon(R.drawable.ic_favorite_outline_24dp);
                    Reddit.getInstance().unsubscribe(getApplicationContext(), mSubredditName);
                    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.unsubscribed), Toast.LENGTH_LONG).show();
                } else {
                    mSubscribed = true;
                    mSubscribe.setIcon(R.drawable.ic_favorite_24dp);
                    Reddit.getInstance().subscribe(getApplicationContext(), mSubredditName);
                    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.subscribed), Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });

        return true;
    }

}
