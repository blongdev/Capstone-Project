package com.blongdev.sift.database;

/**
 * Created by Brian on 3/12/2016.
 */

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class SiftProvider extends ContentProvider {

    private SiftDbHelper mDbHelper;

    public static final int POSTS = 1;
    public static final int POSTS_ID = 2;
    public static final int SUBREDDITS = 3;
    public static final int USERS = 4;
    public static final int COMMENTS = 5;
    public static final int MESSAGES = 6;
    public static final int ACCOUNTS = 7;
    public static final int SUBSCRIPTIONS = 8;
    public static final int FAVORITES = 9;
    public static final int VOTES = 10;
    public static final int FRIENDS = 11;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(SiftContract.AUTHORITY, SiftContract.Posts.TABLE_NAME, POSTS);
        sURIMatcher.addURI(SiftContract.AUTHORITY, SiftContract.Posts.TABLE_NAME + "/#", POSTS_ID);
        sURIMatcher.addURI(SiftContract.AUTHORITY, SiftContract.Subreddits.TABLE_NAME, SUBREDDITS);
        sURIMatcher.addURI(SiftContract.AUTHORITY, SiftContract.Users.TABLE_NAME, USERS);
        sURIMatcher.addURI(SiftContract.AUTHORITY, SiftContract.Comments.TABLE_NAME, COMMENTS);
        sURIMatcher.addURI(SiftContract.AUTHORITY, SiftContract.Messages.TABLE_NAME, MESSAGES);
        sURIMatcher.addURI(SiftContract.AUTHORITY, SiftContract.Accounts.TABLE_NAME, ACCOUNTS);
        sURIMatcher.addURI(SiftContract.AUTHORITY, SiftContract.Subscriptions.TABLE_NAME, SUBSCRIPTIONS);
        sURIMatcher.addURI(SiftContract.AUTHORITY, SiftContract.Favorites.TABLE_NAME, FAVORITES);
        sURIMatcher.addURI(SiftContract.AUTHORITY, SiftContract.Votes.TABLE_NAME, VOTES);
        sURIMatcher.addURI(SiftContract.AUTHORITY, SiftContract.Friends.TABLE_NAME, FRIENDS);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsDeleted = 0;

        switch (uriType) {
            case POSTS:
                rowsDeleted = db.delete(SiftContract.Posts.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;

            case POSTS_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(SiftContract.Posts.TABLE_NAME,
                            SiftContract.Posts._ID + "=" + id,
                            null);
                } else {
                    rowsDeleted = db.delete(SiftContract.Posts.TABLE_NAME,
                            SiftContract.Posts._ID + "=" + id
                                    + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public String getType(Uri arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long id = 0;
        switch (uriType) {
            case POSTS:
                id = db.insert(SiftContract.Posts.TABLE_NAME,
                        null, values);
                break;
            case SUBREDDITS:
                id = db.insert(SiftContract.Subreddits.TABLE_NAME,
                        null, values);
                break;
            case USERS:
                id = db.insert(SiftContract.Users.TABLE_NAME,
                        null, values);
                break;
            case COMMENTS:
                id = db.insert(SiftContract.Comments.TABLE_NAME,
                        null, values);
                break;
            case MESSAGES:
                id = db.insert(SiftContract.Messages.TABLE_NAME,
                        null, values);
                break;
            case ACCOUNTS:
                id = db.insert(SiftContract.Accounts.TABLE_NAME,
                        null, values);
                break;
            case SUBSCRIPTIONS:
                id = db.insert(SiftContract.Subscriptions.TABLE_NAME,
                        null, values);
                break;
            case FAVORITES:
                id = db.insert(SiftContract.Favorites.TABLE_NAME,
                        null, values);
                break;
            case VOTES:
                id = db.insert(SiftContract.Votes.TABLE_NAME,
                        null, values);
                break;
            case FRIENDS:
                id = db.insert(SiftContract.Friends.TABLE_NAME,
                        null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(SiftContract.Posts.TABLE_NAME + "/" + id);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new SiftDbHelper(getContext(), null, null, 1);
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                               String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(SiftContract.Posts.TABLE_NAME);

        int uriType = sURIMatcher.match(uri);

        switch (uriType) {
            case POSTS_ID:
                queryBuilder.appendWhere(SiftContract.Posts._ID + "="
                        + uri.getLastPathSegment());
                break;
            case POSTS:
                break;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }

        Cursor cursor = queryBuilder.query(mDbHelper.getReadableDatabase(),
                projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsUpdated = 0;

        switch (uriType) {
            case POSTS:
                rowsUpdated = db.update(SiftContract.Posts.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case POSTS_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated =
                            db.update(SiftContract.Posts.TABLE_NAME,
                                    values,
                                    SiftContract.Posts._ID + "=" + id,
                                    null);
                } else {
                    rowsUpdated =
                            db.update(SiftContract.Posts.TABLE_NAME,
                                    values,
                                    SiftContract.Posts._ID + "=" + id
                                            + " and "
                                            + selection,
                                    selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

}