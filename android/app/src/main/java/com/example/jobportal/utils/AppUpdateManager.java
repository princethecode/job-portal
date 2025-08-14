package com.example.jobportal.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.jobportal.data.model.AppVersionResponse;
import com.example.jobportal.network.ApiCallback;
import com.example.jobportal.network.ApiClient;
import com.example.jobportal.network.ApiResponse;
import com.example.jobportal.data.model.AppVersionData;
import com.example.jobportal.ui.update.UpdateActivity;

public class AppUpdateManager {
    private static final String TAG = "AppUpdateManager";
    private Context context;
    private ApiClient apiClient;

    public interface UpdateCallback {
        void onUpdateRequired(boolean isForceUpdate);
    }

    public AppUpdateManager(Context context) {
        this.context = context;
        this.apiClient = ApiClient.getInstance(context);
        
        // Log current app version for debugging
        try {
            int versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            String versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            Log.d(TAG, "App initialized with version: " + versionName + " (code: " + versionCode + ")");
        } catch (Exception e) {
            Log.e(TAG, "Error getting app version", e);
        }
    }

    public void checkForUpdates() {
        checkForUpdates(null);
    }

    public void checkForUpdates(UpdateCallback callback) {
        Log.d(TAG, "Starting update check...");
        
        apiClient.checkAppVersion(new ApiCallback<ApiResponse<AppVersionData>>() {
            @Override
            public void onSuccess(ApiResponse<AppVersionData> response) {
                Log.d(TAG, "Update check API response received");
                Log.d(TAG, "Response success: " + response.isSuccess());
                
                if (response.isSuccess() && response.getData() != null) {
                    AppVersionData versionData = response.getData();
                    Log.d(TAG, "Version data received: " + versionData);
                    
                    Log.d(TAG, "Update available: " + versionData.isUpdate_available());
                    Log.d(TAG, "Update required: " + versionData.isUpdate_required());
                    Log.d(TAG, "Force update: " + versionData.isForce_update());
                    
                    if (versionData.getLatest_version() != null) {
                        Log.d(TAG, "Latest version: " + versionData.getLatest_version().getName());
                        Log.d(TAG, "Latest version code: " + versionData.getLatest_version().getCode());
                    } else {
                        Log.w(TAG, "Latest version is null!");
                    }
                    
                    if (versionData.isUpdate_required()) {
                        Log.d(TAG, "Update required - showing dialog");
                        showUpdateDialog(versionData);
                        if (callback != null) {
                            callback.onUpdateRequired(versionData.isForce_update());
                        }
                    } else {
                        Log.d(TAG, "No update required");
                        if (callback != null) {
                            callback.onUpdateRequired(false);
                        }
                    }
                } else {
                    Log.d(TAG, "No update required - response not successful or no data");
                    if (callback != null) {
                        callback.onUpdateRequired(false);
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error checking for updates: " + errorMessage);
                if (callback != null) {
                    callback.onUpdateRequired(false);
                }
            }
        });
    }

    private void showUpdateDialog(AppVersionData versionData) {
        Log.d(TAG, "Preparing to show update dialog");
        
        try {
            Intent intent = new Intent(context, UpdateActivity.class);
            
            if (versionData.getLatest_version() != null) {
                Log.d(TAG, "Setting update dialog data:");
                Log.d(TAG, "- Update message: " + versionData.getLatest_version().getUpdate_message());
                Log.d(TAG, "- Download URL: " + versionData.getLatest_version().getDownload_url());
                Log.d(TAG, "- Force update: " + versionData.isForce_update());
                Log.d(TAG, "- Version name: " + versionData.getLatest_version().getName());
                
                intent.putExtra(UpdateActivity.EXTRA_UPDATE_MESSAGE, versionData.getLatest_version().getUpdate_message());
                intent.putExtra(UpdateActivity.EXTRA_DOWNLOAD_URL, versionData.getLatest_version().getDownload_url());
                intent.putExtra(UpdateActivity.EXTRA_FORCE_UPDATE, versionData.isForce_update());
                intent.putExtra(UpdateActivity.EXTRA_VERSION_NAME, versionData.getLatest_version().getName());
            } else {
                Log.w(TAG, "Latest version info is null!");
            }
            
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            
            Log.d(TAG, "Starting UpdateActivity");
            context.startActivity(intent);
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing update dialog: " + e.getMessage(), e);
        }
    }
}