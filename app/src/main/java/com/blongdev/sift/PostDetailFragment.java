package com.blongdev.sift;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blongdev.sift.database.SiftContract;
import com.squareup.okhttp.internal.Util;
import com.squareup.picasso.Picasso;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.Submission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class PostDetailFragment extends Fragment {

    LinearLayout mPostDetailLayout;
    TextView mTitle;
    TextView mUsername;
    TextView mSubreddit;
    TextView mPoints;
    TextView mComments;
    TextView mUrl;
    TextView mAge;
    ImageView mImage;
    WebView mWebView;
    ProgressBar mLoadingSpinner;
    TextView mBody;

    private int mPostId = 0;
    private String mPostServerId;

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
//        String title = intent.getStringExtra(getString(R.string.title));
//        String username = intent.getStringExtra(getString(R.string.username));
//        String subreddit = intent.getStringExtra(getString(R.string.subreddit));
//        String points = intent.getStringExtra(getString(R.string.points));
//        String comments = intent.getStringExtra(getString(R.string.comments));
        String url = intent.getStringExtra(getString(R.string.url));
//        String age = intent.getStringExtra(getString(R.string.age));
//        String imageUrl = intent.getStringExtra(getString(R.string.image_url));
        String body = intent.getStringExtra(getString(R.string.body));
//        String domain = intent.getStringExtra(getString(R.string.domain));
////
//
//        mPostDetailLayout = (LinearLayout) rootView.findViewById(R.id.post_detail_layout);
//        mTitle = (TextView) rootView.findViewById(R.id.post_title);
//        mUsername = (TextView) rootView.findViewById(R.id.post_username);
//        mSubreddit = (TextView) rootView.findViewById(R.id.post_subreddit);
//        mPoints = (TextView) rootView.findViewById(R.id.post_points);
//        mComments = (TextView) rootView.findViewById(R.id.post_comments);
//        mUrl = (TextView) rootView.findViewById(R.id.post_url);
//        mAge = (TextView) rootView.findViewById(R.id.post_age);
//        mImage = (ImageView) rootView.findViewById(R.id.post_detail_image);
        mWebView = (WebView) rootView.findViewById(R.id.post_web_view);
        mLoadingSpinner = (ProgressBar) rootView.findViewById(R.id.progressSpinner);
        mBody = (TextView) rootView.findViewById(R.id.post_body);

//
//        mTitle.setText(title);
//        mUsername.setText(username);
//        mSubreddit.setText(subreddit);
//        mPoints.setText(points);
//        mComments.setText(comments);
//        mUrl.setText(domain);
//        mAge.setText(age);
        mBody.setText(body);

//        Picasso.with(getContext()).load(imageUrl).into(mImage);


        Bundle args = getArguments();
        if (args != null) {
            mPostId = args.getInt(getString(R.string.post_id));
            mPostServerId = args.getString(getString(R.string.server_id));
        }




//        String selection = SiftContract.Posts._ID + " = ?";
//        String[] selectionArgs =  new String[]{String.valueOf(mPostId)};
//        Cursor cursor = getContext().getContentResolver().query(SiftContract.Posts.CONTENT_URI, null, selection, selectionArgs, null);
//        if(cursor != null && cursor.moveToFirst()) {
//
//            String title = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_TITLE));
//            String username = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_OWNER_USERNAME));
//            String subreddit = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_SUBREDDIT_NAME));
//            String points = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_POINTS));
//            String comments = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_NUM_COMMENTS));
//            String url = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_URL));
//            String age = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_DATE_CREATED));
//            String imageUrl = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_IMAGE_URL));
//            String serverId = cursor.getString(cursor.getColumnIndex(SiftContract.Posts.COLUMN_SERVER_ID));
//
//            mTitle.setText(title);
//            mUsername.setText(username);
//            mSubreddit.setText(subreddit);
//            mPoints.setText(points);
//            mComments.setText(comments);
//            mUrl.setText(url);
//            mAge.setText(age);
//
//            Picasso.with(getContext()).load(imageUrl).into(mImage);
//
//            if (!TextUtils.isEmpty(url)) {
//                mWebView.loadUrl(url);
//
//                mWebView.setWebChromeClient(new WebChromeClient() {
//                    @Override
//                    public void onProgressChanged(WebView view, int newProgress) {
////                activity.setProgress(newProgress * 1000);
////                    progressBar.setProgress(newProgress);
//                    }
//                });
//
//                mWebView.setWebViewClient(new WebViewClient() {
//                    @Override
//                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
////                    if (url.contains("code=")) {
////                        mReddit.runUserChallengeTask(url, getApplicationContext());
////                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
////                        startActivity(intent);
////                        //finish();
////                    }
//                    }
//                });
//            }
//        }



        if (TextUtils.isEmpty(body) && !TextUtils.isEmpty(url)) {
//            String mimeType = Utilities.getMimeType(url);
//            if (!TextUtils.isEmpty(mimeType) && mimeType.contains("image")) {
//                mWebView.setVisibility(View.GONE);
////                mPostDetailLayout.setVisibility(View.GONE);
//                mImage.setVisibility(View.VISIBLE);
//                Picasso.with(getContext()).load(url).into(mImage);
//            } else {
                mWebView.loadUrl(url);

                mWebView.setWebChromeClient(new WebChromeClient() {
                    @Override
                    public void onProgressChanged(WebView view, int newProgress) {
//                activity.setProgress(newProgress * 1000);
//                    progressBar.setProgress(newProgress);
                    }
                });

                mWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
//                        mPostDetailLayout.setVisibility(View.GONE);
                        mLoadingSpinner.setVisibility(View.VISIBLE);

//                    if (url.contains("code=")) {
//                        mReddit.runUserChallengeTask(url, getApplicationContext());
//                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                        startActivity(intent);
//                        //finish();
//                    }
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        mLoadingSpinner.setVisibility(View.GONE);
                    }
                });

                mWebView.getSettings().setJavaScriptEnabled(true);
                mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

//            }
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


//    private final class getPostTask extends AsyncTask<String, Void, Void> {
//
//        @Override
//        protected void onPreExecute() {
//            mLoadingSpinner.setVisibility(View.VISIBLE);
//        }
//
//        @Override
//        protected Void doInBackground(String... params) {
//            long startTime = System.currentTimeMillis();
//            Reddit reddit = Reddit.getInstance();
//            Submission post = reddit.mRedditClient.getSubmission(mPostServerId);
//
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void nothing) {
//
//            mLoadingSpinner.setVisibility(View.GONE);
//
//        }
//    }
}
