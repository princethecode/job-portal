package com.example.jobportal.data.model;

public class VersionInfo {
    private String version_name;  // Changed from 'name' to match API
    private int version_code;     // Changed from 'code' to match API
    private String download_url;
    private String update_message;

    // Constructors
    public VersionInfo() {}

    public VersionInfo(String version_name, int version_code) {
        this.version_name = version_name;
        this.version_code = version_code;
    }

    public VersionInfo(String version_name, int version_code, String download_url, String update_message) {
        this.version_name = version_name;
        this.version_code = version_code;
        this.download_url = download_url;
        this.update_message = update_message;
    }

    // Getters
    public String getName() {
        return version_name;  // Return version_name for backward compatibility
    }
    
    public String getVersion_name() {
        return version_name;
    }

    public int getCode() {
        return version_code;  // Return version_code for backward compatibility
    }
    
    public int getVersion_code() {
        return version_code;
    }

    public String getDownload_url() {
        return download_url;
    }

    public String getUpdate_message() {
        return update_message;
    }

    // Setters
    public void setName(String name) {
        this.version_name = name;  // Set version_name for backward compatibility
    }
    
    public void setVersion_name(String version_name) {
        this.version_name = version_name;
    }

    public void setCode(int code) {
        this.version_code = code;  // Set version_code for backward compatibility
    }
    
    public void setVersion_code(int version_code) {
        this.version_code = version_code;
    }

    public void setDownload_url(String download_url) {
        this.download_url = download_url;
    }

    public void setUpdate_message(String update_message) {
        this.update_message = update_message;
    }
}