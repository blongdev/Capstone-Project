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
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.deser.Deserializers;

public class PostDetailActivity extends BaseActivity {
    private int mPostId = 0;
    private String mPostServerId;
    PostDetailFragment mPostFragment;
    CommentsFragment mCommentsFragment;
    FragmentManager mFragmentManager;
    boolean mPostShowing;

    LinearLayout mPostDetailLayout;
    TextView mTitle;
    TextView mUsername;
    TextView mSubreddit;
    TextView mPoints;
    TextView mComments;
    TextView mUrl;
    TextView mAge;
    ImageView mImage;
    WebView mWebView;
    ProgressBar mLoadingSpinner;
    TextView mBody;

    FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        Intent intent = getIntent();
        if (intent != null) {
            mPostId = intent.getIntExtra(getString(R.string.post_id), 0);
            mPostServerId = intent.getStringExtra(getString(R.string.server_id));
        }

        String title = intent.getStringExtra(getString(R.string.title));
        String username = intent.getStringExtra(getString(R.string.username));
        String subreddit = intent.getStringExtra(getString(R.string.subreddit));
        String points = intent.getStringExtra(getString(R.string.points));
        String comments = intent.getStringExtra(getString(R.string.comments));
        String url = intent.getStringExtra(getString(R.string.url));
        String age = intent.getStringExtra(getString(R.string.age));
        String imageUrl = intent.getStringExtra(getString(R.string.image_url));
        String body = intent.getStringExtra(getString(R.string.body));
        String domain = intent.getStringExtra(getString(R.string.domain));
//

        mPostDetailLayout = (LinearLayout) findViewById(R.id.post_detail_layout);
        mTitle = (TextView) findViewById(R.id.post_title);
        mUsername = (TextView) findViewById(R.id.post_username);
        mSubreddit = (TextView) findViewById(R.id.post_subreddit);
        mPoints = (TextView) findViewById(R.id.post_points);
        mComments = (TextView) findViewById(R.id.post_comments);
        mUrl = (TextView) findViewById(R.id.post_url);
        mAge = (TextView) findViewById(R.id.post_age);
        mImage = (ImageView) findViewById(R.id.post_detail_image);
        mWebView = (WebView) findViewById(R.id.post_web_view);
        mLoadingSpinner = (ProgressBar) findViewById(R.id.progressSpinner);
        mBody = (TextView) findViewById(R.id.post_body);


        mTitle.setText(title);
        mUsername.setText(username);
        mSubreddit.setText(subreddit);
        mPoints.setText(points);
        mComments.setText(comments);
        mUrl.setText(domain);
        mAge.setText(age);
        //mBody.setText(body);

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

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.support.v4.app.FragmentTransaction ft = mFragmentManager.beginTransaction();
                if (mPostShowing) {
                    mPostShowing = false;
                    commentsView.setVisibility(View.VISIBLE);
                    mFab.setImageResource(R.drawable.ic_attachment_24dp);
                } else {
                    mPostShowing = true;
                    commentsView.setVisibility(View.GONE);
                    mFab.setImageResource(R.drawable.ic_forum_24dp);
                }
                ft.commit();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
