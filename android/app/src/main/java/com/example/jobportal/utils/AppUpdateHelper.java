package com.example.jobportal.utils;

import android.app.Activity;
import android.content.Context;
import com.example.jobportal.JobPortalApplication;

public class AppUpdateHelper {
    
    public static void checkForAppUpdates(Activity activity) {
        JobPortalApplication app = (JobPortalApplication) activity.getApplication();
        app.getUpdateManager().checkForUpdates();
    }
    
    public static AppUpdateManager getAppUpdateManager(Context context) {
        JobPortalApplication app = (JobPortalApplication) context.getApplicationContext();
        return app.getUpdateManager();
    }
    
    public static void checkForAppUpdatesWithCallback(Activity activity, AppUpdateManager.UpdateCallback callback) {
        JobPortalApplication app = (JobPortalApplication) activity.getApplication();
        app.getUpdateManager().checkForUpdates(callback);
    }
}