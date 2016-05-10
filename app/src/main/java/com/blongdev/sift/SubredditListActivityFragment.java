package com.blongdev.sift;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.blongdev.sift.database.SiftContract;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.Paginator;
import net.dean.jraw.paginators.SubredditSearchPaginator;
import java.util.ArrayList;
import java.util.List;


public class SubredditListActivityFragment extends Fragment {

    ListView mSubredditListView;
    ArrayList<SubredditInfo> mSubreddits;
    SubredditAdapter mSubredditAdapter;

    Paginator mPaginator;
    Reddit mReddit;
    String mSearchTerm;

    ProgressBar mLoadingSpinner;

    private static final int CURSOR_LOADER_ID = 1;
    private static final int ASYNCTASK_LOADER_ID = 2;

    private LoaderManager.LoaderCallbacks<Cursor> mSubscriptionLoader
            = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            mLoadingSpinner.setVisibility(View.VISIBLE);
            return new CursorLoader(getContext(), SiftContract.Subscriptions.VIEW_URI,
                    null, null, null, SiftContract.Subreddits.COLUMN_NAME + " COLLATE NOCASE");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            mLoadingSpinner.setVisibility(View.GONE);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    SubredditInfo sub = new SubredditInfo();
                    sub.mId = cursor.getLong(cursor.getColumnIndex(SiftContract.Subscriptions.COLUMN_SUBREDDIT_ID));
                    sub.mName = cursor.getString(cursor.getColumnIndex(SiftContract.Subreddits.COLUMN_NAME));
                    sub.mDescription = cursor.getString(cursor.getColumnIndex(SiftContract.Subreddits.COLUMN_DESCRIPTION));
                    sub.mSubscribers = cursor.getLong(cursor.getColumnIndex(SiftContract.Subreddits.COLUMN_SUBSCRIBERS));
                    mSubreddits.add(sub);
                }
            }
            mSubredditAdapter.setData(mSubreddits);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mSubredditAdapter.setData(new ArrayList<SubredditInfo>());
        }
    };

    private LoaderManager.LoaderCallbacks<List<SubredditInfo>> mSearchSubredditsLoader
            = new LoaderManager.LoaderCallbacks<List<SubredditInfo>>() {

        @Override
        public Loader<List<SubredditInfo>> onCreateLoader(int id, Bundle args) {
            return new SubredditLoader(getContext(), mPaginator);
        }

        @Override
        public void onLoadFinished(Loader<List<SubredditInfo>> loader, List<SubredditInfo> data) {
            mLoadingSpinner.setVisibility(View.GONE);
            mSubredditAdapter.setData(data);
        }

        @Override
        public void onLoaderReset(Loader<List<SubredditInfo>> loader) {
            mSubredditAdapter.setData(new ArrayList<SubredditInfo>());
        }

    };


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
                getActivity().getSupportLoaderManager().initLoader(ASYNCTASK_LOADER_ID, null, mSearchSubredditsLoader).forceLoad();
                mLoadingSpinner.setVisibility(View.VISIBLE);
            }
        } else {
            getActivity().getSupportLoaderManager().initLoader(CURSOR_LOADER_ID, null, mSubscriptionLoader).forceLoad();
        }

        mSubredditAdapter = new SubredditAdapter(getActivity(), mSubreddits);
        mSubredditListView.setAdapter(mSubredditAdapter);
        mSubredditListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SubredditInfo sub = mSubreddits.get(position);
                if (sub.mId == 0) {
                    //check for subreddit in database, and if not found insert.
                    sub.mId = Utilities.getSubredditId(getContext(), sub.mServerId);
                    if (sub.mId == -1) {
                        ContentValues cv = new ContentValues();
                        cv.put(SiftContract.Subreddits.COLUMN_NAME, sub.mName);
                        cv.put(SiftContract.Subreddits.COLUMN_SERVER_ID, sub.mServerId);
                        cv.put(SiftContract.Subreddits.COLUMN_DESCRIPTION, sub.mDescription);
                        cv.put(SiftContract.Subreddits.COLUMN_SUBSCRIBERS, sub.mSubscribers);
                        Uri uri = getContext().getContentResolver().insert(SiftContract.Subreddits.CONTENT_URI, cv);
                        sub.mId = ContentUris.parseId(uri);
                    }
                }

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

        public void setData(List<SubredditInfo> data) {
            mSubreddits.addAll(data);
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {

            SubredditViewHolder viewHolder;

            if (view == null) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.subreddit, parent, false);
                viewHolder = new SubredditViewHolder();
                viewHolder.mName = (TextView) view.findViewById(R.id.subreddit_name);
                viewHolder.mDescription = (TextView) view.findViewById(R.id.subreddit_description);
                viewHolder.mSubscribers = (TextView) view.findViewById(R.id.subreddit_subscribers);
                view.setTag(viewHolder);
            } else {
                viewHolder = (SubredditViewHolder) view.getTag();
            }

            SubredditInfo sub = mSubreddits.get(position);
            if(sub != null) {
                viewHolder.mName.setText(sub.mName);
                viewHolder.mDescription.setText(sub.mDescription);
                if (sub.mSubscribers > 0) {
                    viewHolder.mSubscribers.setText(sub.mSubscribers + " " + getContext().getString(R.string.subscribers));
                }
            }

            return view;
        }

    }

    public static class SubredditViewHolder {
        protected TextView mName;
        protected TextView mDescription;
        protected TextView mSubscribers;
    }

}

class SubredditLoader extends AsyncTaskLoader<List<SubredditInfo>> {

    List<SubredditInfo> mSubreddits;
    Paginator mPaginator;
    Context mContext;


    public SubredditLoader(Context context, Paginator paginator) {
        super(context);
        mSubreddits = new ArrayList<SubredditInfo>();
        mPaginator = paginator;
        mContext = context;
    }

    @Override
    public List<SubredditInfo> loadInBackground() {
        Reddit reddit = Reddit.getInstance();
        if(!reddit.mRedditClient.isAuthenticated() || !Utilities.connectedToNetwork(mContext))
            return mSubreddits;

        try {
            if (mPaginator != null && mPaginator.hasNext()) {
                Listing<Subreddit> page = mPaginator.next();
                for (Subreddit subreddit : page) {
                    SubredditInfo sub = new SubredditInfo();
                    sub.mName = subreddit.getDisplayName();
                    sub.mServerId = subreddit.getId();
                    sub.mDescription = subreddit.getPublicDescription();
                    try {
                        //bug in jraw library sometimes throws nullpointerexception
                        sub.mSubscribers = subreddit.getSubscriberCount();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                    mSubreddits.add(sub);
                }
            }

        } catch (NetworkException e) {
            e.printStackTrace();
        }
        return mSubreddits;
    }
}
