package com.blongdev.sift.database;

/**
 * Created by Brian on 3/12/2016.
 */


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import com.blongdev.sift.AccountInfo;
import com.blongdev.sift.CommentInfo;
import com.blongdev.sift.FavoritesInfo;
import com.blongdev.sift.FriendInfo;
import com.blongdev.sift.MessageInfo;
import com.blongdev.sift.PostInfo;
import com.blongdev.sift.SubredditInfo;
import com.blongdev.sift.SubscriptionInfo;
import com.blongdev.sift.UserInfo;
import com.blongdev.sift.VoteInfo;

public class SiftDbHelper extends SQLiteOpenHelper {

    private ContentResolver mContentResolver;

    public SiftDbHelper(Context context, String name,
                        CursorFactory factory, int version) {
        super(context, SiftContract.DATABASE_NAME, factory, SiftContract.DATABASE_VERSION);
        mContentResolver = context.getContentResolver();
    }

    public SiftDbHelper(Context context) {
        super(context, SiftContract.DATABASE_NAME, null, SiftContract.DATABASE_VERSION);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.execSQL("PRAGMA foreign_keys=ON;");
        db.setForeignKeyConstraintsEnabled(true);
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
        db.execSQL(SiftContract.Friends.CREATE_TABLE);

        db.execSQL(SiftContract.Friends.CREATE_VIEW);
        db.execSQL(SiftContract.Subscriptions.CREATE_VIEW);
        db.execSQL(SiftContract.Favorites.CREATE_VIEW);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }


    public static final int NUM_DUMMY_POSTS = 100;
    public static final int NUM_DUMMY_SUBREDDITS = 30;
    public static final int NUM_DUMMY_COMMENTS = 500;
    public static final int NUM_DUMMY_USERS = 50;
    public static final int NUM_DUMMY_ACCOUNTS = 2;
    public static final int NUM_DUMMY_MESSAGES = 50;
    public static final int NUM_DUMMY_SUBSCRIPTIONS = 10;
    public static final int NUM_DUMMY_FAVORITES = 20;
    public static final int NUM_DUMMY_VOTES = 300;
    public static final int NUM_DUMMY_FRIENDS = 15;


    public void insertDummyData() {
        insertDummyPosts();
        insertDummyUsers();
        insertDummySubreddits();
        //insertDummyAccounts();
        insertDummyMessages();
        insertDummyComments();
        insertDummySubscriptions();
        insertDummyFavorites();
        insertDummyVotes();
        insertDummyFriends();
    }

    public void insertDummyPosts() {
        for (int i=1; i <= NUM_DUMMY_POSTS; i++) {
            PostInfo post = new PostInfo();
            post.mUsername = "Username" + i;
            post.mSubreddit = "Subreddit" + i;

            if(i%3 == 0) {
                post.mTitle = "This is an extended title to test larger cards in this layout. " +
                        "How far can I stretch this card before it breaks?";
            } else {
                post.mTitle = "Title" + i;
            }

            if(i%2 == 0) {
                post.mImageUrl = "http://www.melovemypet.com/wp-content/uploads/2014/05/Bluenosepitbullpuppy1.jpg";
            } else if (i%5 == 0) {
                post.mImageUrl = "https://s-media-cache-ak0.pinimg.com/236x/1e/49/8f/1e498f4bb5069b00d92f977dc8187f55.jpg";
            } else {
                post.mImageUrl = null;
            }

            post.mPoints = (int )(Math.random() * 300);
            post.mComments = (int )(Math.random() * 30);
            post.mUrl = "Url" + i;
            post.mAge = (int )(Math.random() * 12) + 1;
            post.mSubredditId = (int) (Math.random() * NUM_DUMMY_SUBREDDITS);
            post.mUserId = (int) (Math.random() * NUM_DUMMY_USERS) + 1;

            ContentValues cv = new ContentValues();
            cv.put(SiftContract.Posts.COLUMN_OWNER_USERNAME, post.mUsername);
            cv.put(SiftContract.Posts.COLUMN_SUBREDDIT_NAME, post.mSubreddit);
            cv.put(SiftContract.Posts.COLUMN_TITLE, post.mTitle);
            cv.put(SiftContract.Posts.COLUMN_IMAGE_URL, post.mImageUrl);
            cv.put(SiftContract.Posts.COLUMN_POINTS, post.mPoints);
            cv.put(SiftContract.Posts.COLUMN_NUM_COMMENTS, post.mComments);
            cv.put(SiftContract.Posts.COLUMN_URL, post.mUrl);
            cv.put(SiftContract.Posts.COLUMN_DATE_CREATED, post.mAge);
            cv.put(SiftContract.Posts.COLUMN_SUBREDDIT_ID, post.mSubredditId);
            cv.put(SiftContract.Posts.COLUMN_OWNER_ID, post.mUserId);

            mContentResolver.insert(SiftContract.Posts.CONTENT_URI, cv);
        }
    }


