package com.blongdev.sift;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

public class FriendsListActivity extends BaseActivity implements FriendsListActivityFragment.Callback {

    boolean mIsTablet;
    UserPagerAdapter mUserPagerAdapter;
    ViewPager mViewPager;
    String mUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        if (findViewById(R.id.friend_pager) != null) {
            mIsTablet = true;
            mUserPagerAdapter = new UserPagerAdapter(getSupportFragmentManager(), mUsername);
            mViewPager = (ViewPager) findViewById(R.id.friend_pager);
            mViewPager.setAdapter(mUserPagerAdapter);

            TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
            tabLayout.setupWithViewPager(mViewPager);

            if (savedInstanceState == null) {
                FragmentManager fm = getSupportFragmentManager();
                FriendsListActivityFragment friendsListFrag = new FriendsListActivityFragment();

                Bundle args = new Bundle();
                args.putBoolean(getString(R.string.isTablet), true);
                friendsListFrag.setArguments(args);
                android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.fragment, friendsListFrag);
                ft.commit();
            }


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

            if (Utilities.loggedIn()) {
                if (TextUtils.equals(Utilities.getLoggedInUsername(), mUsername)) {
                    fab.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public void onItemSelected(String name) {
        if (mIsTablet) {
            mUserPagerAdapter.setUser(name);
            mUsername = name;
            mViewPager.setAdapter(mUserPagerAdapter);
        } else {
            Intent intent = new Intent(SiftApplication.getContext(), UserInfoActivity.class);
            intent.putExtra(getString(R.string.username), name);
            startActivity(intent);
        }
    }


}
