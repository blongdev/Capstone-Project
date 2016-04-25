package com.blongdev.sift;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Debug;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blongdev.sift.database.SiftContract;
import com.blongdev.sift.database.SiftDbHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.squareup.okhttp.internal.Util;
import com.squareup.picasso.Picasso;

import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.models.Comment;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.Paginator;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.SubmissionSearchPaginator;
import net.dean.jraw.paginators.SubredditPaginator;
import net.dean.jraw.paginators.TimePeriod;
import net.dean.jraw.paginators.UserContributionPaginator;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class SubredditFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    ViewPager mPager;
    PagerAdapter mPagerAdapter;
    private long mSubredditId;
    private String mSubredditName;
    private List<ContributionInfo> mPosts;
    private PostListAdapter mPostListAdapter;
    private RecyclerView mRecyclerView;
    private ContentResolver mContentResolver;
    private ProgressBar mLoadingSpinner;
    private Context mContext;

    private Reddit mReddit;
    private Paginator mPaginator;
    private boolean mLoading;
    private LinearLayoutManager mLayoutManager;

    private int mPaginatorType;
    private String mSearchTerm;
    private String mUsername;
    private String mCategory;

    private static final int PAGE_SIZE = 25;
    private boolean savePosts;

    private TextView mEmptyText;

    private int mRefreshPoint = 0;

    public SubredditFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mContext = getContext();
        mContentResolver = mContext.getContentResolver();

        mLoadingSpinner = (ProgressBar) rootView.findViewById(R.id.progressSpinner);
        mEmptyText = (TextView) rootView.findViewById(R.id.empty);

        mReddit = Reddit.getInstance();
        mPosts = new ArrayList<ContributionInfo>();

        Bundle arg = getArguments();
        if (arg != null) {
            mSubredditId = arg.getLong(getString(R.string.subreddit_id));
            mSubredditName = arg.getString(getString(R.string.subreddit_name));
            mPaginatorType = arg.getInt(getString(R.string.paginator_type), SubredditInfo.SUBREDDIT_PAGINATOR);
            mSearchTerm = arg.getString(getString(R.string.search_term));
            mUsername = arg.getString(getString(R.string.username));
            mCategory = arg.getString(getString(R.string.category));
        } else {
            Intent intent = getActivity().getIntent();
            mSubredditName = intent.getStringExtra(getString(R.string.subreddit_name));
            mSubredditId = intent.getLongExtra(getString(R.string.subreddit_id), 0);
            mPaginatorType = intent.getIntExtra(getString(R.string.paginator_type), SubredditInfo.SUBREDDIT_PAGINATOR);
            mSearchTerm = intent.getStringExtra(getString(R.string.search_term));
            mUsername = intent.getStringExtra(getString(R.string.username));
            mCategory = intent.getStringExtra(getString(R.string.category));
        }

        if (mPaginatorType == SubredditInfo.SUBMISSION_SEARCH_PAGINATOR && !TextUtils.isEmpty(mSearchTerm)) {
            mPaginator = new SubmissionSearchPaginator(mReddit.mRedditClient, mSearchTerm);
            mPaginator.setTimePeriod(TimePeriod.ALL);
            savePosts = false;
        } else if (mPaginatorType == SubredditInfo.USER_CONTRIBUTION_PAGINATOR && !TextUtils.isEmpty(mUsername) && !TextUtils.isEmpty(mCategory)) {
            mPaginator = new UserContributionPaginator(mReddit.mRedditClient, mCategory, mUsername);
            savePosts = false;
        } else {
            if (TextUtils.equals(mSubredditName, getString(R.string.frontPage))) {
                mPaginator = new SubredditPaginator(mReddit.mRedditClient);
            } else {
                mPaginator = new SubredditPaginator(mReddit.mRedditClient, mSubredditName);
            }
            savePosts = true;
            //have to use setSearchSorting for submissionSearchPaginator;
            mPaginator.setSorting(Sorting.HOT); // Default is HOT (Paginator.DEFAULT_SORTING)
        }
        mPaginator.setLimit(PAGE_SIZE);

        if (savePosts) {
            getLoaderManager().initLoader(0, null, this);
        }

        //RACE CONDITION WITH AUTHENTICATION IN MAIN ACTIVITY
        // resetting the viewpager adapter in main activity as workaround
        if (Utilities.connectedToNetwork(mContext)) {
            if (mReddit.mRedditClient.isAuthenticated()) {
                new GetPostsTask().execute();
            } else {
                //Base Activity is authenticating. add spinner until authenticated
                mLoadingSpinner.setVisibility(View.VISIBLE);
            }
        }
//          else {
            //getPostsFromDb();
