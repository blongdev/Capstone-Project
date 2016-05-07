package com.blongdev.sift;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.blongdev.sift.database.SiftContract;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import net.dean.jraw.http.NetworkException;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.SubredditStream;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    public static final String LOG_TAG = "MainActivity";

    private static final int CURSOR_LOADER_ID = 1;
    private static final int ASYNCTASK_LOADER_ID = 2;

    ViewPager mPager;
    SubredditPagerAdapter mPagerAdapter;

    private ArrayList<SubscriptionInfo> mSubreddits;
    ProgressBar mLoadingSpinner;
    String mCategory = null;

    FloatingActionButton mFab;

    private LoaderManager.LoaderCallbacks<Cursor> mSubscriptionLoader
            = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            mLoadingSpinner.setVisibility(View.VISIBLE);
            return new CursorLoader(getApplicationContext(), SiftContract.Subscriptions.VIEW_URI,
                    null, null, null, SiftContract.Subreddits.COLUMN_NAME + " COLLATE NOCASE");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mLoadingSpinner.setVisibility(View.GONE);
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
    };

    private LoaderManager.LoaderCallbacks<List<SubscriptionInfo>> mPopularSubredditsLoader
            = new LoaderManager.LoaderCallbacks<List<SubscriptionInfo>>() {

        @Override
        public Loader<List<SubscriptionInfo>> onCreateLoader(int id, Bundle args) {
            mLoadingSpinner.setVisibility(View.VISIBLE);
            if (Utilities.loggedIn(getApplicationContext())) {
                return new PopularSubredditLoader(getApplicationContext(), false);
            } else {
                //hide fab from frontpage
                mFab.hide();
                return new PopularSubredditLoader(getApplicationContext(), true);
            }
        }

        @Override
        public void onLoadFinished(Loader<List<SubscriptionInfo>> loader, List<SubscriptionInfo> data) {
            mLoadingSpinner.setVisibility(View.GONE);
            mPagerAdapter.swapData(data);
        }

        @Override
        public void onLoaderReset(Loader<List<SubscriptionInfo>> loader) {
            mPagerAdapter.swapData(new ArrayList<SubscriptionInfo>());
        }


    };

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

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Utilities.loggedIn(getApplicationContext())) {
                    Toast.makeText(getApplicationContext(), getString(R.string.must_log_in), Toast.LENGTH_LONG).show();
                    return;
                }

                Intent intent = new Intent(getApplicationContext(), ComposePostActivity.class);
                intent.putExtra(getString(R.string.subreddit_name), mSubreddits.get(mPager.getCurrentItem()).mSubredditName);
                startActivity(intent);
//                Snackbar.make(view, "Not yet implemented", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });

        Intent intent = getIntent();
        if (intent != null) {
            mCategory = intent.getStringExtra(getString(R.string.category));
        }

        if (mCategory != null && mReddit.mRedditClient.isAuthenticated()) {
//            new GetSubredditsTask(getApplicationContext()).execute();
            getSupportLoaderManager().initLoader(ASYNCTASK_LOADER_ID, null, mPopularSubredditsLoader).forceLoad();
        } else if (Utilities.loggedIn(getApplicationContext())){
            getSupportLoaderManager().initLoader(CURSOR_LOADER_ID, null, mSubscriptionLoader).forceLoad();
            //hide fab on frontpage
            mFab.hide();
        } else if (mReddit.mRedditClient.isAuthenticated()){
//            new GetSubredditsTask(getApplicationContext()).execute();
            getSupportLoaderManager().initLoader(ASYNCTASK_LOADER_ID, null, mPopularSubredditsLoader).forceLoad();
        }

        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new SubredditPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);


        TabLayout tabLayout = (TabLayout) findViewById(R.id.subreddit_tabs);
        tabLayout.setupWithViewPager(mPager);


        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (TextUtils.equals(mPagerAdapter.getPageTitle(mPager.getCurrentItem()), getString(R.string.frontPage))) {
                    mFab.hide();
                } else {
                    mFab.show();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

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

        private List<SubscriptionInfo> mSubreddits;

        public SubredditPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void swapData(List<SubscriptionInfo> subreddits) {
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
                //new GetSubredditsTask(getApplicationContext()).execute();

                if (mCategory != null && mReddit.mRedditClient.isAuthenticated()) {
                    getSupportLoaderManager().initLoader(ASYNCTASK_LOADER_ID, null, mPopularSubredditsLoader).forceLoad();
                } else if (Utilities.loggedIn(getApplicationContext())){
                    getSupportLoaderManager().initLoader(CURSOR_LOADER_ID, null, mSubscriptionLoader).forceLoad();
                } else if (mReddit.mRedditClient.isAuthenticated()){
                    getSupportLoaderManager().initLoader(ASYNCTASK_LOADER_ID, null, mPopularSubredditsLoader).forceLoad();
                }

            }
            mPager.setAdapter(mPagerAdapter);
        }
    }
