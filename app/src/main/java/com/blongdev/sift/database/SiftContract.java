package com.blongdev.sift.database;

import android.accounts.Account;
import android.app.Notification;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.concurrent.ConcurrentMap;

/**
 * Created by Brian on 3/12/2016.
 */
public final class SiftContract {

    public static final String AUTHORITY = "com.blongdev.sift.database.SiftProvider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "sift.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ", ";





    private SiftContract() {
        
    }

    public static abstract class Posts implements BaseColumns {
        public static final String TABLE_NAME = "posts";
        public static final Uri CONTENT_URI = Uri.parse(BASE_CONTENT_URI + "/" + TABLE_NAME);
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_OWNER_USERNAME = "ownerUsername";
        public static final String COLUMN_OWNER_ID = "ownerId";
        public static final String COLUMN_SUBREDDIT_NAME = "subredditName";
        public static final String COLUMN_SUBREDDIT_ID = "subredditId";
        public static final String COLUMN_POINTS = "points";
        public static final String COLUMN_DATE_CREATED = "dateCreated";
        public static final String COLUMN_IMAGE_URL = "imageUrl";
        public static final String COLUMN_URL = "url";
        public static final String COLUMN_NUM_COMMENTS = "numComments";
        public static final String COLUMN_FAVORITED = "favorited";

        public static final int NOT_FAVORITED = 0;
        public static final int FAVORITED = 1;


        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_TITLE + TEXT_TYPE + COMMA_SEP +
                COLUMN_OWNER_USERNAME + TEXT_TYPE + COMMA_SEP +
                COLUMN_OWNER_ID + INTEGER_TYPE + COMMA_SEP +
                COLUMN_SUBREDDIT_NAME + TEXT_TYPE + COMMA_SEP +
                COLUMN_SUBREDDIT_ID + INTEGER_TYPE + COMMA_SEP +
                COLUMN_POINTS + INTEGER_TYPE + COMMA_SEP +
                COLUMN_DATE_CREATED + INTEGER_TYPE + COMMA_SEP +
                COLUMN_IMAGE_URL + TEXT_TYPE + COMMA_SEP +
                COLUMN_URL + TEXT_TYPE + COMMA_SEP +
                COLUMN_NUM_COMMENTS + INTEGER_TYPE + COMMA_SEP +
                COLUMN_FAVORITED + INTEGER_TYPE + COMMA_SEP +
                " FOREIGN KEY(" + COLUMN_SUBREDDIT_ID + ") REFERENCES " + Subreddits.TABLE_NAME + "(" + Subreddits._ID + ")" + COMMA_SEP +
                " FOREIGN KEY(" + COLUMN_OWNER_ID + ") REFERENCES " + Users.TABLE_NAME + "(" + Users._ID + ")" +
                " )";


        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class Accounts implements BaseColumns {
        public static final String TABLE_NAME = "accounts";
        public static final Uri CONTENT_URI = Uri.parse(BASE_CONTENT_URI + "/" + TABLE_NAME);

        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_DATE_CREATED = "dateCreated";
        public static final String COLUMN_PASSWORD = "password";
        public static final String COLUMN_USER_ID = "userId";
        public static final String COLUMN_REFRESH_KEY = "refreshKey";

        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_USERNAME + TEXT_TYPE + COMMA_SEP +
                COLUMN_PASSWORD + TEXT_TYPE + COMMA_SEP +
                COLUMN_DATE_CREATED + INTEGER_TYPE + COMMA_SEP +
                COLUMN_USER_ID + INTEGER_TYPE + COMMA_SEP +
                COLUMN_REFRESH_KEY + TEXT_TYPE + COMMA_SEP +
                " FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + Users.TABLE_NAME + "(" + Users._ID + ")" +  " )";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class Comments implements BaseColumns {
        public static final String TABLE_NAME = "comments";
        public static final Uri CONTENT_URI = Uri.parse(BASE_CONTENT_URI + "/" + TABLE_NAME);

