package com.example.jobportal.models;

public class Notification {
    private String id;
    private String title;
    private String message;
    private String date;
    private boolean isRead;

    public Notification(String id, String title, String message, String date, boolean isRead) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.date = date;
        this.isRead = isRead;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getDate() {
        return date;
    }

    public boolean isRead() {
        return isRead;
    }
} 