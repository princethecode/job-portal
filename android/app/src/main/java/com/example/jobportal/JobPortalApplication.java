package com.example.jobportal;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.example.jobportal.network.ApiClient;

public class JobPortalApplication extends MultiDexApplication {
    private static final String CHANNEL_ID = "job_portal_notifications";
    private static final String CHANNEL_NAME = "Job Portal Notifications";
    private static final String CHANNEL_DESCRIPTION = "Notifications for job updates and applications";
    private static final String PREFS_NAME = "JobPortalPrefs";
    private static final String PREF_THEME = "theme_mode";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize theme
        initializeTheme();
        
        // Initialize API Client
        ApiClient.init(this);
        
        // Create notification channel for Android O and above
        createNotificationChannel();
    }
    
    private void initializeTheme() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int themeMode = prefs.getInt(PREF_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(themeMode);
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