        public static final String COLUMN_BODY = "body";
        public static final String COLUMN_OWNER_USERNAME = "ownerUsername";
        public static final String COLUMN_OWNER_ID = "ownerId";
        public static final String COLUMN_DATE_CREATED = "dateCreated";
        public static final String COLUMN_POINTS = "points";
        public static final String COLUMN_PARENT_ID = "parentId";
        public static final String COLUMN_POST_ID = "postId";


        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_BODY + TEXT_TYPE + COMMA_SEP +
                COLUMN_OWNER_USERNAME + TEXT_TYPE + COMMA_SEP +
                COLUMN_OWNER_ID + INTEGER_TYPE + COMMA_SEP +
                COLUMN_DATE_CREATED + INTEGER_TYPE + COMMA_SEP +
                COLUMN_POINTS + INTEGER_TYPE + COMMA_SEP +
                COLUMN_PARENT_ID + INTEGER_TYPE + COMMA_SEP +
                COLUMN_POST_ID + INTEGER_TYPE + COMMA_SEP +
                " FOREIGN KEY(" + COLUMN_OWNER_ID + ") REFERENCES " + Users.TABLE_NAME + "(" + Users._ID + ")" + COMMA_SEP +
                " FOREIGN KEY(" + COLUMN_POST_ID + ") REFERENCES " + Posts.TABLE_NAME + "(" + Posts._ID + ")" +" )";


        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class Users implements BaseColumns {
        public static final String TABLE_NAME = "users";
        public static final Uri CONTENT_URI = Uri.parse(BASE_CONTENT_URI + "/" + TABLE_NAME);

        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_USER_TYPE = "userType";
        public static final String COLUMN_POINTS = "points";
        public static final String COLUMN_DATE_CREATED = "dateCreated";

        public static final int USER_TYPE_NEUTRAL = 0;
        public static final int USER_TYPE_FRIEND = 1;
        public static final int USER_TYPE_OWNER = 2;


        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_USERNAME + TEXT_TYPE + COMMA_SEP +
                COLUMN_USER_TYPE + INTEGER_TYPE + COMMA_SEP +
                COLUMN_DATE_CREATED + INTEGER_TYPE + COMMA_SEP +
                COLUMN_POINTS + INTEGER_TYPE + " )";


        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class Subreddits implements BaseColumns {
        public static final String TABLE_NAME = "subreddits";
        public static final Uri CONTENT_URI = Uri.parse(BASE_CONTENT_URI + "/" + TABLE_NAME);

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DATE_CREATED = "dateCreated";


        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_NAME + TEXT_TYPE + COMMA_SEP +
                COLUMN_DATE_CREATED + INTEGER_TYPE + " )";


        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class Messages implements BaseColumns {
        public static final String TABLE_NAME = "messages";
        public static final Uri CONTENT_URI = Uri.parse(BASE_CONTENT_URI + "/" + TABLE_NAME);

        public static final String COLUMN_USER_TO = "userTo";
        public static final String COLUMN_USER_FROM = "userFrom";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_BODY = "body";
        public static final String COLUMN_READ = "read";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_ACCOUNT_ID = "accountId";


        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_BODY + TEXT_TYPE + COMMA_SEP +
                COLUMN_TITLE + TEXT_TYPE + COMMA_SEP +
                COLUMN_USER_TO + INTEGER_TYPE + COMMA_SEP +
                COLUMN_USER_FROM + INTEGER_TYPE + COMMA_SEP +
                COLUMN_DATE + INTEGER_TYPE + COMMA_SEP +
                COLUMN_READ + INTEGER_TYPE + COMMA_SEP +
                COLUMN_ACCOUNT_ID + INTEGER_TYPE + COMMA_SEP +
                " FOREIGN KEY(" + COLUMN_ACCOUNT_ID + ") REFERENCES " + Accounts.TABLE_NAME + "(" + Accounts._ID + ")" + COMMA_SEP +
                " FOREIGN KEY(" + COLUMN_USER_TO + ") REFERENCES " + Users.TABLE_NAME + "(" + Users._ID + ")" + COMMA_SEP +
                " FOREIGN KEY(" + COLUMN_USER_FROM + ") REFERENCES " + Users.TABLE_NAME + "(" + Users._ID + ")" + " )";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class Subscriptions implements BaseColumns {
        public static final String TABLE_NAME = "subscriptions";
        public static final String VIEW_NAME = "subscriptions_view";
        public static final Uri CONTENT_URI = Uri.parse(BASE_CONTENT_URI + "/" + TABLE_NAME);
        public static final Uri VIEW_URI = Uri.parse(BASE_CONTENT_URI + "/" + VIEW_NAME);


        public static final String COLUMN_SUBREDDIT_ID = "subredditId";
        public static final String COLUMN_ACCOUNT_ID = "accountId";

        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_ACCOUNT_ID + INTEGER_TYPE + COMMA_SEP +
                COLUMN_SUBREDDIT_ID + INTEGER_TYPE + COMMA_SEP +
                " FOREIGN KEY(" + COLUMN_ACCOUNT_ID + ") REFERENCES " + Accounts.TABLE_NAME + "(" + Accounts._ID + ")" + COMMA_SEP +
                " FOREIGN KEY(" + COLUMN_SUBREDDIT_ID + ") REFERENCES " + Subreddits.TABLE_NAME + "(" + Subreddits._ID + ")" + " )";


        public static final String CREATE_VIEW = "CREATE VIEW " + VIEW_NAME + " AS SELECT " + Subscriptions.COLUMN_ACCOUNT_ID + COMMA_SEP +
                Subscriptions.COLUMN_SUBREDDIT_ID + COMMA_SEP +
                Subreddits.COLUMN_NAME + COMMA_SEP + Subreddits.COLUMN_DATE_CREATED +
                " FROM " + Subscriptions.TABLE_NAME + " INNER JOIN " + Subreddits.TABLE_NAME +
                " WHERE " + Subscriptions.COLUMN_SUBREDDIT_ID + " = " + Subreddits.TABLE_NAME + "." + Subreddits._ID;

        public static final String DELETE_VIEW = "DROP VIEW IF EXISTS " + VIEW_NAME;

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class Favorites implements BaseColumns {
        public static final String TABLE_NAME = "favorites";
        public static final String VIEW_NAME = "favorites_view";

        public static final Uri CONTENT_URI = Uri.parse(BASE_CONTENT_URI + "/" + TABLE_NAME);
        public static final Uri VIEW_URI = Uri.parse(BASE_CONTENT_URI + "/" + VIEW_NAME);


        public static final String COLUMN_POST_ID = "postId";
        public static final String COLUMN_ACCOUNT_ID = "accountId";


        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_ACCOUNT_ID + INTEGER_TYPE + COMMA_SEP +
                COLUMN_POST_ID + INTEGER_TYPE + COMMA_SEP +
                " FOREIGN KEY(" + COLUMN_ACCOUNT_ID + ") REFERENCES " + Accounts.TABLE_NAME + "(" + Accounts._ID + ")" + COMMA_SEP +
                " FOREIGN KEY(" + COLUMN_POST_ID + ") REFERENCES " + Users.TABLE_NAME + "(" + Posts._ID + ")" + " )";


        public static final String CREATE_VIEW = "CREATE VIEW " + VIEW_NAME + " AS SELECT " + Favorites.COLUMN_ACCOUNT_ID + COMMA_SEP +
                Favorites.COLUMN_POST_ID + COMMA_SEP +
                Posts.COLUMN_TITLE + COMMA_SEP + Posts.COLUMN_DATE_CREATED + COMMA_SEP + Posts.COLUMN_OWNER_USERNAME + COMMA_SEP +
                Posts.COLUMN_OWNER_ID + COMMA_SEP + Posts.COLUMN_SUBREDDIT_ID + COMMA_SEP + Posts.COLUMN_SUBREDDIT_NAME + COMMA_SEP +
                Posts.COLUMN_IMAGE_URL + COMMA_SEP + Posts.COLUMN_POINTS + COMMA_SEP + Posts.COLUMN_NUM_COMMENTS +
                " FROM " + Favorites.TABLE_NAME + " INNER JOIN " + Posts.TABLE_NAME +
                " WHERE " + Favorites.COLUMN_POST_ID + " = " + Posts.TABLE_NAME + "." + Posts._ID;


        public static final String DELETE_VIEW = "DROP VIEW IF EXISTS " + VIEW_NAME;
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class Votes implements BaseColumns {
        public static final String TABLE_NAME = "votes";
        public static final Uri CONTENT_URI = Uri.parse(BASE_CONTENT_URI + "/" + TABLE_NAME);

        public static final String COLUMN_POST_ID = "postId";
        public static final String COLUMN_COMMENT_ID = "commentId";
        public static final String COLUMN_ACCOUNT_ID = "accountId";
        public static final String COLUMN_VOTE = "vote";

        public static final int UDOWNVOTE = 0;
        public static final int UPVOTE = 1;

        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_ACCOUNT_ID + INTEGER_TYPE + COMMA_SEP +
                COLUMN_POST_ID + INTEGER_TYPE + COMMA_SEP +
                COLUMN_COMMENT_ID + INTEGER_TYPE + COMMA_SEP +
                COLUMN_VOTE + INTEGER_TYPE + COMMA_SEP +
                " FOREIGN KEY(" + COLUMN_ACCOUNT_ID + ") REFERENCES " + Accounts.TABLE_NAME + "(" + Accounts._ID + ")" + COMMA_SEP +
                " FOREIGN KEY(" + COLUMN_POST_ID + ") REFERENCES " + Posts.TABLE_NAME + "(" + Posts._ID + ")" + COMMA_SEP +
                " FOREIGN KEY(" + COLUMN_COMMENT_ID + ") REFERENCES " + Comments.TABLE_NAME + "(" + Comments._ID + ")" + " )";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class Friends implements BaseColumns {
        public static final String TABLE_NAME = "friends";
        public static final String VIEW_NAME = "friends_view";
        public static final Uri CONTENT_URI = Uri.parse(BASE_CONTENT_URI + "/" + TABLE_NAME);
        public static final Uri VIEW_URI = Uri.parse(BASE_CONTENT_URI + "/" + VIEW_NAME);


        public static final String COLUMN_ACCOUNT_ID = "accountId";
        public static final String COLUMN_FRIEND_USER_ID = "friendUserId";

        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_ACCOUNT_ID + INTEGER_TYPE + COMMA_SEP +
                COLUMN_FRIEND_USER_ID + INTEGER_TYPE + COMMA_SEP +
                " FOREIGN KEY(" + COLUMN_ACCOUNT_ID + ") REFERENCES " + Accounts.TABLE_NAME + "(" + Accounts._ID + ")" + COMMA_SEP +
                " FOREIGN KEY(" + COLUMN_FRIEND_USER_ID + ") REFERENCES " + Users.TABLE_NAME + "(" + Users._ID + ")" + " )";

        public static final String CREATE_VIEW = "CREATE VIEW " + VIEW_NAME + " AS SELECT " + Friends.COLUMN_ACCOUNT_ID + COMMA_SEP +
                Friends.COLUMN_FRIEND_USER_ID + COMMA_SEP +
                Users.COLUMN_USERNAME + COMMA_SEP + Users.COLUMN_USER_TYPE + COMMA_SEP + Users.COLUMN_DATE_CREATED + COMMA_SEP + Users.COLUMN_POINTS +
                " FROM " + Friends.TABLE_NAME + " INNER JOIN " + Users.TABLE_NAME +
                " WHERE " + Friends.COLUMN_FRIEND_USER_ID + " = " + Users.TABLE_NAME + "." + Users._ID;

        public static final String DELETE_VIEW = "DROP VIEW IF EXISTS " + VIEW_NAME;

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }



}