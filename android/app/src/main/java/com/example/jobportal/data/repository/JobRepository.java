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

    public JobRepository(Context context, ApiService apiService) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.jobDao = db.jobDao();
        this.apiService = apiService;
        this.executorService = Executors.newSingleThreadExecutor();
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
} 