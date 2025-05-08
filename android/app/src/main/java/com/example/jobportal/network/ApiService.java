package com.example.jobportal.network;

import com.example.jobportal.models.Application;
import com.example.jobportal.models.Job;
import com.example.jobportal.models.JobsListResponse;
import com.example.jobportal.models.User;
import com.example.jobportal.models.Notification;
import com.example.jobportal.models.LoginResponse;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface ApiService {
    
    // Authentication endpoints
    @POST("login")
    Call<ApiResponse<LoginResponse>> login(@Body Map<String, String> loginData);
    
    @POST("register")
    Call<ApiResponse<User>> register(@Body Map<String, String> registerData);
    
    @POST("forgot-password")
    Call<ApiResponse<Void>> forgotPassword(@Body Map<String, String> emailData);
    
    @GET("user")
    Call<ApiResponse<User>> getUserProfile();
    
    @PUT("profile")
    Call<ApiResponse<User>> updateUserProfile(@Body Map<String, String> profileData);
    
    @Multipart
    @POST("profile/resume")
    Call<ApiResponse<User>> uploadResume(
            @Part MultipartBody.Part resume
    );
    
    // Job endpoints
    // Add this if it doesn't exist or update if it does
    @GET("jobs")
    Call<JobsListResponse> getJobs();

    @GET("jobs")
    Call<ApiResponse<List<Job>>> getJobsWithFilters(@QueryMap Map<String, String> filters);


    @GET("jobs/{id}")
    Call<ApiResponse<Job>> getJobDetails(@Path("id") int jobId);

    // Application endpoints
    @Multipart
    @POST("jobs/{id}/apply")
    Call<ApiResponse<Application>> applyForJob(
            @Path("id") int jobId,
            @Part("cover_letter") RequestBody coverLetter,
            @Part MultipartBody.Part resume
    );

    @GET("applications")
    Call<ApiResponse<List<Application>>> getUserApplications();

    @GET("applications/{id}")
    Call<ApiResponse<Application>> getApplicationDetails(@Path("id") int applicationId);

    @GET("notifications")
    Call<ApiResponse<List<Notification>>> getNotifications();
    
    @POST("logout")
    Call<ApiResponse<Void>> logout();
    
    @POST("change-password")
    Call<ApiResponse<Void>> changePassword(@Body Map<String, String> passwordData);
    

}
