package com.example.jobportal.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "users")
public class User {
    @PrimaryKey
    @NonNull
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String fullName;

    @SerializedName("email")
    private String email;

    @SerializedName("mobile")
    private String phone;

    @SerializedName("skills")
    private String skills;

    @SerializedName("experience")
    private String experience;

    @SerializedName("resume_path")
    private String resume;
    
    @SerializedName("profile_photo")
    private String profilePhoto;
    
    @SerializedName("location")
    private String location;
    
    @SerializedName("job_title")
    private String jobTitle;
    
    @SerializedName("about_me")
    private String aboutMe;

    @SerializedName("is_active")
    private boolean isActive;

    @SerializedName("is_admin")
    private boolean isAdmin;

    @SerializedName("email_verified_at")
    private String emailVerifiedAt;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;
    
    // New employment fields
    @SerializedName("current_company")
    private String currentCompany;
    
    @SerializedName("department")
    private String department;
    
    @SerializedName("current_salary")
    private String currentSalary;
    
    @SerializedName("expected_salary")
    private String expectedSalary;
    
    @SerializedName("joining_period")
    private String joiningPeriod;

    @SerializedName("contact")
    private String contact;

    @SerializedName("last_contact_sync")
    private String lastContactSync;

    // Default constructor required by Room
    public User() {}

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getResume() {
        return resume;
    }

    public void setResume(String resume) {
        this.resume = resume;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public String getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public void setEmailVerifiedAt(String emailVerifiedAt) {
        this.emailVerifiedAt = emailVerifiedAt;
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
    
    // Getters and setters for the new employment fields
    public String getCurrentCompany() {
        return currentCompany;
    }

    public void setCurrentCompany(String currentCompany) {
        this.currentCompany = currentCompany;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getCurrentSalary() {
        return currentSalary;
    }

    public void setCurrentSalary(String currentSalary) {
        this.currentSalary = currentSalary;
    }

    public String getExpectedSalary() {
        return expectedSalary;
    }

    public void setExpectedSalary(String expectedSalary) {
        this.expectedSalary = expectedSalary;
    }

    public String getJoiningPeriod() {
        return joiningPeriod;
    }

    public void setJoiningPeriod(String joiningPeriod) {
        this.joiningPeriod = joiningPeriod;
    }
    
    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getLastContactSync() {
        return lastContactSync;
    }

    public void setLastContactSync(String lastContactSync) {
        this.lastContactSync = lastContactSync;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getJobTitle() {
        return jobTitle;
    }
    
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }
    
    public String getAboutMe() {
        return aboutMe;
    }
    
    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }
}
