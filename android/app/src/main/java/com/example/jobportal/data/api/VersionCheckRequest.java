package com.example.jobportal.data.api;

public class VersionCheckRequest {
    private String platform;
    private int current_version_code;

    // Constructors
    public VersionCheckRequest() {
        this.platform = "android";
    }

    public VersionCheckRequest(String platform, int current_version_code) {
        this.platform = platform;
        this.current_version_code = current_version_code;
    }

    // Legacy constructor for backward compatibility
    public VersionCheckRequest(String platform, int version_code, String version_name) {
        this.platform = platform;
        this.current_version_code = version_code;
    }

    // Getters
    public String getPlatform() {
        return platform;
    }

    public int getCurrent_version_code() {
        return current_version_code;
    }

    // Legacy getter for backward compatibility
    public int getVersion_code() {
        return current_version_code;
    }

    // Setters
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public void setCurrent_version_code(int current_version_code) {
        this.current_version_code = current_version_code;
    }

    // Legacy setter for backward compatibility
    public void setVersion_code(int version_code) {
        this.current_version_code = version_code;
    }
}