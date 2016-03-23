package com.blongdev.sift;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.support.annotation.LayoutRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class BaseActivity extends AppCompatActivity implements Reddit.OnRefreshCompleted {

    public static final String ACCOUNT_TYPE = "blongdev.com";
    public static final String ACCOUNT = "Sift";

    Reddit mReddit;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    View mNavHeader;

    Account mAccount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mReddit = Reddit.getInstance();

        mAccount = CreateSyncAccount(this);

        if (!mReddit.mRedditClient.isAuthenticated()) {
            mReddit.refreshKey(getApplicationContext(), this);
        }
    }

    protected void onCreateDrawer() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavHeader = mNavigationView.inflateHeaderView(R.layout.nav_header);
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
                        if (!reddit.mRedditClient.isAuthenticated()) {
                            intent = new Intent(getApplicationContext(), AuthenticationActivity.class);
//                        intent.putExtra(getString(R.string.username), "My Profile");
                            startActivity(intent);
                        } else {
                            Toast.makeText(getApplicationContext(), "AUTHENTICATED", Toast.LENGTH_LONG).show();
                        }
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

        if (mReddit.mRedditClient.isAuthenticated()) {
            String username = mReddit.mRedditClient.getAuthenticatedUser();
            TextView navUser = (TextView) mNavHeader.findViewById(R.id.nav_username);
            navUser.setText(username);
        } else {

        }

    }

    @Override
    public void onRefreshCompleted() {
        if (mReddit.mRedditClient.isAuthenticated()) {
            String username = mReddit.mRedditClient.getAuthenticatedUser();
            TextView navUser = (TextView) mNavHeader.findViewById(R.id.nav_username);
            navUser.setText(username);
        }
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        onCreateDrawer();
    }

    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    public static Account CreateSyncAccount(Context context) {
        // Create the account type and default account
        Account newAccount = new Account(
                ACCOUNT, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(
                        ACCOUNT_SERVICE);
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
        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
        }
        return newAccount;
    }

}