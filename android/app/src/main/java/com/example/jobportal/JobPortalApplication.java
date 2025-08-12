package com.example.jobportal;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.example.jobportal.network.ApiClient;

public class JobPortalApplication extends MultiDexApplication {
    private static final String CHANNEL_ID = "job_portal_notifications";
    private static final String CHANNEL_NAME = "Job Portal Notifications";
    private static final String CHANNEL_DESCRIPTION = "Notifications for job updates and applications";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize API Client
        ApiClient.init(this);
        
        // Create notification channel for Android O and above
        createNotificationChannel();
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
} 