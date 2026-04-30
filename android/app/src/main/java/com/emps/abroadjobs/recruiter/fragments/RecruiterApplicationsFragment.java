package com.emps.abroadjobs.recruiter.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.emps.abroadjobs.R;
import com.emps.abroadjobs.models.Application;
import com.emps.abroadjobs.models.ApplicationListResponse;
import com.emps.abroadjobs.network.ApiClient;
import com.emps.abroadjobs.recruiter.adapters.ApplicationsAdapter;
import com.emps.abroadjobs.recruiter.RecruiterApplicationDetailsActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RecruiterApplicationsFragment extends Fragment {
    
    private RecyclerView rvApplications;
    private TextView tvApplicationsCount;
    private View progressBar;
    private ApplicationsAdapter applicationsAdapter;
    private ChipGroup chipGroupFilters;
    private Chip chipAll, chipPending, chipReviewing, chipShortlisted, chipRejected, chipHired;
    private TextInputEditText etSearchApplications;
    
    // Store all applications for filtering
    private List<Application> allApplications = new ArrayList<>();
    private String currentFilter = "all";
    private String currentSearchQuery = "";
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recruiter_applications, container, false);
        
        initializeViews(view);
        setupRecyclerView();
        setupFilterChips();
        setupSearchBar();
        loadApplications();
        
        return view;
    }
    
    private void initializeViews(View view) {
        // Use the correct view IDs from the layout
        rvApplications = view.findViewById(R.id.rv_applications);
        tvApplicationsCount = view.findViewById(R.id.tv_total_applications);
        progressBar = view.findViewById(R.id.ll_loading);
        
        if (progressBar == null) {
            // Try alternative ID
            progressBar = view.findViewById(R.id.progress_bar);
        }
        
        // Initialize filter chips
        chipGroupFilters = view.findViewById(R.id.chip_group_filters);
        chipAll = view.findViewById(R.id.chip_all);
        chipPending = view.findViewById(R.id.chip_pending);
        chipReviewing = view.findViewById(R.id.chip_reviewing);
        chipShortlisted = view.findViewById(R.id.chip_shortlisted);
        chipRejected = view.findViewById(R.id.chip_rejected);
        chipHired = view.findViewById(R.id.chip_hired);
        etSearchApplications = view.findViewById(R.id.et_search_applications);
    }
    
    private void setupRecyclerView() {
        rvApplications.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        applicationsAdapter = new ApplicationsAdapter();
        applicationsAdapter.setOnApplicationClickListener(this::onApplicationClick);
        rvApplications.setAdapter(applicationsAdapter);
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
                } else if (checkedId == R.id.chip_pending) {
                    currentFilter = "pending";
                } else if (checkedId == R.id.chip_reviewing) {
                    currentFilter = "reviewing";
                } else if (checkedId == R.id.chip_shortlisted) {
                    currentFilter = "shortlisted";
                } else if (checkedId == R.id.chip_rejected) {
                    currentFilter = "rejected";
                } else if (checkedId == R.id.chip_hired) {
                    currentFilter = "hired";
                }
                
                applyFilters();
            });
        }
    }
    
    private void setupSearchBar() {
        if (etSearchApplications != null) {
            etSearchApplications.addTextChangedListener(new TextWatcher() {
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
        List<Application> filteredApplications = new ArrayList<>(allApplications);
        
        // Apply status filter
        if (!currentFilter.equals("all")) {
            filteredApplications = filteredApplications.stream()
                .filter(application -> {
                    String status = application.getStatus();
                    if (status == null) {
                        return false;
                    }
                    
                    // Normalize status for comparison
                    String normalizedStatus = status.toLowerCase().trim();
                    String normalizedFilter = currentFilter.toLowerCase().trim();
                    
                    // Handle "reviewing" matching both "reviewing" and "under review"
                    if (normalizedFilter.equals("reviewing")) {
                        return normalizedStatus.equals("reviewing") || 
                               normalizedStatus.equals("under review");
                    }
                    
                    return normalizedStatus.equals(normalizedFilter);
                })
                .collect(Collectors.toList());
        }
        
        // Apply search filter
        if (!currentSearchQuery.isEmpty()) {
            filteredApplications = filteredApplications.stream()
                .filter(application -> {
                    String jobTitle = application.getJobTitle() != null ? 
                        application.getJobTitle().toLowerCase() : "";
                    String userName = application.getUser() != null && 
                        application.getUser().getName() != null ? 
                        application.getUser().getName().toLowerCase() : "";
                    String userEmail = application.getUser() != null && 
                        application.getUser().getEmail() != null ? 
                        application.getUser().getEmail().toLowerCase() : "";
                    String status = application.getStatus() != null ? 
                        application.getStatus().toLowerCase() : "";
                    
                    return jobTitle.contains(currentSearchQuery) ||
                           userName.contains(currentSearchQuery) ||
                           userEmail.contains(currentSearchQuery) ||
                           status.contains(currentSearchQuery);
                })
                .collect(Collectors.toList());
        }
        
        // Update adapter and count
        applicationsAdapter.setApplications(filteredApplications);
        updateApplicationsCount(filteredApplications.size());
        
        // Show/hide empty state
        View emptyState = getView() != null ? getView().findViewById(R.id.ll_empty_state) : null;
        if (emptyState != null) {
            if (filteredApplications.isEmpty()) {
                rvApplications.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
            } else {
                rvApplications.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);
            }
        }
    }
    
    private void loadApplications() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        ApiClient.getRecruiterApiService().getApplications(null, null, null)
            .enqueue(new retrofit2.Callback<ApplicationListResponse>() {
            @Override
            public void onResponse(Call<ApplicationListResponse> call, Response<ApplicationListResponse> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    ApplicationListResponse applicationResponse = response.body();
                    if (applicationResponse.isSuccess() && applicationResponse.getData() != null) {
                        List<Application> applications = applicationResponse.getData().getApplications();
                        if (applications != null) {
                            allApplications = new ArrayList<>(applications);
                            applyFilters();
                        }
                    } else {
                        showError("Failed to load applications: " + applicationResponse.getMessage());
                    }
                } else {
                    showError("Failed to load applications");
                }
            }
            
            @Override
            public void onFailure(Call<ApplicationListResponse> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                showError("Network error: " + t.getMessage());
            }
        });
    }
    
    private void updateApplicationsCount(int count) {
        if (tvApplicationsCount != null) {
            tvApplicationsCount.setText("Applications (" + count + ")");
        }
    }
    
    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
    
    private void onApplicationClick(Application application) {
        // Navigate to application details activity
        Intent intent = new Intent(getContext(), RecruiterApplicationDetailsActivity.class);
        intent.putExtra(RecruiterApplicationDetailsActivity.EXTRA_APPLICATION_ID, application.getId());
        startActivity(intent);
    }
}
