package com.blongdev.sift;

import android.content.ContentValues;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.webkit.MimeTypeMap;

/**
 * Created by Brian on 3/29/2016.
 */
public class Utilities {

    private static final int SECONDS_IN_MINUTE = 60;
    private static final int SECONDS_IN_HOUR = SECONDS_IN_MINUTE * 60;
    private static final int SECONDS_IN_DAY = SECONDS_IN_HOUR * 24;
    private static final int SECONDS_IN_MONTH_ISH = SECONDS_IN_DAY * 30;
    private static final int SECONDS_IN_YEAR_ISH = SECONDS_IN_DAY * 365;

    public static boolean connectedToNetwork(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public static void insertOrUpdate(Context context, Uri uri, ContentValues cv, String where, String[] selectionArgs) {
        int count = context.getContentResolver().update(uri, cv, where, selectionArgs);
        if (count <= 0) {
            context.getContentResolver().insert(uri, cv);
        }
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }


    public static long getAgeInSeconds(long date) {
        long currentTime = System.currentTimeMillis();
        return (currentTime - date)/(1000);
    }

    public static String getAgeString(long date) {
        long age = getAgeInSeconds(date);
        if (age < SECONDS_IN_HOUR) {
            return age/SECONDS_IN_MINUTE + "m";
        } else if (age < SECONDS_IN_DAY) {
            return age/SECONDS_IN_HOUR + "h";
        } else if (age < SECONDS_IN_MONTH_ISH){
            return age/SECONDS_IN_DAY + "d";
        } else if (age < SECONDS_IN_YEAR_ISH) {
            return age/SECONDS_IN_MONTH_ISH + "M";
        } else {
            return age/SECONDS_IN_YEAR_ISH + "Y";
        }
    }
}
