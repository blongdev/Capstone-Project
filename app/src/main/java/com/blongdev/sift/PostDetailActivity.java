package com.blongdev.sift;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class PostDetailActivity extends AppCompatActivity {

    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private int mPostId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        if (intent != null) {
            mPostId = intent.getIntExtra(getString(R.string.post_id), 0);
        }

        //add post and comment fragments
        FragmentManager fm = getSupportFragmentManager();
        PostDetailFragment postFragment = new PostDetailFragment();
        CommentsFragment commentsFragment = new CommentsFragment();
        Bundle args = new Bundle();
        args.putInt(getString(R.string.post_id), mPostId);
        postFragment.setArguments(args);
        commentsFragment.setArguments(args);
        android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.post_detail_fragment, postFragment);
        ft.add(R.id.comments_fragment, commentsFragment);
        ft.commit();

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
    }
}
