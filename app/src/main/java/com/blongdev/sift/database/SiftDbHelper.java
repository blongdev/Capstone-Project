package com.blongdev.sift.database;

/**
 * Created by Brian on 3/12/2016.
 */


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Debug;

public class SiftDbHelper extends SQLiteOpenHelper {

    private ContentResolver mContentResolver;

    public SiftDbHelper(Context context, String name,
                        CursorFactory factory, int version) {
        super(context, SiftContract.DATABASE_NAME, factory, SiftContract.DATABASE_VERSION);
        mContentResolver = context.getContentResolver();
    }

    public SiftDbHelper(Context context) {
        super(context, SiftContract.DATABASE_NAME, null, SiftContract.DATABASE_VERSION);
    }

    public void createDb() {
        SQLiteDatabase db = this.getReadableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SiftContract.Posts.CREATE_TABLE);
        db.execSQL(SiftContract.Accounts.CREATE_TABLE);
        db.execSQL(SiftContract.Comments.CREATE_TABLE);
        db.execSQL(SiftContract.Subreddits.CREATE_TABLE);
        db.execSQL(SiftContract.Users.CREATE_TABLE);
        db.execSQL(SiftContract.Messages.CREATE_TABLE);
        db.execSQL(SiftContract.Subscriptions.CREATE_TABLE);
        db.execSQL(SiftContract.Favorites.CREATE_TABLE);
        db.execSQL(SiftContract.Votes.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SiftContract.Posts.DELETE_TABLE);
        db.execSQL(SiftContract.Accounts.DELETE_TABLE);
        db.execSQL(SiftContract.Comments.DELETE_TABLE);
        db.execSQL(SiftContract.Subreddits.DELETE_TABLE);
        db.execSQL(SiftContract.Users.DELETE_TABLE);
        db.execSQL(SiftContract.Messages.DELETE_TABLE);
        db.execSQL(SiftContract.Subscriptions.DELETE_TABLE);
        db.execSQL(SiftContract.Favorites.DELETE_TABLE);
        db.execSQL(SiftContract.Votes.DELETE_TABLE);
        onCreate(db);
    }

}