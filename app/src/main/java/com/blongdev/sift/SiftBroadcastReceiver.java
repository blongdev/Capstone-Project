package com.blongdev.sift;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

public class SiftBroadcastReceiver extends BroadcastReceiver {

    private final static String LOGGED_IN = "com.blongdev.sift.loggedIn";

    public SiftBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (TextUtils.equals(intent.getAction(), LOGGED_IN)) {
            Intent activity = new Intent(context, MainActivity.class);
            activity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activity);
        }
    }
}
