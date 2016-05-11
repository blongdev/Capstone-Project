package com.blongdev.sift;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

public class SiftBroadcastReceiver extends BroadcastReceiver {

    public final static String LOGGED_IN = "com.blongdev.sift.loggedIn";

    public SiftBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (TextUtils.equals(intent.getAction(), LOGGED_IN)) {
            Toast.makeText(context, context.getString(R.string.logged_in), Toast.LENGTH_LONG).show();
            Intent activity = new Intent(context, MainActivity.class);
            activity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activity);
        }
    }
}
