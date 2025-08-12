package com.example.jobportal.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.jobportal.BuildConfig;
import com.example.jobportal.utils.ContactsUtils;
import com.example.jobportal.utils.SessionManager;
import com.example.jobportal.models.User;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.HashMap;

/**
 * Repository class for handling contacts-related operations
 */
public class ContactsRepository {
    private static final String TAG = "ContactsRepository";
    private final Context context;
    private final ApiClient apiClient;

    /**
     * Constructor for ContactsRepository
     * @param context Application context
     */
    public ContactsRepository(Context context) {
        this.context = context.getApplicationContext();
        this.apiClient = ApiClient.getInstance(context);
    }

    /**
     * Reads contacts from the device and uploads them to the server
     * @param callback Callback to handle the result
     */
    public void fetchAndUploadContacts(final ApiCallback<Boolean> callback) {
        // Run on a background thread
        new Thread(() -> {
            try {
                // Read contacts
                List<ContactsUtils.Contact> contacts = ContactsUtils.readContacts(context);
                if (contacts.isEmpty()) {
                    Log.d(TAG, "No contacts found to upload");
                    notifyCallback(callback, false);
                    return;
                }
                
                // Create CSV file
                File csvFile = ContactsUtils.createContactsCsvFile(context, contacts);
                if (csvFile == null) {
                    Log.e(TAG, "Failed to create contacts CSV file");
                    notifyCallback(callback, false);
                    return;
                }
                
                // Log file size and name for debugging
                Log.d(TAG, "Created contacts CSV file: " + csvFile.getName() + 
                      ", size: " + (csvFile.length() / 1024) + "KB");
                
                // Get authentication token
                String token = apiClient.getAuthToken();
                if (token == null || token.isEmpty()) {
                    Log.e(TAG, "Authentication token is missing");
                    notifyCallback(callback, false);
                    return;
                }
                
                // Create multipart request manually
                OkHttpClient httpClient = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build();
                
                // Create the request body with the file
                RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "contacts", 
                        csvFile.getName(),
                        RequestBody.create(MediaType.parse("text/csv"), csvFile)
                    )
                    .build();
                
                // Create the request
                Request request = new Request.Builder()
                    .url(BuildConfig.API_BASE_URL + "contacts/upload")
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Accept", "application/json")
                    .post(requestBody)
                    .build();
                
                // Execute the request
                httpClient.newCall(request).enqueue(new okhttp3.Callback() {
                    @Override
                    public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                        // Delete temporary file regardless of response
                        csvFile.delete();
                        
                        String responseBody = "";
                        if (response.body() != null) {
                            responseBody = response.body().string();
                        }
                        
                        Log.d(TAG, "Response code: " + response.code());
                        Log.d(TAG, "Response body: " + responseBody);
                        
                        // Check if response is HTML (likely a login page redirect)
                        if (responseBody.trim().startsWith("<!DOCTYPE html>") || 
                            responseBody.trim().startsWith("<html")) {
                            Log.e(TAG, "Received HTML response instead of JSON. Session may have expired.");
                            notifyCallback(callback, false);
                            return;
                        }
                        
                        if (response.isSuccessful()) {
                            try {
                                // Save contact path and last sync date to user/session
                                SessionManager sessionManager = SessionManager.getInstance(context);
                                User user = sessionManager.getUser();
                                if (user != null) {
                                    user.setContact(csvFile.getAbsolutePath());
                                    String now = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date());
                                    user.setLastContactSync(now);
                                    sessionManager.saveUser(user);
                                    
                                    // Update user contact fields on server
                                    updateUserContactFields(user, new ApiCallback<Boolean>() {
                                        @Override
                                        public void onSuccess(Boolean result) {
                                            if (result) {
                                                Log.d(TAG, "User contact fields updated on server successfully");
                                            } else {
                                                Log.e(TAG, "Failed to update user contact fields on server");
                                            }
                                        }

                                        @Override
                                        public void onError(String errorMessage) {
                                            Log.e(TAG, "Error updating user contact fields: " + errorMessage);
                                        }
                                    });
                                }
                                // Try to parse the response as JSON
                                Gson gson = new Gson();
                                JsonReader reader = new JsonReader(new StringReader(responseBody));
                                reader.setLenient(true); // Allow malformed JSON
                                JsonElement jsonElement = gson.fromJson(reader, JsonElement.class);
                                
                                Log.d(TAG, "Contacts uploaded successfully");
                                notifyCallback(callback, true);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing response", e);
                                notifyCallback(callback, false);
                            }
                        } else {
                            Log.e(TAG, "Upload failed with code: " + response.code());
                            notifyCallback(callback, false);
                        }
                    }
                    
                    @Override
                    public void onFailure(okhttp3.Call call, IOException e) {
                        // Delete temporary file on failure
                        csvFile.delete();
                        
                        Log.e(TAG, "Error uploading contacts", e);
                        notifyCallback(callback, false);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error processing contacts", e);
                notifyCallback(callback, false);
            }
        }).start();
    }
    
    /**
     * Helper method to notify callback on the main thread
     * @param callback Callback to notify
     * @param success Whether the operation was successful
     */
    private void notifyCallback(ApiCallback<Boolean> callback, boolean success) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (success) {
                callback.onSuccess(true);
            } else {
                callback.onError("Failed to upload contacts");
            }
        });
    }

    /**
     * Updates user contact fields on the server
     * @param user User object containing updated contact fields
     * @param callback Callback to handle the result
     */
    private void updateUserContactFields(User user, ApiCallback<Boolean> callback) {
        // Run on a background thread
        new Thread(() -> {
            try {
                // Get authentication token
                String token = apiClient.getAuthToken();
                if (token == null || token.isEmpty()) {
                    Log.e(TAG, "Authentication token is missing");
                    notifyCallback(callback, false);
                    return;
                }

                // Create request data
                Map<String, String> contactData = new HashMap<>();
                contactData.put("contact", user.getContact());
                contactData.put("last_contact_sync", user.getLastContactSync());

                // Get API service
                ApiService apiService = ApiClient.getApiService();
                
                // Make the API call
                Call<ApiResponse<User>> call = apiService.updateUserContact(contactData);
                call.enqueue(new Callback<ApiResponse<User>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<User> apiResponse = response.body();
                            if (apiResponse.isSuccess()) {
                                Log.d(TAG, "User contact fields updated successfully");
                                notifyCallback(callback, true);
                            } else {
                                Log.e(TAG, "Failed to update user contact fields: " + apiResponse.getMessage());
                                notifyCallback(callback, false);
                            }
                        } else {
                            try {
                                String errorBody = response.errorBody() != null ? 
                                    response.errorBody().string() : "Unknown error";
                                Log.e(TAG, "Failed to update user contact fields. Error: " + errorBody);
                                
                              
                            } catch (IOException e) {
                                Log.e(TAG, "Error reading error response", e);
                            }
                            notifyCallback(callback, false);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                        Log.e(TAG, "Error updating user contact fields", t);
                        notifyCallback(callback, false);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error updating user contact fields", e);
                notifyCallback(callback, false);
            }
        }).start();
    }
} 