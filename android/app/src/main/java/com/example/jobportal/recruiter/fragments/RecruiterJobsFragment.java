package com.example.jobportal.recruiter.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobportal.R;
import com.example.jobportal.models.Job;
import com.example.jobportal.models.JobListResponse;
import com.example.jobportal.network.ApiClient;
import com.example.jobportal.recruiter.JobPostingActivity;
import com.example.jobportal.recruiter.adapters.RecentJobsAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.button.MaterialButton;
import retrofit2.Call;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class RecruiterJobsFragment extends Fragment {
    
    private RecyclerView rvJobs;
    private RecentJobsAdapter jobsAdapter;
    private View progressBar;
    private FloatingActionButton fabAddJob;
    private MaterialButton btnPostFirstJob;
    private View llEmptyState;
    private ActivityResultLauncher<Intent> jobPostingLauncher;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize the job posting launcher
        jobPostingLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    boolean jobPosted = result.getData().getBooleanExtra("job_posted", false);
                    if (jobPosted) {
                        // Refresh the jobs list
                        loadJobs();
                        Toast.makeText(getContext(), "Job posted successfully!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recruiter_jobs, container, false);
        
        initializeViews(view);
        setupRecyclerView();
        setupClickListeners();
        loadJobs();
        
        return view;
    }
    
    private void initializeViews(View view) {
        rvJobs = view.findViewById(R.id.rv_jobs);
        fabAddJob = view.findViewById(R.id.fab_add_job);
        btnPostFirstJob = view.findViewById(R.id.btn_post_first_job);
        llEmptyState = view.findViewById(R.id.ll_empty_state);
        progressBar = view.findViewById(R.id.ll_loading);
    }
    
    private void setupRecyclerView() {
        jobsAdapter = new RecentJobsAdapter(new ArrayList<>());
        rvJobs.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvJobs.setAdapter(jobsAdapter);
    }
    
    private void setupClickListeners() {
        if (fabAddJob != null) {
            fabAddJob.setOnClickListener(v -> openJobPostingActivity());
        }
        
        if (btnPostFirstJob != null) {
            btnPostFirstJob.setOnClickListener(v -> openJobPostingActivity());
        }
    }
    
    private void openJobPostingActivity() {
        Intent intent = new Intent(getActivity(), JobPostingActivity.class);
        jobPostingLauncher.launch(intent);
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
                        if (jobs != null && !jobs.isEmpty()) {
                            jobsAdapter.updateJobs(jobs);
                            showJobsList();
                        } else {
                            showEmptyState();
                        }
                    } else {
                        showError("Failed to load jobs: " + jobListResponse.getMessage());
                        showEmptyState();
                    }
                } else {
                    showError("Failed to load jobs");
                    showEmptyState();
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
    
    private void showJobsList() {
        if (rvJobs != null) rvJobs.setVisibility(View.VISIBLE);
        if (llEmptyState != null) llEmptyState.setVisibility(View.GONE);
    }
    
    private void showEmptyState() {
        if (rvJobs != null) rvJobs.setVisibility(View.GONE);
        if (llEmptyState != null) llEmptyState.setVisibility(View.VISIBLE);
    }
}
