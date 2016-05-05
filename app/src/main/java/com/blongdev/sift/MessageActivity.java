package com.blongdev.sift;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import com.blongdev.sift.database.SiftContract;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MessageActivity extends BaseActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.message_container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);


//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Bundle args = new Bundle();

            MessageActivityFragment messageFrag = new MessageActivityFragment();

            switch (position) {
                case 0:
                    args.putInt(getString(R.string.mailbox), SiftContract.Messages.MAILBOX_TYPE_INBOX);
                    break;
                case 1:
                    args.putInt(getString(R.string.mailbox), SiftContract.Messages.MAILBOX_TYPE_INBOX);
                    args.putBoolean(getString(R.string.unread), true);
                    break;
                case 2:
                    args.putInt(getString(R.string.mailbox), SiftContract.Messages.MAILBOX_TYPE_SENT);
                    break;
                case 3:
                    args.putInt(getString(R.string.mailbox), SiftContract.Messages.MAILBOX_TYPE_MENTIONS);
                    break;
            }

            messageFrag.setArguments(args);
            return messageFrag;

        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.inbox);
                case 1:
                    return getString(R.string.unread);
                case 2:
                    return getString(R.string.sent);
                case 3:
                    return getString(R.string.mentions);
            }
            return null;
        }
    }

}
