package com.example.jobportal.ui.application;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.jobportal.R;
import com.example.jobportal.models.Application;
import com.example.jobportal.models.Experience;
import com.example.jobportal.models.Job;
import com.example.jobportal.models.User;
import com.example.jobportal.network.ApiCallback;
import com.example.jobportal.network.ApiClient;
import com.example.jobportal.network.ApiResponse;
import com.example.jobportal.ui.applications.ApplicationsFragment;
import com.example.jobportal.ui.experience.ExperienceFormFragment;
import com.example.jobportal.ui.experience.ExperienceListFragment;
import com.example.jobportal.ui.experience.ExperienceViewModel;
import com.example.jobportal.utils.FileUtils;
import com.example.jobportal.utils.SessionManager;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobApplicationNewFragment extends Fragment {
    private static final String TAG = "JobApplicationNewFragment";
    private static final String ARG_JOB_ID = "job_id";
    private static final String ARG_JOB_TITLE = "job_title";
    private static final String ARG_COMPANY = "company";
    private static final String ARG_IS_FEATURED = "is_featured";

    // Step fragments
    private Fragment personalInfoFragment;
    private Fragment employmentDetailsFragment;
    private Fragment experienceDetailsFragment;
    private Fragment reviewSubmitFragment;

    // UI components
    private TextView toolbarTitle;
    private TextView stepTitle;
    private TextView stepIndicator;
    private ProgressBar progressBarSteps;
    private Button btnBack;
    private Button btnNext;
    private ProgressBar progressBar;

    // Data
    private String jobId;
    private String jobTitle;
    private String company;
    private boolean isFeaturedJob = false;
    public int currentStep = 1;
    private final int TOTAL_STEPS = 3;
    private ApiClient apiClient;
    private SessionManager sessionManager;
    private User currentUser;
    private ExperienceViewModel experienceViewModel;
    private List<Experience> userExperiences = new ArrayList<>();
    private Uri resumeUri;
    private String resumeFileName;

    // Application data
    private Map<String, String> applicationData = new HashMap<>();

    public static JobApplicationNewFragment newInstance(String jobId, String jobTitle, String company) {
        return newInstance(jobId, jobTitle, company, false);
    }
    
    public static JobApplicationNewFragment newInstance(String jobId, String jobTitle, String company, boolean isFeaturedJob) {
        JobApplicationNewFragment fragment = new JobApplicationNewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_JOB_ID, jobId);
        args.putString(ARG_JOB_TITLE, jobTitle);
        args.putString(ARG_COMPANY, company);
        args.putBoolean(ARG_IS_FEATURED, isFeaturedJob);
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
            isFeaturedJob = getArguments().getBoolean(ARG_IS_FEATURED, false);
        }
        
        Log.d(TAG, "Job Application for ID: " + jobId + ", Is Featured: " + isFeaturedJob);

        apiClient = ApiClient.getInstance(requireContext());
        sessionManager = SessionManager.getInstance(requireContext());
        currentUser = sessionManager.getUser();
        
        // Initialize the ViewModel for experiences
        experienceViewModel = new ViewModelProvider(requireActivity()).get(ExperienceViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_job_application_new, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        toolbarTitle = view.findViewById(R.id.toolbar_title);
        stepTitle = view.findViewById(R.id.step_title);
        stepIndicator = view.findViewById(R.id.step_indicator);
        progressBarSteps = view.findViewById(R.id.progress_bar_steps);
        btnBack = view.findViewById(R.id.btn_back);
        btnNext = view.findViewById(R.id.btn_next);
        progressBar = view.findViewById(R.id.progress_bar);

        // Set up toolbar navigation
        view.findViewById(R.id.toolbar).setOnClickListener(v -> requireActivity().onBackPressed());

        // Set up button listeners
        btnBack.setOnClickListener(v -> navigateToPreviousStep());
        btnNext.setOnClickListener(v -> navigateToNextStep());

        // Initialize step fragments
        personalInfoFragment = new JobApplicationPersonalInfoFragment();
        employmentDetailsFragment = new JobApplicationEmploymentFragment();
        experienceDetailsFragment = new JobApplicationExperienceFragment();
        reviewSubmitFragment = new JobApplicationReviewFragment();

        // Set initial step
        updateStepUI(currentStep);
        loadStepFragment(currentStep);

        // Fetch latest user profile to ensure data is fresh
        fetchUserProfile();

        // Observe experiences data
        experienceViewModel.getUserExperiences().observe(getViewLifecycleOwner(), experiences -> {
            if (experiences != null) {
                userExperiences = experiences;
                // Update the review fragment if it's currently visible
                if (currentStep == 4 && reviewSubmitFragment.isVisible()) {
                    ((JobApplicationReviewFragment) reviewSubmitFragment).updateExperiencesList(userExperiences);
                }
            }
        });

        // Load user experiences
        experienceViewModel.syncExperiences();
    }

    private void fetchUserProfile() {
        progressBar.setVisibility(View.VISIBLE);
        apiClient.getUserProfile(new ApiCallback<ApiResponse<User>>() {
            @Override
            public void onSuccess(ApiResponse<User> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccess() && response.getData() != null) {
                    // Save the fresh user data to the session
                    sessionManager.saveUser(response.getData());
                    currentUser = response.getData();
                    // Reload the current fragment to ensure it uses the fresh data
                    loadStepFragment(currentStep);
                } else {
                    Toast.makeText(getContext(), "Failed to load profile: " + response.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Make accessible to child fragments
    public void updateStepUI(int step) {
        // Update progress bar
        int progress = (step * 100) / TOTAL_STEPS;
        progressBarSteps.setProgress(progress);

        // Update step indicator
        stepIndicator.setText(step + " of " + TOTAL_STEPS + " Steps");

        // Update step title
        switch (step) {
            case 1:
                stepTitle.setText("Personal Info");
                btnBack.setVisibility(View.INVISIBLE);
                btnNext.setText("Next");
                break;
            case 2:
                stepTitle.setText("Employment Details");
                btnBack.setVisibility(View.VISIBLE);
                btnNext.setText("Next");
                break;
            case 3:
                stepTitle.setText("Review & Submit");
                btnBack.setVisibility(View.VISIBLE);
                btnNext.setText("Submit Application");
                break;
        }
    }

    // Make accessible to child fragments
    public void loadStepFragment(int step) {
        Fragment fragment;
        switch (step) {
            case 1:
                fragment = personalInfoFragment;
                break;
            case 2:
                fragment = employmentDetailsFragment;
                break;
            case 3:
                fragment = reviewSubmitFragment;
                // Pass application data to review fragment
                Bundle args = new Bundle();
                args.putSerializable("application_data", new HashMap<>(applicationData));
                args.putString("job_id", jobId);
                args.putString("job_title", jobTitle);
                args.putString("company", company);
                if (resumeUri != null) {
                    args.putString("resume_uri", resumeUri.toString());
                    args.putString("resume_name", resumeFileName);
                }
                fragment.setArguments(args);
                break;
            default:
                fragment = personalInfoFragment;
                break;
        }

        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.step_container, fragment);
        transaction.commit();
    }

    // Make accessible to child fragments
    public void navigateToNextStep() {
        // Validate current step
        if (!validateCurrentStep()) {
            return;
        }

        // If on the last step, submit application
        if (currentStep == TOTAL_STEPS) {
            submitApplication();
            return;
        }

        // Otherwise, move to next step
        currentStep++;
        updateStepUI(currentStep);
        loadStepFragment(currentStep);
    }

    private void navigateToPreviousStep() {
        if (currentStep > 1) {
            currentStep--;
            updateStepUI(currentStep);
            loadStepFragment(currentStep);
        }
    }

    private boolean validateCurrentStep() {
        switch (currentStep) {
            case 1:
                return ((JobApplicationPersonalInfoFragment) personalInfoFragment).validateAndSaveData(applicationData);
            case 2:
                return ((JobApplicationEmploymentFragment) employmentDetailsFragment).validateAndSaveData(applicationData);
            case 3:
                return true; // Review step is always valid
            default:
                return false;
        }
    }

    private void submitApplication() {
        progressBar.setVisibility(View.VISIBLE);
        btnNext.setEnabled(false);

        // Create a map with all the application details
        Map<String, String> employmentDetails = new HashMap<>(applicationData);
        
        // Log information about the application submission
        Log.d(TAG, "Submitting application for job ID: " + jobId + ", Is Featured: " + isFeaturedJob);
        
        // Check if this is a featured job application
        if (isFeaturedJob) {
            // For featured jobs, we need to call a different API endpoint or include a flag
            employmentDetails.put("is_featured_job", "true");
        }
        
        // Call the API to submit the application
        apiClient.applyForJobWithDetails(Integer.parseInt(jobId), employmentDetails, new ApiCallback<ApiResponse<Application>>() {
            @Override
            public void onSuccess(ApiResponse<Application> response) {
                if (isAdded()) {  // Check if fragment is still attached
                    progressBar.setVisibility(View.GONE);
                    
                    // Show success message
                    Toast.makeText(requireContext(), 
                        "Application submitted successfully", Toast.LENGTH_SHORT).show();
                    
                    // Navigate to the applications fragment
                    navigateToApplicationsFragment();
                }
            }
            
            @Override
            public void onError(String errorMessage) {
                if (isAdded()) {  // Check if fragment is still attached
                    progressBar.setVisibility(View.GONE);
                    btnNext.setEnabled(true);
                    
                    // Show error message
                    Toast.makeText(requireContext(), 
                        "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void navigateToApplicationsFragment() {
        // Navigate to the applications fragment
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new ApplicationsFragment())
                .commit();
    }

    // Method to update resume information from employment fragment
    public void setResumeInfo(Uri uri, String fileName) {
        this.resumeUri = uri;
        this.resumeFileName = fileName;
        applicationData.put("resume_name", fileName);
    }
    
    // Alternative method to set just the resume URI
    public void setSelectedResumeUri(Uri uri) {
        this.resumeUri = uri;
        if (uri != null) {
            this.resumeFileName = FileUtils.getFileName(requireContext(), uri);
            applicationData.put("resume_name", resumeFileName);
        }
    }

    // Inner fragment classes for each step
    public static class JobApplicationPersonalInfoFragment extends Fragment {
        private TextView fullNameEditText;
        private TextView emailEditText;
        private TextView phoneEditText;
        private User currentUser;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_job_application_personal_info, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            // Initialize UI components
            fullNameEditText = view.findViewById(R.id.et_full_name);
            emailEditText = view.findViewById(R.id.et_email);
            phoneEditText = view.findViewById(R.id.et_phone);

            // Get current user data
            SessionManager sessionManager = SessionManager.getInstance(requireContext());
            currentUser = sessionManager.getUser();

            // Pre-populate fields with user data if available
            if (currentUser != null) {
                fullNameEditText.setText(currentUser.getFullName());
                emailEditText.setText(currentUser.getEmail());
                phoneEditText.setText(currentUser.getPhone());
            }
        }

        public boolean validateAndSaveData(Map<String, String> applicationData) {
            // Validate inputs
            String fullName = fullNameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();

            if (fullName.isEmpty()) {
                fullNameEditText.setError("Full name is required");
                return false;
            }

            if (email.isEmpty()) {
                emailEditText.setError("Email is required");
                return false;
            }

            if (phone.isEmpty()) {
                phoneEditText.setError("Phone number is required");
                return false;
            }

            // Save data to application map
            applicationData.put("full_name", fullName);
            applicationData.put("email", email);
            applicationData.put("phone", phone);

            return true;
        }
    }

    public static class JobApplicationEmploymentFragment extends Fragment {
        private TextView currentCompanyEditText;
        private TextView departmentEditText;
        private TextView currentSalaryEditText;
        private TextView expectedSalaryEditText;
        private TextView totalExperienceEditText;
        private TextView joiningPeriodEditText;
        private TextView skillsEditText;
        private TextView resumeNameTextView;
        private TextView resumeStatusTextView;
        private View resumeUploadContainer;
        private View resumeUploadUI;
        private View resumeLoadingUI;
        private View resumeDisplayUI;
        private Uri selectedResumeUri;
        private String selectedResumeFileName;
        private User currentUser;
        private ActivityResultLauncher<String> getContentLauncher;
        private File resumeFile;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_job_application_employment, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            // Initialize UI components
            currentCompanyEditText = view.findViewById(R.id.current_company);
            departmentEditText = view.findViewById(R.id.department);
            currentSalaryEditText = view.findViewById(R.id.current_salary);
            expectedSalaryEditText = view.findViewById(R.id.expected_salary);
            totalExperienceEditText = view.findViewById(R.id.total_experience);
            joiningPeriodEditText = view.findViewById(R.id.joining_period);
            skillsEditText = view.findViewById(R.id.skills);
            resumeNameTextView = view.findViewById(R.id.tv_resume_name);
            resumeStatusTextView = view.findViewById(R.id.tv_resume_status);
            resumeUploadContainer = view.findViewById(R.id.resume_upload_container);
            resumeUploadUI = view.findViewById(R.id.resume_upload_ui);
            resumeLoadingUI = view.findViewById(R.id.resume_loading_ui);
            resumeDisplayUI = view.findViewById(R.id.resume_display_ui);

            // Get current user data
            SessionManager sessionManager = SessionManager.getInstance(requireContext());
            currentUser = sessionManager.getUser();

            // Set up resume upload
            setupResumeUpload();

            // Pre-populate fields with user data if available
            if (currentUser != null) {
                try {
                    currentCompanyEditText.setText(currentUser.getCurrentCompany());
                    departmentEditText.setText(currentUser.getDepartment());
                    currentSalaryEditText.setText(currentUser.getCurrentSalary());
                    expectedSalaryEditText.setText(currentUser.getExpectedSalary());
                    joiningPeriodEditText.setText(currentUser.getJoiningPeriod());
                    skillsEditText.setText(currentUser.getSkills());
                    
                    // Set experience directly from user data
                    String experience = currentUser.getExperience();
                    if (experience != null && !experience.isEmpty()) {
                        totalExperienceEditText.setText(experience);
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Some employment fields may not exist in User model: " + e.getMessage());
                }
            }
        }

        private void setupResumeUpload() {
            // Initialize the ActivityResultLauncher
            getContentLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            try {
                                selectedResumeUri = uri;
                                selectedResumeFileName = getFileNameFromUri(uri);
                                
                                // Create a temporary file from the URI
                                resumeFile = createFileFromUri(uri);
                                
                                // Upload resume to server immediately
                                uploadResumeToServer(resumeFile);
                                
                            } catch (IOException e) {
                                Log.e(TAG, "Error processing resume file", e);
                                // Show error and revert to upload state
                                updateResumeUI(null, false);
                                Toast.makeText(requireContext(), 
                                    "Error processing resume file", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

            // Set up click listener for resume upload
            resumeUploadContainer.setOnClickListener(v -> {
                // Launch file picker for PDF files only
                getContentLauncher.launch("application/pdf");
            });
        }
        
        private void uploadResumeToServer(File resumeFile) {
            // Show loading state
            showResumeLoading();
            
            // Get ApiClient instance
            ApiClient apiClient = ApiClient.getInstance(requireContext());
            
            // Upload resume using the same method as in edit profile
            apiClient.uploadResume(resumeFile, new ApiCallback<ApiResponse<User>>() {
                @Override
                public void onSuccess(ApiResponse<User> response) {
                    if (response.isSuccess() && response.getData() != null) {
                        // Update UI to show success with new upload indicator
                        updateResumeUI(selectedResumeFileName, true);
                        
                        // Pass the resume info to the parent fragment
                        if (getParentFragment() instanceof JobApplicationNewFragment) {
                            ((JobApplicationNewFragment) getParentFragment()).setResumeInfo(selectedResumeUri, selectedResumeFileName);
                        }
                        
                        Log.d(TAG, "Resume uploaded successfully to server");
                    } else {
                        // Show error and revert to upload state
                        updateResumeUI(null, false);
                        Toast.makeText(requireContext(), 
                            "Failed to upload resume: " + response.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onError(String errorMessage) {
                    // Show error and revert to upload state
                    updateResumeUI(null, false);
                    Toast.makeText(requireContext(), 
                        "Error uploading resume: " + errorMessage, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Resume upload failed: " + errorMessage);
                }
            });
        }
        
        /**
         * Updates the resume UI section with different states.
         * @param fileName The name of the resume file. If null or empty, shows the "Upload Now" UI.
         * @param isNewUpload Whether this is a newly uploaded file (shows success message)
         */
        private void updateResumeUI(String fileName, boolean isNewUpload) {
            // Hide loading state
            resumeLoadingUI.setVisibility(View.GONE);
            
            if (fileName != null && !fileName.isEmpty()) {
                // A resume exists, so show its name.
                resumeNameTextView.setText(fileName);
                
                // Show appropriate status message
                if (isNewUpload) {
                    resumeStatusTextView.setText("✓ Successfully uploaded");
                    resumeStatusTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                } else {
                    resumeStatusTextView.setText("✓ Resume available");
                    resumeStatusTextView.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                }
                
                resumeUploadUI.setVisibility(View.GONE);
                resumeDisplayUI.setVisibility(View.VISIBLE);
            } else {
                // No resume, so show the "Upload Now" prompt.
                resumeNameTextView.setText("");
                resumeUploadUI.setVisibility(View.VISIBLE);
                resumeDisplayUI.setVisibility(View.GONE);
            }
        }
        
        /**
         * Shows the loading state during resume upload
         */
        private void showResumeLoading() {
            resumeUploadUI.setVisibility(View.GONE);
            resumeDisplayUI.setVisibility(View.GONE);
            resumeLoadingUI.setVisibility(View.VISIBLE);
        }

        private String getFileNameFromUri(Uri uri) {
            String result = null;
            if (uri.getScheme().equals("content")) {
                try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        int displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        if (displayNameIndex != -1) {
                            result = cursor.getString(displayNameIndex);
                        }
                    }
                }
            }
            if (result == null) {
                result = uri.getPath();
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
            return result;
        }

        private File createFileFromUri(Uri uri) throws IOException {
            File tempFile = File.createTempFile("resume_upload", ".pdf", requireContext().getCacheDir());
            tempFile.deleteOnExit();
            
            try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                 OutputStream outputStream = new FileOutputStream(tempFile)) {
                
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            
            return tempFile;
        }

        public boolean validateAndSaveData(Map<String, String> applicationData) {
            // Validate inputs
            String currentCompany = currentCompanyEditText.getText().toString().trim();
            String department = departmentEditText.getText().toString().trim();
            String currentSalary = currentSalaryEditText.getText().toString().trim();
            String expectedSalary = expectedSalaryEditText.getText().toString().trim();
            String totalExperience = totalExperienceEditText.getText().toString().trim();
            String joiningPeriod = joiningPeriodEditText.getText().toString().trim();
            String skills = skillsEditText.getText().toString().trim();

            if (currentCompany.isEmpty()) {
                currentCompanyEditText.setError("Current company is required");
                return false;
            }

            if (department.isEmpty()) {
                departmentEditText.setError("Department is required");
                return false;
            }

            if (totalExperience.isEmpty()) {
                totalExperienceEditText.setError("Total experience is required");
                return false;
            }

            if (joiningPeriod.isEmpty()) {
                joiningPeriodEditText.setError("Joining period is required");
                return false;
            }

            if (skills.isEmpty()) {
                skillsEditText.setError("Skills are required");
                return false;
            }

            // Save data to application map
            applicationData.put("current_company", currentCompany);
            applicationData.put("department", department);
            applicationData.put("current_salary", currentSalary);
            applicationData.put("expected_salary", expectedSalary);
            applicationData.put("experience", totalExperience);
            applicationData.put("experience_display", totalExperience);
            applicationData.put("joining_period", joiningPeriod);
            applicationData.put("skills", skills);

            // Validate resume
            if (resumeFile == null) {
                Toast.makeText(requireContext(), "Please upload your resume", Toast.LENGTH_SHORT).show();
                return false;
            }

            // Save resume information
            applicationData.put("resume_uri", selectedResumeUri.toString());
            applicationData.put("resume_name", selectedResumeFileName);
            applicationData.put("resume_file", resumeFile.getAbsolutePath());

            return true;
        }
    }

    public static class JobApplicationExperienceFragment extends Fragment {
        private TextView jobTitleEditText;
        private TextView companyNameEditText;
        private TextView startDateEditText;
        private TextView endDateEditText;
        private TextView descriptionEditText;
        private View addMoreExperienceButton;
        private ExperienceViewModel viewModel;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_job_application_experience, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            // Initialize UI components
            jobTitleEditText = view.findViewById(R.id.job_title);
            companyNameEditText = view.findViewById(R.id.company_name);

            descriptionEditText = view.findViewById(R.id.description);

            // Set up ViewModel
            viewModel = new ViewModelProvider(requireActivity()).get(ExperienceViewModel.class);

            // Set up date pickers
            setupDatePickers();

            // Set up add more experience button if it exists
            if (addMoreExperienceButton != null) {
                addMoreExperienceButton.setOnClickListener(v -> {
                    saveCurrentExperience();
                    clearExperienceForm();
                });
            }
        }

        private void setupDatePickers() {
            // Implementation for date pickers would go here
            // This would typically involve showing a DatePickerDialog when the date fields are clicked
        }

        private void saveCurrentExperience() {
            // Validate inputs
            String jobTitle = jobTitleEditText.getText().toString().trim();
            String companyName = companyNameEditText.getText().toString().trim();
            String startDate = startDateEditText.getText().toString().trim();
            String endDate = endDateEditText.getText().toString().trim();
            String description = descriptionEditText.getText().toString().trim();

            if (jobTitle.isEmpty() || companyName.isEmpty() || startDate.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create and save experience
            Experience experience = new Experience();
            experience.setJobTitle(jobTitle);
            experience.setCompanyName(companyName);
            experience.setStartDate(startDate);
            experience.setEndDate(endDate);
            experience.setDescription(description);
            experience.setCurrent(endDate.isEmpty());

            viewModel.saveExperience(experience);
        }

        private void clearExperienceForm() {
            jobTitleEditText.setText("");
            companyNameEditText.setText("");
            startDateEditText.setText("");
            endDateEditText.setText("");
            descriptionEditText.setText("");
        }

        public boolean validateAndSaveData(Map<String, String> applicationData) {
            // In the simplified version, we only show existing experiences
            // No need to save anything - just return true since experience is optional
            
            // Add a note about using the external experience form
            Log.d(TAG, "Using external experience form - no validation needed in job application flow");
            
            return true; // Experience is optional, so always return true
        }
    }

    public static class JobApplicationReviewFragment extends Fragment {
        private TextView fullNameValue;
        private TextView emailValue;
        private TextView phoneValue;
        private TextView currentCompanyValue;
        private TextView departmentValue;
        private TextView currentSalaryValue;
        private TextView expectedSalaryValue;
        private TextView totalExperienceValue;
        private TextView joiningPeriodValue;
        private TextView skillsValue;
        private TextView resumeValue;
        private View personalInfoEdit;
        private View employmentDetailsEdit;
        private Button submitApplicationButton;
        private Map<String, String> applicationData;
        private String jobId;
        private String jobTitle;
        private String company;
        private List<Experience> experiences = new ArrayList<>();

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_job_application_review, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            // Initialize UI components
            fullNameValue = view.findViewById(R.id.full_name_value);
            emailValue = view.findViewById(R.id.email_value);
            phoneValue = view.findViewById(R.id.phone_value);
            currentCompanyValue = view.findViewById(R.id.current_company_value);
            departmentValue = view.findViewById(R.id.department_value);
            currentSalaryValue = view.findViewById(R.id.current_salary_value);
            expectedSalaryValue = view.findViewById(R.id.expected_salary_value);
            totalExperienceValue = view.findViewById(R.id.total_experience_value);
            joiningPeriodValue = view.findViewById(R.id.joining_period_value);
            skillsValue = view.findViewById(R.id.skills_value);
            resumeValue = view.findViewById(R.id.resume_value);
            personalInfoEdit = view.findViewById(R.id.personal_info_edit);
            employmentDetailsEdit = view.findViewById(R.id.employment_details_edit);
            submitApplicationButton = view.findViewById(R.id.submit_application_button);

            // Get data from arguments
            Bundle args = getArguments();
            if (args != null) {
                applicationData = (HashMap<String, String>) args.getSerializable("application_data");
                jobId = args.getString("job_id");
                jobTitle = args.getString("job_title");
                company = args.getString("company");
                
                // Populate UI with application data
                populateReviewUI();
            }

            // Set up edit button listeners
            personalInfoEdit.setOnClickListener(v -> navigateToStep(1));
            employmentDetailsEdit.setOnClickListener(v -> navigateToStep(2));

            // Set up submit button
            submitApplicationButton.setOnClickListener(v -> {
                if (getParentFragment() instanceof JobApplicationNewFragment) {
                    ((JobApplicationNewFragment) getParentFragment()).navigateToNextStep();
                }
            });
        }

        private void populateReviewUI() {
            if (applicationData != null) {
                // Personal information
                fullNameValue.setText(applicationData.get("full_name"));
                emailValue.setText(applicationData.get("email"));
                phoneValue.setText(applicationData.get("phone"));

                // Employment details
                currentCompanyValue.setText(applicationData.get("current_company"));
                departmentValue.setText(applicationData.get("department"));
                currentSalaryValue.setText(applicationData.get("current_salary"));
                expectedSalaryValue.setText(applicationData.get("expected_salary"));
                totalExperienceValue.setText(applicationData.get("experience_display"));
                joiningPeriodValue.setText(applicationData.get("joining_period") + " days");
                skillsValue.setText(applicationData.get("skills"));
                
                // Resume
                String resumeName = applicationData.get("resume_name");
                if (resumeName != null && !resumeName.isEmpty()) {
                    resumeValue.setText(resumeName);
                } else {
                    resumeValue.setText("No resume uploaded");
                }
            }
        }

        private void navigateToStep(int step) {
            if (getParentFragment() instanceof JobApplicationNewFragment) {
                JobApplicationNewFragment parent = (JobApplicationNewFragment) getParentFragment();
                // Update current step in parent fragment and load appropriate fragment
                parent.currentStep = step;
                parent.updateStepUI(step);
                parent.loadStepFragment(step);
            }
        }

        public void updateExperiencesList(List<Experience> experiences) {
            this.experiences = experiences;
            // Update UI to show experiences
            // This would typically involve setting up a RecyclerView with the experiences
        }
    }
}
