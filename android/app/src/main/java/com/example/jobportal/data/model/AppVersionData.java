package com.example.jobportal.data.model;

public class AppVersionData {
    // For /check-update endpoint response
    private boolean update_available;
    private boolean update_required;
    private boolean force_update;
    private VersionInfo latest_version;
    
    // For /latest endpoint response (direct version info)
    private String platform;
    private String version_name;
    private int version_code;
    private String minimum_version_name;
    private int minimum_version_code;
    private String update_message;
    private String download_url;
    
    // Legacy fields for backward compatibility
    private boolean is_latest;
    private VersionInfo current_version;

    // Constructors
    public AppVersionData() {}

    public AppVersionData(boolean update_required, boolean force_update, boolean is_latest, 
                         VersionInfo current_version, VersionInfo latest_version) {
        this.update_required = update_required;
        this.force_update = force_update;
        this.is_latest = is_latest;
        this.current_version = current_version;
        this.latest_version = latest_version;
    }

    // Getters for new API format
    public boolean isUpdate_available() {
        return update_available;
    }

    public boolean isUpdate_required() {
        return update_required;
    }

    public boolean isForce_update() {
        return force_update;
    }

    public String getPlatform() {
        return platform;
    }

    public String getVersion_name() {
        return version_name;
    }

    public int getVersion_code() {
        return version_code;
    }

    public String getMinimum_version_name() {
        return minimum_version_name;
    }

    public int getMinimum_version_code() {
        return minimum_version_code;
    }

    public String getUpdate_message() {
        return update_message;
    }

    public String getDownload_url() {
        return download_url;
    }

    // Legacy getters for backward compatibility
    public boolean isIs_latest() {
        return is_latest;
    }

    public VersionInfo getCurrent_version() {
        return current_version;
    }

    public VersionInfo getLatest_version() {
        return latest_version;
    }

    // Setters for new API format
    public void setUpdate_available(boolean update_available) {
        this.update_available = update_available;
    }

    public void setUpdate_required(boolean update_required) {
        this.update_required = update_required;
    }

    public void setForce_update(boolean force_update) {
        this.force_update = force_update;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public void setVersion_name(String version_name) {
        this.version_name = version_name;
    }

    public void setVersion_code(int version_code) {
        this.version_code = version_code;
    }

    public void setMinimum_version_name(String minimum_version_name) {
        this.minimum_version_name = minimum_version_name;
    }

    public void setMinimum_version_code(int minimum_version_code) {
        this.minimum_version_code = minimum_version_code;
    }

    public void setUpdate_message(String update_message) {
        this.update_message = update_message;
    }

    public void setDownload_url(String download_url) {
        this.download_url = download_url;
    }

    // Legacy setters for backward compatibility
    public void setIs_latest(boolean is_latest) {
        this.is_latest = is_latest;
    }

    public void setCurrent_version(VersionInfo current_version) {
        this.current_version = current_version;
    }

    public void setLatest_version(VersionInfo latest_version) {
        this.latest_version = latest_version;
    }
}