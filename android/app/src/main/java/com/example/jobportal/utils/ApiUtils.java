package com.example.jobportal.utils;

import android.util.Log;
import com.example.jobportal.network.ApiResponse;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Utility class for API-related operations
 */
public class ApiUtils {
    private static final String TAG = "ApiUtils";
    
    /**
     * Safely parse a JSON response string into an ApiResponse object
     * @param jsonString The JSON string to parse
     * @param dataClass The class type for the data field
     * @return ApiResponse object or null if parsing fails
     */
    public static <T> ApiResponse<T> safeParseApiResponse(String jsonString, Class<T> dataClass) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            Log.e(TAG, "Empty or null JSON string");
            return new ApiResponse<>(false, "Empty response from server", null);
        }
        
        String trimmed = jsonString.trim();
        
        // Check if it's HTML (common error case)
        if (trimmed.startsWith("<")) {
            Log.e(TAG, "Server returned HTML instead of JSON");
            return new ApiResponse<>(false, "Server error - please try again later", null);
        }
        
        // Check if it's a plain text error message
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
            Log.e(TAG, "Server returned plain text: " + trimmed);
            return new ApiResponse<>(false, trimmed, null);
        }
        
        try {
            Gson gson = new Gson();
            return gson.fromJson(jsonString, ApiResponse.class);
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "JSON parsing failed for: " + jsonString, e);
            return new ApiResponse<>(false, "Invalid response format from server", null);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error parsing JSON", e);
            return new ApiResponse<>(false, "Error processing server response", null);
        }
    }
    
    /**
     * Check if a string is valid JSON
     * @param jsonString The string to check
     * @return true if valid JSON, false otherwise
     */
    public static boolean isValidJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = jsonString.trim();
        return (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
               (trimmed.startsWith("[") && trimmed.endsWith("]"));
    }
    
    /**
     * Create an error ApiResponse for network failures
     * @param throwable The error that occurred
     * @return ApiResponse with error details
     */
    public static <T> ApiResponse<T> createErrorResponse(Throwable throwable) {
        String errorMessage = "Network error";
        
        if (throwable instanceof JsonSyntaxException) {
            errorMessage = "Server returned invalid data format";
        } else if (throwable instanceof java.net.UnknownHostException) {
            errorMessage = "Unable to connect to server. Please check your internet connection.";
        } else if (throwable instanceof java.net.SocketTimeoutException) {
            errorMessage = "Connection timeout. Please try again.";
        } else if (throwable instanceof java.net.ConnectException) {
            errorMessage = "Unable to connect to server. Please try again later.";
        } else if (throwable.getMessage() != null) {
            errorMessage = throwable.getMessage();
        }
        
        return new ApiResponse<>(false, errorMessage, null);
    }
}