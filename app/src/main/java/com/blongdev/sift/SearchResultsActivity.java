package com.blongdev.sift;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class SearchResultsActivity extends BaseActivity {

    private SearchPagerAdapter mSearchPagerAdapter;
    private ViewPager mViewPager;

    private String mSearchTerm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        Intent intent = getIntent();
        if (intent != null && Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search
            mSearchTerm = query;

            ActionBar toolbar = getSupportActionBar();
            if (toolbar != null) {
                toolbar.setTitle(mSearchTerm);
            }
        }

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSearchPagerAdapter = new SearchPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSearchPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search
            mSearchTerm = query;
        }
    }


    public class SearchPagerAdapter extends FragmentPagerAdapter {

        public SearchPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
//            return UserInfoActivity.PlaceholderFragment.newInstance(position + 1);

            Bundle args = new Bundle();
            args.putString(getString(R.string.search_term), mSearchTerm);
            args.putInt(getString(R.string.paginator_type), SubredditInfo.SUBMISSION_SEARCH_PAGINATOR);

            switch (position) {
                case 0:
                    SubredditFragment subFrag = new SubredditFragment();
                    subFrag.setArguments(args);
                    return subFrag;
                case 1:
                    SubredditListActivityFragment subredditList = new SubredditListActivityFragment();
                    subredditList.setArguments(args);
                    return subredditList;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.submissions);
                case 1:
                    return getString(R.string.subreddits);
            }
            return null;
        }
    }

}
