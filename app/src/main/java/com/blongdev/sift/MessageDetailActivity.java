package com.blongdev.sift;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

public class MessageDetailActivity extends BaseActivity {

    String mUsername;
    String mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_detail);

        //Change toolbar title to username
        Intent intent = getIntent();
        mUsername = intent.getStringExtra(getString(R.string.username));
        mTitle = intent.getStringExtra(getString(R.string.title));

        if (!TextUtils.isEmpty(mUsername)) {
            ActionBar toolbar = getSupportActionBar();
            if (toolbar != null) {
                toolbar.setTitle(mUsername);
            }
        }


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Utilities.loggedIn(getApplicationContext())) {
                    Toast.makeText(getApplicationContext(), getString(R.string.must_log_in), Toast.LENGTH_LONG).show();
                    return;
                }

                Intent intent = new Intent(getApplicationContext(), ComposeMessageActivity.class);
                intent.putExtra(getString(R.string.username), mUsername);
                intent.putExtra(getString(R.string.title), getString(R.string.re) + mTitle);
                startActivity(intent);
            }
        });

        //hide reply fab when viewing sent message
        if(TextUtils.equals(mUsername, Utilities.getLoggedInUsername(getApplicationContext()))) {
            fab.setVisibility(View.GONE);
        }
    }

}
