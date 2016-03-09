package com.blongdev.sift;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

        Intent intent = getActivity().getIntent();
        String title = intent.getStringExtra(getString(R.string.title));
        String username = intent.getStringExtra(getString(R.string.username));
        String subreddit = intent.getStringExtra(getString(R.string.subreddit));
        String points = intent.getStringExtra(getString(R.string.points));
        String comments = intent.getStringExtra(getString(R.string.comments));
        String url = intent.getStringExtra(getString(R.string.url));
        String age = intent.getStringExtra(getString(R.string.age));

        View cardView = rootView.findViewById(R.id.card_view);
        mTitle = (TextView) cardView.findViewById(R.id.post_title);
        mUsername = (TextView) cardView.findViewById(R.id.post_username);
        mSubreddit = (TextView) cardView.findViewById(R.id.post_subreddit);
        mPoints = (TextView) cardView.findViewById(R.id.post_points);
        mComments = (TextView) cardView.findViewById(R.id.post_comments);
        mUrl = (TextView) cardView.findViewById(R.id.post_url);
        mAge = (TextView) cardView.findViewById(R.id.post_age);

        mTitle.setText(title);
        mUsername.setText(username);
        mSubreddit.setText(subreddit);
        mPoints.setText(points);
        mComments.setText(comments);
        mUrl.setText(url);
        mAge.setText(age);

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
