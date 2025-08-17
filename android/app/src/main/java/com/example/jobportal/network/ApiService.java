package com.example.jobportal.network;

import com.example.jobportal.models.Application;
import com.example.jobportal.models.Experience;
import com.example.jobportal.models.Job;
import com.example.jobportal.models.JobsListResponse;
import com.example.jobportal.models.User;
import com.example.jobportal.models.Notification;
import com.example.jobportal.models.LoginResponse;
import com.example.jobportal.models.FeaturedJob;
import com.example.jobportal.data.model.AppVersionResponse;
import com.example.jobportal.data.model.AppVersionData;
import com.example.jobportal.data.api.VersionCheckRequest;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.http.Headers;

public interface ApiService {
    
    // Authentication endpoints
    @POST("login")
    Call<ApiResponse<LoginResponse>> login(@Body Map<String, String> loginData);
    
    @POST("login/google")
    Call<ApiResponse<LoginResponse>> loginWithGoogle(@Body Map<String, String> googleData);
    
    @POST("register")
    Call<ApiResponse<User>> register(@Body Map<String, String> registerData);
    
    @POST("register/google")
    Call<ApiResponse<User>> registerWithGoogle(@Body Map<String, String> googleData);
    
    @POST("forgot-password")
    Call<ApiResponse<Object>> forgotPassword(@Body Map<String, String> emailData);
    
    @POST("reset-password")
    Call<ApiResponse<Void>> resetPassword(@Body Map<String, String> resetData);
    
    @GET("user")
    Call<User> getUserProfile();
    
    @POST("profile")
    Call<ApiResponse<User>> updateUserProfile(@Body Map<String, String> profileData);
    
    @Multipart
    @POST("profile/resume")
    Call<ApiResponse<User>> uploadResume(
            @Part MultipartBody.Part resume
    );
    
    @Multipart
    @POST("profile/photo")
    Call<ApiResponse<User>> uploadProfilePhoto(
            @Part MultipartBody.Part photo
    );

    // Contact Upload Endpoint
    @Multipart
    @POST("contacts/upload")
    Call<ApiResponse<Map<String, Object>>> uploadContacts(
            @Part MultipartBody.Part contacts
    );

    // Job endpoints
    // Add this if it doesn't exist or update if it does
    @GET("jobs")
    Call<JobsListResponse> getJobs();

    @GET("jobs")
    Call<ApiResponse<List<Job>>> getJobsWithFilters(@QueryMap Map<String, String> filters);


    @GET("jobs/{id}")
    Call<ApiResponse<Job>> getJobDetails(@Path("id") int jobId);

    // Share count endpoint
    @POST("jobs/{id}/share")
    Call<ApiResponse<Map<String, Object>>> incrementShareCount(@Path("id") String jobId);

    // Application endpoints
    @Multipart
    @POST("jobs/{id}/apply")
    Call<ApiResponse<Application>> applyForJob(
            @Path("id") int jobId,
            @Part("cover_letter") RequestBody coverLetter,
            @Part MultipartBody.Part resume
    );
    
    // New endpoint for job application with employment details
    @POST("jobs/{id}/apply-with-details")
    Call<ApiResponse<Application>> applyForJobWithDetails(
            @Path("id") int jobId,
            @Body Map<String, String> employmentDetails
    );

    @GET("applications")
    Call<ApiResponse<List<Application>>> getUserApplications();
    
    @GET("user/applied-jobs")
    Call<ApiResponse<Map<String, Object>>> getUserAppliedJobs();

    @GET("applications/{id}")
    Call<ApiResponse<Application>> getApplicationDetails(@Path("id") int applicationId);

    // Experience endpoints
    @GET("experiences")
    Call<ApiResponse<List<Experience>>> getUserExperiences();
    
    @POST("experiences")
    Call<ApiResponse<Experience>> addExperience(@Body Experience experience);
    
    @GET("experiences/{id}")
    Call<ApiResponse<Experience>> getExperienceDetails(@Path("id") long experienceId);
    
    @PUT("experiences/{id}")
    Call<ApiResponse<Experience>> updateExperience(@Path("id") long experienceId, @Body Experience experience);
    
    @DELETE("experiences/{id}")
    Call<ApiResponse<Void>> deleteExperience(@Path("id") long experienceId);

    // Notification endpoints
    @GET("notifications")
    Call<ApiResponse<List<Notification>>> getNotifications();
    
    @POST("notifications/{id}/read")
    Call<ApiResponse<Void>> markNotificationAsRead(@Path("id") String notificationId);
    
    @POST("logout")
    Call<ApiResponse<Void>> logout();
    
    @POST("change-password")
    Call<ApiResponse<Void>> changePassword(@Body Map<String, String> passwordData);
    
    // Add contact update endpoint
    @POST("users/update-contact")
    @Headers({
        "Accept: application/json",
        "Content-Type: application/json"
    })
    Call<ApiResponse<User>> updateUserContact(@Body Map<String, String> contactData);

    // Add FCM token registration endpoint
    @POST("users/register-fcm-token")
    Call<ResponseBody> registerFcmToken(@Body Map<String, String> tokenData);

    @GET("featured-jobs")
    Call<ApiResponse<List<FeaturedJob>>> getFeaturedJobs();
    
    @GET("featured-jobs/{id}")
    Call<ApiResponse<FeaturedJob>> getFeaturedJobDetails(@Path("id") int jobId);
    
    // App Version Check endpoints
    @GET("app-versions/latest")
    Call<ApiResponse<AppVersionData>> getLatestVersion(@QueryMap Map<String, String> params);
    
    @POST("app-versions/check-update")
    Call<ApiResponse<AppVersionData>> checkUpdate(@Body VersionCheckRequest request);
}
