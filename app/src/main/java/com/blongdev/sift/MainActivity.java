package com.blongdev.sift;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.blongdev.sift.database.SiftContract;
import com.blongdev.sift.database.SiftDbHelper;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthHelper;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.SubredditPaginator;

import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends BaseActivity {

    ViewPager mPager;
    SubredditPagerAdapter mPagerAdapter;

    private ArrayList<SubscriptionInfo> mSubreddits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new SubredditPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.subreddit_tabs);
        tabLayout.setupWithViewPager(mPager);

        //new GetSubredditsTask().execute();

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
            return mSubreddits.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            SubscriptionInfo sub = mSubreddits.get(position);
            if (sub.mSubredditName == null) {
                return "FrontPage";
            }
            return sub.mSubredditName;
        }
    }

    @Override
    public void onRefreshCompleted() {
        super.onRefreshCompleted();
        if (mReddit.mRedditClient.isAuthenticated()) {
            mPager.setAdapter(mPagerAdapter);
        }
    }

    private final class GetSubredditsTask extends AsyncTask<String, Void, ArrayList<SubscriptionInfo>> {
        @Override
        protected ArrayList<SubscriptionInfo> doInBackground(String... params) {
            ArrayList<SubscriptionInfo> subredditArray = new ArrayList<SubscriptionInfo>();
            SubredditPaginator paginator = new SubredditPaginator(mReddit.mRedditClient);
            while (paginator.hasNext()) {
                paginator.next();
                SubscriptionInfo sub = new SubscriptionInfo();
                //post.mId =
                sub.mSubredditName = paginator.getSubreddit();
                subredditArray.add(sub);
            }

            return subredditArray;
        }

        @Override
        protected void onPostExecute(ArrayList<SubscriptionInfo> subs) {
            mSubreddits = subs;
            mPagerAdapter.notifyDataSetChanged();
        }
    }


}
