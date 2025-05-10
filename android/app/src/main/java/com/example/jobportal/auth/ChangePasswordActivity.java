package com.example.jobportal.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jobportal.R;
import com.example.jobportal.network.ApiCallback;
import com.example.jobportal.network.ApiClient;
import com.example.jobportal.network.ApiResponse;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ChangePasswordActivity extends AppCompatActivity {
    private static final String TAG = "ChangePasswordActivity";
    
    private TextInputLayout currentPasswordLayout;
    private TextInputLayout newPasswordLayout;
    private TextInputLayout confirmPasswordLayout;
    private TextInputEditText currentPasswordInput;
    private TextInputEditText newPasswordInput;
    private TextInputEditText confirmPasswordInput;
    private Button changePasswordButton;
    private ProgressBar progressBar;
    
    private ApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        
        // Initialize views
        currentPasswordLayout = findViewById(R.id.currentPasswordLayout);
        newPasswordLayout = findViewById(R.id.newPasswordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);
        currentPasswordInput = findViewById(R.id.currentPasswordInput);
        newPasswordInput = findViewById(R.id.newPasswordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        progressBar = findViewById(R.id.progressBar);
        
        // Initialize API client
        apiClient = ApiClient.getInstance(getApplicationContext());
        
        // Setup action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Change Password");
        }
        
        // Setup change password button
        changePasswordButton.setOnClickListener(v -> changePassword());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void changePassword() {
        // Reset errors
        currentPasswordLayout.setError(null);
        newPasswordLayout.setError(null);
        confirmPasswordLayout.setError(null);
        
        // Get input values
        String currentPassword = currentPasswordInput.getText().toString().trim();
        String newPassword = newPasswordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();
        
        // Validate input
        boolean isValid = true;
        
        if (TextUtils.isEmpty(currentPassword)) {
            currentPasswordLayout.setError("Current password is required");
            isValid = false;
        }
        
        if (TextUtils.isEmpty(newPassword)) {
            newPasswordLayout.setError("New password is required");
            isValid = false;
        } else if (newPassword.length() < 8) {
            newPasswordLayout.setError("Password must be at least 8 characters long");
            isValid = false;
        }
        
        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordLayout.setError("Please confirm your new password");
            isValid = false;
        } else if (!newPassword.equals(confirmPassword)) {
            confirmPasswordLayout.setError("Passwords do not match");
            isValid = false;
        }
        
        if (!isValid) {
            return;
        }
        
        // Show loading
        setLoading(true);
        
        // Call API
        apiClient.changePassword(currentPassword, newPassword, new ApiCallback<ApiResponse<Void>>() {
            @Override
            public void onSuccess(ApiResponse<Void> response) {
                setLoading(false);
                Toast.makeText(ChangePasswordActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                setLoading(false);
                Toast.makeText(ChangePasswordActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Password change error: " + errorMessage);
                
                // Handle specific error cases
                if (errorMessage.contains("current password") || errorMessage.contains("incorrect")) {
                    currentPasswordLayout.setError("Current password is incorrect");
                }
            }
        });
    }
    
    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            changePasswordButton.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            changePasswordButton.setEnabled(true);
        }
    }
} 