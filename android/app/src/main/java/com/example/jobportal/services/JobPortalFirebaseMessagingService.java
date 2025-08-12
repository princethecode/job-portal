package com.example.jobportal.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.jobportal.MainActivity;
import com.example.jobportal.R;
import com.example.jobportal.network.ApiClient;
import com.example.jobportal.network.ApiService;
import com.example.jobportal.network.ApiResponse;
import com.example.jobportal.utils.SessionManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import okhttp3.ResponseBody;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class JobPortalFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FirebaseMsgService";
    private static final String CHANNEL_ID = "job_portal_notifications";
    private static final String CHANNEL_NAME = "Job Portal Notifications";
    private static final String CHANNEL_DESCRIPTION = "Notifications for job updates and applications";
    private SessionManager sessionManager;

    @Override
    public void onCreate() {
        super.onCreate();
        sessionManager = SessionManager.getInstance(getApplicationContext());
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);
        // Store the token instead of sending it immediately
        if (token != null && !token.isEmpty()) {
            Log.d(TAG, "Token is valid, storing for later use");
            sessionManager.saveFcmToken(token);
        } else {
            Log.e(TAG, "Received null or empty token");
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            handleDataMessage(remoteMessage.getData());
        }

        // Check if message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            sendNotification(
                remoteMessage.getNotification().getTitle(),
                remoteMessage.getNotification().getBody(),
                remoteMessage.getData()
            );
        }
    }

    private void handleDataMessage(Map<String, String> data) {
        // Handle different types of notifications based on data
        String type = data.get("type");
        if (type != null) {
            switch (type) {
                case "job_application":
                    // Handle job application notification
                    break;
                case "job_update":
                    // Handle job update notification
                    break;
                case "message":
                    // Handle message notification
                    break;
            }
        }
    }

    private void sendNotification(String title, String messageBody, Map<String, String> data) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        // Add data to intent if needed
        if (data != null) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
        }
        
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        // Create notification channel for Android O and above
        createNotificationChannel();

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendRegistrationToServer(String token) {
        Log.d(TAG, "Attempting to register FCM token: " + token);
        
        // Get API service
        ApiService apiService = ApiClient.getApiService();
        if (apiService == null) {
            Log.e(TAG, "ApiService is null");
            return;
        }
        
        // Create request data
        Map<String, String> tokenData = new HashMap<>();
        tokenData.put("fcm_token", token);
        
        Log.d(TAG, "Sending token registration request to server with data: " + tokenData);
        
        // Make the API call
        apiService.registerFcmToken(tokenData).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        Log.d(TAG, "FCM token registration response: " + responseBody);
                        
                        // Try to parse as JSON first
                        try {
                            Gson gson = new Gson();
                            ApiResponse<Void> apiResponse = gson.fromJson(responseBody, 
                                new TypeToken<ApiResponse<Void>>(){}.getType());
                            if (apiResponse != null && apiResponse.isSuccess()) {
                                Log.d(TAG, "FCM token registered successfully");
                            } else {
                                Log.e(TAG, "Failed to register FCM token: " + 
                                    (apiResponse != null ? apiResponse.getMessage() : "Unknown error"));
                            }
                        } catch (JsonSyntaxException e) {
                            // If JSON parsing fails, treat as a simple success message
                            Log.d(TAG, "FCM token registered successfully (string response)");
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading response body", e);
                    }
                } else {
                    Log.e(TAG, "Failed to register FCM token. Response code: " + response.code());
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                            // Try to parse error response
                            try {
                                ApiResponse<?> errorResponse = new Gson().fromJson(errorBody, 
                                    new TypeToken<ApiResponse<?>>(){}.getType());
                                if (errorResponse != null) {
                                    Log.e(TAG, "Error message from server: " + errorResponse.getMessage());
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to parse error response", e);
                            }
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Error registering FCM token", t);
                Log.e(TAG, "Error message: " + t.getMessage());
                if (t.getCause() != null) {
                    Log.e(TAG, "Cause: " + t.getCause().getMessage());
                }
            }
        });
    }
} 