//        }

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.cardList);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mPostListAdapter = new PostListAdapter(mPosts);
        mRecyclerView.setAdapter(mPostListAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0 && mLayoutManager.findLastVisibleItemPosition() >= mRefreshPoint && !mLoading) {
                    if (Utilities.connectedToNetwork(mContext)) {
                        if (mReddit.mRedditClient.isAuthenticated()) {
                            new GetPostsTask().execute();
                        }
                    }
                }
            }
        });

        return rootView;
    }

    private void getPostsFromDb() {
        Cursor cursor = null;
        try {
            String selection = SiftContract.Posts.COLUMN_SUBREDDIT_ID + " = ?";
            String[] selectionArgs = new String[]{String.valueOf(mSubredditId)};
            cursor = getContext().getContentResolver().query(SiftContract.Posts.CONTENT_URI, null, selection, selectionArgs, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    PostInfo post = new PostInfo();
                    post.mId = cursor.getInt(cursor.getColumnIndex(SiftContract.Posts._ID));
                    post.mTitle = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_TITLE));
                    post.mUsername = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_OWNER_USERNAME));
                    post.mUserId = cursor.getInt(cursor.getColumnIndex(SiftContract.Posts.COLUMN_OWNER_ID));
                    post.mSubreddit = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_SUBREDDIT_NAME));
                    post.mSubredditId = cursor.getInt(cursor.getColumnIndex(SiftContract.Posts.COLUMN_SUBREDDIT_ID));
                    post.mPoints = cursor.getInt(cursor.getColumnIndex(SiftContract.Posts.COLUMN_POINTS));
                    post.mImageUrl = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_IMAGE_URL));
                    post.mUrl = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_URL));
                    post.mComments = cursor.getInt(cursor.getColumnIndex(SiftContract.Posts.COLUMN_NUM_COMMENTS));
                    post.mAge= cursor.getInt(cursor.getColumnIndex(SiftContract.Posts.COLUMN_DATE_CREATED));
                    post.mFavorited = cursor.getInt(cursor.getColumnIndex(SiftContract.Posts.COLUMN_FAVORITED)) == 1 ? true : false;
                    post.mBody = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_BODY));
                    post.mDomain = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_DOMAIN));
                    mPosts.add(post);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private final class GetPostsTask extends AsyncTask<String, Void, ArrayList<ContributionInfo>> {
        @Override
        protected ArrayList<ContributionInfo> doInBackground(String... params) {
            ArrayList<ContributionInfo> newPostArray = new ArrayList<ContributionInfo>();
            if (mReddit.mRedditClient.isAuthenticated() && mPaginator != null && mPaginator.hasNext()) {
                Listing<Contribution> page = mPaginator.next();
                int i = 0;
                for (Contribution contribution : page) {
                    if (contribution instanceof Comment) {
                        Comment comment = (Comment) contribution;
                        CommentInfo commentInfo = new CommentInfo();
                        commentInfo.mUsername = comment.getAuthor();
                        commentInfo.mPoints = comment.getScore();
//                        commentInfo.mComments = comment.getReplies();
                        commentInfo.mBody = comment.getBody();
                        commentInfo.mAge = comment.getCreatedUtc().getTime();
                        commentInfo.mContributionType = ContributionInfo.CONTRIBUTION_COMMENT;
                        commentInfo.mVote = comment.getVote().getValue();
                        commentInfo.mJrawComment = comment;
                        commentInfo.mPostServerId = Utilities.getServerIdFromFullName(comment.getSubmissionId());
                        newPostArray.add(commentInfo);
                        mPosts.add(commentInfo);
                    } else {
                        Submission submission = (Submission) contribution;
                        PostInfo post = new PostInfo();
                        post.mServerId = submission.getId();
                        post.mTitle = submission.getTitle();
                        post.mUsername = submission.getAuthor();
                        post.mSubreddit = submission.getSubredditName();
                        post.mPoints = submission.getScore();
                        post.mUrl = submission.getUrl();
                        post.mImageUrl = Reddit.getImageUrl(submission);
                        post.mComments = submission.getCommentCount();
                        post.mBody = submission.getSelftext();
                        post.mDomain = submission.getDomain();
                        post.mAge = submission.getCreatedUtc().getTime();
                        post.mPosition = ((mPaginator.getPageIndex() - 1) * PAGE_SIZE) + i;
                        post.mContributionType = ContributionInfo.CONTRIBUTION_POST;
                        post.mVote = submission.getVote().getValue();
                        post.mFavorited = submission.isSaved();
                        newPostArray.add(post);
                        mPosts.add(post);
                    }
                    i++;
                }
                mRefreshPoint = ((mPaginator.getPageIndex() -1) * PAGE_SIZE);
            }

            return newPostArray;
        }

        @Override
        protected void onPreExecute() {
            mLoading = true;

            if(mPosts.size() == 0) {
                mLoadingSpinner.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected void onPostExecute(ArrayList<ContributionInfo> posts) {
            mLoading = false;
            mLoadingSpinner.setVisibility(View.GONE);
            //mPostListAdapter.refreshWithList(mPosts);

            if (mPosts.size() == 0) {
                mEmptyText.setVisibility(View.VISIBLE);
            }

            if(savePosts) {
                new AddPostsToDbTask(posts).execute();
            } else {
                mPostListAdapter.refreshWithList(mPosts);
            }
        }

    }

    private final class AddPostsToDbTask extends AsyncTask<String, Void, Void> {

        private ArrayList<ContributionInfo> mNewPosts;

        public AddPostsToDbTask(ArrayList<ContributionInfo> posts) {
            mNewPosts = posts;
        }

        @Override
        protected Void doInBackground(String... params) {
            //add to db
            for (ContributionInfo post : mNewPosts) {
                if (post.mContributionType != ContributionInfo.CONTRIBUTION_COMMENT) {
                    addPostToDb((PostInfo) post);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {

        }

        //TODO batch insert
        private void addPostToDb(PostInfo post) {
            ContentValues cv = new ContentValues();
            cv.put(SiftContract.Posts.COLUMN_OWNER_USERNAME, post.mUsername);
            cv.put(SiftContract.Posts.COLUMN_SERVER_ID, post.mServerId);
            cv.put(SiftContract.Posts.COLUMN_NUM_COMMENTS, post.mComments);
            cv.put(SiftContract.Posts.COLUMN_POINTS, post.mPoints);
            cv.put(SiftContract.Posts.COLUMN_SUBREDDIT_ID, mSubredditId);
            cv.put(SiftContract.Posts.COLUMN_SUBREDDIT_NAME, post.mSubreddit);
            cv.put(SiftContract.Posts.COLUMN_URL, post.mUrl);
            cv.put(SiftContract.Posts.COLUMN_IMAGE_URL, post.mImageUrl);
            cv.put(SiftContract.Posts.COLUMN_TITLE, post.mTitle);
            cv.put(SiftContract.Posts.COLUMN_BODY, post.mBody);
            cv.put(SiftContract.Posts.COLUMN_DATE_CREATED, post.mAge);
            cv.put(SiftContract.Posts.COLUMN_DOMAIN, post.mDomain);
            cv.put(SiftContract.Posts.COLUMN_POSITION, post.mPosition);
            cv.put(SiftContract.Posts.COLUMN_SERVER_ID, post.mServerId);
            cv.put(SiftContract.Posts.COLUMN_VOTE, post.mVote);
            cv.put(SiftContract.Posts.COLUMN_FAVORITED, post.mFavorited);
            mContentResolver.insert(SiftContract.Posts.CONTENT_URI, cv);
        }
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = SiftContract.Posts.COLUMN_SUBREDDIT_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(mSubredditId)};
        return new CursorLoader(getActivity(), SiftContract.Posts.CONTENT_URI,
                null, selection, selectionArgs,
                SiftContract.Posts.COLUMN_POSITION);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null) {
            mPosts.clear();
            //limit added so paginator refresh point happens at the right time to keep data fresh
            //int limit = (mPaginator.getPageIndex()+1) * PAGE_SIZE;
            while (cursor.moveToNext()) {
                PostInfo post = new PostInfo();
                post.mId = cursor.getInt(cursor.getColumnIndex(SiftContract.Posts._ID));
                post.mTitle = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_TITLE));
                post.mUsername = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_OWNER_USERNAME));
                post.mUserId = cursor.getInt(cursor.getColumnIndex(SiftContract.Posts.COLUMN_OWNER_ID));
                post.mSubreddit = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_SUBREDDIT_NAME));
                post.mSubredditId = cursor.getInt(cursor.getColumnIndex(SiftContract.Posts.COLUMN_SUBREDDIT_ID));
                post.mPoints = cursor.getInt(cursor.getColumnIndex(SiftContract.Posts.COLUMN_POINTS));
                post.mImageUrl = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_IMAGE_URL));
                post.mUrl = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_URL));
                post.mComments = cursor.getInt(cursor.getColumnIndex(SiftContract.Posts.COLUMN_NUM_COMMENTS));
                post.mAge = cursor.getLong(cursor.getColumnIndex(SiftContract.Posts.COLUMN_DATE_CREATED));
                post.mFavorited = cursor.getInt(cursor.getColumnIndex(SiftContract.Posts.COLUMN_FAVORITED)) == 1 ? true : false;
                post.mBody = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_BODY));
                post.mServerId =cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_SERVER_ID));
                post.mDomain = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_DOMAIN));
                post.mPosition = cursor.getInt(cursor.getColumnIndex(SiftContract.Posts.COLUMN_POSITION));
                post.mVote = cursor.getInt(cursor.getColumnIndex(SiftContract.Posts.COLUMN_VOTE));
                mPosts.add(post);
            }
        }

        mPostListAdapter.refreshWithList(mPosts);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mPostListAdapter.refreshWithList(null);
    }

    @Override
    public void onResume() {
        super.onResume();
//        //RACE CONDITION WITH AUTHENTICATION IN MAIN ACTIVITY
//        // resetting the viewpager adapter in main activity as workaround
//        if (Utilities.connectedToNetwork(mContext)) {
//            if (mReddit.mRedditClient.isAuthenticated()) {
//                new GetPostsTask().execute();
//            } else {
//                //Base Activity is authenticating. add spinner until authenticated
//                mLoadingSpinner.setVisibility(View.VISIBLE);
//            }
//        }
    }


}
