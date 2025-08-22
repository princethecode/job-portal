package com.example.jobportal.models;

import com.google.gson.annotations.SerializedName;

public class RecruiterLoginResponse {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("data")
    private RecruiterLoginData data;
    
    public static class RecruiterLoginData {
        @SerializedName("recruiter")
        private Recruiter recruiter;
        
        @SerializedName("token")
        private String token;
        
        @SerializedName("token_type")
        private String tokenType;
        
        // Getters
        public Recruiter getRecruiter() { return recruiter; }
        public String getToken() { return token; }
        public String getTokenType() { return tokenType; }
    }
    
    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public RecruiterLoginData getData() { return data; }
}
