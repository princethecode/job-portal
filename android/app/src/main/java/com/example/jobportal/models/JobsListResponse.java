package com.example.jobportal.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class JobsListResponse {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("data")
    private JobsData data;
    
    public boolean isSuccess() {
        return success;
    }
    
    public JobsData getData() {
        return data;
    }
    
    public static class JobsData {
        @SerializedName("current_page")
        private int currentPage;
        
        @SerializedName("data")
        private List<com.example.jobportal.models.Job> jobs;
        
        @SerializedName("total")
        private int total;
        
        public int getCurrentPage() {
            return currentPage;
        }
        
        public List<Job> getJobs() {
            return jobs;
        }
        
        public int getTotal() {
            return total;
        }
    }
} 