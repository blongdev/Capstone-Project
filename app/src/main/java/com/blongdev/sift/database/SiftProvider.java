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

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(SiftContract.AUTHORITY, SiftContract.Posts.TABLE_NAME, POSTS);
        sURIMatcher.addURI(SiftContract.AUTHORITY, SiftContract.Posts.TABLE_NAME + "/#", POSTS_ID);
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