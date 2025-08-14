package com.example.jobportal.data.model;

public class AppVersionResponse {
    private boolean success;
    private String message;
    private AppVersionData data;

    // Constructors
    public AppVersionResponse() {}

    public AppVersionResponse(boolean success, String message, AppVersionData data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public AppVersionData getData() {
        return data;
    }

    // Setters
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(AppVersionData data) {
        this.data = data;
    }

    // Helper methods for backward compatibility
    public boolean isUpdate_required() {
        return data != null && data.isUpdate_required();
    }

    public boolean isForce_update() {
        return data != null && data.isForce_update();
    }

    public VersionInfo getCurrent_version() {
        return data != null ? data.getCurrent_version() : null;
    }

    public VersionInfo getLatest_version() {
        return data != null ? data.getLatest_version() : null;
    }
}