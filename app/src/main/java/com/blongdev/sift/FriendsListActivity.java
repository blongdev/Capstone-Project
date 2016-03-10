package com.blongdev.sift;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class FriendsListActivity extends AppCompatActivity {

    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

}
