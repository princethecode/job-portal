package com.example.jobportal.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.Nullable;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@Entity(tableName = "experiences",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.SET_NULL,
                deferred = true
        ),
        indices = {@Index("userId")}
)
public class Experience implements Serializable {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @SerializedName("user_id")
    private String userId; // Can be null with SET_NULL constraint
    
    @SerializedName("job_title")
    private String jobTitle;
    
    @SerializedName("company_name")
    private String companyName;
    
    @SerializedName("start_date")
    private String startDate;
    
    @SerializedName("end_date")
    private String endDate;
    
    @SerializedName("is_current")
    private boolean isCurrent;
    
    @SerializedName("description")
    private String description;
    
    // Default constructor required by Room
    public Experience() {
        // Initialize with empty strings to avoid null binding issues
        this.jobTitle = "";
        this.companyName = "";
        this.startDate = "";
        this.endDate = "";
        this.description = "";
    }
    
    // Constructor with parameters
    public Experience(String userId, String jobTitle, String companyName, 
                     String startDate, String endDate, boolean isCurrent, 
                     String description) {
        this.userId = userId;
        this.jobTitle = jobTitle;
        this.companyName = companyName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isCurrent = isCurrent;
        this.description = description;
    }
    
    // Getters and setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId != null ? userId : "";
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getJobTitle() {
        return jobTitle != null ? jobTitle : "";
    }
    
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }
    
    public String getCompanyName() {
        return companyName != null ? companyName : "";
    }
    
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    
    public String getStartDate() {
        return startDate != null ? startDate : "";
    }
    
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    
    public String getEndDate() {
        return endDate != null ? endDate : "";
    }
    
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    
    public boolean isCurrent() {
        return isCurrent;
    }
    
    public void setCurrent(boolean current) {
        isCurrent = current;
    }
    
    public String getDescription() {
        return description != null ? description : "";
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return jobTitle + " at " + companyName;
    }
}
