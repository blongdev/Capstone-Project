package com.blongdev.sift;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.blongdev.sift.database.SiftContract;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MessageActivityFragment extends Fragment {

    ArrayList<MessageInfo> mMessages;
    ListView mMessagesListView;
    MessagesAdapter mMessagesAdapter;

    public MessageActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_message_list, container, false);

        mMessages = new ArrayList<MessageInfo>();
        populateMessages();

        mMessagesListView = (ListView) rootView.findViewById(R.id.messages_list);
        mMessagesAdapter = new MessagesAdapter(getActivity(), mMessages);
        mMessagesListView.setAdapter(mMessagesAdapter);

        mMessagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MessageInfo user = mMessages.get(position);
                Intent intent = new Intent(getContext(), MessageDetailActivity.class);
                intent.putExtra(getString(R.string.username), user.mTitle);
                startActivity(intent);
            }
        });

        return rootView;
    }

    public class MessagesAdapter extends ArrayAdapter<MessageInfo> {
        public MessagesAdapter(Context context, ArrayList<MessageInfo> messages) {
            super(context, 0, messages);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {

            MessageViewHolder viewHolder;

            if (view == null) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.message, parent, false);
                viewHolder = new MessageViewHolder();
                viewHolder.mTitle = (TextView) view.findViewById(R.id.message_title);
                viewHolder.mBody = (TextView) view.findViewById(R.id.message_body);
                view.setTag(viewHolder);
            } else {
                viewHolder = (MessageViewHolder) view.getTag();
            }

            MessageInfo msg = mMessages.get(position);
            if(msg != null) {
                viewHolder.mBody.setText(msg.mFrom);
                //Picasso.with(getContext()).load(R.drawable.ic_account_circle_24dp).placeholder(R.drawable.ic_account_circle_24dp).into(viewHolder.mImage);
            }

            return view;
        }

    }

    public void populateMessages() {
        String selection = SiftContract.Messages.COLUMN_ACCOUNT_ID + " = ?";
        Cursor cursor = getContext().getContentResolver().query(SiftContract.Messages.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                MessageInfo msg = new MessageInfo();
                msg.mFrom = cursor.getString(cursor.getColumnIndex(SiftContract.Messages.COLUMN_USER_FROM));
                msg.mTo = cursor.getString(cursor.getColumnIndex(SiftContract.Messages.COLUMN_USER_TO));
                msg.mTitle = cursor.getString(cursor.getColumnIndex(SiftContract.Messages.COLUMN_TITLE));
                msg.mBody = cursor.getString(cursor.getColumnIndex(SiftContract.Messages.COLUMN_BODY));
                msg.mDate = cursor.getInt(cursor.getColumnIndex(SiftContract.Messages.COLUMN_DATE));
                mMessages.add(msg);
            }
        }

//        for (int i = 1; i<=15; i++) {
//            MessageInfo msg = new MessageInfo();
//            msg.mFrom = "From: " + i;
//            msg.mTo = "To: " + i;
//            msg.mTitle = "Title " + i;
//            msg.mBody = "Body " + i;
//            msg.mDate = 12;
//            mMessages.add(msg);
//        }
    }

    public static class MessageViewHolder {
        protected TextView mTo;
        protected TextView mFrom;
        protected TextView mTitle;
        protected TextView mBody;
        protected TextView mDate;
    }

    private static final String ARG_SECTION_NUMBER = "section_number";

    public static MessageActivityFragment newInstance(int sectionNumber) {
        MessageActivityFragment fragment = new MessageActivityFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

}
