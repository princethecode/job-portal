package com.example.jobportal.ui.application;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.jobportal.R;
import com.example.jobportal.models.Application;
import com.example.jobportal.models.Job;
import com.example.jobportal.models.User;
import com.example.jobportal.network.ApiCallback;
import com.example.jobportal.network.ApiClient;
import com.example.jobportal.network.ApiResponse;
import com.example.jobportal.ui.jobdetails.JobDetailsFragment;
import com.example.jobportal.auth.LoginActivity;
import com.example.jobportal.utils.SessionManager;

import java.util.HashMap;
import java.util.Map;

public class JobApplicationFragment extends Fragment {
    private static final String TAG = "JobApplicationFragment";
    private static final String ARG_JOB_ID = "job_id";
    private static final String ARG_JOB_TITLE = "job_title";
    private static final String ARG_COMPANY = "company";
    
    private String jobId;
    private String jobTitle;
    private String company;
    
    private EditText currentCompanyEditText;
    private EditText departmentEditText;
    private EditText currentSalaryEditText;
    private EditText expectedSalaryEditText;
    private EditText joiningPeriodEditText;
    private EditText skillsEditText;
    private EditText experienceEditText;
    private Button submitButton;
    private Button cancelButton;
    private ProgressBar progressBar;
    
    private ApiClient apiClient;
    private SessionManager sessionManager;
    
    public static JobApplicationFragment newInstance(String jobId, String jobTitle, String company) {
        JobApplicationFragment fragment = new JobApplicationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_JOB_ID, jobId);
        args.putString(ARG_JOB_TITLE, jobTitle);
        args.putString(ARG_COMPANY, company);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            jobId = getArguments().getString(ARG_JOB_ID);
            jobTitle = getArguments().getString(ARG_JOB_TITLE);
            company = getArguments().getString(ARG_COMPANY);
        }
        
        apiClient = ApiClient.getInstance(requireContext());
        sessionManager = SessionManager.getInstance(requireContext());
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_job_application, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        currentCompanyEditText = view.findViewById(R.id.current_company);
        departmentEditText = view.findViewById(R.id.department);
        currentSalaryEditText = view.findViewById(R.id.current_salary);
        expectedSalaryEditText = view.findViewById(R.id.expected_salary);
        joiningPeriodEditText = view.findViewById(R.id.joining_period);
        skillsEditText = view.findViewById(R.id.skills);
        experienceEditText = view.findViewById(R.id.experience);
        submitButton = view.findViewById(R.id.submit_button);
        cancelButton = view.findViewById(R.id.cancel_button);
        progressBar = view.findViewById(R.id.progress_bar);
        
        // Populate job information
        if (jobTitle != null && company != null) {
            // Could set these to TextViews in the layout if desired
            Log.d(TAG, "Applying for job: " + jobTitle + " at " + company);
        }
        
        // Set up button listeners
        submitButton.setOnClickListener(v -> submitApplication());
        cancelButton.setOnClickListener(v -> goBack());
        
        // Pre-populate fields with existing user data if available
        loadUserData();
    }
    
    private void loadUserData() {
        progressBar.setVisibility(View.VISIBLE);
        
        User user = sessionManager.getUser();
        if (user != null) {
            // Note: These fields may not exist in the current User model
            // We'll need to update the User model to include these new fields
            
            // For now, we'll check if the methods exist and use them if they do
            try {
                currentCompanyEditText.setText(user.getCurrentCompany());
                departmentEditText.setText(user.getDepartment());
                currentSalaryEditText.setText(user.getCurrentSalary());
                expectedSalaryEditText.setText(user.getExpectedSalary());
                joiningPeriodEditText.setText(user.getJoiningPeriod());
                skillsEditText.setText(user.getSkills());
                experienceEditText.setText(user.getExperience());
            } catch (Exception e) {
                Log.w(TAG, "Some employment fields may not exist in User model yet: " + e.getMessage());
            }
        }
        
        progressBar.setVisibility(View.GONE);
    }
    
    private void submitApplication() {
        if (!validateInputs()) {
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);
        submitButton.setEnabled(false);
        
        // Create a map with the employment details
        Map<String, String> employmentDetails = new HashMap<>();
        employmentDetails.put("current_company", currentCompanyEditText.getText().toString());
        employmentDetails.put("department", departmentEditText.getText().toString());
        employmentDetails.put("current_salary", currentSalaryEditText.getText().toString());
        employmentDetails.put("expected_salary", expectedSalaryEditText.getText().toString());
        employmentDetails.put("joining_period", joiningPeriodEditText.getText().toString());
        employmentDetails.put("skills", skillsEditText.getText().toString());
        employmentDetails.put("experience", experienceEditText.getText().toString());
        
        // Call the updated ApiClient method to submit the application with employment details
        apiClient.applyForJobWithDetails(Integer.parseInt(jobId), employmentDetails, new ApiCallback<ApiResponse<Application>>() {
            @Override
            public void onSuccess(ApiResponse<Application> response) {
                if (isAdded()) {  // Check if fragment is still attached
                    progressBar.setVisibility(View.GONE);
                    
                    // Show success message
                    Toast.makeText(requireContext(), 
                        "Application submitted successfully", Toast.LENGTH_SHORT).show();
                    
                    // Go back to the job details screen
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            }
            
            @Override
            public void onError(String errorMessage) {
                if (isAdded()) {  // Check if fragment is still attached
                    progressBar.setVisibility(View.GONE);
                    submitButton.setEnabled(true);
                    
                    if (errorMessage.contains("not logged in")) {
                        // Clear session and redirect to login
                        sessionManager.logout();
                        Intent intent = new Intent(requireContext(), LoginActivity.class);
                        startActivity(intent);
                        requireActivity().finish();
                    } else {
                        // Show error message
                        Toast.makeText(requireContext(), 
                            "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
    
    private boolean validateInputs() {
        boolean isValid = true;
        
        if (currentCompanyEditText.getText().toString().trim().isEmpty()) {
            currentCompanyEditText.setError("Current company is required");
            isValid = false;
        }
        
        if (departmentEditText.getText().toString().trim().isEmpty()) {
            departmentEditText.setError("Department is required");
            isValid = false;
        }
        
        if (currentSalaryEditText.getText().toString().trim().isEmpty()) {
            currentSalaryEditText.setError("Current salary is required");
            isValid = false;
        }
        
        if (expectedSalaryEditText.getText().toString().trim().isEmpty()) {
            expectedSalaryEditText.setError("Expected salary is required");
            isValid = false;
        }
        
        if (joiningPeriodEditText.getText().toString().trim().isEmpty()) {
            joiningPeriodEditText.setError("Joining period is required");
            isValid = false;
        }
        
        if (skillsEditText.getText().toString().trim().isEmpty()) {
            skillsEditText.setError("Skills are required");
            isValid = false;
        }
        
        return isValid;
    }
    
    private void goBack() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }
} 