package com.blongdev.sift;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.blongdev.sift.database.SiftContract;

import net.dean.jraw.RedditClient;

/**
 * Created by Brian on 3/29/2016.
 */
public class Utilities {

    private static final int SECONDS_IN_MINUTE = 60;
    private static final int SECONDS_IN_HOUR = SECONDS_IN_MINUTE * 60;
    private static final int SECONDS_IN_DAY = SECONDS_IN_HOUR * 24;
    private static final int SECONDS_IN_MONTH_ISH = SECONDS_IN_DAY * 30;
    private static final int SECONDS_IN_YEAR_ISH = SECONDS_IN_DAY * 365;

    public static boolean connectedToNetwork(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }


    //returns _ID or -1 if updated
    public static long insertOrUpdate(Context context, Uri uri, ContentValues cv, String where, String[] selectionArgs) {
        int count = context.getContentResolver().update(uri, cv, where, selectionArgs);
        if (count <= 0) {
            Uri newUri = context.getContentResolver().insert(uri, cv);
            return ContentUris.parseId(newUri);
        }
        return -1;
    }

    public static int getSubredditId(Context context, String serverId) {
        String[] projection = new String[]{SiftContract.Subreddits._ID};
        String selection = SiftContract.Subreddits.COLUMN_SERVER_ID + " =?";
        String[] selectionArgs = new String[]{serverId};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(SiftContract.Subreddits.CONTENT_URI, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return -1;
    }

    public static int getSubscriptionId(Context context, String subredditName) {
        String[] projection = new String[]{SiftContract.Subscriptions.COLUMN_SUBREDDIT_ID};
        String selection = SiftContract.Subreddits.COLUMN_NAME + " =?";
        String[] selectionArgs = new String[]{subredditName};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(SiftContract.Subscriptions.VIEW_URI, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return -1;
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }


    public static long getAgeInSeconds(long date) {
        long currentTime = System.currentTimeMillis();
        return (currentTime - date)/(1000);
    }

    public static String getAgeString(long date) {
        long age = getAgeInSeconds(date);
        if (age < SECONDS_IN_HOUR) {
            return age/SECONDS_IN_MINUTE + "m";
        } else if (age < SECONDS_IN_DAY) {
            return age/SECONDS_IN_HOUR + "h";
        } else if (age < SECONDS_IN_MONTH_ISH){
            return age/SECONDS_IN_DAY + "d";
        } else if (age < SECONDS_IN_YEAR_ISH) {
            return age/SECONDS_IN_MONTH_ISH + "M";
        } else {
            return age/SECONDS_IN_YEAR_ISH + "Y";
        }
    }

    public static boolean loggedIn (Context context) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(SiftContract.Accounts.CONTENT_URI, null, null,null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return true;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return false;
    }

    public static long getAccountId (Context context, RedditClient redditClient) {
        Cursor cursor = null;
        try {
            if(redditClient.isAuthenticated() && redditClient.hasActiveUserContext()) {
                String selection = SiftContract.Accounts.COLUMN_USERNAME + " = ?";
                String[] selectionArgs = new String[]{redditClient.me().getFullName()};
                cursor = context.getContentResolver().query(SiftContract.Accounts.CONTENT_URI, null, selection, selectionArgs, null);
                if (cursor != null && cursor.moveToFirst()) {
                    return cursor.getLong(cursor.getColumnIndex(SiftContract.Accounts._ID));
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return -1;
    }
}
