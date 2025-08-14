package com.example.jobportal.ui.application;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.jobportal.R;
import com.example.jobportal.models.User;
import com.example.jobportal.network.ApiCallback;
import com.example.jobportal.network.ApiClient;
import com.example.jobportal.network.ApiResponse;
import com.example.jobportal.utils.FileUtils;
import com.example.jobportal.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class JobApplicationEmploymentFragment extends Fragment {
    private static final String TAG = "JobAppEmploymentFrag";

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

        // Setup the launcher that handles the result of the file picker
        setupGetContentLauncher();
        
        // Populate fields with user's existing data, including the resume status
        populateFieldsWithUserData();
        
        // Set up the click listener to open the file picker
        resumeUploadContainer.setOnClickListener(v -> openFilePicker());
    }

    /**
     * Initializes the ActivityResultLauncher for picking a resume file.
     */
    private void setupGetContentLauncher() {
        getContentLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedResumeUri = uri;
                    selectedResumeFileName = FileUtils.getFileName(requireContext(), uri);
                    
                    // Upload resume to server immediately
                    uploadResumeToServer(uri, selectedResumeFileName);
                    
                    Log.d(TAG, "New resume selected: " + selectedResumeFileName);
                }
            });
    }

    /**
     * Populates the form fields with data from the current user.
     * This includes showing the existing resume or the upload prompt.
     */
    private void populateFieldsWithUserData() {
        if (currentUser == null) {
            updateResumeUI(null); // Show "Upload Now" if no user data
            return;
        }

        try {
            currentCompanyEditText.setText(currentUser.getCurrentCompany());
            departmentEditText.setText(currentUser.getDepartment());
            currentSalaryEditText.setText(currentUser.getCurrentSalary());
            expectedSalaryEditText.setText(currentUser.getExpectedSalary());
            joiningPeriodEditText.setText(currentUser.getJoiningPeriod());
            skillsEditText.setText(currentUser.getSkills());
            
            // Set total experience if available
            String experience = currentUser.getExperience();
            if (experience != null && !experience.isEmpty()) {
                try {
                    // Assuming experience is stored as a number (total months)
                    int totalMonths = Integer.parseInt(experience);
                    int years = totalMonths / 12;
                    int months = totalMonths % 12;
                    totalExperienceEditText.setText(years + " years " + months + " months");
                } catch (NumberFormatException e) {
                    // If experience is not a number, just use the raw value
                    totalExperienceEditText.setText(experience);
                    Log.w(TAG, "Experience is not in expected format: " + e.getMessage());
                }
            }
            
            // Check for an existing resume and update the UI
            String resumePath = currentUser.getResume();
            if (resumePath != null && !resumePath.isEmpty()) {
                String fileName = resumePath.contains("/") ? resumePath.substring(resumePath.lastIndexOf("/") + 1) : resumePath;
                selectedResumeFileName = fileName;
                updateResumeUI(fileName);
                Log.d(TAG, "Existing resume found: " + fileName);
            } else {
                updateResumeUI(null);
                Log.d(TAG, "No existing resume found");
            }
            
        } catch (Exception e) {
            Log.w(TAG, "Some employment fields may not exist in User model: " + e.getMessage());
            updateResumeUI(null); // Show "Upload Now" in case of any error
        }
    }

    /**
     * Opens the system file picker to select a PDF resume.
     */
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        getContentLauncher.launch("application/pdf");
    }
    
    /**
     * Uploads the selected resume to the server using the same API as edit profile
     */
    private void uploadResumeToServer(Uri resumeUri, String fileName) {
        try {
            // Show loading state
            showResumeLoading();
            
            // Create a temporary file from the URI
            File resumeFile = createFileFromUri(resumeUri);
            
            // Get ApiClient instance
            ApiClient apiClient = ApiClient.getInstance(requireContext());
            
            // Upload resume using the same method as in edit profile
            apiClient.uploadResume(resumeFile, new ApiCallback<ApiResponse<User>>() {
                @Override
                public void onSuccess(ApiResponse<User> response) {
                    if (response.isSuccess() && response.getData() != null) {
                        // Update UI to show success with new upload indicator
                        updateResumeUI(fileName, true);
                        
                        // Pass the resume info to the parent fragment
                        if (getParentFragment() instanceof JobApplicationNewFragment) {
                            ((JobApplicationNewFragment) getParentFragment()).setSelectedResumeUri(resumeUri);
                        }
                        
                        Log.d(TAG, "Resume uploaded successfully to server");
                    } else {
                        // Show error and revert to upload state
                        updateResumeUI(null);
                        Toast.makeText(requireContext(), 
                            "Failed to upload resume: " + response.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onError(String errorMessage) {
                    // Show error and revert to upload state
                    updateResumeUI(null);
                    Toast.makeText(requireContext(), 
                        "Error uploading resume: " + errorMessage, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Resume upload failed: " + errorMessage);
                }
            });
            
        } catch (Exception e) {
            // Show error and revert to upload state
            updateResumeUI(null);
            Log.e(TAG, "Error creating file from URI", e);
            Toast.makeText(requireContext(), 
                "Error processing resume file", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Creates a temporary file from the given URI
     */
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

    /**
     * Updates the resume UI section to either show the resume's name or the "Upload Now" prompt.
     * @param fileName The name of the resume file. If null or empty, shows the "Upload Now" UI.
     */
    private void updateResumeUI(String fileName) {
        updateResumeUI(fileName, false);
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

        if (joiningPeriod.isEmpty()) {
            joiningPeriodEditText.setError("Joining period is required");
            return false;
        }

        if (skills.isEmpty()) {
            skillsEditText.setError("Skills are required");
            return false;
        }

        // Use the experience directly from the user's profile
        int totalExperienceMonths = 0;
        
        // Store the display value for the review screen
        applicationData.put("experience_display", totalExperience);
        
        // Try to extract numeric value if possible (for API)
        if (!totalExperience.isEmpty()) {
            try {
                // If it's just a number, use it directly
                totalExperienceMonths = Integer.parseInt(totalExperience);
            } catch (NumberFormatException e) {
                // Not a simple number, leave as is
                Log.d(TAG, "Experience is not a simple number: " + totalExperience);
            }
        }

        // Save data to application map
        applicationData.put("current_company", currentCompany);
        applicationData.put("department", department);
        applicationData.put("current_salary", currentSalary);
        applicationData.put("expected_salary", expectedSalary);
        applicationData.put("experience", String.valueOf(totalExperienceMonths));
        // The experience display value is already set above
        applicationData.put("joining_period", joiningPeriod);
        applicationData.put("skills", skills);

        if (selectedResumeUri != null) {
            applicationData.put("resume_uri", selectedResumeUri.toString());
            applicationData.put("resume_name", selectedResumeFileName);
        }

        return true;
    }
}
