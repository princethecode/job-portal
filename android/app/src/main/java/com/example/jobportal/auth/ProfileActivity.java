package com.example.jobportal.auth;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.jobportal.BuildConfig;
import com.example.jobportal.R;
import com.example.jobportal.models.User;
import com.example.jobportal.network.ApiCallback;
import com.example.jobportal.network.ApiClient;
import com.example.jobportal.network.ApiResponse;
import com.example.jobportal.utils.SessionManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private static final int PICK_RESUME_REQUEST = 1001;
    private static final int PICK_IMAGE_REQUEST = 1002;
    
    // UI Elements
    private TextInputEditText etName, etEmail, etMobile, etSkills, etExperience;
    private TextInputEditText etLocation, etJobTitle, etAboutMe; // New fields
    private Button saveButton, uploadResumeButton, changePasswordButton, logoutButton;
    private Button uploadImageButton; // New button
    private ImageView profileImageView; // New image view
    private ProgressBar progressBar;
    private Toolbar toolbar;
    
    // Data handling
    private ApiClient apiClient;
    private SessionManager sessionManager;
    private User currentUser;
    private File resumeFile = null;
    private String selectedFileName = null;
    private Uri selectedImageUri = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        // Initialize API client and session manager
        apiClient = ApiClient.getInstance(getApplicationContext());
        sessionManager = SessionManager.getInstance(getApplicationContext());

        // Set up toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        // Initialize existing views with correct IDs from layout
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
        
        // Initialize new UI elements
        etLocation = findViewById(R.id.locationInput);
        etJobTitle = findViewById(R.id.jobTitleInput);
        etAboutMe = findViewById(R.id.aboutMeInput);
        profileImageView = findViewById(R.id.profileImageView);
        uploadImageButton = findViewById(R.id.uploadImageButton);

        // Load current profile data
        loadProfileData();

        // Set up resume upload
        uploadResumeButton.setOnClickListener(v -> pickResumeFile());
        
        // Set up image upload
        uploadImageButton.setOnClickListener(v -> pickProfileImage());
        
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
    
    /**
     * Opens image picker to select profile image
     */
    private void pickProfileImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        
        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select Profile Image"),
                    PICK_IMAGE_REQUEST);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager app", 
                    Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Create a temp file from the selected image URI
     * @param uri The image URI
     * @return The created file
     */
    private File createImageFileFromUri(Uri uri) throws IOException {
        // Create a temp file with appropriate prefix and suffix
        File tempFile = File.createTempFile("profile_photo", ".jpg", getCacheDir());
        tempFile.deleteOnExit();
        
        // Copy the content from the URI to the file
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(tempFile)) {
            
            if (inputStream == null) {
                throw new IOException("Failed to open input stream");
            }
            
            // Copy in chunks
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        
        return tempFile;
    }
    
    /**
     * Upload the selected profile image to the server
     */
    private void uploadProfileImage(File photoFile) {
        if (photoFile == null) {
            showToast("No photo selected");
            return;
        }
        
        // Show loading indicator
        progressBar.setVisibility(View.VISIBLE);
        uploadImageButton.setEnabled(false);
        
        // Upload the photo file
        apiClient.uploadProfilePhoto(photoFile, new ApiCallback<ApiResponse<User>>() {
            @Override
            public void onSuccess(ApiResponse<User> response) {
                // Hide loading indicator
                progressBar.setVisibility(View.GONE);
                uploadImageButton.setEnabled(true);
                
                if (response.isSuccess() && response.getData() != null) {
                    // Update the user in session manager
                    currentUser = response.getData();
                    sessionManager.saveUser(currentUser);
                    
                    // Show success message
                    showToast("Profile photo uploaded successfully");
                } else {
                    showToast("Failed to upload profile photo: " + response.getMessage());
                }
            }

            @Override
            public void onError(String errorMessage) {
                // Hide loading indicator
                progressBar.setVisibility(View.GONE);
                uploadImageButton.setEnabled(true);
                
                showToast("Error: " + errorMessage);
            }
        });
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
        } else if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            try {
                selectedImageUri = data.getData();
                
                // Display the selected image in the ImageView
                profileImageView.setImageURI(selectedImageUri);
                profileImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                profileImageView.setPadding(0, 0, 0, 0);
                
                // Create a file from the selected image URI
                File photoFile = createImageFileFromUri(selectedImageUri);
                
                // Upload the photo file to the server
                uploadProfileImage(photoFile);
                
            } catch (Exception e) {
                Log.e(TAG, "Error handling profile image", e);
                showToast("Error processing profile image: " + e.getMessage());
            }
        }
    }
    
    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
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
            
            // Set location from user data if available
            if (currentUser.getLocation() != null && !currentUser.getLocation().isEmpty()) {
                etLocation.setText(currentUser.getLocation());
            }
            
            // Set job title from user data if available
            if (currentUser.getJobTitle() != null && !currentUser.getJobTitle().isEmpty()) {
                etJobTitle.setText(currentUser.getJobTitle());
            }
            
            // Set about me text from user data if available
            if (currentUser.getAboutMe() != null && !currentUser.getAboutMe().isEmpty()) {
                etAboutMe.setText(currentUser.getAboutMe());
            }
            
            if (currentUser.getSkills() != null) {
                etSkills.setText(currentUser.getSkills());
            }
            
            if (currentUser.getExperience() != null) {
                etExperience.setText(currentUser.getExperience());
            }
            
            if (currentUser.getResume() != null && !currentUser.getResume().isEmpty()) {
                uploadResumeButton.setText("Resume: " + currentUser.getResume());
            }
            
            // Load profile photo if available
            if (currentUser.getProfilePhoto() != null && !currentUser.getProfilePhoto().isEmpty()) {
                // Use Glide to load the image from URL
                Glide.with(this)
                    .load(BuildConfig.API_BASE_URL + currentUser.getProfilePhoto())
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .centerCrop()
                    .into(profileImageView);
                
                // Set proper styling for the ImageView
                profileImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                profileImageView.setPadding(0, 0, 0, 0);
            } else if (selectedImageUri != null) {
                // If we have image URI saved locally but not yet uploaded, display it
                profileImageView.setImageURI(selectedImageUri);
                profileImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                profileImageView.setPadding(0, 0, 0, 0);
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
        // Validate inputs
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String skills = etSkills.getText().toString().trim();
        String experience = etExperience.getText().toString().trim();
        
        // Get values from new UI fields
        String location = etLocation.getText().toString().trim();
        String jobTitle = etJobTitle.getText().toString().trim();
        String aboutMe = etAboutMe.getText().toString().trim();
        
        if (name.isEmpty() || email.isEmpty()) {
            showToast("Name and email are required");
            return;
        }
        
        // Show loading indicator
        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);
        
        // Create user object with form data
        User updatedUser = new User();
        updatedUser.setFullName(name);
        updatedUser.setEmail(email);
        updatedUser.setPhone(mobile);
        updatedUser.setSkills(skills);
        updatedUser.setExperience(experience);
        
        // Set the new profile fields
        updatedUser.setLocation(location);
        updatedUser.setJobTitle(jobTitle);
        updatedUser.setAboutMe(aboutMe);
        
        Log.d(TAG, "Saving profile fields - Location: " + location);
        Log.d(TAG, "Saving profile fields - Job Title: " + jobTitle);
        Log.d(TAG, "Saving profile fields - About Me: " + aboutMe);
        
        // Keep the resume filename if it exists
        if (currentUser != null && currentUser.getResume() != null) {
            updatedUser.setResume(currentUser.getResume());
        }
        
        // Update profile via API
        apiClient.updateUserProfile(
            updatedUser, 
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
                    // Update session with new user data
                    User savedUser = response.getData();
                    sessionManager.saveUser(savedUser);
                    
                    // For a complete implementation, we would also save the profile image
                    // and the new fields (location, job title, aboutMe) to the user's profile
                    
                    showToast("Profile updated successfully");
                    finish();
                } else {
                    showToast("Failed to update profile: " + response.getMessage());
                }
            }

            @Override
            public void onError(String errorMessage) {
                // Hide loading indicator
                progressBar.setVisibility(View.GONE);
                saveButton.setEnabled(true);
                
                showToast("Error: " + errorMessage);
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