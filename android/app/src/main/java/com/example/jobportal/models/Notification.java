package com.example.jobportal.models;

public class Notification {
    private String id;
    private String title;
    private String description;
    private String date;
    private boolean isRead;

    public Notification(String id, String title, String description, String date, boolean isRead) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.isRead = isRead;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
    
    // For backward compatibility
    public String getMessage() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public boolean isRead() {
        return isRead;
    }
} 