package com.blongdev.sift.sync;

import android.accounts.Account;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.blongdev.sift.AccountInfo;
import com.blongdev.sift.MessageActivity;
import com.blongdev.sift.R;
import com.blongdev.sift.Reddit;
import com.blongdev.sift.SiftApplication;
import com.blongdev.sift.SiftBroadcastReceiver;
import com.blongdev.sift.SiftWidget;
import com.blongdev.sift.Utilities;
import com.blongdev.sift.database.SiftContract;
import com.google.android.gms.analytics.GoogleAnalytics;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthHelper;
import net.dean.jraw.models.Contribution;
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
    ContentResolver mContentResolver;
    Context mContext;


    public SiftSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
        mContext = context;
    }


    public SiftSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.v("SiftSyncAdapter", "onPerformSync()");
        GoogleAnalytics.getInstance(mContext).dispatchLocalHits();


        long startTime = System.currentTimeMillis();
        boolean initialSync = extras.getBoolean(SiftApplication.getContext().getString(R.string.initial_sync), false);

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

        RedditClient redditClient = new RedditClient(Reddit.getUserAgent());

        for (AccountInfo currentAccount : accounts) {
            OAuthHelper oAuthHelper = redditClient.getOAuthHelper();
            oAuthHelper.setRefreshToken(currentAccount.mRefreshKey);
            try {
                OAuthData data = oAuthHelper.refreshToken(Reddit.getCredentials());
                redditClient.authenticate(data);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (redditClient.isAuthenticated() && redditClient.hasActiveUserContext()) {
                try {
                    getData(redditClient, currentAccount.mId, provider, initialSync);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }

        if (redditClient.isAuthenticated()) {
            try {
                //sync front page posts
                SubredditPaginator paginator = new SubredditPaginator(redditClient);
                Listing<Submission> submissions = paginator.next();

                ContentValues cv = new ContentValues();
                int i = 0;
                for (Submission sub : submissions) {
                    cv.put(SiftContract.Posts.COLUMN_SERVER_ID, sub.getId());
                    cv.put(SiftContract.Posts.COLUMN_TITLE, sub.getTitle());
                    cv.put(SiftContract.Posts.COLUMN_OWNER_USERNAME, sub.getAuthor());
                    cv.put(SiftContract.Posts.COLUMN_SUBREDDIT_NAME, sub.getSubredditName());
                    cv.put(SiftContract.Posts.COLUMN_SUBREDDIT_ID, -1);
                    cv.put(SiftContract.Posts.COLUMN_POINTS, sub.getScore());
                    cv.put(SiftContract.Posts.COLUMN_URL, sub.getUrl());
                    cv.put(SiftContract.Posts.COLUMN_IMAGE_URL, Reddit.getImageUrl(sub));
                    cv.put(SiftContract.Posts.COLUMN_NUM_COMMENTS, sub.getCommentCount());
                    cv.put(SiftContract.Posts.COLUMN_BODY, sub.getSelftext());
                    cv.put(SiftContract.Posts.COLUMN_DOMAIN, sub.getDomain());
                    cv.put(SiftContract.Posts.COLUMN_DATE_CREATED, sub.getCreatedUtc().getTime());
                    cv.put(SiftContract.Posts.COLUMN_VOTE, sub.getVote().getValue());
                    cv.put(SiftContract.Posts.COLUMN_FAVORITED, sub.isSaved());
                    cv.put(SiftContract.Posts.COLUMN_POSITION, i);
                    mContentResolver.insert(SiftContract.Posts.CONTENT_URI, cv);
                    cv.clear();
                    i++;
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }

            //update widgets with new frontpage posts
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(mContext, SiftWidget.class));
            Intent updateIntent = new Intent();
            updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            updateIntent.putExtra(SiftWidget.WIDGET_IDS, appWidgetIds);
            mContext.sendBroadcast(updateIntent);
        }

        long endTime = System.currentTimeMillis();

        Log.v("SiftSyncAdapter", "Sync Completed. Total Time: " + (endTime - startTime) / 1000 + " seconds");
    }

    private void getData(RedditClient redditClient, int accountId, ContentProviderClient provider, boolean initialSync) {
        ContentValues cv = new ContentValues();
        //Subscribed
        UserSubredditsPaginator subscribed = new UserSubredditsPaginator(redditClient, SiftApplication.getContext().getString(R.string.subscriber));
        subscribed.setLimit(Integer.MAX_VALUE);
        if (subscribed.hasNext()) {
            Listing<Subreddit> subreddits = subscribed.next();
            for (Subreddit s : subreddits) {
                //add subreddit
                String subName = s.getDisplayName();
                String serverId = s.getId();
                String description = s.getPublicDescription();
                long numSubscribers = -1;
                try {
                    //bug in jraw library sometimes throws nullpointerexception
                     numSubscribers = s.getSubscriberCount();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                //check if subreddit exists in db
                long subredditId = Utilities.getSubredditId(SiftApplication.getContext(), serverId);
                if (subredditId <= 0) {
                    cv.put(SiftContract.Subreddits.COLUMN_NAME, subName);
                    cv.put(SiftContract.Subreddits.COLUMN_SERVER_ID, serverId);
                    cv.put(SiftContract.Subreddits.COLUMN_DESCRIPTION, description);
                    cv.put(SiftContract.Subreddits.COLUMN_SUBSCRIBERS, numSubscribers);
                    Uri subredditUri = mContentResolver.insert(SiftContract.Subreddits.CONTENT_URI, cv);
                    subredditId = ContentUris.parseId(subredditUri);
                    cv.clear();
                }

                //add subscription
                cv.put(SiftContract.Subscriptions.COLUMN_ACCOUNT_ID, accountId);
                cv.put(SiftContract.Subscriptions.COLUMN_SUBREDDIT_ID, subredditId);
                mContentResolver.insert(SiftContract.Subscriptions.CONTENT_URI, cv);
                cv.clear();

                if (!initialSync) {
                    SubredditPaginator paginator = new SubredditPaginator(redditClient, subName);
                    Listing<Submission> submissions = paginator.next();
                    int i = 0;
                    for (Submission sub : submissions) {
                        cv.put(SiftContract.Posts.COLUMN_SERVER_ID, sub.getId());
                        cv.put(SiftContract.Posts.COLUMN_TITLE, sub.getTitle());
                        cv.put(SiftContract.Posts.COLUMN_OWNER_USERNAME, sub.getAuthor());
                        cv.put(SiftContract.Posts.COLUMN_SUBREDDIT_NAME, sub.getSubredditName());
                        cv.put(SiftContract.Posts.COLUMN_SUBREDDIT_ID, subredditId);
                        cv.put(SiftContract.Posts.COLUMN_POINTS, sub.getScore());
                        cv.put(SiftContract.Posts.COLUMN_URL, sub.getUrl());
                        cv.put(SiftContract.Posts.COLUMN_IMAGE_URL, Reddit.getImageUrl(sub));
                        cv.put(SiftContract.Posts.COLUMN_NUM_COMMENTS, sub.getCommentCount());
                        cv.put(SiftContract.Posts.COLUMN_BODY, sub.getSelftext());
                        cv.put(SiftContract.Posts.COLUMN_DOMAIN, sub.getDomain());
                        cv.put(SiftContract.Posts.COLUMN_DATE_CREATED, sub.getCreatedUtc().getTime());
                        cv.put(SiftContract.Posts.COLUMN_VOTE, sub.getVote().getValue());
                        cv.put(SiftContract.Posts.COLUMN_FAVORITED, sub.isSaved());
                        cv.put(SiftContract.Posts.COLUMN_POSITION, i);
                        mContentResolver.insert(SiftContract.Posts.CONTENT_URI, cv);
                        cv.clear();
                        i++;
                    }
                }
            }
        }

        if (initialSync) {
            Intent intent = new Intent(SiftBroadcastReceiver.LOGGED_IN);
            SiftApplication.getContext().sendBroadcast(intent);
        }

        //add favorite posts
        UserContributionPaginator favorites = new UserContributionPaginator(redditClient, SiftApplication.getContext().getString(R.string.saved), redditClient.getAuthenticatedUser());
        favorites.setLimit(Integer.MAX_VALUE);
        if (favorites.hasNext()) {
            Listing<Contribution> contributions = favorites.next();
            Submission sub;
            for (Contribution c : contributions) {
                if (c instanceof Submission) {
                    sub = (Submission) c;
                    cv.put(SiftContract.Posts.COLUMN_SERVER_ID, sub.getId());
                    cv.put(SiftContract.Posts.COLUMN_TITLE, sub.getTitle());
                    cv.put(SiftContract.Posts.COLUMN_OWNER_USERNAME, sub.getAuthor());
                    cv.put(SiftContract.Posts.COLUMN_SUBREDDIT_NAME, sub.getSubredditName());
                    cv.put(SiftContract.Posts.COLUMN_POINTS, sub.getScore());
                    cv.put(SiftContract.Posts.COLUMN_URL, sub.getUrl());
                    cv.put(SiftContract.Posts.COLUMN_IMAGE_URL, Reddit.getImageUrl(sub));
                    cv.put(SiftContract.Posts.COLUMN_NUM_COMMENTS, sub.getCommentCount());
                    cv.put(SiftContract.Posts.COLUMN_BODY, sub.getSelftext());
                    cv.put(SiftContract.Posts.COLUMN_DOMAIN, sub.getDomain());
                    cv.put(SiftContract.Posts.COLUMN_DATE_CREATED, sub.getCreatedUtc().getTime());
                    cv.put(SiftContract.Posts.COLUMN_VOTE, sub.getVote().getValue());
                    cv.put(SiftContract.Posts.COLUMN_FAVORITED, 1);
                    Uri uri = mContentResolver.insert(SiftContract.Posts.CONTENT_URI, cv);
                    long postId = ContentUris.parseId(uri);

                    cv.clear();
                    cv.put(SiftContract.Favorites.COLUMN_ACCOUNT_ID, accountId);
                    cv.put(SiftContract.Favorites.COLUMN_POST_ID, postId);
                    mContentResolver.insert(SiftContract.Favorites.CONTENT_URI, cv);
                    cv.clear();
                }
            }
        }

        ImportantUserPaginator friends = new ImportantUserPaginator(redditClient, SiftApplication.getContext().getString(R.string.friends));
        friends.setLimit(Integer.MAX_VALUE);
        if (friends.hasNext()) {
            Listing<UserRecord> friend = friends.next();
            for (UserRecord u : friend) {
                //GET USER INFO
                String fullname = u.getFullName();
                net.dean.jraw.models.Account user = redditClient.getUser(fullname);

                cv.put(SiftContract.Users.COLUMN_SERVER_ID, user.getId());
                cv.put(SiftContract.Users.COLUMN_USERNAME, fullname);
                cv.put(SiftContract.Users.COLUMN_DATE_CREATED, user.getCreatedUtc().getTime());
                cv.put(SiftContract.Users.COLUMN_COMMENT_KARMA, user.getCommentKarma());
                cv.put(SiftContract.Users.COLUMN_LINK_KARMA, user.getLinkKarma());
                Uri userUri = mContentResolver.insert(SiftContract.Users.CONTENT_URI, cv);
                long userId = ContentUris.parseId(userUri);
                cv.clear();

                //add friend
                cv.put(SiftContract.Friends.COLUMN_ACCOUNT_ID, accountId);
                cv.put(SiftContract.Friends.COLUMN_FRIEND_USER_ID, userId);
                mContentResolver.insert(SiftContract.Friends.CONTENT_URI, cv);
                cv.clear();
            }
        }

        int newMessages = 0;

        //messages
        InboxPaginator inbox = new InboxPaginator(redditClient, SiftApplication.getContext().getString(R.string.inbox));
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
                cv.put(SiftContract.Messages.COLUMN_READ, m.isRead());
                cv.put(SiftContract.Messages.COLUMN_MAILBOX_TYPE, SiftContract.Messages.MAILBOX_TYPE_INBOX);
                Uri messageUri = mContentResolver.insert(SiftContract.Messages.CONTENT_URI, cv);
                long messageId = ContentUris.parseId(messageUri);
                if (messageId > 0 && !m.isRead()) {
                    newMessages++;
                }
                cv.clear();
            }
        }

        InboxPaginator sent = new InboxPaginator(redditClient, SiftApplication.getContext().getString(R.string.sent));
        sent.setLimit(Integer.MAX_VALUE);
        sent.setTimePeriod(TimePeriod.MONTH);
        if (sent.hasNext()) {
            Listing<Message> message = sent.next();
            for (Message m : message) {
                cv.put(SiftContract.Messages.COLUMN_USER_FROM, m.getAuthor());
                cv.put(SiftContract.Messages.COLUMN_TITLE, m.getSubject());
                cv.put(SiftContract.Messages.COLUMN_BODY, m.getBody());
                cv.put(SiftContract.Messages.COLUMN_SERVER_ID, m.getId());
                cv.put(SiftContract.Messages.COLUMN_ACCOUNT_ID, accountId);
                cv.put(SiftContract.Messages.COLUMN_MAILBOX_TYPE, SiftContract.Messages.MAILBOX_TYPE_SENT);
                mContentResolver.insert(SiftContract.Messages.CONTENT_URI, cv);
                cv.clear();
            }
        }

        InboxPaginator mentions = new InboxPaginator(redditClient, SiftApplication.getContext().getString(R.string.mentions));
        mentions.setLimit(Integer.MAX_VALUE);
        mentions.setTimePeriod(TimePeriod.MONTH);
        if (mentions.hasNext()) {
            Listing<Message> message = mentions.next();
            for (Message m : message) {
                cv.put(SiftContract.Messages.COLUMN_USER_FROM, m.getAuthor());
                cv.put(SiftContract.Messages.COLUMN_TITLE, m.getSubject());
                cv.put(SiftContract.Messages.COLUMN_BODY, m.getBody());
                cv.put(SiftContract.Messages.COLUMN_SERVER_ID, m.getId());
                cv.put(SiftContract.Messages.COLUMN_ACCOUNT_ID, accountId);
                cv.put(SiftContract.Messages.COLUMN_MAILBOX_TYPE, SiftContract.Messages.MAILBOX_TYPE_MENTIONS);
                mContentResolver.insert(SiftContract.Messages.CONTENT_URI, cv);
                cv.clear();
            }
        }

        if (newMessages > 0) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(mContext)
                            .setSmallIcon(R.drawable.notification_icon)
                            .setContentTitle(mContext.getString(R.string.sift));
            if (newMessages == 1) {
                mBuilder.setContentText(mContext.getString(R.string.new_message, newMessages));
            } else {
                mBuilder.setContentText(mContext.getString(R.string.new_messages, newMessages));
            }

            Intent resultIntent = new Intent(mContext, MessageActivity.class);
            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(mContext, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            mBuilder.setContentIntent(resultPendingIntent);

            int mNotificationId = 001;
            NotificationManager mNotifyMgr = (NotificationManager) mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
            mNotifyMgr.notify(mNotificationId, mBuilder.build());
        }

    }
}
