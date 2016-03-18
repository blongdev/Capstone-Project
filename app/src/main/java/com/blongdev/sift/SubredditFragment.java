package com.blongdev.sift;

import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blongdev.sift.database.SiftContract;
import com.squareup.picasso.Picasso;

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

    public SubredditFragment() {
    }

    public SubredditFragment(int id) {
        mSubredditId = id;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        Bundle arg = getArguments();
        mSubredditId = arg.getInt(getString(R.string.subreddit_id));

        mPosts = new ArrayList<PostInfo>();

        populatePosts();

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.cardList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        PostListAdapter ca = new PostListAdapter(mPosts);
        recyclerView.setAdapter(ca);

        return rootView;
    }

    private void populatePosts() {
        String selection = SiftContract.Posts.COLUMN_SUBREDDIT_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(mSubredditId)};
        Cursor cursor = getContext().getContentResolver().query(SiftContract.Posts.CONTENT_URI, null, selection, selectionArgs, null);
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
