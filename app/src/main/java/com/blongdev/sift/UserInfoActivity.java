package com.blongdev.sift;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class UserInfoActivity extends BaseActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private String mUsername;
    private MenuItem mAddFriend;
    private boolean mIsFriend;

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

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Utilities.loggedIn()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.must_log_in), Toast.LENGTH_LONG).show();
                    return;
                }

                Intent intent = new Intent(getApplicationContext(), ComposeMessageActivity.class);
                intent.putExtra(getString(R.string.username), mUsername);
                startActivity(intent);
            }
        });

        if (mReddit.mRedditClient.isAuthenticated() && mReddit.mRedditClient.hasActiveUserContext()) {
            if (TextUtils.equals(mReddit.mRedditClient.getAuthenticatedUser(), mUsername)) {
                fab.setVisibility(View.GONE);
            }
        }
    }


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
                    args.putString(getString(R.string.category), getString(R.string.overview));
                    SubredditFragment overviewFrag = new SubredditFragment();
                    overviewFrag.setArguments(args);
                    return overviewFrag;
                case 1:
                    args.putString(getString(R.string.category), getString(R.string.submitted));
                    SubredditFragment subFrag = new SubredditFragment();
                    subFrag.setArguments(args);
                    return subFrag;
                case 2:
                    args.putString(getString(R.string.category), getString(R.string.comments));
                    SubredditFragment commentsFrag = new SubredditFragment();
                    commentsFrag.setArguments(args);
                    return commentsFrag;
                case 3:
                    args.putString(getString(R.string.category), getString(R.string.gilded));
                    SubredditFragment gildedFrag = new SubredditFragment();
                    gildedFrag.setArguments(args);
                    return gildedFrag;
                case 4:
                    args.putString(getString(R.string.category), getString(R.string.saved));
                    SubredditFragment savedFrag = new SubredditFragment();
                    savedFrag.setArguments(args);
                    return savedFrag;
            }
            return null;
        }

        @Override
        public int getCount() {
            if (mReddit.mRedditClient.isAuthenticated() && mReddit.mRedditClient.hasActiveUserContext()) {
                if (TextUtils.equals(mReddit.mRedditClient.getAuthenticatedUser(), mUsername)) {
                    return 5;
                }
            }
            return 4;
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
                case 3:
                    return getString(R.string.gilded);
                case 4:
                    return getString(R.string.saved);
            }
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_subreddit, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        mAddFriend = (MenuItem) menu.findItem(R.id.subscribe);

        if (Utilities.loggedIn()) {
            if (Utilities.isFriend(mUsername)) {
                mIsFriend = true;
                mAddFriend.setIcon(R.drawable.ic_favorite_24dp);
            }
        }

        mAddFriend.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (!Utilities.loggedIn()) {
                    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.must_log_in), Toast.LENGTH_LONG).show();
                    return false;
                }

                if (mIsFriend) {
                    mIsFriend = false;
                    mAddFriend.setIcon(R.drawable.ic_favorite_outline_24dp);
                    Reddit.getInstance().removeFriend(mUsername);
                } else {
                    mIsFriend = true;
                    mAddFriend.setIcon(R.drawable.ic_favorite_24dp);
                    Reddit.getInstance().addFriend(mUsername);
                }
                return true;
            }
        });

        //hide friend icon on my profile
        if (TextUtils.equals(Utilities.getLoggedInUsername(), mUsername)) {
            mAddFriend.setVisible(false);
        }

        return true;
    }

}
