package com.example.jobportal.data.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.jobportal.network.ApiResponse;
import com.example.jobportal.models.FeaturedJob;
import com.example.jobportal.network.ApiCallback;
import com.example.jobportal.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for featured jobs that directly communicates with the API.
 * Following the pattern from ExperienceRepository that bypasses local database storage.
 */
public class FeaturedJobRepository {
    private static final String TAG = "FeaturedJobRepository";
    private final ApiClient apiClient;
    private final MutableLiveData<List<FeaturedJob>> featuredJobs = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);
    
    public FeaturedJobRepository(Application application) {
        apiClient = ApiClient.getInstance(application);
        // Initialize with empty list to avoid null checks
        featuredJobs.setValue(new ArrayList<>());
    }
    
    public LiveData<List<FeaturedJob>> getFeaturedJobs() {
        return featuredJobs;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getError() {
        return error;
    }
    
    /**
     * Fetches featured jobs from the API
     */
    public void fetchFeaturedJobs() {
        isLoading.setValue(true);
        error.setValue(null);
        
        apiClient.getApiService().getFeaturedJobs().enqueue(new Callback<ApiResponse<List<FeaturedJob>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<FeaturedJob>>> call, Response<ApiResponse<List<FeaturedJob>>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    featuredJobs.setValue(response.body().getData());
                    Log.d(TAG, "Featured jobs fetched successfully: " + response.body().getData().size());
                } else {
                    String errorMsg = response.isSuccessful() ? "No data received" : "Error: " + response.code();
                    error.setValue("Failed to load featured jobs. " + errorMsg);
                    Log.e(TAG, "Error fetching featured jobs: " + errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<FeaturedJob>>> call, Throwable t) {
                isLoading.setValue(false);
                error.setValue("Network error: " + t.getMessage());
                Log.e(TAG, "Network error fetching featured jobs", t);
            }
        });
    }
    
    /**
     * Get featured job by ID
     */
    public void getFeaturedJobDetails(int jobId, ApiCallback<ApiResponse<FeaturedJob>> callback) {
        isLoading.setValue(true);
        
        apiClient.getApiService().getFeaturedJobDetails(jobId).enqueue(new Callback<ApiResponse<FeaturedJob>>() {
            @Override
            public void onResponse(Call<ApiResponse<FeaturedJob>> call, Response<ApiResponse<FeaturedJob>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get job details");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<FeaturedJob>> call, Throwable t) {
                isLoading.setValue(false);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}
