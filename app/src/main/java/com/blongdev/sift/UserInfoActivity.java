package com.blongdev.sift;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;

import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Subreddit;

import java.util.ArrayList;
import java.util.List;

public class UserInfoActivity extends BaseActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private String mUsername;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        //Change toolbar title to username
        Intent intent = getIntent();
        mUsername = intent.getStringExtra(getString(R.string.username));
        if (!TextUtils.isEmpty(mUsername)) {
            ActionBar toolbar = getSupportActionBar();
            if (toolbar != null) {
                toolbar.setTitle(mUsername);
            }
        }

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }



    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Bundle args = new Bundle();
            args.putString(getString(R.string.username), mUsername);
            args.putInt(getString(R.string.paginator_type), SubredditInfo.USER_CONTRIBUTION_PAGINATOR);

            switch (position) {
                case 0:
                    args.putString(getString(R.string.category), SubredditInfo.CATEGORY_OVERVIEW);
                    SubredditFragment overviewFrag = new SubredditFragment();
                    overviewFrag.setArguments(args);
                    return overviewFrag;
//                    args.putString(getString(R.string.category), SubredditInfo.CATEGORY_SUBMITTED);
//                    SubredditFragment subFrag = new SubredditFragment();
//                    subFrag.setArguments(args);
//                    return subFrag;
                case 1:
                    args.putString(getString(R.string.category), SubredditInfo.CATEGORY_SUBMITTED);
                    SubredditFragment subFrag2 = new SubredditFragment();
                    subFrag2.setArguments(args);
                    return subFrag2;
                case 2:
                    args.putString(getString(R.string.category), SubredditInfo.CATEGORY_COMMENTS);
                    SubredditFragment commentsFrag = new SubredditFragment();
                    commentsFrag.setArguments(args);
                    return commentsFrag;

            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.overview);
                case 1:
                    return getString(R.string.submissions);
                case 2:
                    return getString(R.string.comments);
            }
            return null;
        }
    }




//
//
//    private final class GetSubredditsTask extends AsyncTask<String, Void, ArrayList<SubredditInfo>> {
//        @Override
//        protected ArrayList<SubredditInfo> doInBackground(String... params) {
//            if (mPaginator != null && mPaginator.hasNext()) {
//                Listing<Subreddit> page = mPaginator.next();
//                for (Subreddit subreddit : page) {
//                    SubredditInfo sub = new SubredditInfo();
//                    sub.mName = subreddit.getDisplayName();
//                    sub.mServerId = subreddit.getId();
//                    mSubreddits.add(sub);
//                }
//            }
//
//            return mSubreddits;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            if(mSubreddits.size() == 0) {
//                mLoadingSpinner.setVisibility(View.VISIBLE);
//            }
//        }
//
//        @Override
//        protected void onPostExecute(ArrayList<SubredditInfo> subs) {
//            mLoadingSpinner.setVisibility(View.GONE);
//            mSubredditAdapter.refreshWithList(subs);
//        }
//    }
}
