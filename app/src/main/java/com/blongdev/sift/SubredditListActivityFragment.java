package com.blongdev.sift;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blongdev.sift.database.SiftContract;
import com.blongdev.sift.database.SiftDbHelper;
import com.fasterxml.jackson.databind.JsonNode;

import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.Paginator;
import net.dean.jraw.paginators.SubredditPaginator;
import net.dean.jraw.paginators.SubredditSearchPaginator;

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

    Paginator mPaginator;
    Reddit mReddit;
    String mSearchTerm;

    ProgressBar mLoadingSpinner;


    public SubredditListActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_subreddit_list, container, false);

        mReddit = Reddit.getInstance();

        mSubreddits = new ArrayList<SubredditInfo>();
        mSubredditListView = (ListView) rootView.findViewById(R.id.subreddit_list);
        mLoadingSpinner = (ProgressBar) rootView.findViewById(R.id.progressSpinner);

        Bundle args = getArguments();
        if (args != null) {
            mSearchTerm = args.getString(getString(R.string.search_term));
            if (mSearchTerm != null) {
                mPaginator = new SubredditSearchPaginator(mReddit.mRedditClient, mSearchTerm);
                new GetSubredditsTask().execute();
            }
        } else {
            populateSubreddits();
        }

        mSubredditAdapter = new SubredditAdapter(getActivity(), mSubreddits);
        mSubredditListView.setAdapter(mSubredditAdapter);
        mSubredditListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SubredditInfo sub = mSubreddits.get(position);
                Intent intent = new Intent(getContext(), SubredditActivity.class);
                intent.putExtra(getString(R.string.subreddit_id), sub.mId);
                intent.putExtra(getString(R.string.subreddit_name), sub.mName);
                startActivity(intent);
            }
        });

        return rootView;
    }

    public class SubredditAdapter extends ArrayAdapter<SubredditInfo> {

        private List<SubredditInfo> mSubreddits;

        public SubredditAdapter(Context context, ArrayList<SubredditInfo> subreddits) {
            super(context, 0, subreddits);
            mSubreddits = subreddits;
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

        public void refreshWithList(List<SubredditInfo> subList) {
            this.mSubreddits = subList;
            notifyDataSetChanged();
        }


    }

    public void populateSubreddits() {
        Cursor cursor = getContext().getContentResolver().query(SiftContract.Subscriptions.VIEW_URI, null, null, null, null);
        if (cursor != null) {
                while (cursor.moveToNext()) {
                    SubredditInfo sub = new SubredditInfo();
                    sub.mId = cursor.getInt(cursor.getColumnIndex(SiftContract.Subscriptions.COLUMN_SUBREDDIT_ID));
                    sub.mName = cursor.getString(cursor.getColumnIndex(SiftContract.Subreddits.COLUMN_NAME));
                    mSubreddits.add(sub);
                }
        }

    }

    public static class SubredditViewHolder {
        protected TextView mName;
    }


    private final class GetSubredditsTask extends AsyncTask<String, Void, ArrayList<SubredditInfo>> {
        @Override
        protected ArrayList<SubredditInfo> doInBackground(String... params) {
            if (mPaginator != null && mPaginator.hasNext()) {
                Listing<Subreddit> page = mPaginator.next();
                for (Subreddit subreddit : page) {
                    SubredditInfo sub = new SubredditInfo();
                    sub.mName = subreddit.getDisplayName();
                    sub.mServerId = subreddit.getId();
                    mSubreddits.add(sub);
                }
            }

            return mSubreddits;
        }

        @Override
        protected void onPreExecute() {
            if(mSubreddits.size() == 0) {
                mLoadingSpinner.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected void onPostExecute(ArrayList<SubredditInfo> subs) {
            mLoadingSpinner.setVisibility(View.GONE);
            mSubredditAdapter.refreshWithList(subs);
        }
    }
}
