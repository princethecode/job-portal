package com.example.jobportal.ui.applications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobportal.R;
import com.example.jobportal.models.Application;
import com.example.jobportal.ui.applications.ApplicationAdapter.OnApplicationClickListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ApplicationsFragment extends Fragment {
    private RecyclerView recyclerView;
    private ApplicationAdapter applicationAdapter;
    private View progressBar;
    private View emptyView;
    private TextView emptyText;
    private ChipGroup statusFilterChipGroup;
    private Chip chipAll, chipApplied, chipUnderReview, chipShortlisted, chipRejected, chipAccepted;
    private ApplicationsViewModel viewModel;
    
    // Status constants to match server statuses
    private static final String STATUS_ALL = "all";
    private static final String STATUS_APPLIED = "Applied";
    private static final String STATUS_UNDER_REVIEW = "Under Review";
    private static final String STATUS_SHORTLISTED = "Shortlisted";
    private static final String STATUS_REJECTED = "Rejected";
    private static final String STATUS_ACCEPTED = "Accepted";
    
    private String currentFilter = STATUS_ALL;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_applications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        recyclerView = view.findViewById(R.id.applications_recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyView = view.findViewById(R.id.empty_view);
        emptyText = view.findViewById(R.id.empty_text);
        
        // Initialize chip filter group
        statusFilterChipGroup = view.findViewById(R.id.status_filter_chip_group);
        chipAll = view.findViewById(R.id.chip_all);
        chipApplied = view.findViewById(R.id.chip_pending); // Using existing chip_pending for Applied status
        chipUnderReview = view.findViewById(R.id.chip_reviewing); // Using existing chip_reviewing for Under Review status
        chipShortlisted = view.findViewById(R.id.chip_shortlisted);
        chipRejected = view.findViewById(R.id.chip_rejected);
        chipAccepted = view.findViewById(R.id.chip_accepted);
        
        // Update chip text to match server statuses
        chipApplied.setText(STATUS_APPLIED);
        chipUnderReview.setText(STATUS_UNDER_REVIEW);
        
        // Setup chip filter listeners
        setupChipFilters();
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Load applications
        loadApplications();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication()))
                .get(ApplicationsViewModel.class);
    }
    
    private void setupRecyclerView() {
        applicationAdapter = new ApplicationAdapter(application -> {
            // TODO: Navigate to application details or show options
            Toast.makeText(requireContext(), "Application: " + application.getJobTitle(), Toast.LENGTH_SHORT).show();
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(applicationAdapter);
    }
    
    private void loadApplications() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        
        // Observe applications from ViewModel
        viewModel.getApplications().observe(getViewLifecycleOwner(), applications -> {
            progressBar.setVisibility(View.GONE);
            
            if (applications == null || applications.isEmpty()) {
                showEmptyView("You haven't applied to any jobs yet");
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                // Apply current filter to applications
                applyFilter(applications);
            }
        });
        
        // Observe loading state
        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                progressBar.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.GONE);
            }
        });
        
        // Observe error state
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                progressBar.setVisibility(View.GONE);
                showEmptyView("Error: " + error);
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showEmptyView(String message) {
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        emptyText.setText(message);
    }
    
    /**
     * Setup chip filter click listeners for application status filtering
     */
    private void setupChipFilters() {
        // Setup individual chip click listeners
        View.OnClickListener chipClickListener = view -> {
            Chip clickedChip = (Chip) view;
            currentFilter = getFilterValueFromChip(clickedChip);
            
            // Apply filter to current applications list
            if (viewModel.getApplications().getValue() != null) {
                applyFilter(viewModel.getApplications().getValue());
            }
        };
        
        // Set click listeners for each chip
        chipAll.setOnClickListener(chipClickListener);
        chipApplied.setOnClickListener(chipClickListener);
        chipUnderReview.setOnClickListener(chipClickListener);
        chipShortlisted.setOnClickListener(chipClickListener);
        chipRejected.setOnClickListener(chipClickListener);
        chipAccepted.setOnClickListener(chipClickListener);
    }
    
    /**
     * Get the filter value based on the selected chip
     */
    private String getFilterValueFromChip(Chip chip) {
        int chipId = chip.getId();
        
        if (chipId == R.id.chip_all) {
            return STATUS_ALL;
        } else if (chipId == R.id.chip_pending) { // Applied
            return STATUS_APPLIED;
        } else if (chipId == R.id.chip_reviewing) { // Under Review
            return STATUS_UNDER_REVIEW;
        } else if (chipId == R.id.chip_shortlisted) {
            return STATUS_SHORTLISTED;
        } else if (chipId == R.id.chip_rejected) {
            return STATUS_REJECTED;
        } else if (chipId == R.id.chip_accepted) {
            return STATUS_ACCEPTED;
        }
        
        return STATUS_ALL; // Default to all
    }
    
    /**
     * Apply the selected filter to the applications list
     */
    private void applyFilter(List<Application> applications) {
        if (currentFilter.equals(STATUS_ALL)) {
            // Show all applications
            applicationAdapter.setApplications(applications);
        } else {
            // Filter applications by status
            List<Application> filteredList = applications.stream()
                    .filter(app -> app.getStatus() != null && app.getStatus().equals(currentFilter))
                    .collect(Collectors.toList());
            
            applicationAdapter.setApplications(filteredList);
            
            // Show empty view with filter info if no results
            if (filteredList.isEmpty()) {
                showEmptyView("No applications with status: " + currentFilter);
            }
        }
    }
}