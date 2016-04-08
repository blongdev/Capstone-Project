package com.blongdev.sift.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.blongdev.sift.AccountInfo;
import com.blongdev.sift.Reddit;
import com.blongdev.sift.SubscriptionInfo;
import com.blongdev.sift.UserInfo;
import com.blongdev.sift.database.SiftContract;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.http.oauth.OAuthHelper;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Message;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.models.UserRecord;
import net.dean.jraw.paginators.ImportantUserPaginator;
import net.dean.jraw.paginators.InboxPaginator;
import net.dean.jraw.paginators.SubredditPaginator;
import net.dean.jraw.paginators.TimePeriod;
import net.dean.jraw.paginators.UserContributionPaginator;
import net.dean.jraw.paginators.UserSubredditsPaginator;

import java.util.ArrayList;

/**
 * Created by Brian on 3/23/2016.
 */
public class SiftSyncAdapter extends AbstractThreadedSyncAdapter {
    // Global variables
    // Define a variable to contain a content resolver instance
    ContentResolver mContentResolver;

    /**
     * Set up the sync adapter
     */
    public SiftSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
    }

    /**
     * Set up the sync adapter. This form of the
     * constructor maintains compatibility with Android 3.0
     * and later platform versions
     */
    public SiftSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.v("SiftSyncAdapter", "onPerformSync()");
        long startTime = System.currentTimeMillis();

        ArrayList<AccountInfo> accounts = new ArrayList<AccountInfo>();

        Cursor accountCursor = null;
        try {
            accountCursor = provider.query(SiftContract.Accounts.CONTENT_URI, null, null, null, null, null);
            if (accountCursor != null) {
                while (accountCursor.moveToNext()) {
                    AccountInfo info = new AccountInfo();
                    info.mId = accountCursor.getInt(accountCursor.getColumnIndex(SiftContract.Accounts._ID));
                    info.mRefreshKey = accountCursor.getString(accountCursor.getColumnIndex(SiftContract.Accounts.COLUMN_REFRESH_KEY));
                    accounts.add(info);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            if (accountCursor != null) {
                accountCursor.close();
            }
        }

        for (AccountInfo currentAccount : accounts) {
            RedditClient redditClient = new RedditClient(Reddit.getUserAgent());
            OAuthHelper oAuthHelper = redditClient.getOAuthHelper();
            oAuthHelper.setRefreshToken(currentAccount.mRefreshKey);
            try {
                OAuthData data = oAuthHelper.refreshToken(Reddit.getCredentials());
                redditClient.authenticate(data);
            } catch (OAuthException e) {
                e.printStackTrace();
            }

            if (redditClient.isAuthenticated()) {
                getData(redditClient, currentAccount.mId, provider);
            }
        }

        long endTime = System.currentTimeMillis();

        Log.v("SiftSyncAdapter", "Sync Completed. Total Time: " + (endTime - startTime)/1000 + " seconds");
    }

    private void getData(RedditClient redditClient, int accountId, ContentProviderClient provider) {
        ContentValues cv = new ContentValues();
        //Subscribed
        UserSubredditsPaginator subscribed = new UserSubredditsPaginator(redditClient, "subscriber");
        subscribed.setLimit(Integer.MAX_VALUE);
        if (subscribed.hasNext()) {
            Listing<Subreddit> subreddits = subscribed.next();
            for (Subreddit s : subreddits) {
                //add subreddit
                cv.put(SiftContract.Subreddits.COLUMN_NAME, s.getDisplayName());
                cv.put(SiftContract.Subreddits.COLUMN_SERVER_ID, s.getId());
                Uri subredditUri = mContentResolver.insert(SiftContract.Subreddits.CONTENT_URI, cv);
                long subredditId = ContentUris.parseId(subredditUri);
                cv.clear();

                //add subscription
                cv.put(SiftContract.Subscriptions.COLUMN_ACCOUNT_ID, accountId);
                cv.put(SiftContract.Subscriptions.COLUMN_SUBREDDIT_ID, subredditId);
                mContentResolver.insert(SiftContract.Subscriptions.CONTENT_URI, cv);
                cv.clear();
            }
        }

        //add favorite posts
//        Cursor cursor = null;
//        try {
//            cursor = provider.query(SiftContract.Subscriptions.VIEW_URI, null, null, null, null);
//            if (cursor != null) {
//                while (cursor.moveToNext()) {
//                    int subredditId = cursor.getInt(cursor.getColumnIndex(SiftContract.Subscriptions.COLUMN_SUBREDDIT_ID));
//                    String subredditName = cursor.getString(cursor.getColumnIndex(SiftContract.Subreddits.COLUMN_NAME));
//
//                    SubredditPaginator postPager = new SubredditPaginator(redditClient, subredditName);
//                    postPager.setLimit(10);
//                    if (postPager.hasNext()) {
//                        Listing<Submission> submissions = postPager.next();
//                        for (Submission post : submissions) {
//                            cv.put(SiftContract.Posts.COLUMN_OWNER_USERNAME, post.getAuthor());
//                            cv.put(SiftContract.Posts.COLUMN_SERVER_ID, post.getId());
//                            cv.put(SiftContract.Posts.COLUMN_NUM_COMMENTS, post.getCommentCount());
//                            cv.put(SiftContract.Posts.COLUMN_POINTS, post.getScore());
//                            cv.put(SiftContract.Posts.COLUMN_SUBREDDIT_ID, subredditId);
//                            cv.put(SiftContract.Posts.COLUMN_SUBREDDIT_NAME, post.getSubredditName());
//                            cv.put(SiftContract.Posts.COLUMN_IMAGE_URL, post.getThumbnail());
//                            cv.put(SiftContract.Posts.COLUMN_TITLE, post.getTitle());
//                            mContentResolver.insert(SiftContract.Posts.CONTENT_URI, cv);
//                            cv.clear();
//                        }
//                        Log.d("SiftSyncAdapter", submissions.size() + " posts added for " + subredditName);
//                    }
//                }
//            }
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        } finally {
//            if (cursor != null) {
//                cursor.close();
//            }
//        }


        ImportantUserPaginator friends = new ImportantUserPaginator(redditClient, "friends");
        friends.setLimit(Integer.MAX_VALUE);
        if (friends.hasNext()) {
            Listing<UserRecord> friend = friends.next();
            for (UserRecord u : friend) {

                //GET USER INFO
                //UserContributionPaginator userPaginator = new UserContributionPaginator();
                cv.put(SiftContract.Users.COLUMN_SERVER_ID, u.getId());
                Uri userUri = mContentResolver.insert(SiftContract.Users.CONTENT_URI, cv);
                long userId = ContentUris.parseId(userUri);
                cv.clear();

                //add subscription
                cv.put(SiftContract.Friends.COLUMN_ACCOUNT_ID, accountId);
                cv.put(SiftContract.Friends.COLUMN_FRIEND_USER_ID, userId);
                mContentResolver.insert(SiftContract.Friends.CONTENT_URI, cv);
                cv.clear();
            }
        }

        InboxPaginator inbox = new InboxPaginator(redditClient, "inbox");
        inbox.setLimit(Integer.MAX_VALUE);
        inbox.setTimePeriod(TimePeriod.MONTH);
        if (inbox.hasNext()) {
            Listing<Message> message = inbox.next();
            for (Message m : message) {
                cv.put(SiftContract.Messages.COLUMN_USER_FROM, m.getAuthor());
                cv.put(SiftContract.Messages.COLUMN_TITLE, m.getSubject());
                cv.put(SiftContract.Messages.COLUMN_BODY, m.getBody());
                cv.put(SiftContract.Messages.COLUMN_SERVER_ID, m.getId());
                cv.put(SiftContract.Messages.COLUMN_ACCOUNT_ID, accountId);
                mContentResolver.insert(SiftContract.Messages.CONTENT_URI, cv);
                cv.clear();
            }
        }
    }


}
