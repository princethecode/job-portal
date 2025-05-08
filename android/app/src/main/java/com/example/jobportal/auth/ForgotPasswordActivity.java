package com.example.jobportal.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jobportal.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.jobportal.databinding.ActivityForgotPasswordBinding;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;
    private Button btnSendInstructions;
    private TextView tvBackToLogin;
    private ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize views
        initViews();
        
        // Set click listeners
        setupClickListeners();
    }

    private void initViews() {
        tilEmail = binding.emailLayout;
        etEmail = binding.emailInput;
        btnSendInstructions = binding.sendInstructionsButton;
        tvBackToLogin = binding.backToLoginLink;
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
            tilEmail.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email address");
            isValid = false;
        } else {
            tilEmail.setError(null);
        }

        return isValid;
    }

    private void sendResetInstructions() {
        String email = etEmail.getText().toString().trim();

        // TODO: Implement actual API call to send password reset instructions
        // For now, just show a toast message
        Toast.makeText(this, "Password reset instructions sent to " + email, Toast.LENGTH_LONG).show();
        
        // Return to login screen after a short delay
        btnSendInstructions.setEnabled(false);
        btnSendInstructions.setText("Sent!");
        
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 2000);
    }
}
