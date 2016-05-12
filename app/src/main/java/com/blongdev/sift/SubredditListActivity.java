package com.blongdev.sift;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

public class SubredditListActivity extends BaseActivity implements SubredditListActivityFragment.Callback {

    FragmentManager mFragmentManager;
    SubredditFragment mSubredditFragment;
    boolean mIsTablet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddit_list);
        mFragmentManager = getSupportFragmentManager();
        if (findViewById(R.id.posts_fragment) != null) {
            mIsTablet = true;
        }
    }

    @Override
    public void onItemSelected(long id, String name) {
        if (mIsTablet) {
            SubredditFragment subFrag = new SubredditFragment();
            Bundle args = new Bundle();
            args.putLong(getString(R.string.subreddit_id), id);
            args.putString(getString(R.string.subreddit_name), name);
            subFrag.setArguments(args);
            android.support.v4.app.FragmentTransaction ft = mFragmentManager.beginTransaction();
            ft.replace(R.id.posts_fragment, subFrag);
            ft.commit();
        } else {
            Intent intent = new Intent(this, SubredditActivity.class);
            intent.putExtra(getString(R.string.subreddit_id), id);
            intent.putExtra(getString(R.string.subreddit_name), name);
            startActivity(intent);
        }
    }
}
