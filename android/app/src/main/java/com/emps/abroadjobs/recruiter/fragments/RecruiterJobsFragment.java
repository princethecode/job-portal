package com.emps.abroadjobs.recruiter.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.emps.abroadjobs.R;
import com.emps.abroadjobs.models.Job;
import com.emps.abroadjobs.models.JobListResponse;
import com.emps.abroadjobs.network.ApiClient;
import com.emps.abroadjobs.recruiter.JobPostingActivity;
import com.emps.abroadjobs.recruiter.adapters.RecentJobsAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RecruiterJobsFragment extends Fragment {
    
    private RecyclerView rvJobs;
    private RecentJobsAdapter jobsAdapter;
    private View progressBar;
    private FloatingActionButton fabAddJob;
    private MaterialButton btnPostFirstJob;
    private View llEmptyState;
    private ActivityResultLauncher<Intent> jobPostingLauncher;
    private ChipGroup chipGroupFilters;
    private Chip chipAll, chipActive, chipInactive, chipDraft;
    private TextInputEditText etSearchJobs;
    
    // Store all jobs for filtering
    private List<Job> allJobs = new ArrayList<>();
    private String currentFilter = "all";
    private String currentSearchQuery = "";
    
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
        setupFilterChips();
        setupSearchBar();
        loadJobs();
        
        return view;
    }
    
    private void initializeViews(View view) {
        rvJobs = view.findViewById(R.id.rv_jobs);
        fabAddJob = view.findViewById(R.id.fab_add_job);
        btnPostFirstJob = view.findViewById(R.id.btn_post_first_job);
        llEmptyState = view.findViewById(R.id.ll_empty_state);
        progressBar = view.findViewById(R.id.ll_loading);
        chipGroupFilters = view.findViewById(R.id.chip_group_filters);
        chipAll = view.findViewById(R.id.chip_all);
        chipActive = view.findViewById(R.id.chip_active);
        chipInactive = view.findViewById(R.id.chip_inactive);
        chipDraft = view.findViewById(R.id.chip_draft);
        etSearchJobs = view.findViewById(R.id.et_search_jobs);
    }
    
    private void setupRecyclerView() {
        jobsAdapter = new RecentJobsAdapter(new ArrayList<>());
        jobsAdapter.setOnJobActionListener(() -> {
            // Refresh jobs list when a job is updated
            loadJobs();
        });
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
    
    private void setupFilterChips() {
        if (chipGroupFilters != null) {
            chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.isEmpty()) {
                    return;
                }
                
                int checkedId = checkedIds.get(0);
                
                if (checkedId == R.id.chip_all) {
                    currentFilter = "all";
                } else if (checkedId == R.id.chip_active) {
                    currentFilter = "active";
                } else if (checkedId == R.id.chip_inactive) {
                    currentFilter = "inactive";
                } else if (checkedId == R.id.chip_draft) {
                    currentFilter = "draft";
                }
                
                applyFilters();
            });
        }
    }
    
    private void setupSearchBar() {
        if (etSearchJobs != null) {
            etSearchJobs.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchQuery = s.toString().toLowerCase().trim();
                    applyFilters();
                }
                
                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }
    
    private void applyFilters() {
        List<Job> filteredJobs = new ArrayList<>(allJobs);
        
        // Apply status filter
        if (!currentFilter.equals("all")) {
            filteredJobs = filteredJobs.stream()
                .filter(job -> {
                    switch (currentFilter) {
                        case "active":
                            return job.isActive() && 
                                   (job.getApprovalStatus() == null || 
                                    !job.getApprovalStatus().equalsIgnoreCase("draft"));
                        case "inactive":
                            return !job.isActive() && 
                                   (job.getApprovalStatus() == null || 
                                    !job.getApprovalStatus().equalsIgnoreCase("draft"));
                        case "draft":
                            return job.getApprovalStatus() != null && 
                                   job.getApprovalStatus().equalsIgnoreCase("draft");
                        default:
                            return true;
                    }
                })
                .collect(Collectors.toList());
        }
        
        // Apply search filter
        if (!currentSearchQuery.isEmpty()) {
            filteredJobs = filteredJobs.stream()
                .filter(job -> {
                    String title = job.getTitle() != null ? job.getTitle().toLowerCase() : "";
                    String company = job.getCompany() != null ? job.getCompany().toLowerCase() : "";
                    String location = job.getLocation() != null ? job.getLocation().toLowerCase() : "";
                    String category = job.getCategory() != null ? job.getCategory().toLowerCase() : "";
                    
                    return title.contains(currentSearchQuery) ||
                           company.contains(currentSearchQuery) ||
                           location.contains(currentSearchQuery) ||
                           category.contains(currentSearchQuery);
                })
                .collect(Collectors.toList());
        }
        
        // Update adapter
        if (filteredJobs.isEmpty()) {
            showEmptyState();
        } else {
            jobsAdapter.updateJobs(filteredJobs);
            showJobsList();
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
                            allJobs = new ArrayList<>(jobs);
                            applyFilters();
                        } else {
                            allJobs.clear();
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