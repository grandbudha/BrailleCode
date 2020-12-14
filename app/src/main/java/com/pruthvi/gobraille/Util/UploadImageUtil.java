package com.pruthvi.gobraille.Util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import com.pruthvi.gobraille.Constants;
import com.pruthvi.gobraille.Entity.Braille;

public class UploadImageUtil {

    private static final String GOOGLE_PHOTO_URI = "com.google.android.apps.photos.content";
    private static final String EXTERNAL_STORAGE_DOCUMENT = "com.android.externalstorage.documents";
    private static final String DOWNLOAD_STORAGE_DOCUMENT = "com.android.providers.downloads.documents";
    private static final String MEDIA_STORAGE_DOCUMENT = "com.android.providers.media.documents";

    private static String getImagePath(final Context context, final Uri uri) {

        if (DocumentsContract.isDocumentUri(context, uri)) {

            if (EXTERNAL_STORAGE_DOCUMENT.equals(uri.getAuthority())) {

                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/"
                            + split[1];
                }
            }
            else if (DOWNLOAD_STORAGE_DOCUMENT.equals(uri.getAuthority())) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));
                return getColumn(context, contentUri, null, null);
            }
            else if (MEDIA_STORAGE_DOCUMENT.equals(uri.getAuthority())) {

                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[] { split[1] };
                    return getColumn(context, contentUri, selection, selectionArgs);
                }
                else return Constants.NULL;
            }
        }
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            if (GOOGLE_PHOTO_URI.equals(uri.getAuthority())) return uri.getLastPathSegment();
            return getColumn(context, uri, (null), (null));
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return Constants.NULL;
    }

    private static String getColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return Constants.NULL;
    }

    public static boolean save(Context context, Uri uri, Braille braille){

        String path = getImagePath(context, uri);
        if (path.equals(Constants.NULL)) return false;
        else {
            braille.saveBraille(path);
            return true;
        }
    }

}
