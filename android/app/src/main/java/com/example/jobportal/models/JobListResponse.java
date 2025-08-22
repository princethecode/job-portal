package com.example.jobportal.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class JobListResponse {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("data")
    private JobData data;
    
    public static class JobData {
        @SerializedName("jobs")
        private List<Job> jobs;
        
        @SerializedName("pagination")
        private Pagination pagination;
        
        // Getters
        public List<Job> getJobs() { return jobs; }
        public Pagination getPagination() { return pagination; }
    }
    
    public static class Pagination {
        @SerializedName("current_page")
        private int currentPage;
        
        @SerializedName("last_page")
        private int lastPage;
        
        @SerializedName("per_page")
        private int perPage;
        
        @SerializedName("total")
        private int total;
        
        // Getters
        public int getCurrentPage() { return currentPage; }
        public int getLastPage() { return lastPage; }
        public int getPerPage() { return perPage; }
        public int getTotal() { return total; }
    }
    
    // Main getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public JobData getData() { return data; }
}