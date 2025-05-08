package com.example.jobportal.utils;

import android.content.Context;
import android.widget.Toast;

import com.example.jobportal.R;

import java.net.UnknownHostException;

public class ErrorHandler {
    public static void handleError(Context context, Throwable error) {
        String errorMessage;
        
        if (error instanceof UnknownHostException) {
            errorMessage = context.getString(R.string.error_no_internet);
        } else if (error instanceof java.net.SocketTimeoutException) {
            errorMessage = context.getString(R.string.error_timeout);
        } else if (error instanceof retrofit2.HttpException) {
            retrofit2.HttpException httpException = (retrofit2.HttpException) error;
            switch (httpException.code()) {
                case 401:
                    errorMessage = context.getString(R.string.error_unauthorized);
                    break;
                case 403:
                    errorMessage = context.getString(R.string.error_forbidden);
                    break;
                case 404:
                    errorMessage = context.getString(R.string.error_not_found);
                    break;
                case 500:
                    errorMessage = context.getString(R.string.error_server);
                    break;
                default:
                    errorMessage = context.getString(R.string.error_unknown);
            }
        } else {
            errorMessage = context.getString(R.string.error_unknown);
        }
        
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
    }

    public static void handleError(Context context, String errorMessage) {
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
    }
} 