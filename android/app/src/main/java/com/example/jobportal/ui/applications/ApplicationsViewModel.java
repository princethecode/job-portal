package com.example.jobportal.ui.applications;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.jobportal.network.ApiClient;
import com.example.jobportal.network.ApiResponse;
import com.example.jobportal.network.ApiService;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApplicationsViewModel extends AndroidViewModel {
    private final MutableLiveData<List<com.example.jobportal.models.Application>> applications = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final ApiService apiService;

    public ApplicationsViewModel(Application application) {
        super(application);
        apiService = ApiClient.getClient(application.getApplicationContext()).create(ApiService.class);
        loadApplications();
    }

    public LiveData<List<com.example.jobportal.models.Application>> getApplications() {
        return applications;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public void loadApplications() {
        loading.setValue(true);
        apiService.getUserApplications().enqueue(new Callback<ApiResponse<List<com.example.jobportal.models.Application>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<com.example.jobportal.models.Application>>> call,
                                 Response<ApiResponse<List<com.example.jobportal.models.Application>>> response) {
                loading.setValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    applications.setValue(response.body().getData());
                } else {
                    error.setValue("Failed to load applications");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<com.example.jobportal.models.Application>>> call, Throwable t) {
                loading.setValue(false);
                error.setValue(t.getMessage());
            }
        });
    }
}