////
//    private final class GetSubredditsTask extends AsyncTask<String, Void, ArrayList<SubscriptionInfo>> {
//
//        Context mContext;
//
//
//        public GetSubredditsTask(Context context) {
//            mContext = context;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            mLoadingSpinner.setVisibility(View.VISIBLE);
//        }
//
//        @Override
//        protected ArrayList<SubscriptionInfo> doInBackground(String... params) {
//            ArrayList<SubscriptionInfo> subredditArray = new ArrayList<SubscriptionInfo>();
//            SubredditStream paginator = new SubredditStream(mReddit.mRedditClient, "popular");
//            if (paginator.hasNext()) {
//                Listing<Subreddit> subs = paginator.next();
//                ContentValues cv = new ContentValues();
//                String selection = SiftContract.Subreddits.COLUMN_SERVER_ID + " =?";
//                String name, serverId, description;
//                long subredditId;
//                long numSubscribers = -1;
//                for (Subreddit subreddit : subs) {
//                    SubscriptionInfo sub = new SubscriptionInfo();
//                    name = subreddit.getDisplayName();
//                    serverId = subreddit.getId();
//
//                    try {
//                        //bug in jraw library sometimes throws nullpointerexception
//                        numSubscribers = subreddit.getSubscriberCount();
//                    } catch (NullPointerException e) {
//                        e.printStackTrace();
//                    }
//
//                    description = subreddit.getPublicDescription();
//                    subredditId = Utilities.getSubredditId(mContext, serverId);
//                    if (subredditId < 0) {
//                        cv.put(SiftContract.Subreddits.COLUMN_NAME, name);
//                        cv.put(SiftContract.Subreddits.COLUMN_SERVER_ID, serverId);
//                        cv.put(SiftContract.Subreddits.COLUMN_DESCRIPTION, description);
//                        cv.put(SiftContract.Subreddits.COLUMN_SUBSCRIBERS, numSubscribers);
//                        Uri uri = mContext.getContentResolver().insert(SiftContract.Subreddits.CONTENT_URI, cv);
//                        subredditId = ContentUris.parseId(uri);
//                        cv.clear();
//                    }
//                    sub.mSubredditId = subredditId;
//                    sub.mSubredditName = name;
//                    sub.mServerId = serverId;
//                    sub.mSubscribers = numSubscribers;
//                    sub.mDescription = description;
//                    subredditArray.add(sub);
//                    mSubreddits.add(sub);
//                    cv.clear();
//                }
//            }
//
//            return subredditArray;
//        }
//
//        @Override
//        protected void onPostExecute(ArrayList<SubscriptionInfo> subs) {
//            mLoadingSpinner.setVisibility(View.GONE);
//            mPagerAdapter.swapData(mSubreddits);
//        }
//    }

    @Override
    public void onResume() {
        super.onResume();

        //TODO move this to syncadapter
        GoogleAnalytics.getInstance(this).dispatchLocalHits();
    }

}


class PopularSubredditLoader extends AsyncTaskLoader<List<SubscriptionInfo>> {

    Context mContext;
    boolean addFrontPage;

    public PopularSubredditLoader(Context context, boolean frontpage) {
        super(context);
        mContext = context;
        addFrontPage = frontpage;
    }

    @Override
    public List<SubscriptionInfo> loadInBackground() {
        Reddit reddit = Reddit.getInstance();
        ArrayList<SubscriptionInfo> subredditArray = new ArrayList<SubscriptionInfo>();

        if(addFrontPage) {
            SubscriptionInfo frontpage = new SubscriptionInfo();
            frontpage.mSubredditId = -1;
            frontpage.mSubredditName = mContext.getString(R.string.frontPage);
            subredditArray.add(frontpage);
        }

        try {
            SubredditStream paginator = new SubredditStream(reddit.mRedditClient, "popular");
            if (paginator.hasNext()) {
                Listing<Subreddit> subs = paginator.next();
                ContentValues cv = new ContentValues();
                String selection = SiftContract.Subreddits.COLUMN_SERVER_ID + " =?";
                String name, serverId, description;
                long subredditId;
                long numSubscribers = -1;
                for (Subreddit subreddit : subs) {
                    SubscriptionInfo sub = new SubscriptionInfo();
                    name = subreddit.getDisplayName();
                    serverId = subreddit.getId();

                    try {
                        //bug in jraw library sometimes throws nullpointerexception
                        numSubscribers = subreddit.getSubscriberCount();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    description = subreddit.getPublicDescription();
                    subredditId = Utilities.getSubredditId(mContext, serverId);
                    if (subredditId < 0) {
                        cv.put(SiftContract.Subreddits.COLUMN_NAME, name);
                        cv.put(SiftContract.Subreddits.COLUMN_SERVER_ID, serverId);
                        cv.put(SiftContract.Subreddits.COLUMN_DESCRIPTION, description);
                        cv.put(SiftContract.Subreddits.COLUMN_SUBSCRIBERS, numSubscribers);
                        Uri uri = mContext.getContentResolver().insert(SiftContract.Subreddits.CONTENT_URI, cv);
                        subredditId = ContentUris.parseId(uri);
                        cv.clear();
                    }
                    sub.mSubredditId = subredditId;
                    sub.mSubredditName = name;
                    sub.mServerId = serverId;
                    sub.mSubscribers = numSubscribers;
                    sub.mDescription = description;
                    subredditArray.add(sub);
                    cv.clear();
                }
            }
        } catch (NetworkException e) {
            e.printStackTrace();
        }

        return subredditArray;
    }
}
