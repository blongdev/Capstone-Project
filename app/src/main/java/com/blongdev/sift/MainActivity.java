package com.blongdev.sift;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.blongdev.sift.database.SiftContract;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.SubredditStream;

import java.util.ArrayList;

public class MainActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String LOG_TAG = "MainActivity";

    ViewPager mPager;
    SubredditPagerAdapter mPagerAdapter;

    private ArrayList<SubscriptionInfo> mSubreddits;
    ProgressBar mLoadingSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLoadingSpinner = (ProgressBar) findViewById(R.id.progressSpinnerMain);

        mSubreddits = new ArrayList<SubscriptionInfo>();
        SubscriptionInfo frontpage = new SubscriptionInfo();
        frontpage.mSubredditId = -1;
        frontpage.mSubredditName = getString(R.string.frontPage);
        mSubreddits.add(frontpage);

        if (Utilities.loggedIn(getApplicationContext())) {
            getSupportLoaderManager().initLoader(0, null, this);
        } else if (mReddit.mRedditClient.isAuthenticated()){
            new GetSubredditsTask(getApplicationContext()).execute();
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
                Snackbar.make(view, "Not yet implemented", Snackbar.LENGTH_LONG)
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


//    @Override
//    public void onBackPressed() {
//        if (mPager.getCurrentItem() == 0) {
//            // If the user is currently looking at the first step, allow the system to handle the
//            // Back button. This calls finish() on this activity and pops the back stack.
//            super.onBackPressed();
//        } else {
//            // Otherwise, select the previous step.
//            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
//        }
//    }


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
            args.putLong(getString(R.string.subreddit_id), sub.mSubredditId);
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
            if (!Utilities.loggedIn(getApplicationContext())) {
                new GetSubredditsTask(getApplicationContext()).execute();
            }
            mPager.setAdapter(mPagerAdapter);
        }
    }
//
    private final class GetSubredditsTask extends AsyncTask<String, Void, ArrayList<SubscriptionInfo>> {

        Context mContext;

        public GetSubredditsTask(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            mLoadingSpinner.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<SubscriptionInfo> doInBackground(String... params) {
            ArrayList<SubscriptionInfo> subredditArray = new ArrayList<SubscriptionInfo>();
            SubredditStream paginator = new SubredditStream(mReddit.mRedditClient, "popular");
            if (paginator.hasNext()) {
                Listing<Subreddit> subs = paginator.next();
                ContentValues cv = new ContentValues();
                String selection = SiftContract.Subreddits.COLUMN_SERVER_ID + " =?";
                String name, serverId;
                long subredditId;
                for (Subreddit subreddit : subs) {
                    SubscriptionInfo sub = new SubscriptionInfo();
                    name = subreddit.getDisplayName();
                    serverId = subreddit.getId();
                    subredditId = Utilities.getSubredditId(mContext, serverId);
                    if (subredditId < 0) {
                        cv.put(SiftContract.Subreddits.COLUMN_NAME, name);
                        cv.put(SiftContract.Subreddits.COLUMN_SERVER_ID, serverId);
                        Uri uri = mContext.getContentResolver().insert(SiftContract.Subreddits.CONTENT_URI, cv);
                        subredditId = ContentUris.parseId(uri);
                        cv.clear();
                    }
                    sub.mSubredditId = subredditId;
                    sub.mSubredditName = name;
                    sub.mServerId = serverId;
                    subredditArray.add(sub);
                    mSubreddits.add(sub);
                    cv.clear();
                }
            }

            return subredditArray;
        }

        @Override
        protected void onPostExecute(ArrayList<SubscriptionInfo> subs) {
            mLoadingSpinner.setVisibility(View.GONE);
            mPagerAdapter.swapData(mSubreddits);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        //TODO move this to syncadapter
        GoogleAnalytics.getInstance(this).dispatchLocalHits();
    }

}
