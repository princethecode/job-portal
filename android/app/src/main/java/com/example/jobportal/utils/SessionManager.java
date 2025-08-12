package com.example.jobportal.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.example.jobportal.models.User;
import com.google.gson.Gson;

/**
 * Comprehensive session manager to handle user authentication, session state, and user data
 */
public class SessionManager {
    private static final String TAG = "SessionManager";
    
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
    private static final String KEY_USER_OBJECT = "user_object"; // Key for storing the serialized User object
    private static final String KEY_FCM_TOKEN = "fcm_token"; // Key for storing FCM token
    
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
        Log.d(TAG, "Creating login session - User ID: " + userId + ", Name: " + name);
        editor.putBoolean(IS_LOGIN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_TOKEN, token);
        editor.apply();
        Log.d(TAG, "Login session created successfully");
    }
    
    /**
     * Save the full User object
     * @param user User object to save
     */
    public void saveUser(User user) {
        if (user == null) {
            Log.e(TAG, "Cannot save null user");
            return;
        }
        
        try {
            Gson gson = new Gson();
            String userJson = gson.toJson(user);
            editor.putString(KEY_USER_OBJECT, userJson);
            
            // Also update individual fields for backward compatibility
            if (user.getId() != null) {
                try {
                    editor.putInt(KEY_USER_ID, Integer.parseInt(user.getId()));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Could not parse user ID as integer: " + user.getId());
                }
            }
            editor.putString(KEY_NAME, user.getFullName());
            editor.putString(KEY_EMAIL, user.getEmail());
            
            editor.apply();
            Log.d(TAG, "User saved to preferences");
        } catch (Exception e) {
            Log.e(TAG, "Error saving user object: " + e.getMessage());
        }
    }
    
    /**
     * Get the saved User object
     * @return User object or null if not found
     */
    public User getUser() {
        String userJson = pref.getString(KEY_USER_OBJECT, null);
        if (userJson == null) {
            // Try to construct a basic User from individual fields
            if (getUserId() != -1 && getName() != null && getEmail() != null) {
                User user = new User();
                user.setId(String.valueOf(getUserId()));
                user.setFullName(getName());
                user.setEmail(getEmail());
                return user;
            }
            return null;
        }
        
        try {
            Gson gson = new Gson();
            return gson.fromJson(userJson, User.class);
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving user object: " + e.getMessage());
            return null;
        }
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
        Log.d(TAG, "Clearing authentication token - Previous token: " + 
                    (getToken() != null ? "exists" : "null") + 
                    ", User ID: " + getUserId() + 
                    ", Stack trace: " + Log.getStackTraceString(new Exception()));
        editor.remove(KEY_TOKEN);
        editor.apply();
        Log.d(TAG, "Authentication token cleared");
    }
    
    /**
     * Clear user information
     */
    public void clearUserInfo() {
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_NAME);
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_USER_OBJECT);
        editor.apply();
    }
    
    /**
     * Save FCM token
     */
    public void saveFcmToken(String fcmToken) {
        if (fcmToken != null && !fcmToken.isEmpty()) {
            Log.d(TAG, "Saving FCM token");
            editor.putString(KEY_FCM_TOKEN, fcmToken);
            editor.apply();
        }
    }

    /**
     * Get FCM token
     */
    public String getFcmToken() {
        return pref.getString(KEY_FCM_TOKEN, null);
    }

    /**
     * Check if FCM token exists
     */
    public boolean hasFcmToken() {
        return getFcmToken() != null;
    }

    /**
     * Clear FCM token
     */
    public void clearFcmToken() {
        Log.d(TAG, "Clearing FCM token");
        editor.remove(KEY_FCM_TOKEN);
        editor.apply();
    }
    
    /**
     * Logout user and clear all session data
     */
    public void logout() {
        Log.d(TAG, "Logging out user - Previous state: " + 
                    "isLoggedIn=" + isLoggedIn() + 
                    ", hasToken=" + hasToken() + 
                    ", userId=" + getUserId() + 
                    ", hasFcmToken=" + hasFcmToken() +
                    ", Stack trace: " + Log.getStackTraceString(new Exception()));
        editor.clear();
        editor.apply();
        Log.d(TAG, "Session data cleared");
    }
    
    /**
     * Check if session is valid (has both token and user data)
     */
    public boolean isSessionValid() {
        boolean isLoggedIn = isLoggedIn();
        boolean hasToken = hasToken();
        int userId = getUserId();
        String token = getToken();
        
        Log.d(TAG, "Checking session validity - " +
                    "isLoggedIn: " + isLoggedIn + 
                    ", hasToken: " + hasToken + 
                    ", userId: " + userId + 
                    ", token length: " + (token != null ? token.length() : 0) +
                    ", Stack trace: " + Log.getStackTraceString(new Exception()));
                    
        return isLoggedIn && hasToken && userId != -1;
    }
}