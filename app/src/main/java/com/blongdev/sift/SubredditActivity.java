package com.blongdev.sift;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.blongdev.sift.database.SiftContract;

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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_subreddit, menu);
        // Associate searchable configuration with the SearchView
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
