package com.example.jobportal.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.jobportal.BuildConfig;
import com.example.jobportal.models.User;
import com.example.jobportal.models.LoginResponse;
import com.example.jobportal.models.Application;
import com.example.jobportal.utils.SessionManager;
import com.example.jobportal.data.model.AppVersionResponse;
import com.example.jobportal.data.model.AppVersionData;
import com.example.jobportal.data.api.VersionCheckRequest;
import java.io.IOException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class ApiClient {
    private static Retrofit retrofit = null;
    private static final int REQUEST_TIMEOUT = 60;
    private static ApiService apiService;
    private static SessionManager sessionManager;
    private static Context context;
    private static ApiClient instance;

    public static void init(Context context) {
        ApiClient.context = context.getApplicationContext();
        sessionManager = SessionManager.getInstance(ApiClient.context);
        apiService = getClient(ApiClient.context).create(ApiService.class);
    }

    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context);
        }
        return instance;
    }

    public ApiClient(Context context) {
        ApiClient.context = context.getApplicationContext();
        sessionManager = SessionManager.getInstance(ApiClient.context);
        apiService = getClient(ApiClient.context).create(ApiService.class);
    }

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                    .connectTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS);

            // Add logging interceptor in debug mode
            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                httpClient.addInterceptor(logging);
            }

            // Add auth token to requests if available
            SessionManager sessionManager = SessionManager.getInstance(context);
            if (sessionManager.hasToken()) {
                httpClient.addInterceptor(chain -> {
                    Request original = chain.request();
                    String token = sessionManager.getToken();
                    Log.d("ApiClient", "Making API request: " + original.url() + 
                                "\nMethod: " + original.method() +
                                "\nToken exists: " + (token != null && !token.isEmpty()) +
                                "\nToken length: " + (token != null ? token.length() : 0));
                    
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", "Bearer " + token)
                            .header("Accept", "application/json")
                            .method(original.method(), original.body());

                    Request request = requestBuilder.build();
                    okhttp3.Response response = chain.proceed(request);
                    
                    // Log detailed response information
                    Log.d("ApiClient", "API Response for " + original.url() + 
                                "\nStatus: " + response.code() +
                                "\nMessage: " + response.message() +
                                "\nHeaders: " + response.headers().toString());
                    
                    // Check for auth errors
                    if (response.code() == 401 || response.code() == 403) {
                        Log.e("ApiClient", "Authentication error detected:" +
                                    "\nURL: " + original.url() +
                                    "\nStatus: " + response.code() +
                                    "\nMessage: " + response.message() +
                                    "\nToken was: " + (token != null ? "present" : "null") +
                                    "\nStack trace: " + Log.getStackTraceString(new Exception()));
                        sessionManager.clearToken();
                    }
                    
                    return response;
                });
            }

            // Create a Gson instance with lenient parsing enabled
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }

    public static void resetApiClient() {
        retrofit = null;
    }

    public static ApiService getApiService() {
        if (apiService == null) {
            apiService = getClient(context).create(ApiService.class);
        }
        return apiService;
    }

    public static void saveAuthToken(String token) {
        Log.d("ApiClient", "Saving new auth token");
        sessionManager.updateToken(token);
    }

    public static void clearAuthToken() {
        Log.d("ApiClient", "Clearing auth token");
        sessionManager.clearToken();
    }

    public void login(String email, String password, ApiCallback<ApiResponse<LoginResponse>> callback) {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("email", email);
        loginData.put("password", password);

        Call<ApiResponse<LoginResponse>> call = apiService.login(loginData);
        call.enqueue(new Callback<ApiResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoginResponse>> call, Response<ApiResponse<LoginResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Login failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                Log.e("ApiClient", "Network error", t);
                Log.e("ApiClient", Log.getStackTraceString(t));
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * Login with mobile number and password
     * 
     * @param mobile Mobile number
     * @param password Password
     * @param callback Callback to handle response
     */
    public void loginWithMobile(String mobile, String password, ApiCallback<ApiResponse<LoginResponse>> callback) {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("mobile", mobile);
        loginData.put("password", password);

        Call<ApiResponse<LoginResponse>> call = apiService.login(loginData);
        call.enqueue(new Callback<ApiResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoginResponse>> call, Response<ApiResponse<LoginResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Login failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                Log.e("ApiClient", "Network error", t);
                Log.e("ApiClient", Log.getStackTraceString(t));
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void register(String fullName, String email, String mobile, String password, 
                        File resumeFile, final ApiCallback<ApiResponse<User>> callback) {
        
        // Create multipart request if resume file is provided
        if (resumeFile != null) {
            // Create multipart form builder
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM);
            
            // Add text fields
            builder.addFormDataPart("name", fullName);
            builder.addFormDataPart("email", email);
            builder.addFormDataPart("mobile", mobile);
            builder.addFormDataPart("password", password);
            builder.addFormDataPart("password_confirmation", password);
            
            // Add resume file with correct field name
            RequestBody resumeBody = RequestBody.create(MediaType.parse("application/pdf"), resumeFile);
            builder.addFormDataPart("resume", resumeFile.getName(), resumeBody);
            
            RequestBody requestBody = builder.build();
            
            // Create a request with the multipart body
            Request request = new Request.Builder()
                    .url(BuildConfig.API_BASE_URL + "register")
                    .header("Accept", "application/json")
                    .post(requestBody)
                    .build();
            
            Log.d("ApiClient", "Registering with resume file: " + resumeFile.getName());
            
            // Execute the request with proper timeouts
            OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .build();
                
            httpClient.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                    String responseBody = "";
                    if (response.body() != null) {
                        responseBody = response.body().string();
                    }
                    
                    Log.d("ApiClient", "Registration response code: " + response.code());
                    Log.d("ApiClient", "Response body: " + responseBody);
                    
                    if (response.isSuccessful()) {
                        try {
                            Gson gson = new Gson();
                            ApiResponse<User> apiResponse = gson.fromJson(responseBody, 
                                    new TypeToken<ApiResponse<User>>(){}.getType());
                            
                            if (apiResponse != null && apiResponse.isSuccess()) {
                                Handler mainHandler = new Handler(Looper.getMainLooper());
                                mainHandler.post(() -> callback.onSuccess(apiResponse));
                            } else {
                                Handler mainHandler = new Handler(Looper.getMainLooper());
                                mainHandler.post(() -> callback.onError(apiResponse != null ? 
                                    apiResponse.getMessage() : "Registration failed"));
                            }
                        } catch (Exception e) {
                            Log.e("ApiClient", "Error parsing response", e);
                            Log.e("ApiClient", Log.getStackTraceString(e));
                            Handler mainHandler = new Handler(Looper.getMainLooper());
                            mainHandler.post(() -> callback.onError("Error parsing response: " + e.getMessage()));
                        }
                    } else {
                        Handler mainHandler = new Handler(Looper.getMainLooper());
                        if (response.code() == 503) {
                            mainHandler.post(() -> callback.onError("Server temporarily unavailable. Please try again later or upload a smaller file."));
                        } else {
                            mainHandler.post(() -> callback.onError("Registration failed: " + response.message()));
                        }
                    }
                }
                
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    Log.e("ApiClient", "Registration network error", e);
                    Log.e("ApiClient", Log.getStackTraceString(e));
                    Handler mainHandler = new Handler(Looper.getMainLooper());
                    mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
                }
            });
        } else {
            // For registration without resume file
            Map<String, String> registerData = new HashMap<>();
            registerData.put("name", fullName);
            registerData.put("email", email);
            registerData.put("mobile", mobile);
            registerData.put("password", password);
            registerData.put("password_confirmation", password);
            
            Call<ApiResponse<User>> call = apiService.register(registerData);
            executeCall(call, callback);
        }
    }

    public void forgotPassword(Map<String, String> emailData, final ApiCallback<ApiResponse<Object>> callback) {
        Call<ApiResponse<Object>> call = apiService.forgotPassword(emailData);
        executeCall(call, callback);
    }

    // Keep the old method for backward compatibility
    public void forgotPassword(String email, final ApiCallback<ApiResponse<Void>> callback) {
        Map<String, String> emailData = new HashMap<>();
        emailData.put("email", email);
        
        forgotPassword(emailData, new ApiCallback<ApiResponse<Object>>() {
            @Override
            public void onSuccess(ApiResponse<Object> response) {
                // Convert to Void response for old method
                ApiResponse<Void> voidResponse = new ApiResponse<>(
                    response.isSuccess(),
                    response.getMessage(),
                    null
                );
                callback.onSuccess(voidResponse);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    public void getUserProfile(final ApiCallback<ApiResponse<User>> callback) {
        Call<User> call = apiService.getUserProfile();
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                Log.d("ApiClient", "User profile API response code: " + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    
                    // Log full user data
                    Log.d("ApiClient", "User data received: ID=" + user.getId() + 
                          ", Name=" + user.getFullName() + 
                          ", Email=" + user.getEmail() + 
                          ", Mobile=" + user.getPhone());
                    
                    // Create ApiResponse with the user data
                    ApiResponse<User> apiResponse = new ApiResponse<>(true, "Profile retrieved successfully", user);
                    callback.onSuccess(apiResponse);
                } else {
                    Log.e("ApiClient", "Failed to get user profile. Response code: " + response.code());
                    callback.onError("Failed to retrieve profile. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("ApiClient", "Failed to get user profile: " + t.getMessage(), t);
                Log.e("ApiClient", Log.getStackTraceString(t));
                Log.e("ApiClient", "Network error", t);
            callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Update user profile with optional skills, experience, and resume
     * @param user User object with updated profile data
     * @param skills User's skills (can be null)
     * @param experience User's experience (can be null)
     * @param resumeFile Resume file to upload (can be null)
     * @param callback Callback to handle the response
     */

    public void updateUserProfile(User user, String skills, String experience,
                                 File resumeFile, final ApiCallback<ApiResponse<User>> callback) {
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            callback.onError("User not logged in");
            return;
        }
        
        if (resumeFile != null) {
            // For multipart requests (with file upload)
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM);
            
            // Add text fields
            builder.addFormDataPart("name", user.getFullName() != null ? user.getFullName() : "");
            builder.addFormDataPart("email", user.getEmail() != null ? user.getEmail() : "");
            builder.addFormDataPart("mobile", user.getPhone() != null ? user.getPhone() : "");
            
            if (skills != null) {
                builder.addFormDataPart("skills", skills);
            }
            
            if (experience != null) {
                builder.addFormDataPart("experience", experience);
            }
            
            // Add new profile fields
            if (user.getLocation() != null && !user.getLocation().isEmpty()) {
                builder.addFormDataPart("location", user.getLocation());
            }
            
            if (user.getJobTitle() != null && !user.getJobTitle().isEmpty()) {
                builder.addFormDataPart("job_title", user.getJobTitle());
            }
            
            if (user.getAboutMe() != null && !user.getAboutMe().isEmpty()) {
                builder.addFormDataPart("about_me", user.getAboutMe());
            }
            
            // Add resume file
            RequestBody resumeBody = RequestBody.create(MediaType.parse("application/pdf"), resumeFile);
            builder.addFormDataPart("resume", resumeFile.getName(), resumeBody);
            
            RequestBody requestBody = builder.build();
            
            // Create a request with the multipart body
            Request request = new Request.Builder()
                    .url(BuildConfig.API_BASE_URL + "profile")
                    .header("Authorization", "Bearer " + sessionManager.getToken())
                    .header("Accept", "application/json")
                    .post(requestBody)
                    .build();
            
            Log.d("ApiClient", "Updating profile with data including resume file: " + resumeFile.getName());
            
            // Execute the request manually
            OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .build();
            httpClient.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                    String responseBody = response.body().string();
                    Log.d("ApiClient", "Profile update response code: " + response.code());
                    Log.d("ApiClient", "Response body: " + responseBody);
                    
                    if (response.isSuccessful()) {
                        try {
                            Gson gson = new Gson();
                            ApiResponse<User> apiResponse = gson.fromJson(responseBody, 
                                    new TypeToken<ApiResponse<User>>(){}.getType());
                            
                            if (apiResponse.isSuccess()) {
                                Handler mainHandler = new Handler(Looper.getMainLooper());
                                mainHandler.post(() -> callback.onSuccess(apiResponse));
                            } else {
                                Handler mainHandler = new Handler(Looper.getMainLooper());
                                mainHandler.post(() -> callback.onError(apiResponse.getMessage()));
                            }
                        } catch (Exception e) {
                            Log.e("ApiClient", "Error parsing response", e);
                            Log.e("ApiClient", Log.getStackTraceString(e));
                            Handler mainHandler = new Handler(Looper.getMainLooper());
                            mainHandler.post(() -> callback.onError("Error parsing response"));
                        }
                    } else {
                        Handler mainHandler = new Handler(Looper.getMainLooper());
                        if (response.code() == 503) {
                            mainHandler.post(() -> callback.onError("Server temporarily unavailable. Please try again later or upload a smaller file."));
                        } else {
                            mainHandler.post(() -> callback.onError("Profile update failed: " + response.message()));
                        }
                    }
                }
                
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    Log.e("ApiClient", "Profile update network error", e);
                    Log.e("ApiClient", Log.getStackTraceString(e));
                    Handler mainHandler = new Handler(Looper.getMainLooper());
                    mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
                }
            });
            
        } else {
            // For simple JSON requests (no file upload)
            Map<String, String> profileData = new HashMap<>();
            profileData.put("name", user.getFullName());
            profileData.put("email", user.getEmail());
            profileData.put("mobile", user.getPhone());
            
            if (skills != null) {
                profileData.put("skills", skills);
            }
            
            if (experience != null) {
                profileData.put("experience", experience);
            }
            
            // Add new profile fields
            if (user.getLocation() != null && !user.getLocation().isEmpty()) {
                profileData.put("location", user.getLocation());
            }
            
            if (user.getJobTitle() != null && !user.getJobTitle().isEmpty()) {
                profileData.put("job_title", user.getJobTitle());
            }
            
            if (user.getAboutMe() != null && !user.getAboutMe().isEmpty()) {
                profileData.put("about_me", user.getAboutMe());
            }
            
            Log.d("ApiClient", "Updating profile with data: " + profileData);
            
            Call<ApiResponse<User>> call = apiService.updateUserProfile(profileData);
            call.enqueue(new Callback<ApiResponse<User>>() {
                @Override
                public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                    Log.d("ApiClient", "Profile update response code: " + response.code());
                    
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<User> apiResponse = response.body();
                        Log.d("ApiClient", "Profile update success: " + apiResponse.isSuccess() + ", message: " + apiResponse.getMessage());
                        
                        if (apiResponse.isSuccess()) {
                            callback.onSuccess(apiResponse);
                        } else {
                            callback.onError(apiResponse.getMessage());
                        }
                    } else {
                        try {
                            String errorBody = response.errorBody() != null ? 
                                response.errorBody().string() : "Unknown error";
                            Log.e("ApiClient", "Profile update failed with error: " + errorBody);
                            callback.onError("Profile update failed: " + errorBody);
                        } catch (IOException e) {
                            Log.e("ApiClient", "Error reading response body", e);
                            Log.e("ApiClient", Log.getStackTraceString(e));
                            callback.onError("Request failed: " + response.code());
                        }
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                    Log.e("ApiClient", "Profile update network error", t);
                    Log.e("ApiClient", Log.getStackTraceString(t));
                    Log.e("ApiClient", "Network error", t);
            callback.onError("Network error: " + t.getMessage());
                }
            });
        }
    }
    
    /**
     * Simple version of updateUserProfile for backward compatibility
     */
    public void updateUserProfile(User user, final ApiCallback<ApiResponse<User>> callback) {
        updateUserProfile(user, null, null, null, callback);
    }
    
    /**
     * Upload profile photo
     * 
     * @param photoFile The photo file to upload
     * @param callback Callback to handle the response
     */
    public void uploadProfilePhoto(File photoFile, final ApiCallback<ApiResponse<User>> callback) {
        if (photoFile == null) {
            callback.onError("No photo file provided");
            return;
        }
        
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            callback.onError("User not logged in");
            return;
        }
        
        // Create request body for the photo file
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), photoFile);
        
        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part photoPart = MultipartBody.Part.createFormData(
                "photo", photoFile.getName(), requestFile);
        
        // Create the API call
        Call<ApiResponse<User>> call = apiService.uploadProfilePhoto(photoPart);
        
        // Execute the request
        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        // Update the user in session manager
                        sessionManager.saveUser(apiResponse.getData());
                        callback.onSuccess(apiResponse);
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    try {
                        String errorMessage = response.errorBody() != null ? 
                            response.errorBody().string() : "Unknown error";
                        callback.onError("Failed to upload photo: " + errorMessage);
                    } catch (IOException e) {
                        Log.e("ApiClient", Log.getStackTraceString(e));
                        callback.onError("Failed to upload photo: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                Log.e("ApiClient", "Network error", t);
                Log.e("ApiClient", Log.getStackTraceString(t));
            callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void changePassword(String currentPassword, String newPassword, final ApiCallback<ApiResponse<Void>> callback) {
        Map<String, String> passwordData = new HashMap<>();
        passwordData.put("current_password", currentPassword);
        passwordData.put("new_password", newPassword);
        passwordData.put("new_password_confirmation", newPassword);
        
        Call<ApiResponse<Void>> call = apiService.changePassword(passwordData);
        executeCall(call, callback);
    }

    public void logout(final ApiCallback<ApiResponse<Void>> callback) {
        // This method is not in the ApiService yet
        Call<ApiResponse<Void>> call = apiService.logout();
        
        // For now, just clear the token locally
        clearAuthToken();
        // Fix: Use the correct constructor for ApiResponse<Void>
        callback.onSuccess(new ApiResponse<Void>(true, "Logged out successfully", null));
    }

    /**
     * Get jobs that the user has applied for
     * @param callback Callback to handle the response
     */
    public void getUserAppliedJobs(final ApiCallback<ApiResponse<Map<String, Object>>> callback) {
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            callback.onError("User not logged in");
            return;
        }

        // Create a custom OkHttp client with additional logging for debugging
        OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(chain -> {
                Request original = chain.request();
                Request request = original.newBuilder()
                        .header("Authorization", "Bearer " + sessionManager.getToken())
                        .header("Accept", "application/json")
                        .method(original.method(), original.body())
                        .build();
                return chain.proceed(request);
            })
            .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build();

        // Create a custom Gson instance that is lenient with JSON parsing
        Gson gson = new GsonBuilder()
            .setLenient()
            .create();

        // Create a custom Retrofit instance with the lenient Gson
        Retrofit customRetrofit = new Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(httpClient)
            .build();

        // Create API service with custom Retrofit
        ApiService customApiService = customRetrofit.create(ApiService.class);

        // Call API with custom service
        Call<ApiResponse<Map<String, Object>>> call = customApiService.getUserAppliedJobs();
        call.enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, 
                              Response<ApiResponse<Map<String, Object>>> response) {
                Log.d("ApiClient", "Applied jobs response code: " + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? 
                            response.errorBody().string() : "Unknown error";
                        Log.e("ApiClient", "Failed to load applied jobs: " + errorBody);
                        callback.onError("Failed to load applied jobs: " + errorBody);
                    } catch (IOException e) {
                        Log.e("ApiClient", "Error reading error body", e);
                        Log.e("ApiClient", Log.getStackTraceString(e));
                        callback.onError("Failed to load applied jobs: " + response.code());
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                Log.e("ApiClient", "getUserAppliedJobs error", t);
                Log.e("ApiClient", Log.getStackTraceString(t));
                Log.e("ApiClient", "Network error", t);
            callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // Helper method to execute API calls
    private <T> void executeCall(Call<ApiResponse<T>> call, final ApiCallback<ApiResponse<T>> callback) {
        call.enqueue(new Callback<ApiResponse<T>>() {
            @Override
            public void onResponse(Call<ApiResponse<T>> call, Response<ApiResponse<T>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<T> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        callback.onSuccess(apiResponse);
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Request failed. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<T>> call, Throwable t) {
                Log.e("ApiClient", "Network error", t);
                Log.e("ApiClient", Log.getStackTraceString(t));
            callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Apply for a job
     * @param jobId The ID of the job to apply for
     * @param callback Callback to handle the response
     */
    public void applyForJob(int jobId, ApiCallback<ApiResponse<Application>> callback) {
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            callback.onError("User not logged in");
            return;
        }

        // Create default cover letter
        String coverLetter = "I am interested in this position.";
        
        // Convert to RequestBody as required by the API
        RequestBody coverLetterBody = RequestBody.create(MediaType.parse("text/plain"), coverLetter);
        
        // Get the token
        String token = sessionManager.getToken();
        if (token == null || token.isEmpty()) {
            callback.onError("Authentication token is missing");
            return;
        }
        
        // Create a new OkHttpClient with the token explicitly added
        OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(chain -> {
                Request original = chain.request();
                Request request = original.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json")
                    .method(original.method(), original.body())
                    .build();
                return chain.proceed(request);
            })
            .build();
        
        // Create a new Retrofit instance with the authenticated client
        Retrofit authenticatedRetrofit = new Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build();
        
        // Create a new API service with the authenticated client
        ApiService authenticatedApiService = authenticatedRetrofit.create(ApiService.class);
        
        // Call the API with the authenticated service
        Call<ApiResponse<Application>> call = authenticatedApiService.applyForJob(
            jobId, 
            coverLetterBody,
            null  // Resume file is null for simplicity
        );
        
        call.enqueue(new Callback<ApiResponse<Application>>() {
            @Override
            public void onResponse(Call<ApiResponse<Application>> call, Response<ApiResponse<Application>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Application> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        callback.onSuccess(apiResponse);
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? 
                            response.errorBody().string() : "Unknown error";
                        callback.onError("Application failed: " + errorBody);
                    } catch (IOException e) {
                        Log.e("ApiClient", Log.getStackTraceString(e));
                        callback.onError("Application failed: " + response.code());
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Application>> call, Throwable t) {
                Log.e("ApiClient", "Network error", t);
                Log.e("ApiClient", Log.getStackTraceString(t));
            callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Apply for a job with employment details
     * @param jobId The ID of the job to apply for
     * @param employmentDetails Map containing the employment details
     * @param callback Callback to handle the response
     */
    public void applyForJobWithDetails(int jobId, Map<String, String> employmentDetails, ApiCallback<ApiResponse<Application>> callback) {
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            callback.onError("User not logged in");
            return;
        }

        // Get the token
        String token = sessionManager.getToken();
        if (token == null || token.isEmpty()) {
            callback.onError("Authentication token is missing");
            return;
        }
        
        // Create a new OkHttpClient with the token explicitly added
        OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(chain -> {
                Request original = chain.request();
                Request request = original.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json")
                    .method(original.method(), original.body())
                    .build();
                return chain.proceed(request);
            })
            .build();
        
        // Create a new Retrofit instance with the authenticated client
        Retrofit authenticatedRetrofit = new Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build();
        
        // Create a new API service with the authenticated client
        ApiService authenticatedApiService = authenticatedRetrofit.create(ApiService.class);
        
        // Call the API with the authenticated service
        Call<ApiResponse<Application>> call = authenticatedApiService.applyForJobWithDetails(
            jobId, 
            employmentDetails
        );
        
        call.enqueue(new Callback<ApiResponse<Application>>() {
            @Override
            public void onResponse(Call<ApiResponse<Application>> call, Response<ApiResponse<Application>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Application> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        callback.onSuccess(apiResponse);
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? 
                            response.errorBody().string() : "Unknown error";
                        callback.onError("Application failed: " + errorBody);
                    } catch (IOException e) {
                        Log.e("ApiClient", Log.getStackTraceString(e));
                        callback.onError("Application failed: " + response.code());
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Application>> call, Throwable t) {
                Log.e("ApiClient", "Network error", t);
                Log.e("ApiClient", Log.getStackTraceString(t));
            callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    private static boolean isUserLoggedIn(Context context) {
        if (!sessionManager.isSessionValid()) {
            sessionManager = SessionManager.getInstance(context);
            Log.d("JobPortal", "User logged in status: " + sessionManager.isLoggedIn());
            Log.d("JobPortal", "User token: " + sessionManager.getToken());
            
            if (!sessionManager.hasToken()) {
                return false;
            }
        }
        return true;
    }

    public static String getAuthToken() {
        return sessionManager.getToken();
    }

    /**
     * Upload contacts to the server
     * @param contactsFile MultipartBody.Part containing the contacts CSV file
     * @param callback Callback to handle the response
     */
    public void uploadContacts(MultipartBody.Part contactsFile, final ApiCallback<ApiResponse<Map<String, Object>>> callback) {
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            callback.onError("User not logged in");
            return;
        }

        Log.d("ApiClient", "Uploading contacts file: " + contactsFile.headers().toString());
        
        // Create a multipart request body
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(contactsFile)
                .build();
        
        // Build request with proper authentication
        Request request = new Request.Builder()
                .url(BuildConfig.API_BASE_URL + "contacts/upload")
                .header("Authorization", "Bearer " + sessionManager.getToken())
                .header("Accept", "application/json")
                .post(requestBody)
                .build();
        
        // Execute the request manually with OkHttp
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .build();
                
        httpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                // Read response body
                String responseBody = "";
                if (response.body() != null) {
                    responseBody = response.body().string();
                }
                
                Log.d("ApiClient", "Contacts upload response code: " + response.code());
                
                // Check if response is HTML (likely a login page) instead of JSON
                boolean isHtmlResponse = responseBody.startsWith("<!DOCTYPE html>") || 
                                         responseBody.startsWith("<html") ||
                                         response.header("Content-Type", "").contains("text/html");
                
                if (isHtmlResponse) {
                    Log.e("ApiClient", "Received HTML response instead of JSON. Session may have expired.");
                    Handler mainHandler = new Handler(Looper.getMainLooper());
                    mainHandler.post(() -> callback.onError("Session expired. Please login again."));
                    return;
                }
                
                if (response.isSuccessful()) {
                    try {
                        // Try to parse as JSON
                        Gson gson = new Gson();
                        ApiResponse<Map<String, Object>> apiResponse = gson.fromJson(responseBody, 
                                new TypeToken<ApiResponse<Map<String, Object>>>(){}.getType());
                        
                        if (apiResponse != null && apiResponse.isSuccess()) {
                            Handler mainHandler = new Handler(Looper.getMainLooper());
                            mainHandler.post(() -> callback.onSuccess(apiResponse));
                        } else {
                            String message = apiResponse != null ? apiResponse.getMessage() : "Unknown error";
                            Handler mainHandler = new Handler(Looper.getMainLooper());
                            mainHandler.post(() -> callback.onError(message));
                        }
                    } catch (Exception e) {
                        Log.e("ApiClient", "Error parsing contacts upload response", e);
                        Handler mainHandler = new Handler(Looper.getMainLooper());
                        mainHandler.post(() -> callback.onError("Error parsing server response: " + e.getMessage()));
                    }
                } else if (response.code() == 401 || response.code() == 403) {
                    Log.e("ApiClient", "Authentication error: " + response.code());
                    // Session expired or unauthorized, try to log out
                    sessionManager.logout();
                    Handler mainHandler = new Handler(Looper.getMainLooper());
                    mainHandler.post(() -> callback.onError("Session expired. Please login again."));
                } else {
                    Handler mainHandler = new Handler(Looper.getMainLooper());
                    mainHandler.post(() -> callback.onError("Failed to upload contacts: " + response.message()));
                }
            }
            
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e("ApiClient", "Contacts upload network error", e);
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }

    /**
     * Reset password with token
     * @param resetData Map containing email, token, password, and password_confirmation
     * @param callback Callback to handle the response
     */
    public void resetPassword(Map<String, String> resetData, final ApiCallback<ApiResponse<Void>> callback) {
        Call<ApiResponse<Void>> call = apiService.resetPassword(resetData);
        executeCall(call, callback);
    }
    
    /**
     * Check for app version updates using the new API
     * @param callback Callback to handle the response
     */
    public void checkAppVersion(final ApiCallback<ApiResponse<AppVersionData>> callback) {
        try {
            // Get current app version info
            int currentVersionCode = getCurrentVersionCode();
            String currentVersionName = getCurrentVersionName();
            
            Log.d("ApiClient", "Checking app version:");
            Log.d("ApiClient", "- Current version code: " + currentVersionCode);
            Log.d("ApiClient", "- Current version name: " + currentVersionName);
            
            // Create version check request for the new API
            VersionCheckRequest request = new VersionCheckRequest("android", currentVersionCode);
            
            Log.d("ApiClient", "Making API call to check-update endpoint");
            
            // Use the new check-update endpoint
            Call<ApiResponse<AppVersionData>> call = apiService.checkUpdate(request);
            call.enqueue(new Callback<ApiResponse<AppVersionData>>() {
                @Override
                public void onResponse(Call<ApiResponse<AppVersionData>> call, Response<ApiResponse<AppVersionData>> response) {
                    Log.d("ApiClient", "Version check API response received");
                    Log.d("ApiClient", "Response code: " + response.code());
                    Log.d("ApiClient", "Response successful: " + response.isSuccessful());
                    
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<AppVersionData> apiResponse = response.body();
                        Log.d("ApiClient", "API response success: " + apiResponse.isSuccess());
                        Log.d("ApiClient", "API response message: " + apiResponse.getMessage());
                        
                        if (apiResponse.isSuccess()) {
                            Log.d("ApiClient", "Calling success callback");
                            callback.onSuccess(apiResponse);
                        } else {
                            Log.e("ApiClient", "API returned error: " + apiResponse.getMessage());
                            callback.onError(apiResponse.getMessage());
                        }
                    } else {
                        String errorMsg = "Failed to check app version: " + response.message();
                        Log.e("ApiClient", errorMsg);
                        callback.onError(errorMsg);
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<AppVersionData>> call, Throwable t) {
                    Log.e("ApiClient", "Version check network error", t);
                    callback.onError("Network error: " + t.getMessage());
                }
            });
            
        } catch (Exception e) {
            Log.e("ApiClient", "Error in checkAppVersion", e);
            callback.onError("Error checking version: " + e.getMessage());
        }
    }
    
    /**
     * Get latest version info using the new API
     * @param callback Callback to handle the response
     */
    public void getLatestVersion(final ApiCallback<ApiResponse<AppVersionData>> callback) {
        try {
            // Get current app version info
            int currentVersionCode = getCurrentVersionCode();
            
            // Create query parameters
            Map<String, String> params = new HashMap<>();
            params.put("platform", "android");
            params.put("current_version_code", String.valueOf(currentVersionCode));
            
            // Use the new latest version endpoint
            Call<ApiResponse<AppVersionData>> call = apiService.getLatestVersion(params);
            call.enqueue(new Callback<ApiResponse<AppVersionData>>() {
                @Override
                public void onResponse(Call<ApiResponse<AppVersionData>> call, Response<ApiResponse<AppVersionData>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<AppVersionData> apiResponse = response.body();
                        if (apiResponse.isSuccess()) {
                            callback.onSuccess(apiResponse);
                        } else {
                            callback.onError(apiResponse.getMessage());
                        }
                    } else {
                        callback.onError("Failed to get latest version: " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<AppVersionData>> call, Throwable t) {
                    Log.e("ApiClient", "Latest version network error", t);
                    callback.onError("Network error: " + t.getMessage());
                }
            });
            
        } catch (Exception e) {
            Log.e("ApiClient", "Error in getLatestVersion", e);
            callback.onError("Error getting latest version: " + e.getMessage());
        }
    }
    
    /**
     * Get current app version code
     */
    private int getCurrentVersionCode() {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            return 1;
        }
    }
    
    /**
     * Get current app version name
     */
    private String getCurrentVersionName() {
        try {
            String versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            return versionName != null ? versionName : "1.0";
        } catch (Exception e) {
            return "1.0";
        }
    }
    
    /**
     * Upload resume file
     * 
     * @param resumeFile The resume file to upload
     * @param callback Callback to handle the response
     */
    public void uploadResume(File resumeFile, final ApiCallback<ApiResponse<User>> callback) {
        if (resumeFile == null) {
            callback.onError("No resume file provided");
            return;
        }
        
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            callback.onError("User not logged in");
            return;
        }
        
        // Create request body for the resume file
        RequestBody requestFile = RequestBody.create(MediaType.parse("application/pdf"), resumeFile);
        
        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part resumePart = MultipartBody.Part.createFormData(
                "resume", resumeFile.getName(), requestFile);
        
        Log.d("ApiClient", "Uploading resume file: " + resumeFile.getName());
        
        // Create the API call
        Call<ApiResponse<User>> call = apiService.uploadResume(resumePart);
        
        // Execute the request
        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                Log.d("ApiClient", "Resume upload response code: " + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();
                    Log.d("ApiClient", "Resume upload success: " + apiResponse.isSuccess() + ", message: " + apiResponse.getMessage());
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        // Update the user in session manager
                        sessionManager.saveUser(apiResponse.getData());
                        callback.onSuccess(apiResponse);
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    try {
                        String errorMessage = response.errorBody() != null ? 
                            response.errorBody().string() : "Unknown error";
                        Log.e("ApiClient", "Resume upload failed with error: " + errorMessage);
                        callback.onError("Failed to upload resume: " + errorMessage);
                    } catch (IOException e) {
                        Log.e("ApiClient", "Error reading response body", e);
                        callback.onError("Failed to upload resume: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                Log.e("ApiClient", "Resume upload network error", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}



