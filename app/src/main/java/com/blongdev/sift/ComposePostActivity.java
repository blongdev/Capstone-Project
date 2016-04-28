package com.blongdev.sift;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import net.dean.jraw.ApiException;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.models.Captcha;

import java.net.MalformedURLException;
import java.net.URL;

public class ComposePostActivity extends BaseActivity {

    String mSubredditName;
    Captcha mCaptcha;
    ImageView mCaptchaImage;
    CheckBox mLinkBox;
    TextView mTextLabel;
    EditText mTitle;
    EditText mBody;
    EditText mCaptchaText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_post);

        TextView subreddit = (TextView) findViewById(R.id.subreddit_label);
        mCaptchaImage = (ImageView) findViewById(R.id.captcha_image);
        mLinkBox = (CheckBox) findViewById(R.id.link);
        mTextLabel = (TextView) findViewById(R.id.body_label);
        mTitle = (EditText) findViewById(R.id.title_text);
        mBody = (EditText) findViewById(R.id.body_text);
        mCaptchaText = (EditText) findViewById(R.id.captcha_text);

        Intent intent = getIntent();
        if (intent != null) {
            mSubredditName = intent.getStringExtra(getString(R.string.subreddit_name));
            subreddit.setText(mSubredditName);
        }

        new GetCaptchaTask(getApplicationContext()).execute();

        mLinkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLinkBox.isChecked()) {
                    mTextLabel.setText(getString(R.string.compose_url));
                } else {
                    mTextLabel.setText(getString(R.string.compose_text));
                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String title = mTitle.getText().toString();
                String body = mBody.getText().toString();
                String captchaText = mCaptchaText.getText().toString();

                if (TextUtils.isEmpty(title)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.add_title), Toast.LENGTH_LONG).show();
                    return;
                }

                if (mLinkBox.isChecked()) {
                    if (TextUtils.isEmpty(body) || !URLUtil.isValidUrl(body)) {
                        Toast.makeText(getApplicationContext(), getString(R.string.add_url), Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                if (mCaptcha != null && TextUtils.isEmpty(captchaText)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.add_captcha), Toast.LENGTH_LONG).show();
                    return;
                }

                if (mLinkBox.isChecked()) {
                    try {
                        URL url = new URL(body);
                        Reddit.linkSubmission(getApplicationContext(), mSubredditName, title, url, mCaptcha, captchaText);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                } else {
                    Reddit.textSubmission(getApplicationContext(), mSubredditName, title, body, mCaptcha, captchaText);
                }

//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });
    }


    private final class GetCaptchaTask extends AsyncTask<String, Void, Captcha> {

        Context mContext;

        public GetCaptchaTask(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Captcha doInBackground(String... params) {
            Reddit reddit = Reddit.getInstance();
            if (!reddit.mRedditClient.isAuthenticated()) {
                return null;
            }

            try {
                if (reddit.mRedditClient.needsCaptcha()) {
                    return reddit.mRedditClient.getNewCaptcha();
                }
            } catch (NetworkException | ApiException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Captcha captcha) {
            if (captcha != null) {
                mCaptcha = captcha;
                Picasso.with(mContext).load(captcha.getImageUrl().toString()).into(mCaptchaImage);
            }
        }
    }

}
