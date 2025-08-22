package com.example.jobportal.recruiter.fragments;

import android.os.Bundle;
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
import com.example.jobportal.R;
import com.example.jobportal.models.Application;
import com.example.jobportal.models.ApplicationListResponse;
import com.example.jobportal.network.ApiClient;
import com.example.jobportal.recruiter.adapters.ApplicationsAdapter;
import retrofit2.Call;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class RecruiterApplicationsFragment extends Fragment {
    
    private RecyclerView rvApplications;
    private TextView tvApplicationsCount;
    private View progressBar;
    private ApplicationsAdapter applicationsAdapter;
    private List<Application> applicationsList = new ArrayList<>();
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recruiter_applications, container, false);
        
        initializeViews(view);
        setupRecyclerView();
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
    }
    
    private void setupRecyclerView() {
        rvApplications.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        applicationsAdapter = new ApplicationsAdapter();
        applicationsAdapter.setOnApplicationClickListener(this::onApplicationClick);
        rvApplications.setAdapter(applicationsAdapter);
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
                            applicationsList.clear();
                            applicationsList.addAll(applications);
                            updateApplicationsCount(applications.size());
                            
                            // Update adapter with new data
                            applicationsAdapter.setApplications(applications);
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
        // Handle application item click - you can implement application details view
        if (getContext() != null) {
            String message = "Clicked on application from " + 
                (application.getUser() != null ? application.getUser().getName() : "Unknown") +
                " for " + application.getJobTitle();
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
