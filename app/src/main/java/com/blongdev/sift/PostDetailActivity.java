package com.blongdev.sift;

import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blongdev.sift.database.SiftContract;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class PostDetailActivity extends BaseActivity {
    private int mPostId = 0;
    private String mPostServerId;
    PostDetailFragment mPostFragment;
    CommentsFragment mCommentsFragment;
    FragmentManager mFragmentManager;
    boolean mPostShowing;
    ImageView mFavorite;
    boolean mFavorited;

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
    ImageView mUpvote;
    ImageView mDownvote;
    ImageView mReply;
    LinearLayout mCommentArea;
    EditText mReplyText;
    ImageView mSendComment;

    int mVote;

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
        mVote = intent.getIntExtra(getString(R.string.vote),0);

//
//        if (!TextUtils.isEmpty(title)) {
//            Cursor cursor = null;
//            try {
//                String selection = SiftContract.Posts._ID + " = ?";
//                String[] selectionArgs = new String[]{String.valueOf(mPostId)};
//                cursor = getContentResolver().query(SiftContract.Posts.CONTENT_URI, null, selection, selectionArgs, null);
//                if (cursor != null && cursor.moveToFirst()) {
//                    title = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_TITLE));
//                    username = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_OWNER_USERNAME));
//                    subreddit = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_SUBREDDIT_NAME));
//                    points = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_POINTS));
//                    comments = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_NUM_COMMENTS));
//                    url = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_URL));
//                    age = Utilities.getAgeString(cursor.getLong(cursor.getColumnIndex(SiftContract.Posts.COLUMN_DATE_CREATED)));
//                    imageUrl = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_IMAGE_URL));
//                    body = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_BODY));
//                    domain = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_DOMAIN));
//                    mVote = cursor.getInt(cursor.getColumnIndex(SiftContract.Posts.COLUMN_VOTE));
//                }
//            } finally {
//                if (cursor != null) {
//                    cursor.close();
//                }
//            }
//        }

        mPostDetailLayout = (LinearLayout) findViewById(R.id.post_detail_layout);
        mTitle = (TextView) findViewById(R.id.post_title);
        mUsername = (TextView) findViewById(R.id.post_username);
        mSubreddit = (TextView) findViewById(R.id.post_subreddit);
        mPoints = (TextView) findViewById(R.id.post_points);
        mComments = (TextView) findViewById(R.id.post_comments);
        mUrl = (TextView) findViewById(R.id.post_url);
        mAge = (TextView) findViewById(R.id.post_age);
//        mImage = (ImageView) findViewById(R.id.post_detail_image);
        mWebView = (WebView) findViewById(R.id.post_web_view);
        mLoadingSpinner = (ProgressBar) findViewById(R.id.progressSpinner);
        mBody = (TextView) findViewById(R.id.post_body);
        mUpvote = (ImageView) findViewById(R.id.upvote);
        mDownvote = (ImageView) findViewById(R.id.downvote);
        mReply = (ImageView) findViewById(R.id.reply);
        mCommentArea = (LinearLayout) findViewById(R.id.comment_area);
        mReplyText = (EditText) findViewById(R.id.reply_text);
        mSendComment = (ImageView) findViewById(R.id.send);

        mFavorite = (ImageView) findViewById(R.id.favorite);

        if (mReddit.mRedditClient.isAuthenticated() && mReddit.mRedditClient.hasActiveUserContext()) {
            if (!TextUtils.isEmpty(mPostServerId)) {
                long favoriteId = Utilities.getFavoriteId(getApplicationContext(), mPostServerId);
                if (favoriteId > 0) {
                    mFavorited = true;
                    mFavorite.setImageResource(R.drawable.ic_favorite_24dp);
                }
            }
        }


        mUpvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upvote(getApplicationContext());
            }
        });

        mDownvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downvote(getApplicationContext());
            }
        });

        mReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Utilities.loggedIn(getApplicationContext())) {
                    Toast.makeText(getApplicationContext(), getString(R.string.must_log_in), Toast.LENGTH_LONG).show();
                    return;
                }

                if (mCommentArea.getVisibility() == View.GONE) {
                    mCommentArea.setVisibility(View.VISIBLE);
                    mReplyText.requestFocus();
                } else {
                    mCommentArea.setVisibility(View.GONE);
                    mReplyText.clearFocus();

                    //hide keyboard
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mReplyText.getWindowToken(), 0);
                }
            }
        });

        mSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(mReplyText.getText().toString())) {
                    Reddit.commentOnPost(getApplicationContext(), mPostServerId, mReplyText.getText().toString());
                    mReplyText.setText(null);
                    mCommentArea.setVisibility(View.GONE);
                    mReplyText.clearFocus();

                    //hide keyboard
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mReplyText.getWindowToken(), 0);
                }
            }
        });

        mTitle.setText(title);
        mUsername.setText(username);
        mSubreddit.setText(subreddit);
        mPoints.setText(points);
        mComments.setText(comments);
        mUrl.setText(domain);
        mAge.setText(age);
        //mBody.setText(body);

        if (mVote == SiftContract.Posts.UPVOTE) {
            mUpvote.setImageDrawable(SiftApplication.getContext().getResources().getDrawable(R.drawable.ic_up_arrow_filled_24dp));
            mDownvote.setImageDrawable(SiftApplication.getContext().getResources().getDrawable(R.drawable.ic_down_arrow_white_24dp));
            mPoints.setTextColor(SiftApplication.getContext().getResources().getColor(R.color.upvote));
        } else if (mVote == SiftContract.Posts.DOWNVOTE) {
            mDownvote.setImageDrawable(SiftApplication.getContext().getResources().getDrawable(R.drawable.ic_down_arrow_filled_24dp));
            mUpvote.setImageDrawable(SiftApplication.getContext().getResources().getDrawable(R.drawable.ic_up_arrow_white_24dp));
            mPoints.setTextColor(SiftApplication.getContext().getResources().getColor(R.color.downvote));
        } else {
            mUpvote.setImageDrawable(SiftApplication.getContext().getResources().getDrawable(R.drawable.ic_up_arrow_white_24dp));
            mDownvote.setImageDrawable(SiftApplication.getContext().getResources().getDrawable(R.drawable.ic_down_arrow_white_24dp));
            mPoints.setTextColor(Color.WHITE);
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

        mFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Utilities.loggedIn(getApplicationContext())) {
                    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.must_log_in), Toast.LENGTH_LONG).show();
                    return;
                }

                if (mFavorited) {
                    mFavorited = false;
                    mFavorite.setImageResource(R.drawable.ic_favorite_outline_24dp);
                    Reddit.getInstance().unfavoritePost(getApplicationContext(), mPostServerId);
//                    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.unsaved), Toast.LENGTH_LONG).show();
                } else {
                    mFavorited = true;
                    mFavorite.setImageResource(R.drawable.ic_favorite_24dp);
                    Reddit.getInstance().favoritePost(getApplicationContext(), mPostServerId);
//                    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.saved), Toast.LENGTH_LONG).show();
                }
            }
        });


        //admob
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("83A93BC0EF7E3BAB7AF855EAF3421EC4")
                .build();
        mAdView.loadAd(adRequest);
    }

    private void upvote(Context context) {
        if (!Utilities.loggedIn(context)) {
            Toast.makeText(context, context.getString(R.string.must_log_in), Toast.LENGTH_LONG).show();
            return;
        }

        if (mVote == SiftContract.Posts.UPVOTE) {
            mVote = SiftContract.Posts.NO_VOTE;
            mUpvote.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_up_arrow_white_24dp));
            mDownvote.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_down_arrow_white_24dp));
            mPoints.setTextColor(Color.WHITE);
            mPoints.setText(String.valueOf(Integer.valueOf(mPoints.getText().toString()) - 1));
        } else if(mVote == SiftContract.Posts.DOWNVOTE) {
            mVote = SiftContract.Posts.UPVOTE;
            mUpvote.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_up_arrow_filled_24dp));
            mDownvote.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_down_arrow_white_24dp));
            mPoints.setTextColor(context.getResources().getColor(R.color.upvote));
            mPoints.setText(String.valueOf(Integer.valueOf(mPoints.getText().toString()) + 2));
        } else {
            mVote = SiftContract.Posts.UPVOTE;
            mUpvote.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_up_arrow_filled_24dp));
            mDownvote.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_down_arrow_white_24dp));
            mPoints.setTextColor(context.getResources().getColor(R.color.upvote));
            mPoints.setText(String.valueOf(Integer.valueOf(mPoints.getText().toString()) + 1));
        }

        Reddit.votePost(context, mPostServerId, mVote);
    }

    private void downvote(Context context) {
        if (!Utilities.loggedIn(context)) {
            Toast.makeText(context, context.getString(R.string.must_log_in), Toast.LENGTH_LONG).show();
            return;
        }

        if (mVote == SiftContract.Posts.DOWNVOTE) {
            mVote = SiftContract.Posts.NO_VOTE;
            mDownvote.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_down_arrow_white_24dp));
            mUpvote.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_up_arrow_white_24dp));
            mPoints.setTextColor(Color.WHITE);
            mPoints.setText(String.valueOf(Integer.valueOf(mPoints.getText().toString()) + 1));
        } else if (mVote == SiftContract.Posts.UPVOTE) {
            mVote = SiftContract.Posts.DOWNVOTE;
            mDownvote.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_down_arrow_filled_24dp));
            mUpvote.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_up_arrow_white_24dp));
            mPoints.setTextColor(context.getResources().getColor(R.color.downvote));
            mPoints.setText(String.valueOf(Integer.valueOf(mPoints.getText().toString()) - 2));
        } else {
            mVote = SiftContract.Posts.DOWNVOTE;
            mDownvote.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_down_arrow_filled_24dp));
            mUpvote.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_up_arrow_white_24dp));
            mPoints.setTextColor(context.getResources().getColor(R.color.downvote));
            mPoints.setText(String.valueOf(Integer.valueOf(mPoints.getText().toString()) - 1));
        }

        Reddit.votePost(context, mPostServerId, mVote);

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_subreddit, menu);
//        // Associate searchable configuration with the SearchView
//        SearchManager searchManager =
//                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        SearchView searchView =
//                (SearchView) menu.findItem(R.id.menu_search).getActionView();
//        searchView.setSearchableInfo(
//                searchManager.getSearchableInfo(getComponentName()));
//
//        mFavorite = (MenuItem) menu.findItem(R.id.favorite);
//
//        if (mReddit.mRedditClient.isAuthenticated() && mReddit.mRedditClient.hasActiveUserContext()) {
//            if (!TextUtils.isEmpty(mPostServerId)) {
//                long subscriptionId = Utilities.getSubscriptionId(getApplicationContext(), mPostServerId);
//                if (subscriptionId > 0) {
//                    mFavorited = true;
//                    mFavorite.setIcon(R.drawable.ic_favorite_24dp);
//                }
//            }
//        }
//
//        mFavorite.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                if (!Utilities.loggedIn(getApplicationContext())) {
//                    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.must_log_in), Toast.LENGTH_LONG).show();
//                    return false;
//                }
//
//                if (mFavorited) {
//                    mFavorited = false;
//                    mFavorite.setIcon(R.drawable.ic_favorite_outline_24dp);
//                    Reddit.getInstance().unfavoritePost(getApplicationContext(), mPostServerId);
////                    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.unsaved), Toast.LENGTH_LONG).show();
//                } else {
//                    mFavorited = true;
//                    mFavorite.setIcon(R.drawable.ic_favorite_24dp);
//                    Reddit.getInstance().favoritePost(getApplicationContext(), mPostServerId);
////                    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.saved), Toast.LENGTH_LONG).show();
//                }
//                return true;
//            }
//        });
//
//        return true;
//    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
