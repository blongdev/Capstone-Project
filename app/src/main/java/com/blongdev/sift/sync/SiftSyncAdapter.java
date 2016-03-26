package com.blongdev.sift.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.blongdev.sift.Reddit;
import com.blongdev.sift.UserInfo;
import com.blongdev.sift.database.SiftContract;

import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Message;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.models.UserRecord;
import net.dean.jraw.paginators.ImportantUserPaginator;
import net.dean.jraw.paginators.InboxPaginator;
import net.dean.jraw.paginators.SubredditPaginator;
import net.dean.jraw.paginators.UserSubredditsPaginator;

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

        Reddit reddit = Reddit.getInstance();
        if (reddit.mRedditClient.isAuthenticated()) {
            ContentValues cv = new ContentValues();
            //Subscribed
            UserSubredditsPaginator subscribed = new UserSubredditsPaginator(reddit.mRedditClient, "subscriber");
            Listing<Subreddit> subreddits = subscribed.next();
            for (Subreddit s : subreddits) {
                cv.put(SiftContract.Subreddits.COLUMN_NAME, s.getDisplayName());
                cv.put(SiftContract.Subreddits.COLUMN_SERVER_ID, s.getId());
                mContentResolver.insert(SiftContract.Subreddits.CONTENT_URI, cv);
                cv.clear();
            }

            ImportantUserPaginator friends = new ImportantUserPaginator(reddit.mRedditClient, "friends");
            Listing<UserRecord> friend = friends.next();
            for (UserRecord u : friend) {
                cv.put(SiftContract.Users.COLUMN_USERNAME, u.getNote());
                cv.put(SiftContract.Users.COLUMN_SERVER_ID, u.getId());
                mContentResolver.insert(SiftContract.Subreddits.CONTENT_URI, cv);
                cv.clear();
            }

            InboxPaginator inbox = new InboxPaginator(reddit.mRedditClient, "inbox");
            Listing<Message> message = inbox.next();
            for (Message m : message) {
                cv.put(SiftContract.Messages.COLUMN_USER_FROM, m.getAuthor());
                cv.put(SiftContract.Messages.COLUMN_TITLE, m.getSubject());
                cv.put(SiftContract.Messages.COLUMN_BODY, m.getBody());
                cv.put(SiftContract.Messages.COLUMN_SERVER_ID, m.getId());
                mContentResolver.insert(SiftContract.Messages.CONTENT_URI, cv);
                cv.clear();
            }


//
//            net.dean.jraw.models.Account user = reddit.mRedditClient.getUser();
//
//            cv.put(SiftContract.Subreddits.COLUMN_NAME, subreddit);
//            try {
//                provider.insert(SiftContract.Subreddits.CONTENT_URI, cv);
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }

            Log.v("SiftSyncAdapter", "Sync Completed");
        }
    }
}
