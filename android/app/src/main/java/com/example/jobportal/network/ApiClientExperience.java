package com.example.jobportal.network;

import android.util.Log;

import com.example.jobportal.models.Experience;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * API Client methods for Experience management
 */
public class ApiClientExperience {

    private static final String TAG = "ApiClientExperience";
    private ApiService apiService;
    private static ApiClientExperience instance;

    private ApiClientExperience() {
        apiService = ApiClient.getApiService();
    }

    public static synchronized ApiClientExperience getInstance() {
        if (instance == null) {
            instance = new ApiClientExperience();
        }
        return instance;
    }

    /**
     * Get all experiences for the current user
     * @param callback Callback to handle the response
     */
    public void getUserExperiences(final ApiCallback<List<Experience>> callback) {
        Call<ApiResponse<List<Experience>>> call = apiService.getUserExperiences();
        
        call.enqueue(new Callback<ApiResponse<List<Experience>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Experience>>> call, Response<ApiResponse<List<Experience>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Experience>> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Failed to fetch experiences: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Experience>>> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage(), t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Get details of a specific experience
     * @param experienceId ID of the experience to fetch
     * @param callback Callback to handle the response
     */
    public void getExperienceDetails(long experienceId, final ApiCallback<Experience> callback) {
        Call<ApiResponse<Experience>> call = apiService.getExperienceDetails(experienceId);
        
        call.enqueue(new Callback<ApiResponse<Experience>>() {
            @Override
            public void onResponse(Call<ApiResponse<Experience>> call, Response<ApiResponse<Experience>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Experience> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Failed to fetch experience details: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Experience>> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage(), t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Add a new experience
     * @param experience Experience object to add
     * @param callback Callback to handle the response
     */
    public void addExperience(Experience experience, final ApiCallback<Experience> callback) {
        Call<ApiResponse<Experience>> call = apiService.addExperience(experience);
        
        call.enqueue(new Callback<ApiResponse<Experience>>() {
            @Override
            public void onResponse(Call<ApiResponse<Experience>> call, Response<ApiResponse<Experience>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Experience> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Failed to add experience: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Experience>> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage(), t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Update an existing experience
     * @param experienceId ID of the experience to update
     * @param experience Updated Experience object
     * @param callback Callback to handle the response
     */
    public void updateExperience(long experienceId, Experience experience, final ApiCallback<Experience> callback) {
        Call<ApiResponse<Experience>> call = apiService.updateExperience(experienceId, experience);
        
        call.enqueue(new Callback<ApiResponse<Experience>>() {
            @Override
            public void onResponse(Call<ApiResponse<Experience>> call, Response<ApiResponse<Experience>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Experience> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Failed to update experience: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Experience>> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage(), t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Delete an experience
     * @param experienceId ID of the experience to delete
     * @param callback Callback to handle the response
     */
    public void deleteExperience(long experienceId, final ApiCallback<Void> callback) {
        Call<ApiResponse<Void>> call = apiService.deleteExperience(experienceId);
        
        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Void> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Failed to delete experience: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage(), t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}
