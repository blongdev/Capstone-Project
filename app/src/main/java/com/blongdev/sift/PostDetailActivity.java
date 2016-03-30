package com.blongdev.sift;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.fasterxml.jackson.databind.deser.Deserializers;

public class PostDetailActivity extends BaseActivity {
    private int mPostId = 0;
    private String mPostServerId;
    PostDetailFragment mPostFragment;
    CommentsFragment mCommentsFragment;
    FragmentManager mFragmentManager;
    boolean mPostShowing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        Intent intent = getIntent();
        if (intent != null) {
            mPostId = intent.getIntExtra(getString(R.string.post_id), 0);
            mPostServerId = intent.getStringExtra(getString(R.string.server_id));
        }

        //add post and comment fragments
        mFragmentManager = getSupportFragmentManager();
        mPostFragment = new PostDetailFragment();
        mCommentsFragment = new CommentsFragment();
        Bundle args = new Bundle();
        args.putInt(getString(R.string.post_id), mPostId);
        args.putString(getString(R.string.server_id), mPostServerId);
        mPostFragment.setArguments(args);
        mCommentsFragment.setArguments(args);
        android.support.v4.app.FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.add(R.id.post_detail_fragment, mPostFragment);
        ft.add(R.id.comments_fragment, mCommentsFragment);
        ft.commit();

        FrameLayout postView = (FrameLayout) findViewById(R.id.post_detail_fragment);
        final FrameLayout commentsView = (FrameLayout) findViewById(R.id.comments_fragment);

        mPostShowing = true;
        commentsView.setVisibility(View.GONE);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.support.v4.app.FragmentTransaction ft = mFragmentManager.beginTransaction();
                if (mPostShowing) {
                    mPostShowing = false;
                    commentsView.setVisibility(View.VISIBLE);
                } else {
                    mPostShowing = true;
                    commentsView.setVisibility(View.GONE);
                }
                ft.commit();
            }
        });
    }
}
