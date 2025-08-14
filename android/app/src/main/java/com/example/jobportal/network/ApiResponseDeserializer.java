package com.example.jobportal.network;

import android.util.Log;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

/**
 * Custom deserializer for ApiResponse that can handle both successful JSON responses
 * and error responses that might be plain text or HTML
 */
public class ApiResponseDeserializer implements JsonDeserializer<ApiResponse> {
    private static final String TAG = "ApiResponseDeserializer";

    @Override
    public ApiResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) 
            throws JsonParseException {
        
        try {
            // If it's a JSON object, parse normally
            if (json.isJsonObject()) {
                JsonObject jsonObject = json.getAsJsonObject();
                
                boolean success = false;
                String message = "";
                Object data = null;
                
                // Parse success field
                if (jsonObject.has("success")) {
                    success = jsonObject.get("success").getAsBoolean();
                }
                
                // Parse message field
                if (jsonObject.has("message")) {
                    message = jsonObject.get("message").getAsString();
                }
                
                // Parse data field
                if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull()) {
                    JsonElement dataElement = jsonObject.get("data");
                    
                    // Try to determine the type of data and deserialize accordingly
                    if (dataElement.isJsonArray()) {
                        data = context.deserialize(dataElement, new TypeToken<java.util.List<Object>>(){}.getType());
                    } else if (dataElement.isJsonObject()) {
                        data = context.deserialize(dataElement, Object.class);
                    } else {
                        data = dataElement.getAsString();
                    }
                }
                
                return new ApiResponse<>(success, message, data);
            }
            // If it's a string (error response), create error ApiResponse
            else if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
                String errorMessage = json.getAsString();
                Log.w(TAG, "Received string response instead of JSON object: " + errorMessage);
                return new ApiResponse<>(false, "Server error: " + errorMessage, null);
            }
            // If it's something else, create generic error
            else {
                Log.w(TAG, "Received unexpected JSON format: " + json.toString());
                return new ApiResponse<>(false, "Unexpected response format", null);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error deserializing ApiResponse", e);
            return new ApiResponse<>(false, "Error parsing server response: " + e.getMessage(), null);
        }
    }
}