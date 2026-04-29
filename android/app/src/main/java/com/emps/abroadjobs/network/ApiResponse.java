package com.emps.abroadjobs.network;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private T data;

    @SerializedName("access_token")
    private String accessToken;
    
    @SerializedName("token_type")
    private String tokenType;
    
    // Additional fields for registration response
    private String extractedAccessToken;
    private String extractedTokenType;
    
    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public String getAccessToken() {
        return accessToken != null ? accessToken : extractedAccessToken;
    }

    public String getTokenType() {
        return tokenType != null ? tokenType : extractedTokenType;
    }
    
    public void setExtractedAccessToken(String extractedAccessToken) {
        this.extractedAccessToken = extractedAccessToken;
    }
    
    public void setExtractedTokenType(String extractedTokenType) {
        this.extractedTokenType = extractedTokenType;
    }
}

