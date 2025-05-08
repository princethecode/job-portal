package com.example.jobportal.ui.notifications;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.jobportal.models.Notification;
import com.example.jobportal.network.ApiClient;
import com.example.jobportal.network.ApiResponse;
import com.example.jobportal.network.ApiService;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsViewModel extends AndroidViewModel {
    private final MutableLiveData<List<Notification>> notifications = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final ApiService apiService;

    public NotificationsViewModel(Application application) {
        super(application);
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
        apiService.getNotifications().enqueue(new Callback<ApiResponse<List<Notification>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Notification>>> call,
                                 Response<ApiResponse<List<Notification>>> response) {
                loading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    notifications.setValue(response.body().getData());
                } else {
                    error.setValue("Failed to load notifications");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Notification>>> call, Throwable t) {
                loading.setValue(false);
                error.setValue(t.getMessage());
            }
        });
    }
} 