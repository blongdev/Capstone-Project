package com.blongdev.sift;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.blongdev.sift.database.SiftContract;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class PostDetailFragment extends Fragment {

    TextView mTitle;
    TextView mUsername;
    TextView mSubreddit;
    TextView mPoints;
    TextView mComments;
    TextView mUrl;
    TextView mAge;
    ImageView mImage;

    private int mPostId = 0;

    public PostDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_post_detail, container, false);

//        Intent intent = getActivity().getIntent();
//        String title = intent.getStringExtra(getString(R.string.title));
//        String username = intent.getStringExtra(getString(R.string.username));
//        String subreddit = intent.getStringExtra(getString(R.string.subreddit));
//        String points = intent.getStringExtra(getString(R.string.points));
//        String comments = intent.getStringExtra(getString(R.string.comments));
//        String url = intent.getStringExtra(getString(R.string.url));
//        String age = intent.getStringExtra(getString(R.string.age));
//        String imageUrl = intent.getStringExtra(getString(R.string.image_url));
//
        mTitle = (TextView) rootView.findViewById(R.id.post_title);
        mUsername = (TextView) rootView.findViewById(R.id.post_username);
        mSubreddit = (TextView) rootView.findViewById(R.id.post_subreddit);
        mPoints = (TextView) rootView.findViewById(R.id.post_points);
        mComments = (TextView) rootView.findViewById(R.id.post_comments);
        mUrl = (TextView) rootView.findViewById(R.id.post_url);
        mAge = (TextView) rootView.findViewById(R.id.post_age);
        mImage = (ImageView) rootView.findViewById(R.id.post_detail_image);

        Bundle args = getArguments();
        if (args != null) {
            mPostId = args.getInt(getString(R.string.post_id));
        }

        String selection = SiftContract.Posts._ID + " = ?";
        String[] selectionArgs =  new String[]{String.valueOf(mPostId)};
        Cursor cursor = getContext().getContentResolver().query(SiftContract.Posts.CONTENT_URI, null, selection, selectionArgs, null);
        if(cursor != null && cursor.moveToFirst()) {

            String title = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_TITLE));
            String username = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_OWNER_USERNAME));
            String subreddit = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_SUBREDDIT_NAME));
            String points = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_POINTS));
            String comments = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_NUM_COMMENTS));
            String url = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_URL));
            String age = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_DATE_CREATED));
            String imageUrl = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_IMAGE_URL));

            mTitle.setText(title);
            mUsername.setText(username);
            mSubreddit.setText(subreddit);
            mPoints.setText(points);
            mComments.setText(comments);
            mUrl.setText(url);
            mAge.setText(age);

            Picasso.with(getContext()).load(imageUrl).into(mImage);
        }

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
