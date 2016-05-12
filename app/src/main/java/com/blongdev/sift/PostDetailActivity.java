package com.blongdev.sift;



import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.RippleDrawable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.blongdev.sift.database.SiftContract;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.unnamed.b.atv.model.TreeNode;

public class PostDetailActivity extends BaseActivity {

    private static final String VOTE = "vote";
    private static final String FAVORITE = "favorite";
    private static final String COMMENTS = "comments";

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
    FrameLayout mPostView;
    FrameLayout mCommentsView;
    AppBarLayout mAppBar;
    TreeNode mReplyNode;
    int mVote;
    FloatingActionButton mFab;

    boolean mIsTablet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        if (findViewById(R.id.tablet) != null) {
            mIsTablet = true;
        }

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


        mPostDetailLayout = (LinearLayout) findViewById(R.id.post_detail_layout);
        mTitle = (TextView) findViewById(R.id.post_title);
        mUsername = (TextView) findViewById(R.id.post_username);
        mSubreddit = (TextView) findViewById(R.id.post_subreddit);
        mPoints = (TextView) findViewById(R.id.post_points);
        mComments = (TextView) findViewById(R.id.post_comments);
        mUrl = (TextView) findViewById(R.id.post_url);
        mAge = (TextView) findViewById(R.id.post_age);
        mWebView = (WebView) findViewById(R.id.post_web_view);
        mLoadingSpinner = (ProgressBar) findViewById(R.id.progressSpinner);
        mBody = (TextView) findViewById(R.id.post_body);
        mUpvote = (ImageView) findViewById(R.id.upvote);
        mDownvote = (ImageView) findViewById(R.id.downvote);
        mReply = (ImageView) findViewById(R.id.reply);
        mCommentArea = (LinearLayout) findViewById(R.id.comment_area);
        mReplyText = (EditText) findViewById(R.id.reply_text);
        mSendComment = (ImageView) findViewById(R.id.send);
        mAppBar = (AppBarLayout) findViewById(R.id.appbar);

        mFavorite = (ImageView) findViewById(R.id.favorite);

        mTitle.setText(title);
        mUsername.setText(username);
        mSubreddit.setText(subreddit);
        mPoints.setText(points);
        mComments.setText(comments);
        mUrl.setText(domain);
        mAge.setText(age);

        if (!mIsTablet) {
            mFab = (FloatingActionButton) findViewById(R.id.fab);
            mFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggleComments();
                }
            });
        }

        mUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUsername.getText().toString();

                Intent intent = new Intent(v.getContext(), UserInfoActivity.class);
                intent.putExtra(v.getContext().getString(R.string.username), username);

                v.getContext().startActivity(intent);
            }
        });

        mSubreddit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subreddit = mSubreddit.getText().toString();

                Intent intent = new Intent(v.getContext(), SubredditActivity.class);
                intent.putExtra(v.getContext().getString(R.string.subreddit_name), subreddit);

                v.getContext().startActivity(intent);
            }
        });

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

                if(mReplyNode == null) {
                    mReplyNode = mCommentsFragment.replyToPost();
                    mReply.setImageResource(R.drawable.ic_clear_24dp);
                    mCommentsFragment.focusOnReply(mReplyNode);
                    mAppBar.setExpanded(false);
                } else {
                    mCommentsFragment.removeReply(mReplyNode);
                    mReply.setImageResource(R.drawable.ic_reply_24dp);
                    mReplyNode = null;
                }

                if (mPostShowing) {
                    toggleComments();
                }

            }
        });

        if (mReddit.mRedditClient.isAuthenticated() && mReddit.mRedditClient.hasActiveUserContext()) {
            if (!TextUtils.isEmpty(mPostServerId)) {
                long favoriteId = Utilities.getFavoriteId(getApplicationContext(), mPostServerId);
                if (favoriteId > 0) {
                    mFavorited = true;
                    mFavorite.setImageResource(R.drawable.ic_favorite_24dp);
                }
            }
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

        mPostView = (FrameLayout) findViewById(R.id.post_detail_fragment);
        mCommentsView = (FrameLayout) findViewById(R.id.comments_fragment);

        if (!mIsTablet) {
            mPostShowing = true;
            mCommentsView.setVisibility(View.GONE);
        }

        if (savedInstanceState != null) {
            mVote = savedInstanceState.getInt(VOTE);
            mFavorited = savedInstanceState.getBoolean(FAVORITE);
            boolean showComments = savedInstanceState.getBoolean(COMMENTS);
            if (showComments && !mIsTablet) {
                toggleComments();
            }

            if(mFavorited) {
                mFavorite.setImageResource(R.drawable.ic_favorite_24dp);
            }
        }


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
                } else {
                    mFavorited = true;
                    mFavorite.setImageResource(R.drawable.ic_favorite_24dp);
                    Reddit.getInstance().favoritePost(getApplicationContext(), mPostServerId);
                }
            }
        });


        //admob
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("83A93BC0EF7E3BAB7AF855EAF3421EC4")
                .addTestDevice("8ECA7CA419932CA97BD67F76A2A69C4F")
                .build();
        mAdView.loadAd(adRequest);
    }

    private void toggleComments() {
        android.support.v4.app.FragmentTransaction ft = mFragmentManager.beginTransaction();
        if (mPostShowing) {
            mPostShowing = false;
            mCommentsView.setVisibility(View.VISIBLE);
            mFab.setImageResource(R.drawable.ic_info_outline_24dp);
        } else {
            mPostShowing = true;
            mCommentsView.setVisibility(View.GONE);
            mFab.setImageResource(R.drawable.ic_forum_24dp);
        }
        ft.commit();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(VOTE, mVote);
        savedInstanceState.putBoolean(FAVORITE, mFavorited);
        savedInstanceState.putBoolean(COMMENTS, !mPostShowing);
        super.onSaveInstanceState(savedInstanceState);
    }
}
