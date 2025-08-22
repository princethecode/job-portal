package com.example.jobportal.models;

import com.google.gson.annotations.SerializedName;

public class Application {
    @SerializedName("id")
    private int id;
    
    @SerializedName("user_id")
    private int userId;
    
    @SerializedName("job_id")
    private int jobId;
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("cover_letter")
    private String coverLetter;
    
    @SerializedName("resume_path")
    private String resumePath;
    
    @SerializedName("posting_date")
    private String postingDate;
    
    @SerializedName("applied_date")
    private String appliedDate;
    
    @SerializedName("notes")
    private String notes;
    
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

    public Application(int id, String status, String coverLetter, String resumePath) {
        this.id = id;
        this.status = status;
        this.coverLetter = coverLetter;
        this.resumePath = resumePath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    // Backward compatibility method for String ID
    public void setId(String id) {
        try {
            this.id = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            this.id = 0; // Default value
        }
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }

    public String getResumePath() {
        return resumePath;
    }

    public void setResumePath(String resumePath) {
        this.resumePath = resumePath;
    }

    public String getPostingDate() {
        return postingDate;
    }

    public void setPostingDate(String postingDate) {
        this.postingDate = postingDate;
    }

    public String getAppliedDate() {
        return appliedDate;
    }

    public void setAppliedDate(String appliedDate) {
        this.appliedDate = appliedDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
    
    // Helper methods to get job information from nested job object
    public String getJobTitle() {
        return job != null ? job.getTitle() : "Unknown Job";
    }
    
    // Backward compatibility setter for job title
    public void setJobTitle(String jobTitle) {
        // For backward compatibility, create job object if it doesn't exist
        if (job == null) {
            job = new Job();
        }
        job.setTitle(jobTitle);
    }
    
    public String getCompany() {
        // Try to get company name from different possible fields in job
        if (job != null) {
            if (job.getCompanyName() != null && !job.getCompanyName().isEmpty()) {
                return job.getCompanyName();
            } else if (job.getCompany() != null && !job.getCompany().isEmpty()) {
                return job.getCompany();
            }
        }
        return "Unknown Company";
    }
    
    // Backward compatibility setter for company
    public void setCompany(String company) {
        // For backward compatibility, create job object if it doesn't exist
        if (job == null) {
            job = new Job();
        }
        job.setCompany(company);
    }
    
    public String getApplicationDate() {
        return appliedDate != null ? appliedDate : createdAt;
    }
    
    // Backward compatibility setter for application date
    public void setApplicationDate(String applicationDate) {
        this.appliedDate = applicationDate;
    }
    
    public String getResumeUrl() {
        return resumePath;
    }
    
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