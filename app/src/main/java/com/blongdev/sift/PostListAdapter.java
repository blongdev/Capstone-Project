package com.blongdev.sift;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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
        postViewHolder.mImageUrl = post.mImageUrl;

        //picasso needs to be passed null to prevent listview from displaying incorrectly cached images
        //if(!TextUtils.isEmpty(post.mImageUrl)) {
            Picasso.with(postViewHolder.mImage.getContext())
                    .load(post.mImageUrl)
                    //.placeholder(R.drawable.ic_photo_camera_24dp)
                    .error(R.drawable.drawer_icon)
                    .into(postViewHolder.mImage, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                        }
                    });
//            Picasso.Builder builder = new Picasso.Builder(postViewHolder.mImage.getContext());
//            builder.listener(new Picasso.Listener()
//            {
//                @Override
//                public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception)
//                {
//                    exception.printStackTrace();
//                }
//            });
//            builder.build().load("http://i.imgur.com/DvpvklR.png").into(postViewHolder.mImage);

        //}
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
        protected ImageView mImage;

        protected String mImageUrl;

        public PostViewHolder(View v) {
            super(v);
            mUsername =  (TextView) v.findViewById(R.id.post_username);
            mSubreddit = (TextView)  v.findViewById(R.id.post_subreddit);
            mTitle = (TextView)  v.findViewById(R.id.post_title);
            mPoints = (TextView) v.findViewById(R.id.post_points);
            mComments = (TextView)  v.findViewById(R.id.post_comments);
            mUrl = (TextView)  v.findViewById(R.id.post_url);
            mAge = (TextView) v.findViewById(R.id.post_age);
            mImage = (ImageView) v.findViewById(R.id.post_image);

            mTitle.setOnClickListener(mOnClickListener);
            mUsername.setOnClickListener(mOnClickListener);
            mImage.setOnClickListener(mOnClickListener);
        }

        //TODO create an interface to handle all clicks with abstract methods
        private View.OnClickListener mOnClickListener = (new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == mTitle) {
                    goToPostDetail(v);
                } else if (v == mUsername) {
                    goToUserInfo(v);
                } else if (v == mImage) {
                    FragmentManager fm = ((AppCompatActivity) v.getContext()).getSupportFragmentManager();
                    ImageDialogFragment imageFragment = new ImageDialogFragment();
                    Bundle args = new Bundle();
                    args.putString(v.getContext().getString(R.string.image_url), mImageUrl);
                    imageFragment.setArguments(args);
                    imageFragment.show(fm, "ImageDialogFragment");
                }
            }

            private void goToPostDetail(View v) {
                String title = mTitle.getText().toString();
                String username = mUsername.getText().toString();
                String subreddit = mSubreddit.getText().toString();
                String points = mPoints.getText().toString();
                String comments = mComments.getText().toString();
                String url = mUrl.getText().toString();
                String age = mAge.getText().toString();

                Intent intent = new Intent(v.getContext(), PostDetailActivity.class);
                intent.putExtra(v.getContext().getString(R.string.title), title);
                intent.putExtra(v.getContext().getString(R.string.username), username);
                intent.putExtra(v.getContext().getString(R.string.subreddit), subreddit);
                intent.putExtra(v.getContext().getString(R.string.points), points);
                intent.putExtra(v.getContext().getString(R.string.comments), comments);
                intent.putExtra(v.getContext().getString(R.string.url), url);
                intent.putExtra(v.getContext().getString(R.string.age), age);
                intent.putExtra(v.getContext().getString(R.string.image_url), mImageUrl);

                v.getContext().startActivity(intent);
            }

            private void goToUserInfo(View v) {
                String username = mUsername.getText().toString();

                Intent intent = new Intent(v.getContext(), UserInfoActivity.class);
                intent.putExtra(v.getContext().getString(R.string.username), username);

                v.getContext().startActivity(intent);
            }
        });
    }

}
