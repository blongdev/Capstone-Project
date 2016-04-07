package com.blongdev.sift;


import android.app.Application;
import android.content.Intent;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * This is a subclass of {@link Application} used to provide shared objects for this app, such as
 * the {@link Tracker}.
 */
public class SiftApplication extends Application {
    private Tracker mTracker;


    //PREVENT CRASHING AND INSTEAD RESTART APPLICATION ON UNHANDLED EXCEPTION
//    public void onCreate ()
//    {
//
//        // Setup handler for uncaught exceptions.
//        Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
//        {
//            @Override
//            public void uncaughtException (Thread thread, Throwable e)
//            {
//                handleUncaughtException (thread, e);
//            }
//        });
//    }
//
//    public void handleUncaughtException (Thread thread, Throwable e)
//    {
//        e.printStackTrace();
//
//        mTracker.send(new HitBuilders.ExceptionBuilder()
//                .setDescription("testError")
//                .build());
//
//        GoogleAnalytics.getInstance(this).dispatchLocalHits();
//
//        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//        intent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK); // required when starting from Application
//        startActivity (intent);
//
//        System.exit(1); // kill off the crashed app
//    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.tracker);
        }
        return mTracker;
    }
}