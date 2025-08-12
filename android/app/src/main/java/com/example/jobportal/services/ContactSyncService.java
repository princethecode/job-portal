package com.example.jobportal.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.jobportal.R;
import com.example.jobportal.network.ContactsRepository;
import com.example.jobportal.network.ApiCallback;

public class ContactSyncService extends Service {
    private static final String TAG = "ContactSyncService";
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "ContactSyncChannel";
    
    private ContactsRepository contactsRepository;
    private boolean isRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        contactsRepository = new ContactsRepository(getApplicationContext());
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            isRunning = true;
            startForeground(NOTIFICATION_ID, createNotification("Starting contact sync..."));
            syncContacts();
        }
        return START_STICKY;
    }

    private void syncContacts() {
        updateNotification("Reading contacts...");
        contactsRepository.fetchAndUploadContacts(new ApiCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (result) {
                    Log.d(TAG, "Contact sync completed successfully");
                    updateNotification("Contact sync completed successfully");
                } else {
                    Log.d(TAG, "No contacts to sync");
                    updateNotification("No contacts to sync");
                }
                stopSelf();
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Contact sync failed: " + errorMessage);
                updateNotification("Contact sync failed: " + errorMessage);
                stopSelf();
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Contact Sync Service",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Shows contact sync progress");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification(String message) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Job Portal")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_sync)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build();
    }

    private void updateNotification(String message) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, createNotification(message));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        // Remove the notification when service is destroyed
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);
    }
} 