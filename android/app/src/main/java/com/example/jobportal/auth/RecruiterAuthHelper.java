package com.example.jobportal.auth;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.jobportal.models.Recruiter;
import com.google.gson.Gson;

public class RecruiterAuthHelper {
    private static final String PREF_NAME = "RecruiterAuthPrefs";
    private static final String KEY_TOKEN = "recruiter_token";
    private static final String KEY_RECRUITER = "recruiter_data";
    private static final String KEY_IS_LOGGED_IN = "recruiter_is_logged_in";
    
    private static RecruiterAuthHelper instance;
    private SharedPreferences prefs;
    private Gson gson;
    
    private RecruiterAuthHelper(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    public static synchronized RecruiterAuthHelper getInstance(Context context) {
        if (instance == null) {
            instance = new RecruiterAuthHelper(context.getApplicationContext());
        }
        return instance;
    }
    
    public void saveRecruiterToken(String token) {
        android.util.Log.d("RecruiterAuth", "Saving recruiter token: " + 
            (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null") +
            " (length: " + (token != null ? token.length() : 0) + ")");
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }
    
    public String getRecruiterToken() {
        String token = prefs.getString(KEY_TOKEN, null);
        android.util.Log.d("RecruiterAuth", "Retrieved recruiter token: " + 
            (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null") +
            " (length: " + (token != null ? token.length() : 0) + ")");
        return token;
    }
    
    public void saveRecruiterData(Recruiter recruiter) {
        String recruiterJson = gson.toJson(recruiter);
        prefs.edit().putString(KEY_RECRUITER, recruiterJson).apply();
    }
    
    public Recruiter getRecruiterData() {
        String recruiterJson = prefs.getString(KEY_RECRUITER, null);
        if (recruiterJson != null) {
            return gson.fromJson(recruiterJson, Recruiter.class);
        }
        return null;
    }
    
    public void setLoggedIn(boolean isLoggedIn) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply();
    }
    
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    public void logout() {
        prefs.edit().clear().apply();
    }
    
    public boolean hasValidToken() {
        String token = getRecruiterToken();
        boolean isValid = token != null && !token.isEmpty() && token.length() > 10;
        android.util.Log.d("RecruiterAuth", "Token validation: " + isValid + 
            " (token length: " + (token != null ? token.length() : 0) + ")");
        return isValid;
    }
    
    public void debugAuthState() {
        android.util.Log.d("RecruiterAuth", "=== Recruiter Auth State Debug ===");
        android.util.Log.d("RecruiterAuth", "Is logged in: " + isLoggedIn());
        android.util.Log.d("RecruiterAuth", "Has valid token: " + hasValidToken());
        android.util.Log.d("RecruiterAuth", "Recruiter data exists: " + (getRecruiterData() != null));
        String token = getRecruiterToken();
        if (token != null) {
            android.util.Log.d("RecruiterAuth", "Token format: " + 
                (token.contains("|") ? "Laravel Sanctum format" : "Unknown format"));
        }
        android.util.Log.d("RecruiterAuth", "===============================");
    }
}
