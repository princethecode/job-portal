package com.example.jobportal.network;

import android.content.Context;
import android.util.Log;

import com.example.jobportal.BuildConfig;
import com.example.jobportal.models.User;
import com.example.jobportal.models.LoginResponse;
import com.example.jobportal.utils.SessionManager;
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

public class ApiClient {
    private static Retrofit retrofit = null;
    private static final int REQUEST_TIMEOUT = 60;
    private static ApiService apiService;
    private static SessionManager sessionManager;
    private static Context context;
    private static ApiClient instance;

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
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", "Bearer " + sessionManager.getToken())
                            .header("Accept", "application/json")
                            .method(original.method(), original.body());

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                });
            }

            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
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
        sessionManager.updateToken(token);
    }

    public static void clearAuthToken() {
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
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void register(String fullName, String email, String mobile, String password, 
                        File resumeFile, final ApiCallback<ApiResponse<User>> callback) {
        
        Map<String, String> registerData = new HashMap<>();
        registerData.put("name", fullName);
        registerData.put("email", email);
        registerData.put("mobile", mobile);
        registerData.put("password", password);
        registerData.put("password_confirmation", password);
        
        // Create multipart request if resume file is provided
        if (resumeFile != null) {
            RequestBody requestFile = RequestBody.create(MediaType.parse("application/pdf"), resumeFile);
            MultipartBody.Part resumePart = MultipartBody.Part.createFormData("resume", resumeFile.getName(), requestFile);
            
            RequestBody fullNameBody = RequestBody.create(MediaType.parse("text/plain"), fullName);
            RequestBody emailBody = RequestBody.create(MediaType.parse("text/plain"), email);
            RequestBody mobileBody = RequestBody.create(MediaType.parse("text/plain"), mobile);
            RequestBody passwordBody = RequestBody.create(MediaType.parse("text/plain"), password);
            RequestBody passwordConfirmationBody = RequestBody.create(MediaType.parse("text/plain"), password);
            
            Call<ApiResponse<User>> call = apiService.uploadResume(resumePart);
            executeCall(call, callback);
        } else {
            Call<ApiResponse<User>> call = apiService.register(registerData);
            executeCall(call, callback);
        }
    }

    public void forgotPassword(String email, final ApiCallback<ApiResponse<Void>> callback) {
        Map<String, String> emailData = new HashMap<>();
        emailData.put("email", email);
        
        Call<ApiResponse<Void>> call = apiService.forgotPassword(emailData);
        executeCall(call, callback);
    }

    public void getUserProfile(final ApiCallback<ApiResponse<User>> callback) {
        Call<ApiResponse<User>> call = apiService.getUserProfile();
        executeCall(call, callback);
    }


    public void updateUserProfile(User user, final ApiCallback<ApiResponse<User>> callback) {
        Map<String, String> profileData = new HashMap<>();
        profileData.put("name", user.getFullName());
        profileData.put("email", user.getEmail());
        profileData.put("mobile", user.getPhone());
        
        Call<ApiResponse<User>> call = apiService.updateUserProfile(profileData);
        executeCall(call, callback);
    }

    public void changePassword(String currentPassword, String newPassword, final ApiCallback<ApiResponse<Void>> callback) {
        // This method is not in the ApiService yet
        Map<String, String> passwordData = new HashMap<>();
        passwordData.put("current_password", currentPassword);
        passwordData.put("new_password", newPassword);
        
        // Add this to ApiService in the future
         Call<ApiResponse<Void>> call = apiService.changePassword(passwordData);
        // executeCall(call, callback);
        
        // For now, show an error
        callback.onError("Password change functionality not implemented yet");
    }

    public void logout(final ApiCallback<ApiResponse<Void>> callback) {
        // This method is not in the ApiService yet
        Call<ApiResponse<Void>> call = apiService.logout();
        
        // For now, just clear the token locally
        clearAuthToken();
        // Fix: Use the correct constructor for ApiResponse<Void>
        callback.onSuccess(new ApiResponse<Void>(true, "Logged out successfully", null));
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
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Apply for a job
     * @param jobId The ID of the job to apply for
     * @param callback Callback to handle the response
     */
    public void applyForJob(int jobId, ApiCallback<ApiResponse<Void>> callback) {
        // Create default cover letter
        String coverLetter = "I am interested in this position.";
        
        // Convert to RequestBody as required by the API
        RequestBody coverLetterBody = RequestBody.create(MediaType.parse("text/plain"), coverLetter);
        
        // Check if token exists and log its status
        if (!sessionManager.hasToken()) {
            // Force refresh the session manager to ensure we're getting the latest state
            sessionManager = SessionManager.getInstance(context);
            Log.d("JobPortal", "User logged in status: " + sessionManager.isLoggedIn());
            Log.d("JobPortal", "User token: " + sessionManager.getToken());
            // Double-check after refresh
            if (!sessionManager.hasToken()) {
                callback.onError("You must be logged in to apply for jobs");
                return;
            }
        }
        
        // Create a new OkHttpClient with the token explicitly added
        OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(chain -> {
                String token = sessionManager.getToken();
                Request original = chain.request();
                Request request = original.newBuilder()
                    .header("Authorization", "Bearer " + token)
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
        Call<ApiResponse<com.example.jobportal.models.Application>> call = authenticatedApiService.applyForJob(
            jobId, 
            coverLetterBody,
            null  // Resume file is null for simplicity
        );
        
        call.enqueue(new Callback<ApiResponse<com.example.jobportal.models.Application>>() {
            @Override
            public void onResponse(Call<ApiResponse<com.example.jobportal.models.Application>> call, 
                                 Response<ApiResponse<com.example.jobportal.models.Application>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<com.example.jobportal.models.Application> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        // Convert to the expected void response
                        ApiResponse<Void> voidResponse = new ApiResponse<>(
                            apiResponse.isSuccess(),
                            apiResponse.getMessage(),
                            null
                        );
                        callback.onSuccess(voidResponse);
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? 
                            response.errorBody().string() : "Unknown error";
                        callback.onError("Application failed: " + errorBody);
                    } catch (IOException e) {
                        callback.onError("Application failed: " + response.code());
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<com.example.jobportal.models.Application>> call, Throwable t) {
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
}



