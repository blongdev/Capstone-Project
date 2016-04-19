package com.blongdev.sift;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blongdev.sift.database.SiftContract;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.Submission;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class CommentsFragment extends Fragment {

    TreeNode mRoot;
    FrameLayout mCommentsContainer;
    String mPostServerId;
    Activity mActivity;
    TextView mNoComments;
    ProgressBar mLoadingSpinner;

    public CommentsFragment() {
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
        View rootView =  inflater.inflate(R.layout.fragment_comments, container, false);

        mActivity = getActivity();

        mNoComments = (TextView) rootView.findViewById(R.id.no_comments);
        mLoadingSpinner = (ProgressBar) rootView.findViewById(R.id.progressSpinner);

        //createTree();

        mCommentsContainer = (FrameLayout) rootView.findViewById(R.id.comments_container);
//        AndroidTreeView tView = new AndroidTreeView(getActivity(), mRoot);
//        tView.setDefaultContainerStyle(R.style.CommentStyle);
//        //tView.setDefaultViewHolder(CommentViewHolder.class);
//        mCommentsContainer.addView(tView.getView());

        Bundle args = getArguments();
        if (args != null) {
            mPostServerId = args.getString(getString(R.string.server_id));
        }

        if (!TextUtils.isEmpty(mPostServerId)) {
            new getCommentsTask().execute();
        }

        return rootView;
    }

    private TreeNode createCommentNode(CommentInfo comment) {
        return new TreeNode(comment).setViewHolder(new CommentViewHolder(getContext()));
    }


    public class CommentViewHolder extends TreeNode.BaseNodeViewHolder<CommentInfo> {

        ImageView mUpvote;
        ImageView mDownvote;
        TextView mPoints;
        Comment mComment;
        int mVote;
        ImageView mReply;
        EditText mReplyText;
        ImageView mSendComment;
        LinearLayout mCommentArea;

        public CommentViewHolder(Context context) {
            super(context);
        }

        @Override
        public View createNodeView(TreeNode node, CommentInfo value) {
            final LayoutInflater inflater = LayoutInflater.from(context);
            final View view = inflater.inflate(R.layout.comment, null, false);

//            final float dpScale = getResources().getDisplayMetrics().density;
//            int padding_left = (int) (15 * dpScale + 0.5f) * node.getLevel();
//            int padding = (int) (6 * dpScale + 0.5f);

            int padding_left = (int) getResources().getDimension(R.dimen.comment_indent) * (node.getLevel()-1);
            int padding = (int) getResources().getDimension(R.dimen.comment_padding);


            LinearLayout commentView = (LinearLayout) view.findViewById(R.id.comment_view);
            commentView.setPadding(padding_left, 0, padding, 0);

            mComment = value.mJrawComment;
            mVote = mComment.getVote().getValue();

            TextView body = (TextView) view.findViewById(R.id.comment_body);
            TextView username = (TextView) view.findViewById(R.id.comment_username);
            mPoints = (TextView) view.findViewById(R.id.comment_points);
            mUpvote = (ImageView) view.findViewById(R.id.upvote);
            mDownvote = (ImageView) view.findViewById(R.id.downvote);
            mReply = (ImageView) view.findViewById(R.id.reply_to_comment);
            mReplyText = (EditText) view.findViewById(R.id.reply_text);
            mCommentArea = (LinearLayout) view.findViewById(R.id.comment_area);
            mSendComment = (ImageView) view.findViewById(R.id.send);

            mUpvote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    upvote(v.getContext());
                }
            });

            mDownvote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    downvote(v.getContext());
                }
            });

            mReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!Utilities.loggedIn(v.getContext())) {
                        Toast.makeText(v.getContext(), getString(R.string.must_log_in), Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (mCommentArea.getVisibility() == View.GONE) {
                        mCommentArea.setVisibility(View.VISIBLE);
                        mReplyText.requestFocus();
                    } else {
                        mCommentArea.setVisibility(View.GONE);
                        mReplyText.clearFocus();

                        //hide keyboard
                        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(mReplyText.getWindowToken(), 0);
                    }
                }
            });

            mSendComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(mReplyText.getText().toString())) {
                        Reddit.replyToComment(view.getContext(), mComment, mReplyText.getText().toString());
                        mReplyText.setText(null);
                        mCommentArea.setVisibility(View.GONE);
                        mReplyText.clearFocus();

                        //hide keyboard
                        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(mReplyText.getWindowToken(), 0);
                    }
                }
            });

            if (mVote == SiftContract.Posts.UPVOTE) {
                mUpvote.setImageDrawable(SiftApplication.getContext().getResources().getDrawable(R.drawable.ic_up_arrow_filled_24dp));
                mDownvote.setImageDrawable(SiftApplication.getContext().getResources().getDrawable(R.drawable.ic_down_arrow_24dp));
                mPoints.setTextColor(SiftApplication.getContext().getResources().getColor(R.color.upvote));
            } else if (mVote == SiftContract.Posts.DOWNVOTE) {
                mDownvote.setImageDrawable(SiftApplication.getContext().getResources().getDrawable(R.drawable.ic_down_arrow_filled_24dp));
                mUpvote.setImageDrawable(SiftApplication.getContext().getResources().getDrawable(R.drawable.ic_up_arrow_24dp));
                mPoints.setTextColor(SiftApplication.getContext().getResources().getColor(R.color.downvote));
            } else {
                mUpvote.setImageDrawable(SiftApplication.getContext().getResources().getDrawable(R.drawable.ic_up_arrow_24dp));
                mDownvote.setImageDrawable(SiftApplication.getContext().getResources().getDrawable(R.drawable.ic_down_arrow_24dp));
                mPoints.setTextColor(Color.BLACK);
            }

            body.setText(value.mBody);
            username.setText(value.mUsername);
            mPoints.setText(String.valueOf(value.mPoints));

            Linkify.addLinks(body, Linkify.ALL);

            return view;
        }

        private void upvote(Context context) {
            if (!Utilities.loggedIn(context)) {
                Toast.makeText(context, context.getString(R.string.must_log_in), Toast.LENGTH_LONG).show();
                return;
            }

            if (mVote == SiftContract.Posts.UPVOTE) {
                mVote = SiftContract.Posts.NO_VOTE;
                mUpvote.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_up_arrow_24dp));
                mDownvote.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_down_arrow_24dp));
                mPoints.setTextColor(Color.BLACK);
                mPoints.setText(String.valueOf(Integer.valueOf(mPoints.getText().toString()) - 1));
            } else if(mVote == SiftContract.Posts.DOWNVOTE) {
                mVote = SiftContract.Posts.UPVOTE;
                mUpvote.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_up_arrow_filled_24dp));
                mDownvote.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_down_arrow_24dp));
                mPoints.setTextColor(context.getResources().getColor(R.color.upvote));
                mPoints.setText(String.valueOf(Integer.valueOf(mPoints.getText().toString()) + 2));
            } else {
                mVote = SiftContract.Posts.UPVOTE;
                mUpvote.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_up_arrow_filled_24dp));
                mDownvote.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_down_arrow_24dp));
                mPoints.setTextColor(context.getResources().getColor(R.color.upvote));
                mPoints.setText(String.valueOf(Integer.valueOf(mPoints.getText().toString()) + 1));
            }

            Reddit.voteComment(context, mComment, mVote);
        }

        private void downvote(Context context) {
            if (!Utilities.loggedIn(context)) {
                Toast.makeText(context, context.getString(R.string.must_log_in), Toast.LENGTH_LONG).show();
                return;
            }

            if (mVote == SiftContract.Posts.DOWNVOTE) {
                mVote = SiftContract.Posts.NO_VOTE;
                mDownvote.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_down_arrow_24dp));
                mUpvote.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_up_arrow_24dp));
                mPoints.setTextColor(Color.BLACK);
                mPoints.setText(String.valueOf(Integer.valueOf(mPoints.getText().toString()) + 1));
            } else if (mVote == SiftContract.Posts.UPVOTE) {
                mVote = SiftContract.Posts.DOWNVOTE;
                mDownvote.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_down_arrow_filled_24dp));
                mUpvote.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_up_arrow_24dp));
                mPoints.setTextColor(context.getResources().getColor(R.color.downvote));
                mPoints.setText(String.valueOf(Integer.valueOf(mPoints.getText().toString()) - 2));
            } else {
                mVote = SiftContract.Posts.DOWNVOTE;
                mDownvote.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_down_arrow_filled_24dp));
                mUpvote.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_up_arrow_24dp));
                mPoints.setTextColor(context.getResources().getColor(R.color.downvote));
                mPoints.setText(String.valueOf(Integer.valueOf(mPoints.getText().toString()) - 1));
            }

            Reddit.voteComment(context, mComment, mVote);

        }

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private final class getCommentsTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            mLoadingSpinner.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... params) {
            long startTime = System.currentTimeMillis();
            Reddit reddit = Reddit.getInstance();
            Submission post = reddit.mRedditClient.getSubmission(mPostServerId);
            CommentNode root = post.getComments();

            mRoot = TreeNode.root();

            copyTree(mRoot, root);

            long endTime = System.currentTimeMillis();
            Log.v("CommentsFragment", "Comment download completed. Total time: " + (endTime - startTime) / 1000 + " seconds");
            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {

            //if the user leaves the activity before comments load, return to prevent a crash
            if (getContext() == null) {
                return;
            }

            if (mRoot.size() == 0) {
                mNoComments.setVisibility(View.VISIBLE);
            }

            mLoadingSpinner.setVisibility(View.GONE);

            AndroidTreeView tView = new AndroidTreeView(mActivity, mRoot);
            tView.setDefaultContainerStyle(R.style.CommentStyle);
            mCommentsContainer.addView(tView.getView());
        }

        public void copyTree(TreeNode parent, CommentNode commentParent) {
            for (CommentNode commentNode : commentParent.getChildren()) {
                Comment comment = commentNode.getComment();
                CommentInfo commentInfo = new CommentInfo();
                commentInfo.mUsername = comment.getAuthor();
                commentInfo.mBody = comment.getBody();
                commentInfo.mPoints = comment.getScore();
                commentInfo.mJrawComment = comment;
                TreeNode child = createCommentNode(commentInfo);
                parent.addChild(child);
                copyTree(child, commentNode);
            }
        }

    }
}
