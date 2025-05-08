package com.example.jobportal.models;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("user")
    private User user;

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("token_type")
    private String tokenType;

    public User getUser() {
        return user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }
} 