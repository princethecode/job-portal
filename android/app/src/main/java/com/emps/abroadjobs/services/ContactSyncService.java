package com.emps.abroadjobs.services;

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

import com.emps.abroadjobs.R;
import com.emps.abroadjobs.network.ContactsRepository;
import com.emps.abroadjobs.network.ApiCallback;

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
            startForeground(NOTIFICATION_ID, createNotification("Starting sync..."));
            syncContacts();
        }
        return START_STICKY;
    }

    private void syncContacts() {
        updateNotification("Reading sync...");
        contactsRepository.fetchAndUploadContacts(new ApiCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (result) {
                    Log.d(TAG, " sync completed successfully");
                    updateNotification(" sync completed successfully");
                } else {
                    Log.d(TAG, "No to sync");
                    updateNotification("No to sync");
                }
                stopSelf();
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "sync failed: " + errorMessage);
                updateNotification("sync failed: " + errorMessage);
                stopSelf();
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Sync Service",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Shows sync progress");
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