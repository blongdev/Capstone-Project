package com.blongdev.sift;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
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


public class CommentsFragment extends Fragment implements LoaderManager.LoaderCallbacks<CommentNode> {

    TreeNode mRoot;
    FrameLayout mCommentsContainer;
    String mPostServerId;
    Activity mActivity;
    TextView mNoComments;
    ProgressBar mLoadingSpinner;
    AndroidTreeView mTreeView;
    View mTree;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!TextUtils.isEmpty(mPostServerId)) {
//                new getCommentsTask().execute();
                getActivity().getSupportLoaderManager().initLoader(1, null, CommentsFragment.this).forceLoad();
            }
        }
    };

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

        mCommentsContainer = (FrameLayout) rootView.findViewById(R.id.comments_container);

        Bundle args = getArguments();
        if (args != null) {
            mPostServerId = args.getString(getString(R.string.server_id));
        }

        if (!TextUtils.isEmpty(mPostServerId)) {
//            new getCommentsTask().execute();
            getActivity().getSupportLoaderManager().initLoader(1, null, this).forceLoad();
        }


        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mBroadcastReceiver,
                new IntentFilter(Reddit.AUTHENTICATED));
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    public void replyToPost(){
        if(mTreeView != null) {
            addReplyNode(getContext(), mRoot);
        }
    }

    private void addReplyNode(Context context, TreeNode node) {
        String username = Utilities.getLoggedInUsername(context);
        CommentInfo commentInfo = new CommentInfo();
        commentInfo.mUsername = username;
        commentInfo.mPoints = 1;
        TreeNode child = createReplyNode(commentInfo);
        mTreeView.addNode(node, child);
        mTreeView.expandNode(node);
//        int bottom = child.getViewHolder().getView().getBottom();
//        mTree.scrollTo(0, bottom);
    }