    public void insertDummyUsers() {
        for (int i=1; i <= NUM_DUMMY_USERS; i++) {
            UserInfo user = new UserInfo();
            user.mUsername = "User " + i;
            user.mLinkKarma = (int )(Math.random() * 3000);
            user.mAge = (int )(Math.random() * 12) + 1;
            user.mUserType = 0;

            ContentValues cv = new ContentValues();
            cv.put(SiftContract.Users.COLUMN_USERNAME, user.mUsername);
            cv.put(SiftContract.Users.COLUMN_POINTS, user.mLinkKarma);
            cv.put(SiftContract.Users.COLUMN_DATE_CREATED, user.mAge);
            cv.put(SiftContract.Users.COLUMN_USER_TYPE, user.mUserType);

            mContentResolver.insert(SiftContract.Users.CONTENT_URI, cv);
        }
    }

    public void insertDummySubreddits() {
        for (int i=1; i <= NUM_DUMMY_SUBREDDITS; i++) {
            SubredditInfo sub = new SubredditInfo();
            sub.mName = "Subreddit " + i;
            sub.mAge = (int )(Math.random() * 12) + 1;

            ContentValues cv = new ContentValues();
            cv.put(SiftContract.Subreddits.COLUMN_NAME, sub.mName);
            cv.put(SiftContract.Subreddits.COLUMN_DATE_CREATED, sub.mAge);

            mContentResolver.insert(SiftContract.Subreddits.CONTENT_URI, cv);
        }
    }

    public void insertDummyAccounts() {
        for (int i=1; i <= NUM_DUMMY_ACCOUNTS; i++) {
            AccountInfo acct = new AccountInfo();
            acct.mUsername = "Account " + i;
            acct.mUserId = (int )(Math.random() * NUM_DUMMY_USERS) + 1;
            acct.mPassword = "Password" + i;
            acct.mDateCreated = (int) (Math.random() * 36);

            ContentValues cv = new ContentValues();
            cv.put(SiftContract.Accounts.COLUMN_USERNAME, acct.mUsername);
            cv.put(SiftContract.Accounts.COLUMN_DATE_CREATED, acct.mDateCreated);
            cv.put(SiftContract.Accounts.COLUMN_USER_ID, acct.mUserId);
            cv.put(SiftContract.Accounts.COLUMN_PASSWORD, acct.mPassword);

            mContentResolver.insert(SiftContract.Accounts.CONTENT_URI, cv);
        }
    }

    public void insertDummyMessages() {
        for (int i=1; i <= NUM_DUMMY_MESSAGES; i++) {
            MessageInfo msg = new MessageInfo();
            msg.mToUserId = (int)(Math.random() * NUM_DUMMY_USERS)+1;
            msg.mFromUserId = (int)(Math.random() * NUM_DUMMY_USERS)+1;
            msg.mAccountId = (int)(Math.random() * NUM_DUMMY_ACCOUNTS)+1;
            msg.mDate = (int) (Math.random() * 36);
            msg.mTitle = "Title " + i;
            msg.mBody = "Body " + i;

            ContentValues cv = new ContentValues();
            cv.put(SiftContract.Messages.COLUMN_ACCOUNT_ID, msg.mAccountId);
            cv.put(SiftContract.Messages.COLUMN_USER_TO, msg.mToUserId);
            cv.put(SiftContract.Messages.COLUMN_USER_FROM, msg.mFromUserId);
            cv.put(SiftContract.Messages.COLUMN_DATE, msg.mDate);
            cv.put(SiftContract.Messages.COLUMN_READ, msg.mRead);
            cv.put(SiftContract.Messages.COLUMN_TITLE, msg.mTitle);
            cv.put(SiftContract.Messages.COLUMN_BODY, msg.mBody);

            mContentResolver.insert(SiftContract.Messages.CONTENT_URI, cv);
        }
    }

