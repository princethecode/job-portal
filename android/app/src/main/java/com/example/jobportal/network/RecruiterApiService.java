package com.example.jobportal.network;

import com.example.jobportal.models.Application;
import com.example.jobportal.models.ApplicationListResponse;
import com.example.jobportal.models.DashboardResponse;
import com.example.jobportal.models.InterviewResponse;
import com.example.jobportal.models.Job;
import com.example.jobportal.models.JobListResponse;
import com.example.jobportal.models.Recruiter;
import com.example.jobportal.models.RecruiterLoginRequest;
import com.example.jobportal.models.RecruiterLoginResponse;
import com.example.jobportal.models.RecruiterProfileResponse;
import com.example.jobportal.models.RecruiterRegisterRequest;
import com.example.jobportal.models.User;
import com.example.jobportal.network.ApiResponse;
import java.util.List;
import java.util.Map;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RecruiterApiService {
    
    // Authentication
    @POST("recruiter/login")
    Call<RecruiterLoginResponse> recruiterLogin(@Body RecruiterLoginRequest loginRequest);
    
    @POST("recruiter/register")
    Call<ApiResponse<Recruiter>> recruiterRegister(@Body RecruiterRegisterRequest registerRequest);
    
    // Profile Management
    @GET("recruiter/profile")
    Call<RecruiterProfileResponse> getProfile();
    
    @POST("recruiter/profile")
    Call<RecruiterProfileResponse> updateProfile(@Body Recruiter recruiter);
    
    @POST("recruiter/logout")
    Call<ApiResponse<Void>> logout();
    
    // Dashboard
    @GET("recruiter/dashboard")
    Call<DashboardResponse> getDashboard();
    
    // Test endpoint for debugging
    @GET("recruiter/test")
    Call<ApiResponse<Object>> testEndpoint();
    
    // Jobs
    @GET("recruiter/jobs")
    Call<JobListResponse> getMyJobs();
    
    @POST("recruiter/jobs")
    Call<ApiResponse<Job>> createJob(@Body Job job);
    
    @PUT("recruiter/jobs/{id}")
    Call<ApiResponse<Job>> updateJob(@Path("id") int jobId, @Body Job job);
    
    @PATCH("recruiter/jobs/{id}/toggle-status")
    Call<ApiResponse<Void>> toggleJobStatus(@Path("id") int jobId);
    
    // Applications
    @GET("recruiter/applications")
    Call<ApplicationListResponse> getApplications(@Query("job_id") Integer jobId,
                                                 @Query("status") String status,
                                                 @Query("search") String search);
    
    @PATCH("recruiter/applications/{id}/status")
    Call<ApiResponse<Application>> updateApplicationStatus(@Path("id") int applicationId, @Body Map<String, String> statusUpdate);
    
    @POST("recruiter/applications/{id}/schedule-interview")
    Call<ApiResponse<Void>> scheduleInterview(@Path("id") int applicationId, @Body Map<String, Object> interviewData);
    
    @GET("recruiter/applications/{id}")
    Call<ApiResponse<Application>> getApplicationDetails(@Path("id") int applicationId);
    
    // Candidates
    @GET("recruiter/candidates")
    Call<ApiResponse<List<User>>> getCandidates(@Query("search") String search,
                                               @Query("filter") String filter,
                                               @Query("page") Integer page);
    
    @GET("recruiter/candidates/{id}")
    Call<ApiResponse<User>> getCandidateProfile(@Path("id") int candidateId);
    
    @PATCH("recruiter/candidates/{id}/toggle-save")
    Call<ApiResponse<Void>> toggleSaveCandidate(@Path("id") int candidateId);
    
    @GET("recruiter/candidates/{id}/download-resume")
    Call<ResponseBody> downloadCandidateResume(@Path("id") int candidateId);
    
    // Interviews
    @GET("recruiter/interviews")
    Call<InterviewResponse> getInterviews(@Query("status") String status,
                                         @Query("date") String date);
    
    @PUT("recruiter/interviews/{id}")
    Call<ApiResponse<InterviewResponse.Interview>> updateInterview(@Path("id") int interviewId, @Body Map<String, Object> interviewData);
    
    @PATCH("recruiter/interviews/{id}/status")
    Call<ApiResponse<Void>> updateInterviewStatus(@Path("id") int interviewId, @Body Map<String, String> statusUpdate);
    
    @POST("recruiter/interviews/{id}/cancel")
    Call<ApiResponse<Void>> cancelInterview(@Path("id") int interviewId, @Body Map<String, String> reason);
    
    // Analytics
    @GET("recruiter/analytics")
    Call<ApiResponse<Object>> getAnalytics(@Query("start_date") String startDate,
                                          @Query("end_date") String endDate);
}
