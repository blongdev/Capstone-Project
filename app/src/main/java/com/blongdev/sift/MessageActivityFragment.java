package com.blongdev.sift;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.blongdev.sift.database.SiftContract;
import java.util.ArrayList;

public class MessageActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    ArrayList<MessageInfo> mMessages;
    ListView mMessagesListView;
    MessagesAdapter mMessagesAdapter;
    int mMailbox;
    boolean mUnread;

    public MessageActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_message_list, container, false);

        Bundle args = getArguments();
        if (args != null) {
            mMailbox = args.getInt(getString(R.string.mailbox));
            mUnread = args.getBoolean(getString(R.string.unread));
        } else {
            Intent intent = getActivity().getIntent();
            mMailbox  = intent.getIntExtra(getString(R.string.mailbox),0);
            mUnread = intent.getBooleanExtra(getString(R.string.unread), false);
        }

        mMessages = new ArrayList<MessageInfo>();

        mMessagesListView = (ListView) rootView.findViewById(R.id.messages_list);
        mMessagesAdapter = new MessagesAdapter(getActivity(), mMessages);
        mMessagesListView.setAdapter(mMessagesAdapter);

        getLoaderManager().initLoader(0, null, this);


        mMessagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MessageInfo msg = mMessages.get(position);
                Intent intent = new Intent(getContext(), MessageDetailActivity.class);
                intent.putExtra(getString(R.string.username), msg.mFrom);
                intent.putExtra(getString(R.string.title), msg.mTitle);
                intent.putExtra(getString(R.string.body), msg.mBody);
                startActivity(intent);
            }
        });

        return rootView;
    }

    public class MessagesAdapter extends ArrayAdapter<MessageInfo> {

        private ArrayList<MessageInfo> mMessages;

        public MessagesAdapter(Context context, ArrayList<MessageInfo> messages) {
            super(context, 0, messages);
            mMessages = messages;
        }

        public void swapData(ArrayList<MessageInfo> messageList) {
            this.mMessages = messageList;
            notifyDataSetChanged();
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
                viewHolder.mTitle.setText(msg.mTitle);
                viewHolder.mBody.setText(msg.mBody);
            }

            return view;
        }
    }

    public static class MessageViewHolder {
        protected TextView mTo;
        protected TextView mFrom;
        protected TextView mTitle;
        protected TextView mBody;
        protected TextView mDate;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection;
        String[] selectionArgs;
        if(mUnread) {
            selection = SiftContract.Messages.COLUMN_MAILBOX_TYPE + " = ? AND " + SiftContract.Messages.COLUMN_READ + " = ?";
            selectionArgs = new String[]{String.valueOf(mMailbox), "0"};
        } else {
            selection = SiftContract.Messages.COLUMN_MAILBOX_TYPE + " = ?";
            selectionArgs = new String[]{String.valueOf(mMailbox)};
        }

        return new CursorLoader(getContext(), SiftContract.Messages.CONTENT_URI, null, selection, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null) {
            mMessages.clear();
            cursor.moveToPosition(-1);
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
        mMessagesAdapter.swapData(mMessages);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mMessagesAdapter.swapData(null);
    }

}
