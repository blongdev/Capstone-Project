package com.blongdev.sift;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class MessageDetailActivityFragment extends Fragment {

    public MessageDetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_message_detail, container, false);

        TextView titleView = (TextView) rootView.findViewById(R.id.message_detail_title);
        TextView bodyView = (TextView) rootView.findViewById(R.id.message_detail_body);

        //Change toolbar title to username
        Intent intent = getActivity().getIntent();
        String title = intent.getStringExtra(getString(R.string.title));
        if (!TextUtils.isEmpty(title)) {
            titleView.setText(title);
        }

        String body = intent.getStringExtra(getString(R.string.body));
        if (!TextUtils.isEmpty(body)) {
            bodyView.setText(body);
        }

        return rootView;
    }
}
