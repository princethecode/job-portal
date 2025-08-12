package com.example.jobportal.auth;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jobportal.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.jobportal.databinding.ActivityResetPasswordBinding;
import com.example.jobportal.network.ApiCallback;
import com.example.jobportal.network.ApiClient;
import com.example.jobportal.network.ApiResponse;

import java.util.HashMap;
import java.util.Map;

public class ResetPasswordActivity extends AppCompatActivity {
    private static final String TAG = "ResetPasswordActivity";
    
    private ActivityResetPasswordBinding binding;
    private TextInputLayout passwordLayout;
    private TextInputLayout confirmPasswordLayout;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;
    private Button resetButton;
    private TextView statusMessage;
    private ProgressBar progressBar;
    
    private String token;
    private String email;
    private ApiClient apiClient;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Get data from intent
        token = getIntent().getStringExtra("token");
        email = getIntent().getStringExtra("email");
        
        if (TextUtils.isEmpty(token) || TextUtils.isEmpty(email)) {
            // Missing required data, show error and finish
            Toast.makeText(this, "Invalid reset request", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize ApiClient
        apiClient = ApiClient.getInstance(this);
        
        // Initialize views
        initViews();
        
        // Set up listeners
        setupListeners();
    }
    
    private void initViews() {
        passwordLayout = binding.passwordLayout;
        confirmPasswordLayout = binding.confirmPasswordLayout;
        passwordInput = binding.passwordInput;
        confirmPasswordInput = binding.confirmPasswordInput;
        resetButton = binding.resetButton;
        statusMessage = binding.statusMessage;
        progressBar = binding.progressBar;
        
        // Show email in UI
        TextView emailText = binding.emailText;
        emailText.setText(email);
    }
    
    private void setupListeners() {
        // Password strength checker
        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                validatePassword(s.toString());
            }
        });
        
        // Password confirmation checker
        confirmPasswordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                validatePasswordMatch(passwordInput.getText().toString(), s.toString());
            }
        });
        
        // Reset button click
        resetButton.setOnClickListener(v -> {
            if (validateFormInputs()) {
                resetPassword();
            }
        });
    }
    
    private boolean validatePassword(String password) {
        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Password is required");
            return false;
        }
        
        if (password.length() < 8) {
            passwordLayout.setError("Password must be at least 8 characters");
            return false;
        }
        
        // Check for password strength
        boolean hasLetter = false;
        boolean hasDigit = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
            
            if (hasLetter && hasDigit) {
                break;
            }
        }
        
        if (!hasLetter || !hasDigit) {
            passwordLayout.setError("Password must contain at least one letter and one number");
            return false;
        }
        
        passwordLayout.setError(null);
        return true;
    }
    
    private boolean validatePasswordMatch(String password, String confirmPassword) {
        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordLayout.setError("Please confirm your password");
            return false;
        }
        
        if (!password.equals(confirmPassword)) {
            confirmPasswordLayout.setError("Passwords don't match");
            return false;
        }
        
        confirmPasswordLayout.setError(null);
        return true;
    }
    
    private boolean validateFormInputs() {
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();
        
        boolean isPasswordValid = validatePassword(password);
        boolean isPasswordMatch = validatePasswordMatch(password, confirmPassword);
        
        return isPasswordValid && isPasswordMatch;
    }
    
    private void resetPassword() {
        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        resetButton.setEnabled(false);
        statusMessage.setVisibility(View.GONE);
        
        // Prepare request parameters
        Map<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("token", token);
        params.put("password", passwordInput.getText().toString());
        params.put("password_confirmation", confirmPasswordInput.getText().toString());
        
        // Call API
        apiClient.resetPassword(params, new ApiCallback<ApiResponse<Void>>() {
            @Override
            public void onSuccess(ApiResponse<Void> response) {
                // Hide progress
                progressBar.setVisibility(View.GONE);
                
                // Show success message
                statusMessage.setVisibility(View.VISIBLE);
                statusMessage.setText(response.getMessage() != null ? 
                        response.getMessage() : "Password has been reset successfully");
                statusMessage.setTextColor(getResources().getColor(R.color.green_success, null));
                
                // Disable input fields
                passwordInput.setEnabled(false);
                confirmPasswordInput.setEnabled(false);
                
                // Change button to "Go to Login"
                resetButton.setEnabled(true);
                resetButton.setText("Go to Login");
                resetButton.setOnClickListener(v -> finish());
                
                // Automatically return to login after 3 seconds
                new android.os.Handler().postDelayed(() -> finish(), 3000);
            }
            
            @Override
            public void onError(String errorMessage) {
                // Hide progress
                progressBar.setVisibility(View.GONE);
                resetButton.setEnabled(true);
                
                // Show error message
                statusMessage.setVisibility(View.VISIBLE);
                statusMessage.setText("Error: " + errorMessage);
                statusMessage.setTextColor(getResources().getColor(R.color.red_error, null));
                
                // Log error
                Log.e(TAG, "Reset password error: " + errorMessage);
            }
        });
    }
} 