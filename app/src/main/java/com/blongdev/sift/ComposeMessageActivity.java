package com.blongdev.sift;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class ComposeMessageActivity extends BaseActivity {

    String mUserTo;
    TextView mTo;
    EditText mSubject;
    EditText mBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_message);

        mTo = (TextView) findViewById(R.id.to_label);
        mSubject = (EditText) findViewById(R.id.subject_text);
        mBody = (EditText) findViewById(R.id.body_text);

        Intent intent = getIntent();
        if (intent != null) {
            mUserTo = intent.getStringExtra(getString(R.string.username));
            mTo.setText(mUserTo);
            String subject = intent.getStringExtra(getString(R.string.title));
            if (!TextUtils.isEmpty(subject)) {
                mSubject.setText(subject);
            }
        }


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String subject = mSubject.getText().toString();
                String body = mBody.getText().toString();

                if (TextUtils.isEmpty(subject)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.add_subject), Toast.LENGTH_LONG).show();
                    return;
                }

                if (TextUtils.isEmpty(body)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.add_body), Toast.LENGTH_LONG).show();
                    return;
                }

                Reddit.sendMessage(getApplicationContext(), mUserTo, subject, body);
            }

//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
        });
    }

}