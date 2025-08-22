package com.example.jobportal.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.jobportal.R;
import com.example.jobportal.models.Recruiter;
import com.example.jobportal.models.RecruiterRegisterRequest;
import com.example.jobportal.network.ApiClient;
import com.example.jobportal.network.ApiCallback;
import com.example.jobportal.network.ApiResponse;
import com.example.jobportal.recruiter.RecruiterMainActivity;
import retrofit2.Call;
import retrofit2.Response;

public class RecruiterRegisterActivity extends AppCompatActivity {
    
    private EditText etName, etEmail, etMobile, etPassword, etConfirmPassword;
    private EditText etCompanyName, etCompanyWebsite, etCompanyDescription, etLocation, etDesignation;
    private AutoCompleteTextView spCompanySize, spIndustry;
    private Button btnRegister;
    private ProgressBar progressBar;
    private TextView tvLogin;
    
    private RecruiterAuthHelper authHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recruiter_register);
        
        authHelper = RecruiterAuthHelper.getInstance(this);
        
        initializeViews();
        setupSpinners();
        setClickListeners();
    }
    
    private void initializeViews() {
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etMobile = findViewById(R.id.et_mobile);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        etCompanyName = findViewById(R.id.et_company_name);
        etCompanyWebsite = findViewById(R.id.et_company_website);
        etCompanyDescription = findViewById(R.id.et_company_description);
        etLocation = findViewById(R.id.et_location);
        etDesignation = findViewById(R.id.et_designation);
        spCompanySize = findViewById(R.id.sp_company_size);
        spIndustry = findViewById(R.id.sp_industry);
        btnRegister = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progress_bar);
        tvLogin = findViewById(R.id.tv_login);
    }
    
    private void setupSpinners() {
        // Company size options
        String[] companySizes = {"1-10", "11-50", "51-200", "201-500", "500+"};
        ArrayAdapter<String> companySizeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, companySizes);
        spCompanySize.setAdapter(companySizeAdapter);
        
        // Industry options
        String[] industries = {"Technology", "Healthcare", "Finance", "Education", "Manufacturing", "Retail", "Other"};
        ArrayAdapter<String> industryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, industries);
        spIndustry.setAdapter(industryAdapter);
    }
    
    private void setClickListeners() {
        btnRegister.setOnClickListener(v -> attemptRegister());
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, RecruiterLoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
    
    private void attemptRegister() {
        // Get all input values
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String companyName = etCompanyName.getText().toString().trim();
        String companyWebsite = etCompanyWebsite.getText().toString().trim();
        String companyDescription = etCompanyDescription.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String designation = etDesignation.getText().toString().trim();
        String companySize = spCompanySize.getText() != null ? spCompanySize.getText().toString().trim() : "";
        String industry = spIndustry.getText() != null ? spIndustry.getText().toString().trim() : "";
        
        // Validation
        if (!validateInputs(name, email, mobile, password, confirmPassword, companyName)) {
            return;
        }
        
        // Show progress
        setLoading(true);
        
        // Create registration request
        RecruiterRegisterRequest registerRequest = new RecruiterRegisterRequest(
            name, email, mobile, password, confirmPassword, companyName, companyWebsite,
            companyDescription, companySize, industry, location, designation
        );
        
        // Make API call
        ApiClient.getRecruiterApiService().recruiterRegister(registerRequest).enqueue(new retrofit2.Callback<ApiResponse<Recruiter>>() {
            @Override
            public void onResponse(Call<ApiResponse<Recruiter>> call, Response<ApiResponse<Recruiter>> response) {
                setLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Recruiter> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Recruiter recruiter = apiResponse.getData();
                        
                        // Save recruiter data and token
                        authHelper.saveRecruiterData(recruiter);
                        // Note: Token should come from login response, not registration
                        authHelper.setLoggedIn(true);
                        
                        Toast.makeText(RecruiterRegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        startRecruiterMainActivity();
                    } else {
                        String errorMessage = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Registration failed";
                        Toast.makeText(RecruiterRegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                } else {
                    String errorMessage = "Registration failed. Please try again.";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(RecruiterRegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Recruiter>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(RecruiterRegisterActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private boolean validateInputs(String name, String email, String mobile, String password, 
                                  String confirmPassword, String companyName) {
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return false;
        }
        
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return false;
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            return false;
        }
        
        if (TextUtils.isEmpty(mobile)) {
            etMobile.setError("Mobile number is required");
            return false;
        }
        
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return false;
        }
        
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return false;
        }
        
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return false;
        }
        
        if (TextUtils.isEmpty(companyName)) {
            etCompanyName.setError("Company name is required");
            return false;
        }
        
        return true;
    }
    
    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!isLoading);
    }
    
    private void startRecruiterMainActivity() {
        Intent intent = new Intent(this, RecruiterMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
