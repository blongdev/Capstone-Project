package com.blongdev.sift;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Debug;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.blongdev.sift.database.SiftContract;
import com.squareup.picasso.Picasso;

import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.SubredditPaginator;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class SubredditFragment extends Fragment {

    ViewPager mPager;
    PagerAdapter mPagerAdapter;
    private int mSubredditId;
    private String mSubredditName;
    private ArrayList<PostInfo> mPosts;
    private PostListAdapter mPostListAdapter;
    private RecyclerView mRecyclerView;
    private ContentResolver mContentResolver;
    private ProgressBar mLoadingSpinner;

    private Reddit mReddit;

    public SubredditFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mContentResolver = getContext().getContentResolver();

        mLoadingSpinner = (ProgressBar) rootView.findViewById(R.id.progressSpinner);


        mReddit = Reddit.getInstance();
        mPosts = new ArrayList<PostInfo>();

        Bundle arg = getArguments();
        if (arg != null) {
            mSubredditId = arg.getInt(getString(R.string.subreddit_id));
            mSubredditName = arg.getString(getString(R.string.subreddit_name));
        } else {
            Intent intent = getActivity().getIntent();
            mSubredditName = intent.getStringExtra(getString(R.string.subreddit_name));
            mSubredditId = intent.getIntExtra(getString(R.string.subreddit_id), -1);
        }

        //populatePosts();
        //RACE CONDITION WITH AUTHENTICATION IN MAIN ACTIVITY
        // resetting the viewpager adapter in main activity as workaround
        if (Utilities.connectedToNetwork(getContext()) && mReddit.mRedditClient.isAuthenticated()) {
            new GetPostsTask().execute();
        } else {
            getPostsFromDb();
        }

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.cardList);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mPostListAdapter = new PostListAdapter(mPosts);
        mRecyclerView.setAdapter(mPostListAdapter);

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
                    mPosts.add(post);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private final class GetPostsTask extends AsyncTask<String, Void, ArrayList<PostInfo>> {
        @Override
        protected ArrayList<PostInfo> doInBackground(String... params) {
            ArrayList<PostInfo> postArray = new ArrayList<PostInfo>();
            SubredditPaginator paginator = new SubredditPaginator(mReddit.mRedditClient, mSubredditName);
            if (paginator.hasNext()) {
                Listing<Submission> firstPage = paginator.next();
                for (Submission submission : firstPage) {
                    PostInfo post = new PostInfo();
                    post.mServerId = submission.getId();
                    post.mTitle = submission.getTitle();
                    post.mUsername = submission.getAuthor();
                    post.mSubreddit = submission.getSubredditName();
                    post.mPoints = submission.getScore();
                    post.mUrl = submission.getUrl();
                    post.mImageUrl = submission.getThumbnail();
                    post.mComments = submission.getCommentCount();
                    postArray.add(post);
                }
            }

            return postArray;
        }

        @Override
        protected void onPreExecute() {
            mLoadingSpinner.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(ArrayList<PostInfo> posts) {
            mLoadingSpinner.setVisibility(View.GONE);
            mPosts = posts;
            mPostListAdapter.refreshWithList(posts);

            new AddPostsToDbTask().execute();
        }
    }

    private final class AddPostsToDbTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            //add to db
            for (PostInfo post : mPosts) {
                addPostToDb(post);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {

        }

        private void addPostToDb(PostInfo post) {
            ContentValues cv = new ContentValues();
            cv.put(SiftContract.Posts.COLUMN_OWNER_USERNAME, post.mUsername);
            cv.put(SiftContract.Posts.COLUMN_SERVER_ID, post.mServerId);
            cv.put(SiftContract.Posts.COLUMN_NUM_COMMENTS, post.mComments);
            cv.put(SiftContract.Posts.COLUMN_POINTS, post.mPoints);
            cv.put(SiftContract.Posts.COLUMN_SUBREDDIT_ID, mSubredditId);
            cv.put(SiftContract.Posts.COLUMN_SUBREDDIT_NAME, mSubredditName);
            cv.put(SiftContract.Posts.COLUMN_URL, post.mUrl);
            cv.put(SiftContract.Posts.COLUMN_IMAGE_URL, post.mImageUrl);
            cv.put(SiftContract.Posts.COLUMN_TITLE, post.mTitle);
            mContentResolver.insert(SiftContract.Posts.CONTENT_URI, cv);
        }
    }
}
