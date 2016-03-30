package com.blongdev.sift;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

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



//    private void createTree() {
//        mRoot = TreeNode.root();
//
//        for (int i = 0; i < 15; i++) {
//            TreeNode parent = createCommentNode("Comment " + i);
//            for (int j = 0; j < 3; j++) {
//                TreeNode child = createCommentNode("Reply " + i + " " + j);
//                for (int k = 0; k < 3; k++) {
//                    TreeNode child2 = createCommentNode("Reply " + i + " " + j + " " + k);
//                    child.addChild(child2);
//                }
//                parent.addChild(child);
//            }
//            mRoot.addChild(parent);
//        }


//        TreeNode parent = createCommentNode("Comment 1");
//        TreeNode parent2 = createCommentNode("Comment 2");
//        TreeNode child0 = createCommentNode("Comment 3");
//        TreeNode child1 = createCommentNode("Comment 4");
//        TreeNode child2 = createCommentNode("Comment 5");
//        TreeNode child3 = createCommentNode("Comment 6");
//        parent.addChildren(child0, child1);
//        child1.addChildren(child2, child3);
//        mRoot.addChild(parent);
//        mRoot.addChild(parent2);
//    }

    public class CommentViewHolder extends TreeNode.BaseNodeViewHolder<CommentInfo> {

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

            TextView body = (TextView) view.findViewById(R.id.comment_body);
            TextView username = (TextView) view.findViewById(R.id.comment_username);
            TextView points = (TextView) view.findViewById(R.id.comment_points);

            body.setText(value.mBody);
            username.setText(value.mUsername);
            points.setText(String.valueOf(value.mPoints));

            return view;
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
                TreeNode child = createCommentNode(commentInfo);
                parent.addChild(child);
                copyTree(child, commentNode);
            }
        }

    }

}
