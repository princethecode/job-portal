package com.example.jobportal.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.jobportal.R;
import com.example.jobportal.models.User;
import com.example.jobportal.network.ApiCallback;
import com.example.jobportal.network.ApiClient;
import com.example.jobportal.network.ApiResponse;
import com.example.jobportal.utils.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private static final int PICK_RESUME_REQUEST = 1001;
    
    private EditText etName, etEmail, etMobile, etSkills, etExperience;
    private Button saveButton, uploadResumeButton, changePasswordButton, logoutButton;
    private ProgressBar progressBar;
    
    private ApiClient apiClient;
    private SessionManager sessionManager;
    private User currentUser;
    private File resumeFile = null;
    private String selectedFileName = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        // Initialize API client and session manager
        apiClient = ApiClient.getInstance(getApplicationContext());
        sessionManager = SessionManager.getInstance(getApplicationContext());

        // Initialize views with correct IDs from layout
        etName = findViewById(R.id.nameInput);
        etEmail = findViewById(R.id.emailInput);
        etMobile = findViewById(R.id.mobileInput);
        etSkills = findViewById(R.id.skillsInput);
        etExperience = findViewById(R.id.experienceInput);
        saveButton = findViewById(R.id.saveChangesButton);
        uploadResumeButton = findViewById(R.id.uploadResumeButton);
        progressBar = findViewById(R.id.progressBar);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        logoutButton = findViewById(R.id.logoutButton);
        
        // Set up back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Profile");
        }

        // Load current profile data
        loadProfileData();

        // Set up resume upload
        uploadResumeButton.setOnClickListener(v -> pickResumeFile());
        
        // Set up save button
        saveButton.setOnClickListener(v -> saveProfile());
    
        // Set up change password button
        changePasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });
        
        // Set click listener for logout button
        logoutButton.setOnClickListener(v -> handleLogout());
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void pickResumeFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        
        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select Resume PDF"),
                    PICK_RESUME_REQUEST);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager app", 
                    Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_RESUME_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedFileUri = data.getData();
            try {
                selectedFileName = getFileNameFromUri(selectedFileUri);
                resumeFile = createFileFromUri(selectedFileUri);
                uploadResumeButton.setText("Resume selected: " + selectedFileName);
                showToast("Resume selected: " + selectedFileName);
            } catch (IOException e) {
                Log.e(TAG, "Error handling resume file", e);
                showToast("Error selecting resume file");
            }
        }
    }
    
    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int displayNameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
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
        File tempFile = File.createTempFile("resume_upload", ".pdf", getCacheDir());
        tempFile.deleteOnExit();
        
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(tempFile)) {
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        
        return tempFile;
    }

    private void loadProfileData() {
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            // Redirect to login if not logged in
            navigateToLogin();
            return;
        }
        
        // Show loading indicator
        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);
        
        // First try to get user from session
        currentUser = sessionManager.getUser();
        if (currentUser != null) {
            // Pre-fill form fields with saved data
            etName.setText(currentUser.getFullName());
            etEmail.setText(currentUser.getEmail());
            etMobile.setText(currentUser.getPhone());
            
            if (currentUser.getSkills() != null) {
                etSkills.setText(currentUser.getSkills());
            }
            
            if (currentUser.getExperience() != null) {
                etExperience.setText(currentUser.getExperience());
            }
            
            if (currentUser.getResume() != null && !currentUser.getResume().isEmpty()) {
                uploadResumeButton.setText("Resume: " + currentUser.getResume());
            }
        }
        
        // Then fetch latest data from API
        apiClient.getUserProfile(new ApiCallback<ApiResponse<User>>() {
            @Override
            public void onSuccess(ApiResponse<User> response) {
                // Hide loading indicator
                progressBar.setVisibility(View.GONE);
                saveButton.setEnabled(true);
                
                if (response.isSuccess() && response.getData() != null) {
                    currentUser = response.getData();
                    
                    // Update form fields with user data
                    etName.setText(currentUser.getFullName());
                    etEmail.setText(currentUser.getEmail());
                    etMobile.setText(currentUser.getPhone());
                    
                    if (currentUser.getSkills() != null) {
                        etSkills.setText(currentUser.getSkills());
                    }
                    
                    if (currentUser.getExperience() != null) {
                        etExperience.setText(currentUser.getExperience());
                    }
                    
                    if (currentUser.getResume() != null && !currentUser.getResume().isEmpty()) {
                        uploadResumeButton.setText("Resume: " + currentUser.getResume());
                    }
                    
                    // Save user data to session
                    sessionManager.saveUser(currentUser);
                } else {
                    showToast("Failed to load latest profile data. Using saved data.");
                }
            }

            @Override
            public void onError(String errorMessage) {
                // Hide loading indicator
                progressBar.setVisibility(View.GONE);
                saveButton.setEnabled(true);
                
                showToast("Error: " + errorMessage);
                Log.e(TAG, "Error loading profile: " + errorMessage);
            }
        });
    }

    private void saveProfile() {
        // Validate input
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String skills = etSkills.getText().toString().trim();
        String experience = etExperience.getText().toString().trim();
        
        if (name.isEmpty() || email.isEmpty()) {
            showToast("Name and email are required");
            return;
        }
        
        // Update user object with new data
        if (currentUser == null) {
            currentUser = new User();
        }
        currentUser.setFullName(name);
        currentUser.setEmail(email);
        currentUser.setPhone(mobile);
        
        // Show loading indicator
        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);
        
        // Save to API
        apiClient.updateUserProfile(
            currentUser, 
            skills.isEmpty() ? null : skills, 
            experience.isEmpty() ? null : experience, 
            resumeFile,
            new ApiCallback<ApiResponse<User>>() {
                @Override
                public void onSuccess(ApiResponse<User> response) {
                    // Hide loading indicator
                    progressBar.setVisibility(View.GONE);
                    saveButton.setEnabled(true);
                    
                    if (response.isSuccess() && response.getData() != null) {
                        // Update currentUser with the response
                        currentUser = response.getData();
                        
                        // Save updated user to session
                        sessionManager.saveUser(currentUser);
                        
                        showToast("Profile updated successfully");
                        finish(); // Close activity and return to previous screen
                    } else {
                        showToast("Failed to update profile");
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    // Hide loading indicator
                    progressBar.setVisibility(View.GONE);
                    saveButton.setEnabled(true);
                    
                    showToast("Error: " + errorMessage);
                    Log.e(TAG, "Error updating profile: " + errorMessage);
                }
            });
    }

    private void handleLogout() {
        // Clear session and logout
        sessionManager.logout();
        
        // Navigate to login screen
        navigateToLogin();
    }
    
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}