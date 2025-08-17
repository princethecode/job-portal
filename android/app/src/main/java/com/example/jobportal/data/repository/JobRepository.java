package com.example.jobportal.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.jobportal.data.db.AppDatabase;
import com.example.jobportal.data.db.JobDao;
import com.example.jobportal.models.Job;
import com.example.jobportal.models.JobsListResponse;
import com.example.jobportal.network.ApiService;
import com.example.jobportal.network.ApiCallback;
import com.example.jobportal.network.ApiResponse;
import com.example.jobportal.network.ApiClient;

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
        refreshJobs();
        return jobDao.getAllJobs();
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
        executorService.execute(() -> {
            apiService.getJobs().enqueue(new Callback<JobsListResponse>() {
                @Override
                public void onResponse(Call<JobsListResponse> call, Response<JobsListResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Job> jobs = response.body().getData().getJobs();
                        executorService.execute(() -> jobDao.insertAll(jobs));
                    }
                }

                @Override
                public void onFailure(Call<JobsListResponse> call, Throwable t) {
                    // Handle error
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