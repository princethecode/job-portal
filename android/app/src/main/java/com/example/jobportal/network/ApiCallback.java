package com.example.jobportal.network;

/**
 * Generic callback interface for API responses
 * @param <T> The type of response expected
 */
public interface ApiCallback<T> {
    /**
     * Called when the API call is successful
     * @param response The response data
     */
    void onSuccess(T response);
    
    /**
     * Called when the API call fails
     * @param errorMessage Error message
     */
    void onError(String errorMessage);
}