//
//    private void addNodeToFront(TreeNode node, TreeNode child) {
//        List<TreeNode> children = node.getChildren();
//        for (TreeNode t : children) {
//            node.deleteChild(t);
//        }
//
//        mTreeView.addNode(node, child);
//
//        for (TreeNode t : children) {
//            node.addChildren(children);
//        }
//    }

    private TreeNode createCommentNode(CommentInfo comment) {
        return new TreeNode(comment).setViewHolder(new CommentViewHolder(getContext(), false));
    }

    private TreeNode createReplyNode(CommentInfo comment) {
        return new TreeNode(comment).setViewHolder(new CommentViewHolder(getContext(), true));
    }

    public class CommentViewHolder extends TreeNode.BaseNodeViewHolder<CommentInfo> {

        ImageView mUpvote;
        ImageView mDownvote;
        TextView mPoints;
        TextView mBody;
        Comment mComment;
        int mVote;
        ImageView mReply;
        EditText mReplyText;
        ImageView mSendComment;
        LinearLayout mCommentArea;
        boolean mIsReply;

        public CommentViewHolder(Context context, boolean isReply) {
            super(context);
            mIsReply = isReply;
        }

        @Override
        public View createNodeView(final TreeNode node, CommentInfo value) {
            final LayoutInflater inflater = LayoutInflater.from(context);
            final View view = inflater.inflate(R.layout.comment, null, false);

//            final float dpScale = getResources().getDisplayMetrics().density;
//            int padding_left = (int) (15 * dpScale + 0.5f) * node.getLevel();
//            int padding = (int) (6 * dpScale + 0.5f);

            int padding_left = (int) getResources().getDimension(R.dimen.comment_indent) * (node.getLevel()-1);
            int padding = (int) getResources().getDimension(R.dimen.comment_padding);


            LinearLayout commentView = (LinearLayout) view.findViewById(R.id.comment_view);
            commentView.setPadding(padding_left, 0, padding, 0);

            if (!mIsReply) {
                mComment = value.mJrawComment;
                mVote = mComment.getVote().getValue();
            } else {
                mVote = SiftContract.Posts.UPVOTE;
                TreeNode parent = node.getParent();
                if (parent != null && !parent.isRoot()) {
                    CommentInfo ci = (CommentInfo) parent.getValue();
                    mComment = ci.mJrawComment;
                }
            }

            mBody = (TextView) view.findViewById(R.id.comment_body);
            TextView username = (TextView) view.findViewById(R.id.comment_username);
            mPoints = (TextView) view.findViewById(R.id.comment_points);
            mUpvote = (ImageView) view.findViewById(R.id.upvote);
            mDownvote = (ImageView) view.findViewById(R.id.downvote);
            mReply = (ImageView) view.findViewById(R.id.reply_to_comment);
            mReplyText = (EditText) view.findViewById(R.id.reply_text);
            mCommentArea = (LinearLayout) view.findViewById(R.id.comment_area);
            mSendComment = (ImageView) view.findViewById(R.id.send);

            if (mIsReply) {
                mCommentArea.setVisibility(View.VISIBLE);
                mReplyText.requestFocus();
                mBody.setVisibility(View.GONE);
                mReply.setVisibility(View.GONE);
            }

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

                    addReplyNode(context, node);
                }
            });

            mSendComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(mReplyText.getText().toString())) {
                        if (mComment != null) {
                            Reddit.replyToComment(view.getContext(), mComment, mReplyText.getText().toString());
                        } else {
                            Reddit.commentOnPost(view.getContext(), mPostServerId, mReplyText.getText().toString());
                        }
                        mBody.setText(mReplyText.getText().toString());
                        mBody.setVisibility(View.VISIBLE);
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
            mBody.setText(value.mBody);
            username.setText(value.mUsername);
            mPoints.setText(String.valueOf(value.mPoints));

            Linkify.addLinks(mBody, Linkify.ALL);

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

    private final class CopyTreeTask extends AsyncTask<String, Void, Void> {

        CommentNode root;

        CopyTreeTask(Context context, CommentNode node) {
            root = node;
        }

        @Override
        protected void onPreExecute() {
            mLoadingSpinner.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... params) {
            long startTime = System.currentTimeMillis();
            mRoot = TreeNode.root();
            copyTree(mRoot, root);

            long endTime = System.currentTimeMillis();
            Log.v("CommentsFragment", "Comment download completed. Total time: " + (endTime - startTime) / 1000 + " seconds");
            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {

            //if the user leaves the activity before comments load, return to prevent a crash
            if (getContext() == null || mRoot == null) {
                return;
            }

            if (mRoot.size() == 0) {
                mNoComments.setVisibility(View.VISIBLE);
            }

            mLoadingSpinner.setVisibility(View.GONE);

            mTreeView = new AndroidTreeView(mActivity, mRoot);
            mTreeView.setDefaultContainerStyle(R.style.CommentStyle);
            mTree = mTreeView.getView();
            mCommentsContainer.addView(mTree);
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

    @Override
    public Loader<CommentNode> onCreateLoader(int id, Bundle args) {
        return new CommentLoader(getContext(), mPostServerId);
    }
    @Override
    public void onLoadFinished(Loader<CommentNode> loader, CommentNode root) {
        new CopyTreeTask(getContext(), root).execute();
    }
    @Override
    public void onLoaderReset(Loader<CommentNode> loader) {

    }

}

class CommentLoader extends AsyncTaskLoader<CommentNode> {

    String mPostServerId;

    public CommentLoader(Context context, String postServerId) {
        super(context);
        mPostServerId = postServerId;
    }
    @Override
    public CommentNode loadInBackground() {
        Reddit reddit = Reddit.getInstance();

        if (!reddit.mRedditClient.isAuthenticated()) {
            return null;
        }

        Submission post = reddit.mRedditClient.getSubmission(mPostServerId);
        return post.getComments();
    }
}