    public void insertDummyComments() {
        for (int i=1; i <= NUM_DUMMY_COMMENTS; i++) {
            CommentInfo comment = new CommentInfo();

            comment.mUsername = "Username " + i;
            comment.mUserId = (int) (Math.random() * NUM_DUMMY_USERS) + 1;
            comment.mBody = "Comment " + i;
            comment.mAge = (int) (Math.random() * 36);
            comment.mPoints = (int) (Math.random() * 300);
            comment.mPost = (int) (Math.random() * NUM_DUMMY_POSTS) + 1;
            comment.mParentId = (int) (Math.random() * NUM_DUMMY_COMMENTS);

            ContentValues cv = new ContentValues();
            cv.put(SiftContract.Comments.COLUMN_OWNER_USERNAME, comment.mUsername);
            cv.put(SiftContract.Comments.COLUMN_DATE_CREATED, comment.mAge);
            cv.put(SiftContract.Comments.COLUMN_BODY, comment.mBody);
            cv.put(SiftContract.Comments.COLUMN_OWNER_ID, comment.mUserId);
            cv.put(SiftContract.Comments.COLUMN_POINTS, comment.mPoints);
            cv.put(SiftContract.Comments.COLUMN_PARENT_ID, comment.mParentId);
            cv.put(SiftContract.Comments.COLUMN_POST_ID, comment.mPost);

            mContentResolver.insert(SiftContract.Comments.CONTENT_URI, cv);
        }
    }
    public void insertDummySubscriptions() {
        for (int i=1; i <= NUM_DUMMY_SUBSCRIPTIONS; i++) {
            SubscriptionInfo subscription = new SubscriptionInfo();

            subscription.mAccountId = (int) (Math.random() * NUM_DUMMY_ACCOUNTS) + 1;
            subscription.mSubredditId = (int) (Math.random() * NUM_DUMMY_SUBREDDITS) + 1;

            ContentValues cv = new ContentValues();
            cv.put(SiftContract.Subscriptions.COLUMN_ACCOUNT_ID, subscription.mAccountId);
            cv.put(SiftContract.Subscriptions.COLUMN_SUBREDDIT_ID, subscription.mSubredditId);

            mContentResolver.insert(SiftContract.Subscriptions.CONTENT_URI, cv);
        }
    }
    public void insertDummyFavorites() {
        for (int i=1; i <= NUM_DUMMY_FAVORITES; i++) {
            FavoritesInfo fav = new FavoritesInfo();

            fav.mAccountId = (int) (Math.random() * NUM_DUMMY_ACCOUNTS) + 1;
            fav.mPostId = (int) (Math.random() * NUM_DUMMY_POSTS) + 1;

            ContentValues cv = new ContentValues();
            cv.put(SiftContract.Favorites.COLUMN_ACCOUNT_ID, fav.mAccountId);
            cv.put(SiftContract.Favorites.COLUMN_POST_ID, fav.mPostId);

            mContentResolver.insert(SiftContract.Favorites.CONTENT_URI, cv);
        }
    }

    public void insertDummyVotes() {
        for (int i=1; i <= NUM_DUMMY_VOTES; i++) {
            VoteInfo vote = new VoteInfo();

            vote.mAccountId = (int) (Math.random() * NUM_DUMMY_ACCOUNTS) + 1;
            vote.mPostId = (int) (Math.random() * NUM_DUMMY_POSTS) + 1;
            if (i%2 == 0) {
                vote.mCommentId = (int) (Math.random() * NUM_DUMMY_COMMENTS) + 1;
            }
            vote.mVote = (int) (Math.random() * 2);

            ContentValues cv = new ContentValues();
            cv.put(SiftContract.Votes.COLUMN_ACCOUNT_ID, vote.mAccountId);
            cv.put(SiftContract.Votes.COLUMN_POST_ID, vote.mPostId);
            cv.put(SiftContract.Votes.COLUMN_COMMENT_ID, vote.mCommentId);
            cv.put(SiftContract.Votes.COLUMN_VOTE, vote.mVote);

            mContentResolver.insert(SiftContract.Votes.CONTENT_URI, cv);
        }
    }

    public void insertDummyFriends() {
        for (int i=1; i <= NUM_DUMMY_FRIENDS; i++) {
            FriendInfo friend = new FriendInfo();

            friend.mAccountId = (int) (Math.random() * NUM_DUMMY_ACCOUNTS) + 1;
            friend.mFriendId = (int) (Math.random() * NUM_DUMMY_USERS) + 1;

            ContentValues cv = new ContentValues();
            cv.put(SiftContract.Friends.COLUMN_ACCOUNT_ID, friend.mAccountId);
            cv.put(SiftContract.Friends.COLUMN_FRIEND_USER_ID, friend.mFriendId);

            mContentResolver.insert(SiftContract.Friends.CONTENT_URI, cv);
        }
    }
}