package com.blongdev.sift;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Debug;
import android.support.annotation.LayoutRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.blongdev.sift.database.SiftContract;

public class BaseActivity extends AppCompatActivity implements Reddit.OnRefreshCompleted {

    Reddit mReddit;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    View mNavHeader;
    Menu mNavMenu;

    MenuItem mNavProfile;
    MenuItem mNavInbox;
    MenuItem mNavFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mReddit = Reddit.getInstance();
        //mReddit.addGeneralAccount(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (!mReddit.mRedditClient.isAuthenticated()) {
            mReddit.refreshKey(getApplicationContext(), this);
        }
    }

    protected void onCreateDrawer() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavHeader = mNavigationView.inflateHeaderView(R.layout.nav_header);
        mNavMenu = mNavigationView.getMenu();

        mNavFriends = mNavMenu.findItem(R.id.nav_friends);
        mNavProfile = mNavMenu.findItem(R.id.nav_profile);
        mNavInbox = mNavMenu.findItem(R.id.nav_inbox);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

            if (menuItem.isChecked()) menuItem.setChecked(false);
            else menuItem.setChecked(true);

            mDrawerLayout.closeDrawers();

            Intent intent;

            switch (menuItem.getItemId()) {
                case R.id.nav_home:
                    intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.nav_profile:
                    Reddit reddit = Reddit.getInstance();
                    intent = new Intent(getApplicationContext(), UserInfoActivity.class);
                    intent.putExtra(getString(R.string.username), "My Profile");
                    startActivity(intent);
                    return true;
                case R.id.nav_inbox:
                    intent = new Intent(getApplicationContext(), MessageActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.nav_friends:
                    intent = new Intent(getApplicationContext(), FriendsListActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.nav_subreddits:
                    intent = new Intent(getApplicationContext(), SubredditListActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.nav_settings:
                    intent = new Intent(getApplicationContext(), SettingsActivity.class);
                    startActivity(intent);
                    return true;
                default:
                    return true;
            }
            }
        });

        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(SiftContract.Accounts.CONTENT_URI, null, null, null, null);
            if (cursor != null && !cursor.moveToFirst()) {
                mNavFriends.setVisible(false);
                mNavProfile.setVisible(false);
                mNavInbox.setVisible(false);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        mDrawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        TextView navUser = (TextView) mNavHeader.findViewById(R.id.nav_username);
        if (mReddit.mRedditClient.isAuthenticated() && mReddit.mRedditClient.hasActiveUserContext()) {
            String username = mReddit.mRedditClient.getAuthenticatedUser();
            navUser.setText(username);
        } else {
            navUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent authIntent = new Intent(getApplicationContext(), AuthenticationActivity.class);
                    startActivity(authIntent);
                }
            });
        }
    }

    @Override
    public void onRefreshCompleted() {
        if (mReddit.mRedditClient.isAuthenticated()) {
            if (mReddit.mRedditClient.hasActiveUserContext()) {
                String username = mReddit.mRedditClient.getAuthenticatedUser();
                TextView navUser = (TextView) mNavHeader.findViewById(R.id.nav_username);
                navUser.setText(username);
            }
        }
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        onCreateDrawer();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

}
