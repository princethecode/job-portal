package com.example.jobportal.ui.notifications;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.jobportal.models.Notification;
import com.example.jobportal.network.ApiClient;
import com.example.jobportal.network.ApiCallback;
import com.example.jobportal.network.ApiResponse;
import com.example.jobportal.network.ApiService;
import com.example.jobportal.utils.SessionManager;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsViewModel extends AndroidViewModel {
    private static final String TAG = "NotificationsViewModel";
    private final MutableLiveData<List<Notification>> notifications = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final ApiService apiService;
    private final SessionManager sessionManager;

    public NotificationsViewModel(Application application) {
        super(application);
        sessionManager = SessionManager.getInstance(application);
        
        // Ensure we have a fresh client with the current token
        if (sessionManager.hasToken()) {
            // Reset the API client to force a new instance with the current token
            ApiClient.resetApiClient();
        }
        
        apiService = ApiClient.getClient(application.getApplicationContext()).create(ApiService.class);
        loadNotifications();
    }

    public LiveData<List<Notification>> getNotifications() {
        return notifications;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public void loadNotifications() {
        loading.setValue(true);
        
        // Check if user is authenticated
        if (!sessionManager.isLoggedIn() || !sessionManager.hasToken()) {
            loading.setValue(false);
            error.setValue("User not authenticated. Please log in.");
            return;
        }
        
        Log.d(TAG, "Loading notifications with token: " + (sessionManager.hasToken() ? "Valid token" : "No token"));
        
        apiService.getNotifications().enqueue(new Callback<ApiResponse<List<Notification>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Notification>>> call,
                                 Response<ApiResponse<List<Notification>>> response) {
                loading.setValue(false);
                
                if (response.code() == 401) {
                    // Authentication error
                    Log.e(TAG, "Authentication error: 401 Unauthorized");
                    error.setValue("Session expired. Please log in again.");
                    // You might want to trigger a logout or token refresh here
                    sessionManager.clearToken();
                    return;
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    notifications.setValue(response.body().getData());
                    Log.d(TAG, "Notifications loaded successfully: " + 
                          (response.body().getData() != null ? response.body().getData().size() : 0) + " notifications");
                } else {
                    String errorMsg = "Failed to load notifications. Code: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Log.e(TAG, errorMsg);
                    error.setValue(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Notification>>> call, Throwable t) {
                loading.setValue(false);
                Log.e(TAG, "Network error when loading notifications", t);
                error.setValue("Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * Mark a notification as read
     * @param notificationId ID of the notification to mark as read
     */
    public void markNotificationAsRead(String notificationId) {
        if (notificationId == null || notificationId.isEmpty()) {
            return;
        }
        
        // Check if user is authenticated
        if (!sessionManager.isLoggedIn() || !sessionManager.hasToken()) {
            error.setValue("User not authenticated. Please log in.");
            return;
        }
        
        loading.setValue(true);
        
        // Create the API call
        Call<ApiResponse<Void>> call = apiService.markNotificationAsRead(notificationId);
        
        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                loading.setValue(false);
                
                if (response.code() == 401) {
                    // Authentication error
                    Log.e(TAG, "Authentication error: 401 Unauthorized");
                    error.setValue("Session expired. Please log in again.");
                    sessionManager.clearToken();
                    return;
                }
                
                if (response.isSuccessful()) {
                    // Update the local notification status
                    updateLocalNotificationStatus(notificationId);
                } else {
                    Log.e(TAG, "Failed to mark notification as read: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                loading.setValue(false);
                Log.e(TAG, "Network error when marking notification as read", t);
            }
        });
    }
    
    /**
     * Update local notification status as read
     * @param notificationId ID of the notification to update
     */
    private void updateLocalNotificationStatus(String notificationId) {
        List<Notification> currentNotifications = notifications.getValue();
        if (currentNotifications != null) {
            // Create a new list to avoid modifying the observed list directly
            List<Notification> updatedNotifications = new java.util.ArrayList<>(currentNotifications);
            
            // Find and replace the notification with a read version
            for (int i = 0; i < updatedNotifications.size(); i++) {
                Notification notification = updatedNotifications.get(i);
                if (notification.getId().equals(notificationId)) {
                    // Create new notification with read status
                    Notification updatedNotification = new Notification(
                        notification.getId(),
                        notification.getTitle(),
                        notification.getDescription(),
                        notification.getDate(),
                        true
                    );
                    updatedNotifications.set(i, updatedNotification);
                    break;
                }
            }
            
            // Post the updated list
            notifications.setValue(updatedNotifications);
        }
    }
} 