package com.example.jobportal.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.jobportal.R;
import com.example.jobportal.models.Recruiter;
import com.example.jobportal.models.RecruiterRegisterRequest;
import com.example.jobportal.network.ApiClient;
import com.example.jobportal.network.ApiCallback;
import com.example.jobportal.network.ApiResponse;
import com.example.jobportal.recruiter.RecruiterMainActivity;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class RecruiterRegisterActivity extends AppCompatActivity {
    
    private EditText etName, etEmail, etMobile, etPassword, etConfirmPassword;
    private EditText etCompanyName, etCompanyWebsite, etCompanyDescription, etLocation, etDesignation;
    private AutoCompleteTextView spCompanySize, spIndustry;
    private Button btnRegister;
    private ProgressBar progressBar;
    private TextView tvLogin;
    
    // Company License Upload UI
    private FrameLayout licenseUploadContainer;
    private LinearLayout licenseUploadUI, licenseLoadingUI, licenseDisplayUI;
    private TextView tvLicenseName, tvLicenseStatus;
    private ImageView ivLicenseIcon;
    
    private RecruiterAuthHelper authHelper;
    private Uri selectedLicenseUri;
    private String selectedLicenseFileName;
    private File licenseFile;
    private ActivityResultLauncher<Intent> licensePickerLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recruiter_register);
        
        authHelper = RecruiterAuthHelper.getInstance(this);
        
        initializeViews();
        setupSpinners();
        setupLicenseUpload();
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
        
        // License upload UI
        licenseUploadContainer = findViewById(R.id.license_upload_container);
        licenseUploadUI = findViewById(R.id.license_upload_ui);
        licenseLoadingUI = findViewById(R.id.license_loading_ui);
        licenseDisplayUI = findViewById(R.id.license_display_ui);
        tvLicenseName = findViewById(R.id.tv_license_name);
        tvLicenseStatus = findViewById(R.id.tv_license_status);
        ivLicenseIcon = findViewById(R.id.iv_license_icon);
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
    
    private void setupLicenseUpload() {
        // Initialize the file picker launcher
        licensePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedLicenseUri = result.getData().getData();
                    if (selectedLicenseUri != null) {
                        handleLicenseSelection(selectedLicenseUri);
                    }
                }
            }
        );
    }
    
    private void setClickListeners() {
        btnRegister.setOnClickListener(v -> attemptRegister());
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, RecruiterLoginActivity.class);
            startActivity(intent);
            finish();
        });
        
        // Set up license upload click listener
        licenseUploadContainer.setOnClickListener(v -> openLicenseFilePicker());
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
                        
                        // Upload license if selected
                        if (licenseFile != null) {
                            uploadLicenseToServer();
                        }
                        
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
    
    private void openLicenseFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {"application/pdf", "image/jpeg", "image/jpg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        licensePickerLauncher.launch(Intent.createChooser(intent, "Select Company License"));
    }
    
    private void handleLicenseSelection(Uri uri) {
        try {
            selectedLicenseFileName = getFileNameFromUri(uri);
            licenseFile = createFileFromUri(uri);
            
            // Update UI to show selected file
            updateLicenseUI(selectedLicenseFileName, false);
            
            Toast.makeText(this, "License selected: " + selectedLicenseFileName, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error selecting license file", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    
    private String getFileNameFromUri(Uri uri) {
        String fileName = "company_license";
        try {
            android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    fileName = cursor.getString(nameIndex);
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }
    
    private File createFileFromUri(Uri uri) throws IOException {
        String extension = getFileExtension(uri);
        File tempFile = File.createTempFile("license_upload", extension, getCacheDir());
        tempFile.deleteOnExit();
        
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            
            if (inputStream != null) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        }
        
        return tempFile;
    }
    
    private String getFileExtension(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        if (mimeType != null) {
            if (mimeType.equals("application/pdf")) return ".pdf";
            if (mimeType.equals("image/jpeg") || mimeType.equals("image/jpg")) return ".jpg";
            if (mimeType.equals("image/png")) return ".png";
        }
        return ".pdf"; // default
    }
    
    private void updateLicenseUI(String fileName, boolean isNewUpload) {
        // Hide loading state
        licenseLoadingUI.setVisibility(View.GONE);
        
        if (fileName != null && !fileName.isEmpty()) {
            // Show file display UI
            licenseUploadUI.setVisibility(View.GONE);
            licenseDisplayUI.setVisibility(View.VISIBLE);
            
            tvLicenseName.setText(fileName);
            
            // Set appropriate icon based on file type
            if (fileName.toLowerCase().endsWith(".pdf")) {
                ivLicenseIcon.setImageResource(R.drawable.ic_pdf);
                ivLicenseIcon.setColorFilter(getResources().getColor(R.color.red_error));
            } else {
                ivLicenseIcon.setImageResource(R.drawable.ic_image);
                ivLicenseIcon.setColorFilter(getResources().getColor(R.color.primary));
            }
            
            if (isNewUpload) {
                tvLicenseStatus.setText("✓ Successfully uploaded");
                tvLicenseStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvLicenseStatus.setText("✓ Ready to upload");
                tvLicenseStatus.setTextColor(getResources().getColor(R.color.primary));
            }
        } else {
            // Show upload UI
            licenseUploadUI.setVisibility(View.VISIBLE);
            licenseDisplayUI.setVisibility(View.GONE);
        }
    }
    
    private void showLicenseLoading() {
        licenseUploadUI.setVisibility(View.GONE);
        licenseDisplayUI.setVisibility(View.GONE);
        licenseLoadingUI.setVisibility(View.VISIBLE);
    }
    
    private void uploadLicenseToServer() {
        if (licenseFile == null) {
            return; // No license to upload
        }
        
        showLicenseLoading();
        
        // Create request body for the file
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), licenseFile);
        MultipartBody.Part licensePart = MultipartBody.Part.createFormData("license", licenseFile.getName(), requestFile);
        
        // Upload license using the API
        ApiClient.getRecruiterApiService().uploadCompanyLicense(licensePart).enqueue(new retrofit2.Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // License uploaded successfully
                    updateLicenseUI(selectedLicenseFileName, true);
                    Toast.makeText(RecruiterRegisterActivity.this, "License uploaded successfully", Toast.LENGTH_SHORT).show();
                } else {
                    // Upload failed
                    updateLicenseUI(selectedLicenseFileName, false);
                    Toast.makeText(RecruiterRegisterActivity.this, "Failed to upload license", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                updateLicenseUI(selectedLicenseFileName, false);
                Toast.makeText(RecruiterRegisterActivity.this, "Network error uploading license", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
