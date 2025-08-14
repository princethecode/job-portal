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
                
                Log.d(TAG, "Featured jobs API response code: " + response.code());
                
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (response.body().isSuccess() && response.body().getData() != null) {
                            featuredJobs.setValue(response.body().getData());
                            Log.d(TAG, "Featured jobs fetched successfully: " + response.body().getData().size());
                        } else {
                            String errorMsg = response.body().getMessage() != null ? 
                                response.body().getMessage() : "No featured jobs available";
                            error.setValue(errorMsg);
                            Log.e(TAG, "API returned unsuccessful response: " + errorMsg);
                            // Set empty list to avoid null pointer exceptions
                            featuredJobs.setValue(new ArrayList<>());
                        }
                    } else {
                        error.setValue("Empty response from server");
                        Log.e(TAG, "Response body is null");
                        featuredJobs.setValue(new ArrayList<>());
                    }
                } else {
                    // Handle HTTP error responses
                    String errorMsg = "Server error: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error response body: " + errorBody);
                            errorMsg += " - " + errorBody;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    error.setValue(errorMsg);
                    Log.e(TAG, "HTTP error fetching featured jobs: " + errorMsg);
                    featuredJobs.setValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<FeaturedJob>>> call, Throwable t) {
                isLoading.setValue(false);
                String errorMessage = "Network error: " + t.getMessage();
                
                // Check for specific error types
                if (t instanceof com.google.gson.JsonSyntaxException) {
                    errorMessage = "Server returned invalid data format. Please try again later.";
                    Log.e(TAG, "JSON parsing error - server likely returned HTML or plain text instead of JSON", t);
                } else if (t instanceof java.net.UnknownHostException) {
                    errorMessage = "Unable to connect to server. Please check your internet connection.";
                } else if (t instanceof java.net.SocketTimeoutException) {
                    errorMessage = "Connection timeout. Please try again.";
                } else if (t instanceof java.net.ConnectException) {
                    errorMessage = "Unable to connect to server. Please try again later.";
                }
                
                error.setValue(errorMessage);
                Log.e(TAG, "Network error fetching featured jobs", t);
                featuredJobs.setValue(new ArrayList<>());
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
                
                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().isSuccess()) {
                        callback.onSuccess(response.body());
                    } else {
                        String errorMsg = response.body() != null && response.body().getMessage() != null ? 
                            response.body().getMessage() : "Job details not found";
                        callback.onError(errorMsg);
                    }
                } else {
                    String errorMsg = "Server error: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<FeaturedJob>> call, Throwable t) {
                isLoading.setValue(false);
                String errorMessage = "Network error: " + t.getMessage();
                
                if (t instanceof com.google.gson.JsonSyntaxException) {
                    errorMessage = "Server returned invalid data format";
                    Log.e(TAG, "JSON parsing error for job details", t);
                }
                
                callback.onError(errorMessage);
            }
        });
    }
    
    /**
     * Refresh featured jobs data
     */
    public void refreshFeaturedJobs() {
        fetchFeaturedJobs();
    }
    
    /**
     * Test the featured jobs API endpoint with raw response handling
     */
    public void testFeaturedJobsEndpoint() {
        Log.d(TAG, "Testing featured jobs endpoint...");
        
        // Make a raw HTTP call to see what the server actually returns
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(com.example.jobportal.BuildConfig.API_BASE_URL + "featured-jobs")
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .build();
        
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                Log.e(TAG, "Raw API test failed", e);
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                String responseBody = response.body() != null ? response.body().string() : "null";
                Log.d(TAG, "Raw API test response:");
                Log.d(TAG, "Status: " + response.code());
                Log.d(TAG, "Headers: " + response.headers());
                Log.d(TAG, "Body: " + responseBody);
                
                // Check if it's HTML (common error case)
                if (responseBody.trim().startsWith("<")) {
                    Log.e(TAG, "Server returned HTML instead of JSON - this is the source of the JsonSyntaxException");
                }
            }
        });
    }
}
