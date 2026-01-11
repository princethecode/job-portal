package com.emps.abroadjobs.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.emps.abroadjobs.data.db.AppDatabase;
import com.emps.abroadjobs.data.db.JobDao;
import com.emps.abroadjobs.models.Job;
import com.emps.abroadjobs.models.JobsListResponse;
import com.emps.abroadjobs.network.ApiService;
import com.emps.abroadjobs.network.ApiCallback;
import com.emps.abroadjobs.network.ApiResponse;
import com.emps.abroadjobs.network.ApiClient;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JobRepository {
    private final JobDao jobDao;
    private final ApiService apiService;
    private final ExecutorService executorService;
    private final Context context;

    public JobRepository(Context context, ApiService apiService) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.jobDao = db.jobDao();
        this.apiService = apiService;
        this.executorService = Executors.newSingleThreadExecutor();
        this.context = context;
    }

    public LiveData<List<Job>> getAllJobs() {
        android.util.Log.d("JobRepository", "📋 getAllJobs called");
        refreshJobs();
        LiveData<List<Job>> jobs = jobDao.getAllJobs();
        android.util.Log.d("JobRepository", "📋 Returning LiveData from database");
        return jobs;
    }

    public LiveData<Job> getJobById(String jobId) {
        refreshJob(jobId);
        return jobDao.getJobById(jobId);
    }

    public LiveData<List<Job>> searchJobs(String query) {
        return jobDao.searchJobs(query);
    }
    
    public LiveData<List<Job>> getJobsByCategory(String category) {
        refreshJobs(); // Refresh jobs to ensure we have the latest data
        return jobDao.getJobsByCategory(category);
    }

    private void refreshJobs() {
        android.util.Log.d("JobRepository", "🔄 Starting refreshJobs API call");
        executorService.execute(() -> {
            apiService.getJobs().enqueue(new Callback<JobsListResponse>() {
                @Override
                public void onResponse(Call<JobsListResponse> call, Response<JobsListResponse> response) {
                    android.util.Log.d("JobRepository", "📡 API Response received - Code: " + response.code());
                    if (response.isSuccessful() && response.body() != null) {
                        JobsListResponse jobsResponse = response.body();
                        android.util.Log.d("JobRepository", "✅ Response successful: " + jobsResponse.isSuccess());
                        
                        if (jobsResponse.isSuccess() && jobsResponse.getData() != null) {
                            List<Job> jobs = jobsResponse.getData().getJobs();
                            android.util.Log.d("JobRepository", "📋 Jobs received: " + (jobs != null ? jobs.size() : 0));
                            
                            if (jobs != null && !jobs.isEmpty()) {
                                executorService.execute(() -> {
                                    try {
                                        android.util.Log.d("JobRepository", "💾 Inserting " + jobs.size() + " jobs into database");
                                        jobDao.insertAll(jobs);
                                        android.util.Log.d("JobRepository", "✅ Jobs inserted successfully");
                                    } catch (Exception e) {
                                        android.util.Log.e("JobRepository", "❌ Error inserting jobs into database: " + e.getMessage(), e);
                                    }
                                });
                            } else {
                                android.util.Log.w("JobRepository", "⚠️ No jobs in response");
                            }
                        } else {
                            android.util.Log.w("JobRepository", "⚠️ Response not successful or data is null");
                        }
                    } else {
                        android.util.Log.e("JobRepository", "❌ Response not successful or body is null - Code: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<JobsListResponse> call, Throwable t) {
                    android.util.Log.e("JobRepository", "❌ API call failed: " + t.getMessage(), t);
                }
            });
        });
    }

    private void refreshJob(String jobId) {
        executorService.execute(() -> {
            apiService.getJobDetails(Integer.parseInt(jobId)).enqueue(new Callback<ApiResponse<Job>>() {
                @Override
                public void onResponse(Call<ApiResponse<Job>> call, Response<ApiResponse<Job>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Job job = response.body().getData();
                        if (job != null) {
                            executorService.execute(() -> jobDao.insert(job));
                        }
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Job>> call, Throwable t) {
                    // Handle error
                }
            });
        });
    }

    public void incrementShareCount(String jobId, ApiCallback<Void> callback) {
        android.util.Log.d("JobRepository", "🚀 Starting API call to increment share count for job ID: " + jobId);
        
        // Use ApiClient method instead of direct ApiService call
        ApiClient apiClient = ApiClient.getInstance(context);
        apiClient.incrementShareCount(jobId, new ApiCallback<ApiResponse<java.util.Map<String, Object>>>() {
            @Override
            public void onSuccess(ApiResponse<java.util.Map<String, Object>> response) {
                android.util.Log.d("JobRepository", "✅ Share count API call successful");
                
                // Update local database with new share count
                executorService.execute(() -> {
                    Job job = jobDao.getJobByIdSync(jobId);
                    if (job != null) {
                        int oldCount = job.getShareCount();
                        job.setShareCount(oldCount + 1);
                        jobDao.insert(job);
                        android.util.Log.d("JobRepository", "💾 Local database updated - Old count: " + oldCount + ", New count: " + job.getShareCount());
                    } else {
                        android.util.Log.w("JobRepository", "⚠️ Job not found in local database for ID: " + jobId);
                    }
                });
                
                if (callback != null) {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("JobRepository", "❌ Share count API call failed: " + error);
                if (callback != null) {
                    callback.onError(error);
                }
            }
        });
    }
} 