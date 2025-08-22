package com.example.jobportal.recruiter;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.jobportal.R;
import com.example.jobportal.auth.RecruiterAuthHelper;
import com.example.jobportal.auth.RecruiterLoginActivity;
import com.example.jobportal.models.Recruiter;
import com.example.jobportal.network.ApiClient;
import com.example.jobportal.models.RecruiterProfileResponse;
import com.example.jobportal.network.ApiResponse;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecruiterProfileActivity extends AppCompatActivity {
    
    private TextInputLayout tilName, tilEmail, tilMobile, tilCompanyName, tilCompanyWebsite;
    private TextInputLayout tilCompanyDescription, tilLocation, tilDesignation;
    private TextInputEditText etName, etEmail, etMobile, etCompanyName, etCompanyWebsite;
    private TextInputEditText etCompanyDescription, etLocation, etDesignation;
    private Button btnUpdateProfile, btnLogout;
    private TextView tvProfileTitle;
    private ProgressBar progressBar;
    
    private RecruiterAuthHelper authHelper;
    private Recruiter currentRecruiter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recruiter_profile);
        
        authHelper = RecruiterAuthHelper.getInstance(this);
        
        // Check if recruiter is logged in
        if (!authHelper.isLoggedIn() || !authHelper.hasValidToken()) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initializeViews();
        setupClickListeners();
        fetchProfileFromServer();
        
        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Profile");
        }
    }
    
    private void initializeViews() {
        tilName = findViewById(R.id.til_name);
        tilEmail = findViewById(R.id.til_email);
        tilMobile = findViewById(R.id.til_mobile);
        tilCompanyName = findViewById(R.id.til_company_name);
        tilCompanyWebsite = findViewById(R.id.til_company_website);
        tilCompanyDescription = findViewById(R.id.til_company_description);
        tilLocation = findViewById(R.id.til_location);
        tilDesignation = findViewById(R.id.til_designation);
        
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etMobile = findViewById(R.id.et_mobile);
        etCompanyName = findViewById(R.id.et_company_name);
        etCompanyWebsite = findViewById(R.id.et_company_website);
        etCompanyDescription = findViewById(R.id.et_company_description);
        etLocation = findViewById(R.id.et_location);
        etDesignation = findViewById(R.id.et_designation);
        
        btnUpdateProfile = findViewById(R.id.btn_update_profile);
        btnLogout = findViewById(R.id.btn_logout);
        tvProfileTitle = findViewById(R.id.tv_profile_title);
        progressBar = findViewById(R.id.progress_bar);
    }
    
    private void fetchProfileFromServer() {
        // Show loading state
        progressBar.setVisibility(View.VISIBLE);
        btnUpdateProfile.setEnabled(false);
        btnUpdateProfile.setText("Loading...");
        
        ApiClient.getRecruiterApiService().getProfile().enqueue(new Callback<RecruiterProfileResponse>() {
            @Override
            public void onResponse(Call<RecruiterProfileResponse> call, Response<RecruiterProfileResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnUpdateProfile.setEnabled(true);
                btnUpdateProfile.setText("Update Profile");
                
                if (response.isSuccessful() && response.body() != null) {
                    RecruiterProfileResponse profileResponse = response.body();
                    if (profileResponse.isSuccess() && profileResponse.getRecruiter() != null) {
                        currentRecruiter = profileResponse.getRecruiter();
                        authHelper.saveRecruiterData(currentRecruiter); // Cache locally
                        loadProfileData();
                    } else {
                        Toast.makeText(RecruiterProfileActivity.this, 
                            "Failed to load profile: " + profileResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        
                        // Fall back to cached data if available
                        currentRecruiter = authHelper.getRecruiterData();
                        if (currentRecruiter != null) {
                            loadProfileData();
                        } else {
                            finish();
                        }
                    }
                } else {
                    Toast.makeText(RecruiterProfileActivity.this, 
                        "Failed to load profile: " + response.message(), Toast.LENGTH_SHORT).show();
                    
                    // Fall back to cached data if available
                    currentRecruiter = authHelper.getRecruiterData();
                    if (currentRecruiter != null) {
                        loadProfileData();
                    } else {
                        finish();
                    }
                }
            }
            
            @Override
            public void onFailure(Call<RecruiterProfileResponse> call, Throwable t) {
                btnUpdateProfile.setEnabled(true);
                btnUpdateProfile.setText("Update Profile");
                
                Toast.makeText(RecruiterProfileActivity.this, 
                    "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                
                // Fall back to cached data if available
                currentRecruiter = authHelper.getRecruiterData();
                if (currentRecruiter != null) {
                    loadProfileData();
                } else {
                    finish();
                }
            }
        });
    }
    
    private void loadProfileData() {
        if (currentRecruiter != null) {
            etName.setText(currentRecruiter.getName());
            etEmail.setText(currentRecruiter.getEmail());
            etMobile.setText(currentRecruiter.getMobile());
            etCompanyName.setText(currentRecruiter.getCompanyName());
            etCompanyWebsite.setText(currentRecruiter.getCompanyWebsite());
            etCompanyDescription.setText(currentRecruiter.getCompanyDescription());
            etLocation.setText(currentRecruiter.getLocation());
            etDesignation.setText(currentRecruiter.getDesignation());
            
            tvProfileTitle.setText("Welcome, " + currentRecruiter.getName());
        }
    }
    
    private void setupClickListeners() {
        btnUpdateProfile.setOnClickListener(v -> {
            if (validateInputs()) {
                updateProfile();
            }
        });
        
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
    }
    
    private boolean validateInputs() {
        boolean isValid = true;
        
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String companyName = etCompanyName.getText().toString().trim();
        
        if (TextUtils.isEmpty(name)) {
            tilName.setError("Name is required");
            isValid = false;
        } else {
            tilName.setError(null);
        }
        
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Please enter a valid email");
            isValid = false;
        } else {
            tilEmail.setError(null);
        }
        
        if (TextUtils.isEmpty(mobile)) {
            tilMobile.setError("Mobile number is required");
            isValid = false;
        } else {
            tilMobile.setError(null);
        }
        
        if (TextUtils.isEmpty(companyName)) {
            tilCompanyName.setError("Company name is required");
            isValid = false;
        } else {
            tilCompanyName.setError(null);
        }
        
        return isValid;
    }
    
    private void updateProfile() {
        // Show loading state
        btnUpdateProfile.setEnabled(false);
        btnUpdateProfile.setText("Updating...");
        
        // Get updated values
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String companyName = etCompanyName.getText().toString().trim();
        String companyWebsite = etCompanyWebsite.getText().toString().trim();
        String companyDescription = etCompanyDescription.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String designation = etDesignation.getText().toString().trim();
        
        // Create updated recruiter object
        Recruiter updatedRecruiter = new Recruiter();
        updatedRecruiter.setName(name);
        updatedRecruiter.setEmail(email);
        updatedRecruiter.setMobile(mobile);
        updatedRecruiter.setCompanyName(companyName);
        updatedRecruiter.setCompanyWebsite(companyWebsite);
        updatedRecruiter.setCompanyDescription(companyDescription);
        updatedRecruiter.setLocation(location);
        updatedRecruiter.setDesignation(designation);
        
        // Make API call to update profile
        ApiClient.getRecruiterApiService().updateProfile(updatedRecruiter).enqueue(new Callback<RecruiterProfileResponse>() {
            @Override
            public void onResponse(Call<RecruiterProfileResponse> call, Response<RecruiterProfileResponse> response) {
                btnUpdateProfile.setEnabled(true);
                btnUpdateProfile.setText("Update Profile");
                
                if (response.isSuccessful() && response.body() != null) {
                    RecruiterProfileResponse profileResponse = response.body();
                    if (profileResponse.isSuccess() && profileResponse.getRecruiter() != null) {
                        currentRecruiter = profileResponse.getRecruiter();
                        authHelper.saveRecruiterData(currentRecruiter); // Update local cache
                        
                        Toast.makeText(RecruiterProfileActivity.this, 
                            "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        
                        // Update the title with new name
                        tvProfileTitle.setText("Welcome, " + currentRecruiter.getName());
                    } else {
                        Toast.makeText(RecruiterProfileActivity.this, 
                            "Failed to update profile: " + profileResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RecruiterProfileActivity.this, 
                        "Failed to update profile: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<RecruiterProfileResponse> call, Throwable t) {
                btnUpdateProfile.setEnabled(true);
                btnUpdateProfile.setText("Update Profile");
                
                Toast.makeText(RecruiterProfileActivity.this, 
                    "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes", (dialog, which) -> logout())
            .setNegativeButton("No", null)
            .show();
    }
    
    private void logout() {
        // Show loading state
        btnLogout.setEnabled(false);
        btnLogout.setText("Logging out...");
        
        // Make API call to logout on server
        ApiClient.getRecruiterApiService().logout().enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                // Always proceed with local logout regardless of API response
                performLocalLogout();
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                // Still proceed with local logout even if API call fails
                performLocalLogout();
            }
        });
    }
    
    private void performLocalLogout() {
        authHelper.logout();
        Intent intent = new Intent(this, RecruiterLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
