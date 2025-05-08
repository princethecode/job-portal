package com.example.jobportal.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jobportal.R;
import com.example.jobportal.network.ApiResponse;
import com.example.jobportal.network.ApiCallback;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.jobportal.databinding.ActivityRegisterBinding;
import com.example.jobportal.models.User;
import com.example.jobportal.network.ApiClient;
public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilFullName, tilEmail, tilMobile, tilPassword, tilConfirmPassword;
    private TextInputEditText etFullName, etEmail, etMobile, etPassword, etConfirmPassword;
    private Button btnUploadResume, btnRegister;
    private TextView tvResumeStatus, tvLogin;
    
    private Uri resumeUri = null;
    
    // Activity result launcher for resume file picking
    private final ActivityResultLauncher<String[]> resumePicker = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    // Store URI for later use - no need for persistent permission for this demo
                    resumeUri = uri;
                    String fileName = getFileNameFromUri(uri);
                    tvResumeStatus.setText(fileName);
                    tvResumeStatus.setTextColor(getResources().getColor(R.color.success, null));
                }
            }
    );

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize views
        initViews();
        
        // Set click listeners
        setupClickListeners();
    }

    private void initViews() {
        tilFullName = binding.nameLayout;
        tilEmail = binding.emailLayout;
        tilMobile = binding.mobileLayout;
        tilPassword = binding.passwordLayout;
        tilConfirmPassword = binding.confirmPasswordLayout;
        
        etFullName = binding.nameInput;
        etEmail = binding.emailInput;
        etMobile = binding.mobileInput;
        etPassword = binding.passwordInput;
        etConfirmPassword = binding.confirmPasswordInput;
        
        btnUploadResume = binding.uploadResumeButton;
        btnRegister = binding.registerButton;
        
        tvResumeStatus = binding.resumeFormats; // This TextView serves dual purpose as status
        tvLogin = binding.signInLink;
    }

    private void setupClickListeners() {
        btnUploadResume.setOnClickListener(v -> {
            // Launch file picker for PDF and DOC files
            resumePicker.launch(new String[]{"application/pdf", "application/msword", 
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"});
        });

        btnRegister.setOnClickListener(v -> {
            if (validateInputs()) {
                performRegistration();
            }
        });

        tvLogin.setOnClickListener(v -> {
            // Navigate back to login screen
            finish();
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validate full name
        String fullName = etFullName.getText().toString().trim();
        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError("Full name is required");
            isValid = false;
        } else {
            tilFullName.setError(null);
        }

        // Validate email
        String email = etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email address");
            isValid = false;
        } else {
            tilEmail.setError(null);
        }

        // Validate mobile (optional)
        String mobile = etMobile.getText().toString().trim();
        if (!TextUtils.isEmpty(mobile) && !android.util.Patterns.PHONE.matcher(mobile).matches()) {
            tilMobile.setError("Enter a valid mobile number");
            isValid = false;
        } else {
            tilMobile.setError(null);
        }

        // Validate password
        String password = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            isValid = false;
        } else {
            tilPassword.setError(null);
        }

        // Validate confirm password
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError("Confirm password is required");
            isValid = false;
        } else if (!confirmPassword.equals(password)) {
            tilConfirmPassword.setError("Passwords do not match");
            isValid = false;
        } else {
            tilConfirmPassword.setError(null);
        }

        return isValid;
    }

    private void performRegistration() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
    
        // Show loading state
        btnRegister.setEnabled(false);
        btnRegister.setText("Registering...");
        
        // Convert Uri to File if needed
        java.io.File resumeFile = null;
        if (resumeUri != null) {
            try {
                // Get the file path from URI
                String filePath = getFilePathFromUri(resumeUri);
                if (filePath != null) {
                    resumeFile = new java.io.File(filePath);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Initialize API client
        ApiClient apiClient = new ApiClient(this);
        
        // Call the API for registration with the correct parameters
        // Remove the confirmPassword parameter to match the method signature
        apiClient.register(fullName, email, mobile, password, resumeFile, new ApiCallback<ApiResponse<User>>() {
            @Override
            public void onSuccess(ApiResponse<User> response) {
                btnRegister.setEnabled(true);
                btnRegister.setText("Register");
                
                if (response.isSuccess() && response.getData() != null) {
                    // Registration successful
                    Toast.makeText(RegisterActivity.this, 
                        "Registration successful! Please login.", Toast.LENGTH_LONG).show();
                    
                    // Navigate to login screen
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Registration failed with error from server
                    Toast.makeText(RegisterActivity.this, 
                        response.getMessage() != null ? response.getMessage() : "Registration failed", 
                        Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onError(String errorMessage) {
                btnRegister.setEnabled(true);
                btnRegister.setText("Register");
                
                // Handle network or other errors
                Toast.makeText(RegisterActivity.this, 
                    "Error: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    // Helper method to get file path from URI
    private String getFilePathFromUri(Uri uri) {
        String filePath = null;
        if ("content".equals(uri.getScheme())) {
            String[] projection = { android.provider.MediaStore.Images.Media.DATA };
            android.database.Cursor cursor = null;
            try {
                cursor = getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    filePath = cursor.getString(column_index);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else if ("file".equals(uri.getScheme())) {
            filePath = uri.getPath();
        }
        return filePath;
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = "Resume selected";
        String scheme = uri.getScheme();
        if (scheme.equals("content")) {
            try {
                String[] proj = {android.provider.MediaStore.MediaColumns.DISPLAY_NAME};
                android.database.Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(android.provider.MediaStore.MediaColumns.DISPLAY_NAME);
                    fileName = cursor.getString(columnIndex);
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (scheme.equals("file")) {
            fileName = uri.getLastPathSegment();
        }
        return fileName;
    }
}
