package com.example.jobportal.models;


public class JobCategory {
    private final String name;
    private final int iconRes;

    public JobCategory(String name, int iconRes) {
        this.name = name;
        this.iconRes = iconRes;
    }

    public String getName() {
        return name;
    }

    public int getIconRes() {
        return iconRes;
    }
} 