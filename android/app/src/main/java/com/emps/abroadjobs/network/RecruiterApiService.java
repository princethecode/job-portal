package com.emps.abroadjobs.network;

import com.emps.abroadjobs.models.Application;
import com.emps.abroadjobs.models.ApplicationListResponse;
import com.emps.abroadjobs.models.DashboardResponse;
import com.emps.abroadjobs.models.InterviewResponse;
import com.emps.abroadjobs.models.Job;
import com.emps.abroadjobs.models.JobListResponse;
import com.emps.abroadjobs.models.Recruiter;
import com.emps.abroadjobs.models.RecruiterLoginRequest;
import com.emps.abroadjobs.models.RecruiterLoginResponse;
import com.emps.abroadjobs.models.RecruiterProfileResponse;
import com.emps.abroadjobs.models.RecruiterRegisterRequest;
import com.emps.abroadjobs.models.User;
import com.emps.abroadjobs.network.ApiResponse;
import java.util.List;
import java.util.Map;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RecruiterApiService {
    
    // Authentication
    @POST("recruiter/login")
    Call<RecruiterLoginResponse> recruiterLogin(@Body RecruiterLoginRequest loginRequest);
    
    @POST("recruiter/register")
    Call<ApiResponse<Recruiter>> recruiterRegister(@Body RecruiterRegisterRequest registerRequest);
    
    @Multipart
    @POST("recruiter/company-license")
    Call<ApiResponse<Map<String, Object>>> uploadCompanyLicense(@Part MultipartBody.Part license);
    
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
    
    @GET("recruiter/jobs/{id}")
    Call<ApiResponse<Job>> getJobDetails(@Path("id") int jobId);
    
    @POST("recruiter/jobs")
    Call<ApiResponse<Job>> createJob(@Body Map<String, Object> jobData);
    
    @Multipart
    @POST("recruiter/jobs")
    Call<ApiResponse<Job>> createJobWithImage(@Part("title") okhttp3.RequestBody title,
                                            @Part("location") okhttp3.RequestBody location,
                                            @Part("description") okhttp3.RequestBody description,
                                            @Part("requirements") okhttp3.RequestBody requirements,
                                            @Part("job_type") okhttp3.RequestBody jobType,
                                            @Part("category") okhttp3.RequestBody category,
                                            @Part("experience_level") okhttp3.RequestBody experienceLevel,
                                            @Part("expiry_date") okhttp3.RequestBody expiryDate,
                                            @Part("salary") okhttp3.RequestBody salary,
                                            @Part("benefits") okhttp3.RequestBody benefits,
                                            @Part("skills_required") okhttp3.RequestBody skillsRequired,
                                            @Part MultipartBody.Part image);
    
    @PUT("recruiter/jobs/{id}")
    Call<ApiResponse<Job>> updateJob(@Path("id") int jobId, @Body Map<String, Object> jobData);
    
    @Multipart
    @POST("recruiter/jobs/{id}")
    Call<ApiResponse<Job>> updateJobWithImage(@Path("id") int jobId,
                                            @Part("title") okhttp3.RequestBody title,
                                            @Part("location") okhttp3.RequestBody location,
                                            @Part("description") okhttp3.RequestBody description,
                                            @Part("requirements") okhttp3.RequestBody requirements,
                                            @Part("job_type") okhttp3.RequestBody jobType,
                                            @Part("category") okhttp3.RequestBody category,
                                            @Part("experience_level") okhttp3.RequestBody experienceLevel,
                                            @Part("expiry_date") okhttp3.RequestBody expiryDate,
                                            @Part("salary") okhttp3.RequestBody salary,
                                            @Part("benefits") okhttp3.RequestBody benefits,
                                            @Part("skills_required") okhttp3.RequestBody skillsRequired,
                                            @Part MultipartBody.Part image,
                                            @Part("_method") okhttp3.RequestBody method);
    
    @PATCH("recruiter/jobs/{id}/toggle-status")
    Call<ApiResponse<Void>> toggleJobStatus(@Path("id") int jobId);
    
    @PATCH("recruiter/jobs/{id}/deactivate")
    Call<ApiResponse<Void>> deactivateJob(@Path("id") int jobId);
    
    @PATCH("recruiter/jobs/{id}/activate")
    Call<ApiResponse<Void>> activateJob(@Path("id") int jobId);
    
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
    
    // Contact Management
    @Multipart
    @POST("recruiter/contacts/upload")
    Call<ApiResponse<Map<String, Object>>> uploadContacts(@Part MultipartBody.Part contacts);
    
    @POST("recruiter/update-contact")
    Call<ApiResponse<Recruiter>> updateRecruiterContact(@Body Map<String, String> contactData);
}
