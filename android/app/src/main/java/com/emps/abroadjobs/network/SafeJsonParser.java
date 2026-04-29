package com.emps.abroadjobs.network;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.emps.abroadjobs.models.User;
import com.emps.abroadjobs.models.RegistrationResponse;
import java.util.Map;

/**
 * Safe JSON parser that avoids TypeToken issues with ProGuard
 */
public class SafeJsonParser {
    private static final String TAG = "SafeJsonParser";
    
    /**
     * Safely parse ApiResponse<User> without TypeToken
     */
    public static ApiResponse<User> parseUserResponse(String json) {
        try {
            Gson gson = new Gson();
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            
            boolean success = jsonObject.has("success") && jsonObject.get("success").getAsBoolean();
            String message = jsonObject.has("message") ? jsonObject.get("message").getAsString() : "";
            
            User user = null;
            if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull()) {
                user = gson.fromJson(jsonObject.get("data"), User.class);
            }
            
            return new ApiResponse<>(success, message, user);
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse user response: " + json, e);
            return new ApiResponse<>(false, "Failed to parse response", null);
        }
    }
    
    /**
     * Safely parse registration response with token
     */
    public static ApiResponse<RegistrationResponse> parseRegistrationResponse(String json) {
        try {
            Gson gson = new Gson();
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            
            boolean success = jsonObject.has("success") && jsonObject.get("success").getAsBoolean();
            String message = jsonObject.has("message") ? jsonObject.get("message").getAsString() : "";
            
            RegistrationResponse registrationResponse = null;
            if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull()) {
                registrationResponse = gson.fromJson(jsonObject.get("data"), RegistrationResponse.class);
            }
            
            return new ApiResponse<>(success, message, registrationResponse);
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse registration response: " + json, e);
            return new ApiResponse<>(false, "Failed to parse response", null);
        }
    }
    
    /**
     * Safely parse registration response and return as User response with token
     */
    public static ApiResponse<User> parseRegistrationResponseAsUser(String json) {
        try {
            Log.d(TAG, "Parsing registration response: " + json);
            
            Gson gson = new Gson();
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            
            boolean success = jsonObject.has("success") && jsonObject.get("success").getAsBoolean();
            String message = jsonObject.has("message") ? jsonObject.get("message").getAsString() : "";
            
            Log.d(TAG, "Response success: " + success + ", message: " + message);
            
            User user = null;
            String accessToken = null;
            String tokenType = null;
            
            if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull()) {
                JsonObject dataObject = jsonObject.getAsJsonObject("data");
                Log.d(TAG, "Data object: " + dataObject.toString());
                
                // Extract user object
                if (dataObject.has("user")) {
                    user = gson.fromJson(dataObject.get("user"), User.class);
                    Log.d(TAG, "User extracted: " + (user != null ? user.getId() : "null"));
                }
                
                // Extract token information
                if (dataObject.has("access_token")) {
                    accessToken = dataObject.get("access_token").getAsString();
                    Log.d(TAG, "Access token extracted: " + (accessToken != null ? "Yes" : "No"));
                }
                if (dataObject.has("token_type")) {
                    tokenType = dataObject.get("token_type").getAsString();
                    Log.d(TAG, "Token type extracted: " + tokenType);
                }
            }
            
            // Create ApiResponse with user data and set token fields
            ApiResponse<User> response = new ApiResponse<>(success, message, user);
            if (accessToken != null) {
                response.setExtractedAccessToken(accessToken);
                Log.d(TAG, "Token set in response");
            }
            if (tokenType != null) {
                response.setExtractedTokenType(tokenType);
            }
            
            return response;
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse registration response: " + json, e);
            return new ApiResponse<>(false, "Failed to parse response", null);
        }
    }
    
    /**
     * Safely parse ApiResponse<Void> without TypeToken
     */
    public static ApiResponse<Void> parseVoidResponse(String json) {
        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            
            boolean success = jsonObject.has("success") && jsonObject.get("success").getAsBoolean();
            String message = jsonObject.has("message") ? jsonObject.get("message").getAsString() : "";
            
            return new ApiResponse<>(success, message, null);
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse void response: " + json, e);
            return new ApiResponse<>(false, "Failed to parse response", null);
        }
    }
    
    /**
     * Safely parse ApiResponse<Map<String, Object>> without TypeToken
     */
    @SuppressWarnings("unchecked")
    public static ApiResponse<Map<String, Object>> parseMapResponse(String json) {
        try {
            Gson gson = new Gson();
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            
            boolean success = jsonObject.has("success") && jsonObject.get("success").getAsBoolean();
            String message = jsonObject.has("message") ? jsonObject.get("message").getAsString() : "";
            
            Map<String, Object> data = null;
            if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull()) {
                data = gson.fromJson(jsonObject.get("data"), Map.class);
            }
            
            return new ApiResponse<>(success, message, data);
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse map response: " + json, e);
            return new ApiResponse<>(false, "Failed to parse response", null);
        }
    }
    
    /**
     * Check if response indicates success without full parsing
     */
    public static boolean isSuccessResponse(String json) {
        try {
            return json.contains("\"success\"") && json.contains("true");
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Extract error message from response
     */
    public static String extractErrorMessage(String json) {
        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            if (jsonObject.has("message")) {
                return jsonObject.get("message").getAsString();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to extract error message", e);
        }
        return "Unknown error";
    }
}