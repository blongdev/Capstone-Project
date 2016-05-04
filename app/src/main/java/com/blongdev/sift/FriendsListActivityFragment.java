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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.blongdev.sift.database.SiftContract;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class FriendsListActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    ListView mFriendsListView;
    FriendsAdapter mFriendsAdapter;
    ArrayList<UserInfo> mFriends;

    public FriendsListActivityFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends_list, container, false);

        mFriends = new ArrayList<UserInfo>();
        //populateFriends();
        getLoaderManager().initLoader(0, null, this);


        mFriendsListView = (ListView) rootView.findViewById(R.id.friends_list);
        mFriendsAdapter = new FriendsAdapter(getActivity(), mFriends);
        mFriendsListView.setAdapter(mFriendsAdapter);

        mFriendsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UserInfo user = mFriends.get(position);
                Intent intent = new Intent(getContext(), UserInfoActivity.class);
                intent.putExtra(getString(R.string.username), user.mUsername);
                startActivity(intent);
            }
        });

        return rootView;
    }

//
//    public void populateFriends() {
//        String selection = SiftContract.Friends.COLUMN_ACCOUNT_ID + " = ?";
//        Cursor cursor = getContext().getContentResolver().query(SiftContract.Friends.VIEW_URI, null, null, null, null);
//        if (cursor != null) {
//            while (cursor.moveToNext()) {
//                UserInfo friend = new UserInfo();
//                friend.mUsername = cursor.getString(cursor.getColumnIndex(SiftContract.Users.COLUMN_USERNAME));
//                friend.mPoints = cursor.getInt(cursor.getColumnIndex(SiftContract.Users.COLUMN_POINTS));
//                friend.mAge = cursor.getInt(cursor.getColumnIndex(SiftContract.Users.COLUMN_DATE_CREATED));
//                mFriends.add(friend);
//            }
//        }

//    }


    class FriendsAdapter extends ArrayAdapter<UserInfo> {

        private ArrayList<UserInfo> mFriendsList;

        public FriendsAdapter(Context context, ArrayList<UserInfo> users) {
            super(context, 0, users);
            mFriendsList = users;
        }

        public void swapData(ArrayList<UserInfo> friendList) {
            this.mFriendsList = friendList;
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {

            UserViewHolder viewHolder;

            if (view == null) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.friend, parent, false);
                viewHolder = new UserViewHolder();
                viewHolder.mUsername = (TextView) view.findViewById(R.id.friend_username);
                viewHolder.mImage = (ImageView) view.findViewById(R.id.friend_icon);
                view.setTag(viewHolder);
            } else {
                viewHolder = (UserViewHolder) view.getTag();
            }

            UserInfo user = mFriendsList.get(position);
            if(user != null) {
                viewHolder.mUsername.setText(user.mUsername);
//                Picasso.with(getContext()).load(R.drawable.ic_account_circle_24dp).placeholder(R.drawable.ic_account_circle_24dp).into(viewHolder.mImage);
            }

            return view;
        }

    }

    public static class UserViewHolder {
        protected TextView mUsername;
        protected TextView mPoints;
        protected TextView mAge;
        protected ImageView mImage;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(), SiftContract.Friends.VIEW_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null) {
            while (cursor.moveToNext()) {
                UserInfo friend = new UserInfo();
                friend.mUsername = cursor.getString(cursor.getColumnIndex(SiftContract.Users.COLUMN_USERNAME));
                friend.mPoints = cursor.getInt(cursor.getColumnIndex(SiftContract.Users.COLUMN_POINTS));
                friend.mAge = cursor.getInt(cursor.getColumnIndex(SiftContract.Users.COLUMN_DATE_CREATED));
                mFriends.add(friend);
            }
            mFriendsAdapter.swapData(mFriends);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mFriendsAdapter.swapData(null);
    }
}
