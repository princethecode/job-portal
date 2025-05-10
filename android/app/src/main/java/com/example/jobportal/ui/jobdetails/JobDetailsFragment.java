package com.example.jobportal.ui.jobdetails;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.jobportal.R;
// Use only the data.model.Job class
//import com.example.jobportal.data.model.Job;
import com.example.jobportal.auth.LoginActivity;
import com.example.jobportal.network.ApiCallback;
import com.example.jobportal.network.ApiClient;
import com.example.jobportal.network.ApiResponse;
import com.example.jobportal.models.Job;
import com.example.jobportal.models.Application;
import com.example.jobportal.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JobDetailsFragment extends Fragment {
    private static final String ARG_JOB_ID = "job_id";
    
    private String jobId;
    private TextView titleTextView;
    private TextView companyTextView;
    private TextView locationTextView;
    private TextView salaryTextView;
    private TextView typeTextView;
    private TextView dateTextView;
    private TextView descriptionTextView;
    private Button applyButton;
    private View progressBar;
    private View contentLayout;
    
    public static JobDetailsFragment newInstance(String jobId) {
        JobDetailsFragment fragment = new JobDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_JOB_ID, jobId);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            jobId = getArguments().getString(ARG_JOB_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_job_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        titleTextView = view.findViewById(R.id.job_title);
        companyTextView = view.findViewById(R.id.company_name);
        locationTextView = view.findViewById(R.id.location);
        salaryTextView = view.findViewById(R.id.salary);
        typeTextView = view.findViewById(R.id.job_type);
        dateTextView = view.findViewById(R.id.posting_date);
        descriptionTextView = view.findViewById(R.id.description);
        applyButton = view.findViewById(R.id.apply_button);
        progressBar = view.findViewById(R.id.progress_bar);
        contentLayout = view.findViewById(R.id.content_layout);
        
        // Set up listeners
        applyButton.setOnClickListener(v -> {
            applyForJob();
        });
        
        // Load job details
        loadJobDetails();
    }
    
    private void loadJobDetails() {
        progressBar.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);
        
        Log.d("JobDetailsFragment", "Loading job details for ID: " + jobId);
        
        ApiClient.getApiService().getJobDetails(Integer.parseInt(jobId)).enqueue(new Callback<ApiResponse<Job>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Job>> call, @NonNull Response<ApiResponse<Job>> response) {
                progressBar.setVisibility(View.GONE);
                
                Log.d("JobDetailsFragment", "Response received: " + response.code());
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Job job = response.body().getData();
                    if (job != null) {
                        Log.d("JobDetailsFragment", "Job details received: " + job.getTitle());
                        displayJobDetails(job);
                    } else {
                        Log.e("JobDetailsFragment", "Job data is null");
                        showError("Failed to load job details: Data is null");
                    }
                } else {
                    Log.e("JobDetailsFragment", "Error response: " + response.code() + " " + response.message());
                    showError("Failed to load job details");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Job>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("JobDetailsFragment", "Network error: " + t.getMessage(), t);
                showError("Network error: " + t.getMessage());
            }
        });
    }
    

    private void displayJobDetails(Job job) {
        contentLayout.setVisibility(View.VISIBLE);
        
        titleTextView.setText(job.getTitle());
        companyTextView.setText(job.getCompany());
        locationTextView.setText(job.getLocation());
        salaryTextView.setText(job.getSalary());
        typeTextView.setText(job.getJobType());
        dateTextView.setText("Posted: " + job.getPostingDate());
        descriptionTextView.setText(job.getDescription());
        
        // Enable apply button only if job is active
        applyButton.setEnabled(job.isActive());
        if (!job.isActive()) {
            applyButton.setText("Job Closed");
        }
    }
    
    private void applyForJob() {
        // Check if user is logged in
        SessionManager sessionManager = SessionManager.getInstance(requireContext());
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(requireContext(), 
                "Please login to apply for jobs", Toast.LENGTH_LONG).show();
            // Navigate to login screen
            startActivity(new Intent(requireContext(), LoginActivity.class));
            return;
        }

        // Show loading state
        applyButton.setEnabled(false);
        applyButton.setText("Applying...");
        
        // Create API client with context
        ApiClient apiClient = ApiClient.getInstance(requireContext());
        
        // Make API call to apply for the job
        apiClient.applyForJob(Integer.parseInt(jobId), new ApiCallback<ApiResponse<Application>>() {
            @Override
            public void onSuccess(ApiResponse<Application> response) {
                if (isAdded()) {  // Check if fragment is still attached
                    // Update UI on success
                    applyButton.setText("Applied");
                    applyButton.setEnabled(false);
                    Toast.makeText(requireContext(), 
                            "Application submitted successfully", Toast.LENGTH_SHORT).show();
                }
            }
        
            @Override
            public void onError(String errorMessage) {
                if (isAdded()) {  // Check if fragment is still attached
                    // Re-enable button on error
                    applyButton.setEnabled(true);
                    applyButton.setText("Apply Now");
                    
                    if (errorMessage.contains("not logged in")) {
                        // Clear session and redirect to login
                        sessionManager.logout();
                        startActivity(new Intent(requireContext(), LoginActivity.class));
                        requireActivity().finish();
                    } else {
                        Toast.makeText(requireContext(), 
                                "Failed to apply: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
    
    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}