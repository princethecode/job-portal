package com.example.jobportal.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileUtils {
    private static final String TAG = "FileUtils";

    /**
     * Get the file name from a URI
     * @param context The context
     * @param uri The URI of the file
     * @return The file name
     */
    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting file name from URI: " + e.getMessage());
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    /**
     * Get a file from a URI
     * @param context The context
     * @param uri The URI of the file
     * @return The file
     */
    public static File getFileFromUri(Context context, Uri uri) {
        try {
            String fileName = getFileName(context, uri);
            File file = new File(context.getCacheDir(), fileName);
            
            ContentResolver contentResolver = context.getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            outputStream.close();
            inputStream.close();
            
            return file;
        } catch (Exception e) {
            Log.e(TAG, "Error getting file from URI: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get the file size from a URI
     * @param context The context
     * @param uri The URI of the file
     * @return The file size in bytes
     */
    public static long getFileSize(Context context, Uri uri) {
        long size = 0;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    if (sizeIndex != -1) {
                        size = cursor.getLong(sizeIndex);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting file size from URI: " + e.getMessage());
            }
        }
        return size;
    }
}
