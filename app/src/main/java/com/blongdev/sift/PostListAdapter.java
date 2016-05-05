package com.blongdev.sift;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blongdev.sift.database.SiftContract;
import com.squareup.okhttp.internal.Util;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import net.dean.jraw.ApiException;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Comment;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Brian on 2/24/2016.
 */
public class PostListAdapter extends RecyclerView.Adapter<PostListAdapter.PostViewHolder> {

    private List<ContributionInfo> mPostList;

    public PostListAdapter(List<ContributionInfo> postList) {
        this.mPostList = postList;
    }

    public void refreshWithList(List<ContributionInfo> postList) {
        this.mPostList = postList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mPostList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mPostList.get(position) instanceof CommentInfo) {
            return ContributionInfo.CONTRIBUTION_COMMENT;
        } else  {
            return ContributionInfo.CONTRIBUTION_POST;
        }
    }

    @Override
    public void onBindViewHolder(final PostViewHolder postViewHolder, int i) {
        int type = mPostList.get(i).mContributionType;

        if (type == ContributionInfo.CONTRIBUTION_COMMENT) {
            CommentInfo comment = (CommentInfo) mPostList.get(i);
            postViewHolder.mUsername.setText(comment.mUsername);
            postViewHolder.mPoints.setText(String.valueOf(comment.mPoints));
//            postViewHolder.mComments.setText(comment.mComments + " Replies");
            postViewHolder.mAge.setText(Utilities.getAgeString(comment.mAge));
            postViewHolder.mPostId = comment.mPost;
            postViewHolder.mServerId = comment.mServerId;
            postViewHolder.mPostServerId = comment.mPostServerId;
            postViewHolder.mTitle.setText(comment.mBody);
            postViewHolder.mContributionType = comment.mContributionType;
            postViewHolder.mVote = comment.mVote;
            postViewHolder.mJrawComment = comment.mJrawComment;
        } else {
            //TODO just have postViewHolder with a reference to a PostInfo object rather than copying all of its fields
            PostInfo post = (PostInfo) mPostList.get(i);
            postViewHolder.mUsername.setText(post.mUsername);
            postViewHolder.mSubreddit.setText(post.mSubreddit);
            postViewHolder.mTitle.setText(post.mTitle);
            postViewHolder.mPoints.setText(String.valueOf(post.mPoints));
            postViewHolder.mComments.setText(post.mComments + " Comments");
            postViewHolder.mDomain.setText(post.mDomain);
            postViewHolder.mAge.setText(Utilities.getAgeString(post.mAge));
            postViewHolder.mImageUrl = post.mImageUrl;
            postViewHolder.mPostId = post.mId;
            postViewHolder.mServerId = post.mServerId;
            postViewHolder.mUrl = post.mUrl;
            postViewHolder.mBody = post.mBody;
            postViewHolder.mContributionType = post.mContributionType;
            postViewHolder.mVote = post.mVote;

            if (post.mVote == SiftContract.Posts.UPVOTE) {
                postViewHolder.mUpvote.setImageDrawable(SiftApplication.getContext().getResources().getDrawable(R.drawable.ic_up_arrow_filled_24dp));
                postViewHolder.mDownvote.setImageDrawable(SiftApplication.getContext().getResources().getDrawable(R.drawable.ic_down_arrow_white_24dp));
                postViewHolder.mPoints.setTextColor(SiftApplication.getContext().getResources().getColor(R.color.upvote));
            } else if (post.mVote == SiftContract.Posts.DOWNVOTE) {
                postViewHolder.mDownvote.setImageDrawable(SiftApplication.getContext().getResources().getDrawable(R.drawable.ic_down_arrow_filled_24dp));
                postViewHolder.mUpvote.setImageDrawable(SiftApplication.getContext().getResources().getDrawable(R.drawable.ic_up_arrow_white_24dp));
                postViewHolder.mPoints.setTextColor(SiftApplication.getContext().getResources().getColor(R.color.downvote));
            } else {
                postViewHolder.mUpvote.setImageDrawable(SiftApplication.getContext().getResources().getDrawable(R.drawable.ic_up_arrow_white_24dp));
                postViewHolder.mDownvote.setImageDrawable(SiftApplication.getContext().getResources().getDrawable(R.drawable.ic_down_arrow_white_24dp));
                postViewHolder.mPoints.setTextColor(Color.WHITE);
            }

            //picasso needs to be passed null to prevent listview from displaying incorrectly cached images
            if(!TextUtils.isEmpty(post.mImageUrl)) {

                Picasso.with(postViewHolder.mImage.getContext())
                        .load(post.mImageUrl)
                        .placeholder(R.drawable.ic_photo_48dp)
                                //.error(R.drawable.drawer_icon)
                        .into(postViewHolder.mTarget);

//
//                            postViewHolder.mImage, new com.squareup.picasso.Callback() {
//                        @Override
//                        public void onSuccess() {
//                            postViewHolder.mImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
//                        }
//
//                        @Override
//                        public void onError() {
//
//                        }
//                    });
            } else {
                Picasso.with(postViewHolder.mImage.getContext())
                        .load(post.mImageUrl)
                        .into(postViewHolder.mImage);
            }
        }

    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        View itemView;
        if (type == ContributionInfo.CONTRIBUTION_COMMENT) {
            itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.comment_card, viewGroup, false);
        } else {
            itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.post, viewGroup, false);
        }


        return new PostViewHolder(itemView, type);
    }

    //TODO Separate into 2 viewholders, post and comment
    public static class PostViewHolder extends RecyclerView.ViewHolder  {
        protected TextView mUsername;
        protected TextView mSubreddit;
        protected TextView mTitle;
        protected TextView mPoints;
        protected TextView mComments;
        protected TextView mDomain;
        protected TextView mAge;
        protected ImageView mImage;
        protected Target mTarget;
        protected ImageView mUpvote;
        protected ImageView mDownvote;

        protected String mImageUrl;
        protected int mPostId;
        protected String mServerId;
        protected String mBody;
        protected String mUrl;
        protected int mContributionType;
        protected int mVote;
        protected Comment mJrawComment;
        protected String mPostServerId;

        public PostViewHolder(View v, int contributionType) {
            super(v);

            if (contributionType == ContributionInfo.CONTRIBUTION_COMMENT) {
                mUsername =  (TextView) v.findViewById(R.id.comment_username);
                mTitle = (TextView)  v.findViewById(R.id.comment_body);
                mPoints = (TextView) v.findViewById(R.id.comment_points);
//                mComments = (TextView)  v.findViewById(R.id.comment_replies);
                mAge = (TextView) v.findViewById(R.id.comment_age);
                mUpvote = (ImageView) v.findViewById(R.id.upvote);
                mDownvote = (ImageView) v.findViewById(R.id.downvote);

                mUpvote.setOnClickListener(mOnClickListener);
                mDownvote.setOnClickListener(mOnClickListener);
                mTitle.setOnClickListener(mOnClickListener);
                mUsername.setOnClickListener(mOnClickListener);
            } else {
                mUsername =  (TextView) v.findViewById(R.id.post_username);
                mSubreddit = (TextView) v.findViewById(R.id.post_subreddit);
                mTitle = (TextView) v.findViewById(R.id.post_title);
                mPoints = (TextView) v.findViewById(R.id.post_points);
                mComments = (TextView)  v.findViewById(R.id.post_comments);
                mDomain = (TextView)  v.findViewById(R.id.post_url);
                mAge = (TextView) v.findViewById(R.id.post_age);
                mImage = (ImageView) v.findViewById(R.id.post_image);
                mUpvote = (ImageView) v.findViewById(R.id.upvote);
                mDownvote = (ImageView) v.findViewById(R.id.downvote);

                mUpvote.setOnClickListener(mOnClickListener);
                mDownvote.setOnClickListener(mOnClickListener);

                mTitle.setOnClickListener(mOnClickListener);
                mUsername.setOnClickListener(mOnClickListener);
                mImage.setOnClickListener(mOnClickListener);

                mTarget = new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        //Drawable drawable = new BitmapDrawable(postViewHolder.mImage.getContext().getResources(), bitmap);
                        mImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        mImage.setImageBitmap(bitmap);
                        //postViewHolder.mImage.invalidate();
                        //postViewHolder.mImage.setImageDrawable(drawable);
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        mImage.setScaleType(ImageView.ScaleType.CENTER);
                        mImage.setImageDrawable(placeHolderDrawable);
                    }
                };
            }

        }

        //TODO create an interface to handle all clicks with abstract methods
        private View.OnClickListener mOnClickListener = (new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == mTitle || v == mImage) {
                    goToPostDetail(v);
                } else if (v == mUsername) {
                    goToUserInfo(v);
                } else if (v == mUpvote) {
                    upvote(v.getContext());
                } else if(v == mDownvote) {
                    downvote(v.getContext());
                }
