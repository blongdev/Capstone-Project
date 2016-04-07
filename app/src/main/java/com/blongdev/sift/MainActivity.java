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
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.SubredditPaginator;
import net.dean.jraw.paginators.SubredditStream;

import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    ViewPager mPager;
    SubredditPagerAdapter mPagerAdapter;

    private ArrayList<SubscriptionInfo> mSubreddits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSubreddits = new ArrayList<SubscriptionInfo>();
        SubscriptionInfo frontpage = new SubscriptionInfo();
        frontpage.mSubredditId = -1;
        frontpage.mSubredditName = getString(R.string.frontPage);
        mSubreddits.add(frontpage);

        if (Utilities.loggedIn(getApplicationContext())) {
            getSupportLoaderManager().initLoader(0, null, this);
        } else if (mReddit.mRedditClient.isAuthenticated()){
            new GetSubredditsTask().execute();
        }

        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new SubredditPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.subreddit_tabs);
        tabLayout.setupWithViewPager(mPager);



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
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, SiftContract.Subscriptions.VIEW_URI,
                null, null, null, SiftContract.Subreddits.COLUMN_NAME + " COLLATE NOCASE");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            while (data.moveToNext()) {
                    SubscriptionInfo sub = new SubscriptionInfo();
                    sub.mSubredditId = data.getInt(data.getColumnIndex(SiftContract.Subscriptions.COLUMN_SUBREDDIT_ID));
                    sub.mSubredditName = data.getString(data.getColumnIndex(SiftContract.Subreddits.COLUMN_NAME));
                    mSubreddits.add(sub);
                }
            }
        mPagerAdapter.swapData(mSubreddits);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mPagerAdapter.swapData(null);
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


    private class SubredditPagerAdapter extends FragmentStatePagerAdapter {

        private ArrayList<SubscriptionInfo> mSubreddits;

        public SubredditPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void swapData(ArrayList<SubscriptionInfo> subreddits) {
            this.mSubreddits = subreddits;
            notifyDataSetChanged();
        }

        @Override
        public Fragment getItem(int position) {
            SubscriptionInfo sub = mSubreddits.get(position);
            Bundle args = new Bundle();
            args.putInt(getString(R.string.subreddit_id), sub.mSubredditId);
            args.putString(getString(R.string.subreddit_name), sub.mSubredditName);
            SubredditFragment subFrag = new SubredditFragment();
            subFrag.setArguments(args);
            return subFrag;
        }

        @Override
        public int getCount() {
            if (mSubreddits == null) {
                return 0;
            }

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
//
    private final class GetSubredditsTask extends AsyncTask<String, Void, ArrayList<SubscriptionInfo>> {
        @Override
        protected ArrayList<SubscriptionInfo> doInBackground(String... params) {
            ArrayList<SubscriptionInfo> subredditArray = new ArrayList<SubscriptionInfo>();
            SubredditStream paginator = new SubredditStream(mReddit.mRedditClient, "popular");
            if (paginator.hasNext()) {
                Listing<Subreddit> subs = paginator.next();
                for (Subreddit subreddit : subs) {
                    SubscriptionInfo sub = new SubscriptionInfo();
                    sub.mSubredditName = subreddit.getDisplayName();
                    subredditArray.add(sub);
                    mSubreddits.add(sub);
                }
            }

            return subredditArray;
        }

        @Override
        protected void onPostExecute(ArrayList<SubscriptionInfo> subs) {
            mPagerAdapter.swapData(mSubreddits);
        }
    }

}
