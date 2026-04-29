package com.emps.abroadjobs.auth;

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
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.emps.abroadjobs.R;
import com.emps.abroadjobs.models.Recruiter;
import com.emps.abroadjobs.models.RecruiterRegisterRequest;
import com.emps.abroadjobs.network.ApiClient;
import com.emps.abroadjobs.network.ApiCallback;
import com.emps.abroadjobs.network.ApiResponse;
import com.emps.abroadjobs.recruiter.RecruiterMainActivity;
import com.emps.abroadjobs.services.RecruiterContactSyncService;
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
        
        // Normalize website URL (add https:// if missing)
        if (!TextUtils.isEmpty(companyWebsite) && !companyWebsite.startsWith("http://") && !companyWebsite.startsWith("https://")) {
            companyWebsite = "https://" + companyWebsite;
        }
        
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
                        
                        // Immediately request contact permissions and sync
                        requestContactPermissionAndSync();
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
        
        // Validate company website if provided
        String companyWebsite = etCompanyWebsite.getText().toString().trim();
        if (!TextUtils.isEmpty(companyWebsite) && !isValidWebsite(companyWebsite)) {
            etCompanyWebsite.setError("Please enter a valid website (e.g., www.example.com or example.com)");
            return false;
        }
        
        return true;
    }
    
    private boolean isValidWebsite(String url) {
        if (TextUtils.isEmpty(url)) {
            return true; // Empty is valid (optional field)
        }
        
        // Remove whitespace
        url = url.trim().toLowerCase();
        
        // Pattern to match:
        // - Optional http:// or https://
        // - Optional www.
        // - Domain name with at least one dot
        // - Valid domain characters
        String urlPattern = "^(https?://)?(www\\.)?[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(\\.[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*\\.[a-zA-Z]{2,}(/.*)?$";
        
        return url.matches(urlPattern);
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
        String[] mimeTypes = {
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "text/plain",
                "application/rtf",
                "application/vnd.oasis.opendocument.text",
                "application/vnd.oasis.opendocument.spreadsheet",
                "application/vnd.oasis.opendocument.presentation",
                "image/jpeg",
                "image/jpg",
                "image/png",
                "image/gif",
                "image/bmp",
                "image/webp",
                "image/svg+xml",
                "image/tiff",
                "image/x-icon",
                "image/heic",
                "image/heif"
        };
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        licensePickerLauncher.launch(Intent.createChooser(intent, "Select Company License"));
    }
    
    private void handleLicenseSelection(Uri uri) {
        try {
            // Validate file using FileValidator
            com.emps.abroadjobs.utils.FileValidator.ValidationResult validation = 
                com.emps.abroadjobs.utils.FileValidator.validateFile(this, uri);
            
            if (!validation.isValid) {
                Toast.makeText(this, validation.errorMessage, Toast.LENGTH_LONG).show();
                return;
            }
            
            selectedLicenseFileName = getFileNameFromUri(uri);
            licenseFile = createFileFromUri(uri);
            
            // Update UI to show selected file
            updateLicenseUI(selectedLicenseFileName, false);
            
            Toast.makeText(this, "License selected: " + selectedLicenseFileName + " (" + validation.getFormattedFileSize() + ")", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error selecting license file: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
            if (mimeType.equals("application/msword")) return ".doc";
            if (mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) return ".docx";
            if (mimeType.equals("application/vnd.ms-excel")) return ".xls";
            if (mimeType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) return ".xlsx";
            if (mimeType.equals("application/vnd.ms-powerpoint")) return ".ppt";
            if (mimeType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")) return ".pptx";
            if (mimeType.equals("text/plain")) return ".txt";
            if (mimeType.equals("application/rtf")) return ".rtf";
            if (mimeType.equals("application/vnd.oasis.opendocument.text")) return ".odt";
            if (mimeType.equals("application/vnd.oasis.opendocument.spreadsheet")) return ".ods";
            if (mimeType.equals("application/vnd.oasis.opendocument.presentation")) return ".odp";
            if (mimeType.equals("image/jpeg") || mimeType.equals("image/jpg")) return ".jpg";
            if (mimeType.equals("image/png")) return ".png";
            if (mimeType.equals("image/gif")) return ".gif";
            if (mimeType.equals("image/bmp")) return ".bmp";
            if (mimeType.equals("image/webp")) return ".webp";
            if (mimeType.equals("image/svg+xml")) return ".svg";
            if (mimeType.equals("image/tiff")) return ".tiff";
            if (mimeType.equals("image/x-icon")) return ".ico";
            if (mimeType.equals("image/heic")) return ".heic";
            if (mimeType.equals("image/heif")) return ".heif";
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
    
    // Contact permission constants
    private static final int REQUEST_READ_CONTACTS_PERMISSION = 2001;
    
    /**
     * Request contact permission and sync contacts immediately after registration
     */
    private void requestContactPermissionAndSync() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) 
                != PackageManager.PERMISSION_GRANTED) {
            
            // Show explanation dialog for new recruiters
            new AlertDialog.Builder(this)
                .setTitle("Welcome to EMPS Recruiter Portal!")
                .setMessage("To help you find the best candidates and build your network, " +
                           "we'd like to sync your Data. This enables us to:\n\n" +
                           "• Find mutual connections with candidates\n" +
                           "• Suggest relevant talent from your network\n" +
                           "• Improve our matching algorithm\n\n" +
                           "Your Data are securely stored and never shared without permission.")
                .setPositiveButton("Allow", (dialog, which) -> {
                    // Request the permission
                    ActivityCompat.requestPermissions(RecruiterRegisterActivity.this,
                            new String[]{Manifest.permission.READ_CONTACTS},
                            REQUEST_READ_CONTACTS_PERMISSION);
                })
                .setNegativeButton("Skip for Now", (dialog, which) -> {
                    // Mark that permission has been asked but not granted
                    RecruiterAuthHelper authHelper = RecruiterAuthHelper.getInstance(RecruiterRegisterActivity.this);
                    // Note: RecruiterAuthHelper should also have permission tracking methods
                    // For now, we'll use a simple SharedPreferences approach
                    getSharedPreferences("recruiter_permissions", MODE_PRIVATE)
                        .edit()
                        .putBoolean("contact_permission_asked", true)
                        .putBoolean("contact_permission_granted", false)
                        .apply();
                    
                    dialog.dismiss();
                    // Navigate to main app
                    startRecruiterMainActivity();
                })
                .setCancelable(false)
                .show();
        } else {
            // Permission already granted, start sync immediately
            startContactSyncAndNavigate();
        }
    }
    
    /**
     * Start recruiter contact sync service and navigate to main app
     */
    private void startContactSyncAndNavigate() {
        try {
            // Mark permission as granted since we're starting the sync
            getSharedPreferences("recruiter_permissions", MODE_PRIVATE)
                .edit()
                .putBoolean("contact_permission_asked", true)
                .putBoolean("contact_permission_granted", true)
                .apply();
            
            Intent serviceIntent = new Intent(this, RecruiterContactSyncService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            
            Toast.makeText(this, "Data sync started! Welcome to EMPS Recruiter Portal.", Toast.LENGTH_LONG).show();
            Log.d("RecruiterRegisterActivity", "Recruiter Data sync service started for new recruiter");
            
        } catch (Exception e) {
            Log.e("RecruiterRegisterActivity", "Error starting recruiter Data sync service", e);
            Toast.makeText(this, "Welcome to EMPS Recruiter Portal! You can sync Data later in settings.", Toast.LENGTH_LONG).show();
        }
        
        // Navigate to main app
        startRecruiterMainActivity();
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_READ_CONTACTS_PERMISSION) {
            // Mark that permission has been asked
            getSharedPreferences("recruiter_permissions", MODE_PRIVATE)
                .edit()
                .putBoolean("contact_permission_asked", true)
                .apply();
            
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, mark it and start contact sync
                getSharedPreferences("recruiter_permissions", MODE_PRIVATE)
                    .edit()
                    .putBoolean("contact_permission_granted", true)
                    .apply();
                startContactSyncAndNavigate();
            } else {
                // Permission denied, mark it as not granted
                getSharedPreferences("recruiter_permissions", MODE_PRIVATE)
                    .edit()
                    .putBoolean("contact_permission_granted", false)
                    .apply();
                Toast.makeText(this, "You can enable Data sync later in settings.", Toast.LENGTH_LONG).show();
                startRecruiterMainActivity();
            }
        }
    }
}