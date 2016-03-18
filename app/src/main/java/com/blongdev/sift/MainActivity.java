package com.blongdev.sift;

import android.content.Intent;
import android.database.Cursor;
import android.os.Debug;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.blongdev.sift.database.SiftContract;
import com.blongdev.sift.database.SiftDbHelper;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ViewPager mPager;
    SubredditPagerAdapter mPagerAdapter;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    private ArrayList<SubscriptionInfo> mSubreddits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSubreddits = new ArrayList<SubscriptionInfo>();


        Cursor cursor = getContentResolver().query(SiftContract.Subscriptions.VIEW_URI, null, null, null, null);
        if (cursor != null) {

            if (cursor.getCount() <= 0){
                //TODO replace dummy data with initial sync
                SiftDbHelper dbHelper = new SiftDbHelper(this);
                dbHelper.insertDummyData();
            } else {
                while (cursor.moveToNext()) {
                    SubscriptionInfo sub = new SubscriptionInfo();
                    sub.mSubredditId = cursor.getInt(cursor.getColumnIndex(SiftContract.Subscriptions.COLUMN_SUBREDDIT_ID));
                    sub.mSubredditName = cursor.getString(cursor.getColumnIndex(SiftContract.Subreddits.COLUMN_NAME));
                    mSubreddits.add(sub);
                }
            }
        }

        //final ActionBar actionBar = getSupportActionBar();
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new SubredditPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.subreddit_tabs);
        tabLayout.setupWithViewPager(mPager);

        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                if(menuItem.isChecked()) menuItem.setChecked(false);
                else menuItem.setChecked(true);

                mDrawerLayout.closeDrawers();

                Intent intent;

                switch (menuItem.getItemId()){
                    case R.id.nav_home:
                        intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.nav_profile:
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

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,toolbar, R.string.drawer_open, R.string.drawer_close){

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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class SubredditPagerAdapter extends FragmentStatePagerAdapter {
        public SubredditPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            SubscriptionInfo sub = mSubreddits.get(position);
            Bundle args = new Bundle();
            args.putInt(getString(R.string.subreddit_id), sub.mSubredditId);
            SubredditFragment subFrag = new SubredditFragment();
            subFrag.setArguments(args);
            return subFrag;
        }

        @Override
        public int getCount() {
//            int count = 0;
//            Cursor cursor = getContentResolver().query(SiftContract.Subscriptions.CONTENT_URI, null, null, null, null);
//            if (cursor != null)
//            {
//                count = cursor.getCount();
//            }
//            return count;

            return mSubreddits.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {

            SubscriptionInfo sub = mSubreddits.get(position);
            return sub.mSubredditName;
//
//            switch (position) {
//                case 0:
//                    return "Tab One";
//                case 1:
//                    return "Tab Two";
//                case 2:
//                    return "Tab Three";
//                case 3:
//                    return "Tab Four";
//                case 4:
//                    return "Tab Five";
//                default:
//                    return "" + position;
//            }
            //return null;
        }
    }
}
