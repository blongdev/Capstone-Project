package com.blongdev.sift;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Debug;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blongdev.sift.database.SiftContract;
import com.squareup.picasso.Picasso;

import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.SubredditPaginator;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class SubredditFragment extends Fragment {

    ViewPager mPager;
    PagerAdapter mPagerAdapter;
    private int mSubredditId;
    private ArrayList<PostInfo> mPosts;
    private PostListAdapter mPostListAdapter;
    private RecyclerView mRecyclerView;

    private Reddit mReddit;

    public SubredditFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        Bundle arg = getArguments();
        mSubredditId = arg.getInt(getString(R.string.subreddit_id));

        mReddit = Reddit.getInstance();

        mPosts = new ArrayList<PostInfo>();

        //populatePosts();
        //RACE CONDITION WITH AUTHENTICATION IN MAIN ACTIVITY
        // resetting the viewpager adapter in main activity as workaround
        if (mReddit.mRedditClient.isAuthenticated()) {
            new GetPostsTask().execute();
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

    private final class GetPostsTask extends AsyncTask<String, Void, ArrayList<PostInfo>> {
        @Override
        protected ArrayList<PostInfo> doInBackground(String... params) {
            ArrayList<PostInfo> postArray = new ArrayList<PostInfo>();
            SubredditPaginator paginator = new SubredditPaginator(mReddit.mRedditClient);
            Listing<Submission> firstPage = paginator.next();
            for (Submission submission : firstPage) {
                PostInfo post = new PostInfo();
                //post.mId =
                post.mTitle = submission.getTitle();
                post.mUsername = submission.getAuthor();
                //post.mUserId = submission.
                post.mSubreddit = submission.getSubredditName();
                //post.mSubredditId = cursor.getInt(cursor.getColumnIndex(SiftContract.Posts.COLUMN_SUBREDDIT_ID));
                post.mPoints = submission.getScore();
                post.mImageUrl = submission.getThumbnail();
                //post.mUrl = submission.getShortURL();
                post.mComments = submission.getCommentCount();
                //post.mAge= submission.getCreated().getTime();
//              post.mFavorited = cursor.getInt(cursor.getColumnIndex(SiftContract.Posts.COLUMN_FAVORITED)) == 1 ? true : false;
                postArray.add(post);
            }

            return postArray;
        }

        @Override
        protected void onPostExecute(ArrayList<PostInfo> posts) {
            mPosts = posts;
            mPostListAdapter.refreshWithList(posts);
        }
    }

    private void populatePosts() {

        Reddit reddit = Reddit.getInstance();

        SubredditPaginator paginator = new SubredditPaginator(reddit.mRedditClient);
        Listing<Submission> firstPage = paginator.next();

        for (Submission submission : firstPage) {
            PostInfo post = new PostInfo();
            //post.mId =
            post.mTitle = submission.getTitle();
            post.mUsername = submission.getAuthor();
            //post.mUserId = submission.
            post.mSubreddit = submission.getSubredditName();
            //post.mSubredditId = cursor.getInt(cursor.getColumnIndex(SiftContract.Posts.COLUMN_SUBREDDIT_ID));
            post.mPoints = submission.getScore();
//            post.mImageUrl = submission.getThumbnail();
//            post.mUrl = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_URL));
//            post.mComments = cursor.getInt(cursor.getColumnIndex(SiftContract.Posts.COLUMN_NUM_COMMENTS));
//            post.mAge= cursor.getInt(cursor.getColumnIndex(SiftContract.Posts.COLUMN_DATE_CREATED));
//            post.mFavorited = cursor.getInt(cursor.getColumnIndex(SiftContract.Posts.COLUMN_FAVORITED)) == 1 ? true : false;
            mPosts.add(post);
        }


//        String selection = SiftContract.Posts.COLUMN_SUBREDDIT_ID + " = ?";
//        String[] selectionArgs = new String[]{String.valueOf(mSubredditId)};
//        Cursor cursor = getContext().getContentResolver().query(SiftContract.Posts.CONTENT_URI, null, selection, selectionArgs, null);
//        if (cursor != null) {
//            while (cursor.moveToNext()) {
//                PostInfo post = new PostInfo();
//                post.mId = cursor.getInt(cursor.getColumnIndex(SiftContract.Posts._ID));
//                post.mTitle = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_TITLE));
//                post.mUsername = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_OWNER_USERNAME));
//                post.mUserId = cursor.getInt(cursor.getColumnIndex(SiftContract.Posts.COLUMN_OWNER_ID));
//                post.mSubreddit = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_SUBREDDIT_NAME));
//                post.mSubredditId = cursor.getInt(cursor.getColumnIndex(SiftContract.Posts.COLUMN_SUBREDDIT_ID));
//                post.mPoints = cursor.getInt(cursor.getColumnIndex(SiftContract.Posts.COLUMN_POINTS));
//                post.mImageUrl = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_IMAGE_URL));
//                post.mUrl = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_URL));
//                post.mComments = cursor.getInt(cursor.getColumnIndex(SiftContract.Posts.COLUMN_NUM_COMMENTS));
//                post.mAge= cursor.getInt(cursor.getColumnIndex(SiftContract.Posts.COLUMN_DATE_CREATED));
//                post.mFavorited = cursor.getInt(cursor.getColumnIndex(SiftContract.Posts.COLUMN_FAVORITED)) == 1 ? true : false;
//                mPosts.add(post);
//            }
//        }
    }

    private List<PostInfo> createPostList(int size) {
        List<PostInfo> result = new ArrayList<PostInfo>();
        for (int i=1; i <= size; i++) {
            PostInfo post = new PostInfo();
            post.mUsername = "Username" + i;
            post.mSubreddit = "Subreddit" + i;

            if(i%3 == 0) {
                post.mTitle = "This is an extended title to test larger cards in this layout. " +
                        "How far can I stretch this card before it breaks?";
            } else {
                post.mTitle = "Title" + i;
            }

            if(i%2 == 0) {
                post.mImageUrl = "http://www.melovemypet.com/wp-content/uploads/2014/05/Bluenosepitbullpuppy1.jpg";
            } else if (i%5 == 0) {
                post.mImageUrl = "https://s-media-cache-ak0.pinimg.com/236x/1e/49/8f/1e498f4bb5069b00d92f977dc8187f55.jpg";
            } else {
                post.mImageUrl = null;
            }

            post.mPoints = 12;
            post.mComments = 5;
            post.mUrl = "Url" + i;
            post.mAge = 4;
            result.add(post);
        }
        return result;
    }

}
