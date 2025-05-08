package com.example.jobportal.models;

import com.google.gson.annotations.SerializedName;

public class Application {
    @SerializedName("id")
    private String id;
    
    @SerializedName("job_title")
    private String jobTitle;
    
    @SerializedName("company")
    private String company;
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("application_date")
    private String applicationDate;
    
    @SerializedName("cover_letter")
    private String coverLetter;
    
    @SerializedName("resume_url")
    private String resumeUrl;
    
    @SerializedName("created_at")
    private String createdAt;
    
    @SerializedName("updated_at")
    private String updatedAt;
    
    @SerializedName("job")
    private Job job;
    
    @SerializedName("user")
    private User user;

    public Application() {
    }

    public Application(String id, String jobTitle, String company, String status, 
                      String applicationDate, String coverLetter, String resumeUrl) {
        this.id = id;
        this.jobTitle = jobTitle;
        this.company = company;
        this.status = status;
        this.applicationDate = applicationDate;
        this.coverLetter = coverLetter;
        this.resumeUrl = resumeUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(String applicationDate) {
        this.applicationDate = applicationDate;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }

    public String getResumeUrl() {
        return resumeUrl;
    }

    public void setResumeUrl(String resumeUrl) {
        this.resumeUrl = resumeUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    // Helper methods
    public boolean isPending() {
        return "Applied".equalsIgnoreCase(status) || "Pending".equalsIgnoreCase(status);
    }
    
    public boolean isReviewing() {
        return "Under Review".equalsIgnoreCase(status) || "Reviewing".equalsIgnoreCase(status);
    }
    
    public boolean isShortlisted() {
        return "Shortlisted".equalsIgnoreCase(status);
    }
    
    public boolean isRejected() {
        return "Rejected".equalsIgnoreCase(status);
    }
    
    public boolean isAccepted() {
        return "Accepted".equalsIgnoreCase(status) || "Selected".equalsIgnoreCase(status);
    }
} 