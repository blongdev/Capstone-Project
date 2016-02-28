package com.blongdev.sift;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Brian on 2/24/2016.
 */
public class PostListAdapter extends RecyclerView.Adapter<PostListAdapter.PostViewHolder> {

    private List<PostInfo> mPostList;

    public PostListAdapter(List<PostInfo> postList) {
        this.mPostList = postList;
    }

    @Override
    public int getItemCount() {
        return mPostList.size();
    }

    @Override
    public void onBindViewHolder(PostViewHolder postViewHolder, int i) {
        PostInfo post = mPostList.get(i);
        postViewHolder.mUsername.setText(post.mUsername);
        postViewHolder.mSubreddit.setText(post.mSubreddit);
        postViewHolder.mTitle.setText(post.mTitle);
        postViewHolder.mPoints.setText(post.mPoints);
        postViewHolder.mComments.setText(post.mComments);
        postViewHolder.mUrl.setText(post.mUrl);
        postViewHolder.mAge.setText(post.mAge);
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.post, viewGroup, false);

        return new PostViewHolder(itemView);
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder  {
        protected TextView mUsername;
        protected TextView mSubreddit;
        protected TextView mTitle;
        protected TextView mPoints;
        protected TextView mComments;
        protected TextView mUrl;
        protected TextView mAge;

        public PostViewHolder(View v) {
            super(v);
            mUsername =  (TextView) v.findViewById(R.id.post_username);
            mSubreddit = (TextView)  v.findViewById(R.id.post_subreddit);
            mTitle = (TextView)  v.findViewById(R.id.post_title);
            mPoints = (TextView) v.findViewById(R.id.post_points);
            mComments = (TextView)  v.findViewById(R.id.post_comments);
            mUrl = (TextView)  v.findViewById(R.id.post_url);
            mAge = (TextView) v.findViewById(R.id.post_age);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), PostDetailActivity.class);
                    intent.putExtra("info" , "post");
                    v.getContext().startActivity(intent);
                }
            });

            mUsername.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), PostDetailActivity.class);
                    intent.putExtra("info" , "username");
                    v.getContext().startActivity(intent);
                }
            });
        }
    }


}
