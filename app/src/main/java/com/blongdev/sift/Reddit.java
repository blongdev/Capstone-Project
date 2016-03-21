package com.blongdev.sift;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.UserAgent;

/**
 * Created by Brian on 3/21/2016.
 */
public class Reddit {
    private static Reddit ourInstance = new Reddit();
    public RedditClient mRedditClient;
    public UserAgent mUserAgent;


    public static Reddit getInstance() {
        return ourInstance;
    }

    private Reddit() {
        if (mRedditClient == null) {
            String versionName = BuildConfig.VERSION_NAME;
            UserAgent myUserAgent = UserAgent.of("Android", "com.blongdev.sift", versionName, "toothkey");
            mRedditClient = new RedditClient(myUserAgent);
        }
    }
}
