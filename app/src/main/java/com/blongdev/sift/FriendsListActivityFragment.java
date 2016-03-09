package com.blongdev.sift;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class FriendsListActivityFragment extends Fragment {

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
        populateFriends();

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

    public void populateFriends() {
        for (int i = 1; i<=10; i++) {
            UserInfo user = new UserInfo();
            user.mUsername = "Username" + i;
            user.mPoints = "" + i;
            user.mAge = i + "yrs";
            mFriends.add(user);
        }
    }


    public class FriendsAdapter extends ArrayAdapter<UserInfo> {
        public FriendsAdapter(Context context, ArrayList<UserInfo> users) {
            super(context, 0, users);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {

            UserViewHolder viewHolder;

            if (view == null) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.friend, parent, false);
                viewHolder = new UserViewHolder();
                viewHolder.mUsername = (TextView) view.findViewById(R.id.friend_username);
                view.setTag(viewHolder);
            } else {
                viewHolder = (UserViewHolder) view.getTag();
            }

            UserInfo user = mFriends.get(position);
            if(user != null) {
                viewHolder.mUsername.setText(user.mUsername);
            }

            return view;
        }

    }

    public static class UserViewHolder {
        protected TextView mUsername;
        protected TextView mPoints;
        protected TextView mAge;
    }
}
