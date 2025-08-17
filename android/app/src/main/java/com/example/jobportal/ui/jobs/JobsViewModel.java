package com.example.jobportal.ui.jobs;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
// Update to use the correct Job class
import com.example.jobportal.models.Job;
// Add the correct import for ApiResponse
import com.example.jobportal.models.JobsListResponse;
import com.example.jobportal.network.ApiResponse;
import com.example.jobportal.network.ApiClient;
import com.example.jobportal.network.ApiService;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.jobportal.data.repository.JobRepository;
import com.example.jobportal.network.ApiCallback;

public class JobsViewModel extends AndroidViewModel {
    private final JobRepository repository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public JobsViewModel(@NonNull Application application) {
        super(application);
        repository = new JobRepository(application, ApiClient.getInstance(application).getApiService());
    }

    public LiveData<List<Job>> getJobs() {
        return repository.getAllJobs();
    }

    public LiveData<Job> getJobById(String jobId) {
        return repository.getJobById(jobId);
    }

    public LiveData<List<Job>> searchJobs(String query) {
        return repository.searchJobs(query);
    }
    
    public LiveData<List<Job>> getJobsByCategory(String category) {
        isLoading.setValue(true);
        LiveData<List<Job>> jobs = repository.getJobsByCategory(category);
        isLoading.setValue(false);
        return jobs;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void refreshJobs() {
        isLoading.setValue(true);
        repository.getAllJobs();
        isLoading.setValue(false);
    }

    public void setError(String errorMessage) {
        error.setValue(errorMessage);
    }

    public void incrementShareCount(String jobId, ApiCallback<Void> callback) {
        repository.incrementShareCount(jobId, callback);
    }
}