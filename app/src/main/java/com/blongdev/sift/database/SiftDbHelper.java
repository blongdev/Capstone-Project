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

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SiftContract.Posts.CREATE_POSTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SiftContract.Posts.DELETE_TABLE);
        onCreate(db);
    }

}