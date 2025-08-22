package com.example.jobportal.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.annotation.NonNull;
import androidx.room.Ignore;
import com.google.gson.annotations.SerializedName;
import java.util.Date;

@Entity(tableName = "jobs")
public class Job {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    @SerializedName("id")
    private String id;
    
    @ColumnInfo(name = "title")
    @SerializedName("title")
    private String title;
    
    @ColumnInfo(name = "description")
    @SerializedName("description")
    private String description;
    
    @ColumnInfo(name = "company")
    @SerializedName("company")
    private String company;
    
    @ColumnInfo(name = "location")
    @SerializedName("location")
    private String location;
    
    @ColumnInfo(name = "job_type")
    @SerializedName("job_type")
    private String jobType;
    
    @ColumnInfo(name = "category")
    @SerializedName("category")
    private String category;
    
    @ColumnInfo(name = "salary")
    @SerializedName("salary")
    private String salary;
    
    @ColumnInfo(name = "posting_date")
    @SerializedName("posting_date")
    private String postingDate;
    
    @ColumnInfo(name = "expiry_date")
    @SerializedName("expiry_date")
    private String expiryDate;
    
    @ColumnInfo(name = "is_active")
    @SerializedName("is_active")
    private boolean isActive;
    
    @ColumnInfo(name = "created_at")
    @SerializedName("created_at")
    private String createdAt;
    
    @ColumnInfo(name = "updated_at")
    @SerializedName("updated_at")
    private String updatedAt;
    
    @ColumnInfo(name = "company_logo")
    @SerializedName("company_logo")
    private String companyLogo;

    @ColumnInfo(name = "image")
    @SerializedName("image")
    private String image;

    @ColumnInfo(name = "share_count")
    @SerializedName("share_count")
    private int shareCount = 0;

    @ColumnInfo(name = "applications_count")
    @SerializedName("applications_count")
    private int applicationsCount = 0;

    @ColumnInfo(name = "status")
    @SerializedName("status")
    private String status = "active";
    
    @ColumnInfo(name = "posted_date")
    @SerializedName("posted_date")
    private String postedDate;
    
    @ColumnInfo(name = "company_name")
    @SerializedName("company_name")
    private String companyName;

    
    // Default constructor required by Room
    public Job() {
    }    

    @Ignore
    public Job(String id, String title, String company, String location, String description,
               String salary, String jobType, String postedDate, String expiryDate) {
        this.id = id;
        this.title = title;
        this.company = company;
        this.location = location;
        this.description = description;
        this.salary = salary;
        this.jobType = jobType;
        this.postingDate = postedDate;
        this.expiryDate = expiryDate;
    }
    
    @Ignore
    public Job(String id, String title, String description, String company, String location,
               String salary, String jobType, String category, String postingDate,
               String expiryDate, boolean isActive, String createdAt, String updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.company = company;
        this.location = location;
        this.salary = salary;
        this.jobType = jobType;
        this.category = category;
        this.postingDate = postingDate;
        this.expiryDate = expiryDate;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    @NonNull
    public String getId() {
        return id;
    }
    
    public void setId(@NonNull String id) {
        this.id = id;
    }
    
    // Overloaded setter for int id (converts to String)
    public void setId(int id) {
        this.id = String.valueOf(id);
    }
    
    public String getTitle() {
        return title != null ? title : "";
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description != null ? description : "";
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCompany() {
        return company != null ? company : "";
    }
    
    public void setCompany(String company) {
        this.company = company;
    }
    
    // Alternative setter for companyName (maps to companyName field)
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    
    // Alternative getter for companyName (returns the actual companyName field)
    public String getCompanyName() {
        return companyName != null ? companyName : "";
    }
    
    public String getCompanyLogo() {
        return companyLogo != null ? companyLogo : "";
    }
    
    public void setCompanyLogo(String companyLogo) {
        this.companyLogo = companyLogo;
    }

    public String getImage() {
        return image != null ? image : "";
    }

    public void setImage(String image) {
        this.image = image;
    }
    
    public String getLocation() {
        return location != null ? location : "";
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getJobType() {
        return jobType != null ? jobType : "";
    }
    
    public void setJobType(String jobType) {
        this.jobType = jobType;
    }
    
    public String getCategory() {
        return category != null ? category : "";
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getSalary() {
        return salary != null ? salary : "0.00";
    }
    
    public void setSalary(String salary) {
        this.salary = salary;
    }
    
    public String getPostingDate() {
        return postingDate != null ? postingDate : "";
    }
    
    public void setPostingDate(String postingDate) {
        this.postingDate = postingDate;
    }
    
    public String getExpiryDate() {
        return expiryDate != null ? expiryDate : "";
    }
    
    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public String getCreatedAt() {
        return createdAt != null ? createdAt : "";
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUpdatedAt() {
        return updatedAt != null ? updatedAt : "";
    }
    
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public int getShareCount() {
        return shareCount;
    }
    
    public void setShareCount(int shareCount) {
        this.shareCount = shareCount;
    }
    
    public int getApplicationsCount() {
        return applicationsCount;
    }
    
    public void setApplicationsCount(int applicationsCount) {
        this.applicationsCount = applicationsCount;
    }
    
    public String getStatus() {
        return status != null ? status : "active";
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getPostedDate() {
        return postedDate != null ? postedDate : "";
    }
    
    public void setPostedDate(String postedDate) {
        this.postedDate = postedDate;
    }
    

    public String toString() {
        return "Job{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", company='" + company + '\'' +
                ", location='" + location + '\'' +
                ", jobType='" + jobType + '\'' +
                ", category='" + category + '\'' +
                ", salary='" + salary + '\'' +
                '}';
    }
}