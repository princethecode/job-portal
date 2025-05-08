package com.example.jobportal.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Comprehensive session manager to handle user authentication, session state, and user data
 */
public class SessionManager {
    // Shared Preferences
    private final SharedPreferences pref;
    private final Editor editor;
    private final Context context;
    
    // Shared pref mode
    private static final int PRIVATE_MODE = 0;
    
    // Sharedpref file name
    private static final String PREF_NAME = "JobPortalPrefs";
    
    // Session state keys
    private static final String IS_LOGIN = "IsLoggedIn";
    
    // User data keys
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_TOKEN = "token";
    
    // Singleton instance
    private static volatile SessionManager instance;
    
    /**
     * Private constructor to prevent direct instantiation
     */
    private SessionManager(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        this.context = context.getApplicationContext();
        pref = this.context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }
    
    /**
     * Get singleton instance of SessionManager
     * @param context Application context
     * @return SessionManager instance
     * @throws IllegalArgumentException if context is null
     */
    public static SessionManager getInstance(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager(context);
                }
            }
        }
        return instance;
    }
    
    /**
     * Create a new login session with user data
     */
    public void createLoginSession(int userId, String name, String email, String token) {
        editor.putBoolean(IS_LOGIN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }
    
    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }
    
    /**
     * Get user ID
     */
    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }
    
    /**
     * Get user name
     */
    public String getName() {
        return pref.getString(KEY_NAME, null);
    }
    
    /**
     * Get user email
     */
    public String getEmail() {
        return pref.getString(KEY_EMAIL, null);
    }
    
    /**
     * Get authentication token
     */
    public String getToken() {
        return pref.getString(KEY_TOKEN, null);
    }
    
    /**
     * Check if token exists
     */
    public boolean hasToken() {
        return getToken() != null;
    }
    
    /**
     * Update authentication token
     */
    public void updateToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }
    
    /**
     * Update user information
     */
    public void updateUserInfo(int userId, String name, String email) {
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }
    
    /**
     * Clear authentication token
     */
    public void clearToken() {
        editor.remove(KEY_TOKEN);
        editor.apply();
    }
    
    /**
     * Clear user information
     */
    public void clearUserInfo() {
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_NAME);
        editor.remove(KEY_EMAIL);
        editor.apply();
    }
    
    /**
     * Logout user and clear all session data
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }
    
    /**
     * Check if session is valid (has both token and user data)
     */
    public boolean isSessionValid() {
        return isLoggedIn() && hasToken() && getUserId() != -1;
    }
}