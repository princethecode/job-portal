package com.example.jobportal.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jobportal.MainActivity;
import com.example.jobportal.R;
import com.example.jobportal.models.LoginResponse;
import com.example.jobportal.network.ApiClient;
import com.example.jobportal.databinding.ActivityLoginBinding;
import com.example.jobportal.network.ApiResponse;
import com.example.jobportal.models.User;
import com.example.jobportal.network.ApiCallback;
import com.example.jobportal.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {
    
    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvRegister;
    private View progressBar;
    private ActivityLoginBinding binding;
    
    // User data to be passed after successful login
    private User authenticatedUser;
    private ApiClient apiClient;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize API client and SessionManager
        apiClient = new ApiClient(this);
        sessionManager = SessionManager.getInstance(getApplicationContext());
        
        // Check if user is already logged in
        if (sessionManager.isSessionValid()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Initialize views
        initViews();
        
        // Set click listeners
        setupClickListeners();
    }

    private void initViews() {
        tilEmail = binding.emailLayout;
        tilPassword = binding.passwordLayout;
        etEmail = binding.emailInput;
        etPassword = binding.passwordInput;
        btnLogin = binding.loginButton;
        tvForgotPassword = binding.forgotPassword;
        tvRegister = binding.registerLink;
        progressBar = findViewById(R.id.progress_bar); // Make sure to add this to your layout
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> {
            if (validateInputs()) {
                attemptLogin();
            }
        });

        tvForgotPassword.setOnClickListener(v -> {
            // Navigate to forgot password screen
            navigateToForgotPassword();
        });

        tvRegister.setOnClickListener(v -> {
            // Navigate to registration screen
            navigateToRegister();
        });
    }

    private boolean validateInputs() {
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

        return isValid;
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
    
        // Show loading state
        showProgress(true);
        
        // Check for internet connectivity first
        if (!isNetworkAvailable()) {
            showProgress(false);
            Toast.makeText(this, "No internet connection. Please check your network settings and try again.", 
                    Toast.LENGTH_LONG).show();
            return;
        }
        
        // Call the API for login
        apiClient.login(email, password, new ApiCallback<ApiResponse<LoginResponse>>() {
            @Override
            public void onSuccess(ApiResponse<LoginResponse> response) {
                showProgress(false);
                if (response.isSuccess() && response.getData() != null) {
                    LoginResponse loginResponse = response.getData();
                    User user = loginResponse.getUser();
                    String token = loginResponse.getAccessToken();
                    
                    Log.d("JobPortal", "Login response: " + response.toString());
                    Log.d("JobPortal", "User data: " + user.toString());
                    Log.d("JobPortal", "Token: " + token);
                    
                    if (user != null && token != null && !token.isEmpty()) {
                        // Save user session with token
                        sessionManager.createLoginSession(
                            Integer.parseInt(user.getId()),
                            user.getFullName(),
                            user.getEmail(),
                            token
                        );
                        
                        Log.d("JobPortal", "Login successful - Token: " + token);
                        Log.d("JobPortal", "User ID: " + user.getId());
                        
                        // Navigate to main activity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        String errorMsg = "Login failed: ";
                        if (user == null) errorMsg += "User data is null. ";
                        if (token == null || token.isEmpty()) errorMsg += "Token is missing. ";
                        Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                } else {
                    String errorMsg = response.getMessage() != null ? 
                        response.getMessage() : "Login failed: Invalid response data";
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }
    
            @Override
            public void onError(String errorMessage) {
                showProgress(false);
                
                // Check if it's a DNS or connection error
                if (errorMessage.contains("UnknownHostException") || 
                    errorMessage.contains("Unable to resolve host")) {
                    Toast.makeText(LoginActivity.this, 
                        "Cannot connect to server. Please check your internet connection or try again later.", 
                        Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    
    /**
     * Check if the device has an active internet connection
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                return capabilities != null && (
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                );
            } else {
                // For older Android versions
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
        }
        return false;
    }
    
    private void navigateToForgotPassword() {
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    private void navigateToRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
    
    private void showProgress(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        btnLogin.setEnabled(!show);
        btnLogin.setText(show ? "Logging in..." : "Login");
    }
}
