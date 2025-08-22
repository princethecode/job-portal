package com.example.jobportal.models;

import com.google.gson.annotations.SerializedName;

public class RecruiterProfileResponse {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("data")
    private ProfileData data;
    
    public static class ProfileData {
        @SerializedName("recruiter")
        private Recruiter recruiter;
        
        // Getter
        public Recruiter getRecruiter() { return recruiter; }
    }
    
    // Main getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public ProfileData getData() { return data; }
    
    // Convenience method to get recruiter directly
    public Recruiter getRecruiter() {
        return data != null ? data.getRecruiter() : null;
    }
}