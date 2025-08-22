package com.example.jobportal.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DashboardResponse {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("data")
    private DashboardData data;
    
    public static class DashboardData {
        @SerializedName("stats")
        private DashboardStats stats;
        
        @SerializedName("recent_jobs")
        private List<RecentJob> recentJobs;
        
        @SerializedName("monthly_trends")
        private List<MonthlyTrend> monthlyTrends;
        
        @SerializedName("recruiter")
        private RecruiterInfo recruiter;
        
        // Getters
        public DashboardStats getStats() { return stats; }
        public List<RecentJob> getRecentJobs() { return recentJobs; }
        public List<MonthlyTrend> getMonthlyTrends() { return monthlyTrends; }
        public RecruiterInfo getRecruiter() { return recruiter; }
    }
    
    public static class DashboardStats {
        @SerializedName("total_jobs")
        private int totalJobs;
        
        @SerializedName("active_jobs")
        private int activeJobs;
        
        @SerializedName("total_applications")
        private int totalApplications;
        
        @SerializedName("scheduled_interviews")
        private int scheduledInterviews;
        
        @SerializedName("pending_applications")
        private int pendingApplications;
        
        @SerializedName("hired_candidates")
        private int hiredCandidates;
        
        // Getters
        public int getTotalJobs() { return totalJobs; }
        public int getActiveJobs() { return activeJobs; }
        public int getTotalApplications() { return totalApplications; }
        public int getScheduledInterviews() { return scheduledInterviews; }
        public int getPendingApplications() { return pendingApplications; }
        public int getHiredCandidates() { return hiredCandidates; }
    }
    
    public static class RecentJob {
        @SerializedName("id")
        private int id;
        
        @SerializedName("title")
        private String title;
        
        @SerializedName("company_name")
        private String companyName;
        
        @SerializedName("location")
        private String location;
        
        @SerializedName("job_type")
        private String jobType;
        
        @SerializedName("salary")
        private String salary;
        
        @SerializedName("is_active")
        private boolean isActive;
        
        @SerializedName("applications_count")
        private int applicationsCount;
        
        @SerializedName("created_at")
        private String createdAt;
        
        @SerializedName("posted_date")
        private String postedDate;
        
        // Getters
        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getCompanyName() { return companyName; }
        public String getLocation() { return location; }
        public String getJobType() { return jobType; }
        public String getSalary() { return salary; }
        public boolean isActive() { return isActive; }
        public int getApplicationsCount() { return applicationsCount; }
        public String getCreatedAt() { return createdAt; }
        public String getPostedDate() { return postedDate; }
    }
    
    public static class MonthlyTrend {
        @SerializedName("month")
        private String month;
        
        @SerializedName("jobs")
        private int jobs;
        
        @SerializedName("applications")
        private int applications;
        
        @SerializedName("interviews")
        private int interviews;
        
        // Getters
        public String getMonth() { return month; }
        public int getJobs() { return jobs; }
        public int getApplications() { return applications; }
        public int getInterviews() { return interviews; }
    }
    
    public static class RecruiterInfo {
        @SerializedName("id")
        private int id;
        
        @SerializedName("name")
        private String name;
        
        @SerializedName("company_name")
        private String companyName;
        
        // Getters
        public int getId() { return id; }
        public String getName() { return name; }
        public String getCompanyName() { return companyName; }
    }
    
    // Main getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public DashboardData getData() { return data; }
}