//                  else if (v == mImage) {
//                    FragmentManager fm = ((AppCompatActivity) v.getContext()).getSupportFragmentManager();
//                    ImageDialogFragment imageFragment = new ImageDialogFragment();
//                    Bundle args = new Bundle();
//                    args.putString(v.getContext().getString(R.string.image_url), mImageUrl);
//                    imageFragment.setArguments(args);
//                    imageFragment.show(fm, "ImageDialogFragment");
//                }
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

                if (mContributionType == ContributionInfo.CONTRIBUTION_POST) {
                    Reddit.votePost(context, mServerId, mVote);
                } else if(mContributionType == ContributionInfo.CONTRIBUTION_COMMENT) {
                    Reddit.voteComment(context, mJrawComment, mVote);
                }
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

                if (mContributionType == ContributionInfo.CONTRIBUTION_POST) {
                    Reddit.votePost(context, mServerId, mVote);
                } else if(mContributionType == ContributionInfo.CONTRIBUTION_COMMENT) {
                    Reddit.voteComment(context, mJrawComment, mVote);
                }
            }

            private void goToPostDetail(View v) {
                if (mContributionType == ContributionInfo.CONTRIBUTION_COMMENT) {
                    new GetPostTask(v.getContext(), mPostServerId).execute();
                } else {
                    String title = mTitle.getText().toString();
                    String username = mUsername.getText().toString();
                    String subreddit = mSubreddit.getText().toString();
                    String points = mPoints.getText().toString();
                    String comments = mComments.getText().toString();
                    String age = mAge.getText().toString();
                    String domain = mDomain.getText().toString();

                    Intent intent = new Intent(v.getContext(), PostDetailActivity.class);
                    intent.putExtra(v.getContext().getString(R.string.title), title);
                    intent.putExtra(v.getContext().getString(R.string.username), username);
                    intent.putExtra(v.getContext().getString(R.string.subreddit), subreddit);
                    intent.putExtra(v.getContext().getString(R.string.points), points);
                    intent.putExtra(v.getContext().getString(R.string.comments), comments);
                    intent.putExtra(v.getContext().getString(R.string.url), mUrl);
                    intent.putExtra(v.getContext().getString(R.string.age), age);
                    intent.putExtra(v.getContext().getString(R.string.image_url), mImageUrl);
                    intent.putExtra(v.getContext().getString(R.string.post_id), mPostId);
                    intent.putExtra(v.getContext().getString(R.string.server_id), mServerId);
                    intent.putExtra(v.getContext().getString(R.string.body), mBody);
                    intent.putExtra(v.getContext().getString(R.string.domain), domain);
                    intent.putExtra(v.getContext().getString(R.string.vote), mVote);

                    v.getContext().startActivity(intent);
                }
            }

            private void goToUserInfo(View v) {
                String username = mUsername.getText().toString();

                Intent intent = new Intent(v.getContext(), UserInfoActivity.class);
                intent.putExtra(v.getContext().getString(R.string.username), username);

                v.getContext().startActivity(intent);
            }
        });

        private final class GetPostTask extends AsyncTask<String, Void, Submission> {
            Context mContext;
            String mSubmissionServerId;

            public GetPostTask(Context context, String submissionServerId) {
                mContext = context;
                mSubmissionServerId = submissionServerId;
            }

            @Override
            protected Submission doInBackground(String... params) {
                try {
                    Reddit reddit = Reddit.getInstance();
                    return reddit.mRedditClient.getSubmission(mSubmissionServerId);
                } catch (NetworkException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPreExecute() {

            }

            @Override
            protected void onPostExecute(Submission sub) {
                if (sub != null) {
                    Intent intent = new Intent(mContext, PostDetailActivity.class);
                    intent.putExtra(mContext.getString(R.string.title), sub.getTitle());
                    intent.putExtra(mContext.getString(R.string.username), sub.getAuthor());
                    intent.putExtra(mContext.getString(R.string.subreddit), sub.getSubredditName());
                    intent.putExtra(mContext.getString(R.string.points), sub.getScore());
                    intent.putExtra(mContext.getString(R.string.comments), sub.getCommentCount());
                    intent.putExtra(mContext.getString(R.string.url), sub.getUrl());
                    intent.putExtra(mContext.getString(R.string.age), Utilities.getAgeString(sub.getCreatedUtc().getTime()));
                    intent.putExtra(mContext.getString(R.string.post_id), mPostId);
                    intent.putExtra(mContext.getString(R.string.server_id), mPostServerId);
                    intent.putExtra(mContext.getString(R.string.body), sub.getSelftext());
                    intent.putExtra(mContext.getString(R.string.domain), sub.getDomain());
                    intent.putExtra(mContext.getString(R.string.vote), sub.getVote());

                    mContext.startActivity(intent);
                }
            }

        }
    }

}
