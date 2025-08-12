package com.example.jobportal.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jobportal.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.jobportal.databinding.ActivityForgotPasswordBinding;
import com.example.jobportal.network.ApiCallback;
import com.example.jobportal.network.ApiClient;
import com.example.jobportal.network.ApiResponse;
import android.util.Log;
import android.app.ProgressDialog;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPasswordActivity";
    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;
    private Button btnSendInstructions;
    private TextView tvBackToLogin;
    private TextView tvResultMessage;
    private ActivityForgotPasswordBinding binding;
    private ApiClient apiClient;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize ApiClient
        apiClient = ApiClient.getInstance(this);
        
        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending reset instructions...");
        progressDialog.setCancelable(false);
        
        // Initialize views
        initViews();
        
        // Set click listeners
        setupClickListeners();
    }

    private void initViews() {
        tilEmail = binding.emailLayout;
        etEmail = binding.inputField;
        btnSendInstructions = binding.sendInstructionsButton;
        tvBackToLogin = binding.backToLoginLink;
        tvResultMessage = binding.resultMessage;
    }

    private void setupClickListeners() {
        btnSendInstructions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    sendResetInstructions();
                }
            }
        });

        tvBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to login screen
                finish();
            }
        });
    }

    private boolean validateInput() {
        boolean isValid = true;

        // Validate email
        String email = etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Please enter your email address");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Please enter a valid email address");
            isValid = false;
        } else {
            tilEmail.setError(null);
        }

        return isValid;
    }

    private void sendResetInstructions() {
        String email = etEmail.getText().toString().trim();
        
        // Show progress dialog
        progressDialog.show();
        
        // Disable button to prevent multiple submissions
        btnSendInstructions.setEnabled(false);
        
        // Prepare request parameters
        java.util.Map<String, String> params = new java.util.HashMap<>();
        params.put("email", email);
        
        // Make API call to send password reset instructions
        apiClient.forgotPassword(params, new ApiCallback<ApiResponse<Object>>() {
            @Override
            public void onSuccess(ApiResponse<Object> response) {
                // Hide progress dialog
                progressDialog.dismiss();
                
                // Show success message
                tvResultMessage.setVisibility(View.VISIBLE);
                tvResultMessage.setText("Password reset link sent to your email address. Please check your inbox to continue.");
                
                // Update button
                btnSendInstructions.setText("Sent!");
                
                // Return to login after delay
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 5000); // 5 second delay
            }

            @Override
            public void onError(String errorMessage) {
                // Hide progress dialog
                progressDialog.dismiss();
                
                // Log error
                Log.e(TAG, "Error sending reset instructions: " + errorMessage);
                
                // Show error message
                tvResultMessage.setVisibility(View.VISIBLE);
                tvResultMessage.setText("Error: " + errorMessage);
                
                // Re-enable button
                btnSendInstructions.setEnabled(true);
            }
        });
    }
}
