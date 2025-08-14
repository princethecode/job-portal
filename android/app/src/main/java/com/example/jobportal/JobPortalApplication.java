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
import com.example.jobportal.utils.AppUpdateManager;

public class JobPortalApplication extends MultiDexApplication {
    private static final String CHANNEL_ID = "job_portal_notifications";
    private static final String CHANNEL_NAME = "Job Portal Notifications";
    private static final String CHANNEL_DESCRIPTION = "Notifications for job updates and applications";
    private static final String PREFS_NAME = "JobPortalPrefs";
    private static final String PREF_THEME = "theme_mode";
    
    private AppUpdateManager appUpdateManager;

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
        
        // Initialize app update manager
        appUpdateManager = new AppUpdateManager(this);
        
        // Check for updates when app starts
        checkForUpdates();
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
    
    private void checkForUpdates() {
        // Check for updates with callback to handle force updates
        appUpdateManager.checkForUpdates(new com.example.jobportal.utils.AppUpdateManager.UpdateCallback() {
            @Override
            public void onUpdateRequired(boolean isForceUpdate) {
                if (isForceUpdate) {
                    // Handle force update - you can add additional logic here
                    // For example, disable certain app features until update
                    android.util.Log.d("JobPortalApp", "Force update required!");
                }
            }
        });
    }
    
    public AppUpdateManager getUpdateManager() {
        return appUpdateManager;
    }
} 