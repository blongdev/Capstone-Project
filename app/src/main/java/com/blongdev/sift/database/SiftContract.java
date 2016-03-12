package com.blongdev.sift.database;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Brian on 3/12/2016.
 */
public final class SiftContract {

    public static final String AUTHORITY = "com.blongdev.sift.database.SiftProvider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final  int    DATABASE_VERSION   = 1;
    public static final  String DATABASE_NAME      = "sift.db";
    private static final String TEXT_TYPE          = " TEXT";
    private static final String COMMA_SEP          = ",";

    private SiftContract() {}

    public static abstract class Posts implements BaseColumns {
        public static final String TABLE_NAME       = "posts";
        public static final Uri CONTENT_URI = Uri.parse(BASE_CONTENT_URI + "/" + TABLE_NAME);
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_OWNER= "owner";
        public static final String COLUMN_POINTS = "points";


        public static final String CREATE_POSTS_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_TITLE + TEXT_TYPE + COMMA_SEP +
                COLUMN_OWNER + TEXT_TYPE + COMMA_SEP +
                COLUMN_POINTS + TEXT_TYPE + COMMA_SEP + " )";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}