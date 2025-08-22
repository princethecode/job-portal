package com.example.jobportal.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.jobportal.R;
import com.example.jobportal.models.Recruiter;
import com.example.jobportal.models.RecruiterLoginRequest;
import com.example.jobportal.models.RecruiterLoginResponse;
import com.example.jobportal.network.ApiClient;
import com.example.jobportal.network.ApiCallback;
import com.example.jobportal.recruiter.RecruiterMainActivity;
import retrofit2.Call;
import retrofit2.Response;

public class RecruiterLoginActivity extends AppCompatActivity {
    
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private TextView tvRegister, tvBackToUserLogin;
    
    private RecruiterAuthHelper authHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recruiter_login);
        
        authHelper = RecruiterAuthHelper.getInstance(this);
        
        // Initialize views
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progress_bar);
        tvRegister = findViewById(R.id.tv_register);
        tvBackToUserLogin = findViewById(R.id.tv_back_to_user_login);
        
        // Set click listeners
        btnLogin.setOnClickListener(v -> attemptLogin());
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, RecruiterRegisterActivity.class);
            startActivity(intent);
        });
        tvBackToUserLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
        
        // Check if already logged in
        if (authHelper.isLoggedIn() && authHelper.hasValidToken()) {
            startRecruiterMainActivity();
        }
    }
    
    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        // Validation
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }
        
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            return;
        }
        
        // Show progress
        setLoading(true);
        
        // Create login request
        RecruiterLoginRequest loginRequest = new RecruiterLoginRequest(email, password);
        
        // Make API call
        ApiClient.getRecruiterApiService().recruiterLogin(loginRequest).enqueue(new retrofit2.Callback<RecruiterLoginResponse>() {
            @Override
            public void onResponse(Call<RecruiterLoginResponse> call, Response<RecruiterLoginResponse> response) {
                setLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    RecruiterLoginResponse loginResponse = response.body();
                    
                    if (loginResponse.isSuccess() && loginResponse.getData() != null) {
                        RecruiterLoginResponse.RecruiterLoginData data = loginResponse.getData();
                        Recruiter recruiter = data.getRecruiter();
                        String token = data.getToken();
                        
                        // Save recruiter data and token
                        authHelper.saveRecruiterData(recruiter);
                        if (token != null && !token.isEmpty()) {
                            authHelper.saveRecruiterToken(token);
                        }
                        authHelper.setLoggedIn(true);
                        
                        Toast.makeText(RecruiterLoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        startRecruiterMainActivity();
                    } else {
                        String errorMessage = loginResponse.getMessage() != null ? loginResponse.getMessage() : "Login failed";
                        Toast.makeText(RecruiterLoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                } else {
                    String errorMessage = "Login failed. Please check your credentials.";
                    if (response.errorBody() != null) {
                        try {
                            // Parse error response if available
                            errorMessage = response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(RecruiterLoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onFailure(Call<RecruiterLoginResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(RecruiterLoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!isLoading);
        etEmail.setEnabled(!isLoading);
        etPassword.setEnabled(!isLoading);
    }
    
    private void startRecruiterMainActivity() {
        Intent intent = new Intent(this, RecruiterMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
