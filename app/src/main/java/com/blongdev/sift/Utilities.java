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
}
