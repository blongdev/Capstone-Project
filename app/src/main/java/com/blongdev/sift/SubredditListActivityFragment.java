package com.blongdev.sift;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.blongdev.sift.database.SiftContract;
import com.blongdev.sift.database.SiftDbHelper;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class SubredditListActivityFragment extends Fragment {

    ListView mSubredditListView;
    ArrayList<SubredditInfo> mSubreddits;
    SubredditAdapter mSubredditAdapter;

    public SubredditListActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_subreddit_list, container, false);

        mSubreddits = new ArrayList<SubredditInfo>();
        mSubredditListView = (ListView) rootView.findViewById(R.id.subreddit_list);

        populateSubreddits();

        mSubredditAdapter = new SubredditAdapter(getActivity(), mSubreddits);
        mSubredditListView.setAdapter(mSubredditAdapter);

        mSubredditListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SubredditInfo sub = mSubreddits.get(position);
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.putExtra(getString(R.string.username), sub.mName);
                startActivity(intent);
            }
        });

        return rootView;
    }

    public class SubredditAdapter extends ArrayAdapter<SubredditInfo> {
        public SubredditAdapter(Context context, ArrayList<SubredditInfo> subreddits) {
            super(context, 0, subreddits);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {

            SubredditViewHolder viewHolder;

            if (view == null) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.subreddit, parent, false);
                viewHolder = new SubredditViewHolder();
                viewHolder.mName = (TextView) view.findViewById(R.id.subreddit_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (SubredditViewHolder) view.getTag();
            }

            SubredditInfo sub = mSubreddits.get(position);
            if(sub != null) {
                viewHolder.mName.setText(sub.mName);
                //Picasso.with(getContext()).load(R.drawable.ic_account_circle_24dp).placeholder(R.drawable.ic_account_circle_24dp).into(viewHolder.mImage);
            }

            return view;
        }

    }

    public void populateSubreddits() {
        Cursor cursor = getContext().getContentResolver().query(SiftContract.Subreddits.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
                while (cursor.moveToNext()) {
                    SubredditInfo sub = new SubredditInfo();
                    //sub.mId = cursor.getInt(cursor.getColumnIndex(SiftContract.Subscriptions.COLUMN_SUBREDDIT_ID));
                    sub.mName = cursor.getString(cursor.getColumnIndex(SiftContract.Subreddits.COLUMN_NAME));
                    mSubreddits.add(sub);
                }
        }

//
//        for (int i = 1; i<=15; i++) {
//            SubredditInfo sub = new SubredditInfo();
//            sub.mName = "Subreddit " + i;
//            mSubreddits.add(sub);
//        }
    }

    public static class SubredditViewHolder {
        protected TextView mName;
    }
}
