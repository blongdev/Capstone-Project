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
    private int mPostId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

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

    }
}
