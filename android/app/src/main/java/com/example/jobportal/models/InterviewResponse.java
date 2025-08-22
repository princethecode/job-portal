package com.example.jobportal.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class InterviewResponse {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("data")
    private InterviewData data;
    
    public static class InterviewData {
        @SerializedName("interviews")
        private List<Interview> interviews;
        
        @SerializedName("pagination")
        private Pagination pagination;
        
        // Getters
        public List<Interview> getInterviews() { return interviews; }
        public Pagination getPagination() { return pagination; }
    }
    
    public static class Interview {
        @SerializedName("id")
        private int id;
        
        @SerializedName("application_id")
        private int applicationId;
        
        @SerializedName("recruiter_id")
        private int recruiterId;
        
        @SerializedName("user_id")
        private int userId;
        
        @SerializedName("job_id")
        private int jobId;
        
        @SerializedName("interview_date")
        private String interviewDate;
        
        @SerializedName("interview_time")
        private String interviewTime;
        
        @SerializedName("interview_type")
        private String interviewType;
        
        @SerializedName("meeting_link")
        private String meetingLink;
        
        @SerializedName("location")
        private String location;
        
        @SerializedName("status")
        private String status;
        
        @SerializedName("notes")
        private String notes;
        
        @SerializedName("feedback")
        private String feedback;
        
        @SerializedName("rating")
        private Integer rating;
        
        @SerializedName("created_at")
        private String createdAt;
        
        @SerializedName("updated_at")
        private String updatedAt;
        
        @SerializedName("user")
        private User user;
        
        @SerializedName("job")
        private Job job;
        
        @SerializedName("application")
        private Application application;
        
        // Getters
        public int getId() { return id; }
        public int getApplicationId() { return applicationId; }
        public int getRecruiterId() { return recruiterId; }
        public int getUserId() { return userId; }
        public int getJobId() { return jobId; }
        public String getInterviewDate() { return interviewDate; }
        public String getInterviewTime() { return interviewTime; }
        public String getInterviewType() { return interviewType; }
        public String getMeetingLink() { return meetingLink; }
        public String getLocation() { return location; }
        public String getStatus() { return status; }
        public String getNotes() { return notes; }
        public String getFeedback() { return feedback; }
        public Integer getRating() { return rating; }
        public String getCreatedAt() { return createdAt; }
        public String getUpdatedAt() { return updatedAt; }
        public User getUser() { return user; }
        public Job getJob() { return job; }
        public Application getApplication() { return application; }
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
    public InterviewData getData() { return data; }
}