package com.example.jobportal.recruiter.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobportal.R;
import com.example.jobportal.models.Job;
import com.example.jobportal.models.JobListResponse;
import com.example.jobportal.network.ApiClient;
import com.example.jobportal.recruiter.adapters.RecentJobsAdapter;
import retrofit2.Call;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class RecruiterJobsFragment extends Fragment {
    
    private RecyclerView rvJobs;
    private RecentJobsAdapter jobsAdapter;
    private View progressBar;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recruiter_jobs, container, false);
        
        initializeViews(view);
        setupRecyclerView();
        loadJobs();
        
        return view;
    }
    
    private void initializeViews(View view) {
        // Use the correct view ID from the layout
        rvJobs = view.findViewById(R.id.rv_jobs);
        
        progressBar = view.findViewById(R.id.ll_loading);
        if (progressBar == null) {
            // Try alternative ID
            progressBar = view.findViewById(R.id.progress_bar);
        }
    }
    
    private void setupRecyclerView() {
        jobsAdapter = new RecentJobsAdapter(new ArrayList<>());
        rvJobs.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvJobs.setAdapter(jobsAdapter);
    }
    
    private void loadJobs() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        ApiClient.getRecruiterApiService().getMyJobs().enqueue(new retrofit2.Callback<JobListResponse>() {
            @Override
            public void onResponse(Call<JobListResponse> call, Response<JobListResponse> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    JobListResponse jobListResponse = response.body();
                    if (jobListResponse.isSuccess() && jobListResponse.getData() != null) {
                        List<Job> jobs = jobListResponse.getData().getJobs();
                        if (jobs != null) {
                            jobsAdapter.updateJobs(jobs);
                        }
                    } else {
                        showError("Failed to load jobs: " + jobListResponse.getMessage());
                    }
                } else {
                    showError("Failed to load jobs");
                }
            }
            
            @Override
            public void onFailure(Call<JobListResponse> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                showError("Network error: " + t.getMessage());
            }
        });
    }
    
    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
