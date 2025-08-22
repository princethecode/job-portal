package com.example.jobportal.recruiter.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobportal.R;
import com.example.jobportal.auth.RecruiterAuthHelper;
import com.example.jobportal.models.Job;
import com.example.jobportal.models.Recruiter;
import com.example.jobportal.models.Application;
import com.example.jobportal.models.DashboardResponse;
import com.example.jobportal.network.ApiClient;
import com.example.jobportal.recruiter.adapters.RecentJobsAdapter;
import retrofit2.Call;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class RecruiterDashboardFragment extends Fragment {
    
    private TextView tvTotalJobs, tvActiveJobs, tvTotalApplications, tvPendingApplications;
    private TextView tvScheduledInterviews, tvHiredCandidates;
    private RecyclerView rvRecentJobs;
    private RecentJobsAdapter recentJobsAdapter;
    
    private RecruiterAuthHelper authHelper;
    private Recruiter currentRecruiter;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recruiter_dashboard, container, false);
        
        authHelper = RecruiterAuthHelper.getInstance(requireContext());
        currentRecruiter = authHelper.getRecruiterData();
        
        initializeViews(view);
        setupRecyclerView();
        loadDashboardData();
        
        return view;
    }
    
    private void initializeViews(View view) {
        tvTotalJobs = view.findViewById(R.id.tv_total_jobs);
        tvActiveJobs = view.findViewById(R.id.tv_active_jobs);
        tvTotalApplications = view.findViewById(R.id.tv_total_applications);
        tvPendingApplications = view.findViewById(R.id.tv_pending_applications);
        tvScheduledInterviews = view.findViewById(R.id.tv_scheduled_interviews);
        tvHiredCandidates = view.findViewById(R.id.tv_hired_candidates);
        rvRecentJobs = view.findViewById(R.id.rv_recent_jobs);
    }
    
    private void setupRecyclerView() {
        recentJobsAdapter = new RecentJobsAdapter(new ArrayList<>());
        rvRecentJobs.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRecentJobs.setAdapter(recentJobsAdapter);
    }
    
    private void loadDashboardData() {
        android.util.Log.d("RecruiterDashboard", "Loading dashboard data...");
        
        // Debug authentication state
        authHelper.debugAuthState();
        

        // Check if user is authenticated
        if (!authHelper.isLoggedIn() || !authHelper.hasValidToken()) {
            android.util.Log.e("RecruiterDashboard", "User not authenticated");
            showError("Please login again");
            return;
        }
        
        String token = authHelper.getRecruiterToken();
        android.util.Log.d("RecruiterDashboard", "Making dashboard request with token: " + 
            (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null"));
        
        // Load dashboard statistics
        ApiClient.getRecruiterApiService().getDashboard().enqueue(new retrofit2.Callback<DashboardResponse>() {
            @Override
            public void onResponse(Call<DashboardResponse> call, Response<DashboardResponse> response) {
                android.util.Log.d("RecruiterDashboard", "Dashboard response received: " +
                    "\nStatus Code: " + response.code() +
                    "\nMessage: " + response.message() +
                    "\nHeaders: " + response.headers().toString());
                
                if (response.isSuccessful() && response.body() != null) {
                    DashboardResponse dashboardResponse = response.body();
                    android.util.Log.d("RecruiterDashboard", "Dashboard response body received: " +
                        "\nSuccess: " + dashboardResponse.isSuccess() +
                        "\nMessage: " + dashboardResponse.getMessage());
                    
                    if (dashboardResponse.isSuccess() && dashboardResponse.getData() != null) {
                        DashboardResponse.DashboardData data = dashboardResponse.getData();
                        
                        // Update statistics UI
                        if (data.getStats() != null) {
                            updateDashboardStats(data.getStats());
                        }
                        
                        // Update recent jobs
                        if (data.getRecentJobs() != null) {
                            updateRecentJobs(data.getRecentJobs());
                        }
                        
                        android.util.Log.d("RecruiterDashboard", "Dashboard data loaded successfully");
                    } else {
                        String errorMsg = "Failed to load dashboard: " + dashboardResponse.getMessage();
                        android.util.Log.e("RecruiterDashboard", errorMsg);
                        showError(errorMsg);
                    }
                } else {
                    String errorMsg = "API Error - Status: " + response.code() + ", Message: " + response.message();
                    android.util.Log.e("RecruiterDashboard", errorMsg);
                    
                    // Try to get error body
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("RecruiterDashboard", "Error body: " + errorBody);
                            
                            // Check if it's HTML (likely a redirect to login page)
                            if (errorBody.contains("<html>") || errorBody.contains("<!DOCTYPE")) {
                                android.util.Log.e("RecruiterDashboard", "Received HTML response - likely authentication issue");
                                showError("Authentication failed. Please login again.");
                                // Clear token and redirect to login
                                authHelper.logout();
                            } else {
                                showError("Server error: " + errorMsg);
                            }
                        } else {
                            showError("Failed to load dashboard: " + errorMsg);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("RecruiterDashboard", "Error reading error body", e);
                        showError("Failed to load dashboard: " + errorMsg);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<DashboardResponse> call, Throwable t) {
                String errorMsg = "Network error: " + t.getMessage();
                android.util.Log.e("RecruiterDashboard", errorMsg, t);
                showError(errorMsg);
            }
        });
    }
    
    private void updateDashboardStats(DashboardResponse.DashboardStats stats) {
        tvTotalJobs.setText(String.valueOf(stats.getTotalJobs()));
        tvActiveJobs.setText(String.valueOf(stats.getActiveJobs()));
        tvTotalApplications.setText(String.valueOf(stats.getTotalApplications()));
        tvPendingApplications.setText(String.valueOf(stats.getPendingApplications()));
        tvScheduledInterviews.setText(String.valueOf(stats.getScheduledInterviews()));
        tvHiredCandidates.setText(String.valueOf(stats.getHiredCandidates()));
    }
    
    private void updateRecentJobs(List<DashboardResponse.RecentJob> recentJobs) {
        // Convert DashboardResponse.RecentJob to Job objects for the adapter
        List<Job> jobs = new ArrayList<>();
        for (DashboardResponse.RecentJob recentJob : recentJobs) {
            Job job = new Job();
            job.setId(recentJob.getId());
            job.setTitle(recentJob.getTitle());
            job.setCompanyName(recentJob.getCompanyName());
            job.setLocation(recentJob.getLocation());
            job.setJobType(recentJob.getJobType());
            job.setSalary(recentJob.getSalary());
            job.setActive(recentJob.isActive());
            job.setApplicationsCount(recentJob.getApplicationsCount());
            job.setStatus(recentJob.isActive() ? "Active" : "Inactive");
            jobs.add(job);
        }
        recentJobsAdapter.updateJobs(jobs);
    }
    
    private void showError(String message) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
        }
    }

}
