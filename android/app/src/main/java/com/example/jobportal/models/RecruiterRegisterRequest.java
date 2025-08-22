package com.example.jobportal.models;

import com.google.gson.annotations.SerializedName;

public class RecruiterRegisterRequest {
    @SerializedName("name")
    private String name;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("mobile")
    private String mobile;
    
    @SerializedName("password")
    private String password;
    
    @SerializedName("password_confirmation")
    private String passwordConfirmation;
    
    @SerializedName("company_name")
    private String companyName;
    
    @SerializedName("company_website")
    private String companyWebsite;
    
    @SerializedName("company_description")
    private String companyDescription;
    
    @SerializedName("company_size")
    private String companySize;
    
    @SerializedName("industry")
    private String industry;
    
    @SerializedName("location")
    private String location;
    
    @SerializedName("designation")
    private String designation;

    public RecruiterRegisterRequest(String name, String email, String mobile, String password, 
                                  String passwordConfirmation, String companyName, String companyWebsite,
                                  String companyDescription, String companySize, String industry,
                                  String location, String designation) {
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.password = password;
        this.passwordConfirmation = passwordConfirmation;
        this.companyName = companyName;
        this.companyWebsite = companyWebsite;
        this.companyDescription = companyDescription;
        this.companySize = companySize;
        this.industry = industry;
        this.location = location;
        this.designation = designation;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPasswordConfirmation() { return passwordConfirmation; }
    public void setPasswordConfirmation(String passwordConfirmation) { this.passwordConfirmation = passwordConfirmation; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCompanyWebsite() { return companyWebsite; }
    public void setCompanyWebsite(String companyWebsite) { this.companyWebsite = companyWebsite; }

    public String getCompanyDescription() { return companyDescription; }
    public void setCompanyDescription(String companyDescription) { this.companyDescription = companyDescription; }

    public String getCompanySize() { return companySize; }
    public void setCompanySize(String companySize) { this.companySize = companySize; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
}
