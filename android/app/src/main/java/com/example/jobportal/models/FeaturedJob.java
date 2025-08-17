package com.example.jobportal.models;

import com.google.gson.annotations.SerializedName;

public class FeaturedJob {
    private int id;
    
    @SerializedName("company_logo")
    private String companyLogo;
    
    @SerializedName("job_title")
    private String jobTitle;
    
    @SerializedName("company_name")
    private String companyName;
    
    private String location;
    private String salary;
    
    @SerializedName("job_type")
    private String jobType;
    
    private String description;
    
    @SerializedName("is_active")
    private boolean isActive;
    
    @SerializedName("posted_date")
    private String postedDate; // Or use a Date object if parsing is handled
    
    @SerializedName("created_at")
    private String createdAt; // Or use a Date object
    
    @SerializedName("updated_at")
    private String updatedAt; // Or use a Date object

    @SerializedName("share_count")
    private int shareCount = 0;

    // Add getters and setters for all fields
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCompanyLogo() {
        return companyLogo;
    }

    public void setCompanyLogo(String companyLogo) {
        this.companyLogo = companyLogo;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getPostedDate() {
        return postedDate;
    }

    public void setPostedDate(String postedDate) {
        this.postedDate = postedDate;
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
}
