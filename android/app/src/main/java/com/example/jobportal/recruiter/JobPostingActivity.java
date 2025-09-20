package com.example.jobportal.recruiter;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jobportal.R;
import com.example.jobportal.models.Job;
import com.example.jobportal.network.ApiClient;
import com.example.jobportal.network.ApiResponse;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JobPostingActivity extends AppCompatActivity {

    // UI Components
    private EditText etJobTitle, etLocation, etSalary, etJobDescription, etRequirements, etBenefits, etSkillsRequired, etExpiryDate;
    private AutoCompleteTextView spJobType, spCategory, spExperienceLevel;
    private MaterialButton btnPostJob;
    private ProgressBar progressBar;
    private ImageView ivBack;

    // Image Upload Components
    private FrameLayout imageUploadContainer;
    private LinearLayout imageUploadUI, imageLoadingUI, imageDisplayUI;
    private TextView tvImageName;
    private ImageView ivJobImagePreview;

    // Data
    private Uri selectedImageUri;
    private String selectedImageFileName;
    private File imageFile;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Calendar selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_posting);

        initializeViews();
        setupSpinners();
        setupImageUpload();
        setupDatePicker();
        setClickListeners();
    }

    private void initializeViews() {
        etJobTitle = findViewById(R.id.et_job_title);
        etLocation = findViewById(R.id.et_location);
        etSalary = findViewById(R.id.et_salary);
        etJobDescription = findViewById(R.id.et_job_description);
        etRequirements = findViewById(R.id.et_requirements);
        etBenefits = findViewById(R.id.et_benefits);
        etSkillsRequired = findViewById(R.id.et_skills_required);
        etExpiryDate = findViewById(R.id.et_expiry_date);

        spJobType = findViewById(R.id.sp_job_type);
        spCategory = findViewById(R.id.sp_category);
        spExperienceLevel = findViewById(R.id.sp_experience_level);

        btnPostJob = findViewById(R.id.btn_post_job);
        progressBar = findViewById(R.id.progress_bar);
        ivBack = findViewById(R.id.iv_back);

        // Image upload UI
        imageUploadContainer = findViewById(R.id.image_upload_container);
        imageUploadUI = findViewById(R.id.image_upload_ui);
        imageLoadingUI = findViewById(R.id.image_loading_ui);
        imageDisplayUI = findViewById(R.id.image_display_ui);
        tvImageName = findViewById(R.id.tv_image_name);
        ivJobImagePreview = findViewById(R.id.iv_job_image_preview);
    }

    private void setupSpinners() {
        // Job Type options
        String[] jobTypes = {"Full-time", "Part-time", "Contract", "Freelance", "Internship"};
        ArrayAdapter<String> jobTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, jobTypes);
        spJobType.setAdapter(jobTypeAdapter);

        // Category options
        String[] categories = {
            "Technology", "Healthcare", "Finance", "Education", "Manufacturing", 
            "Retail", "Marketing", "Sales", "Human Resources", "Engineering",
            "Design", "Customer Service", "Operations", "Legal", "Other"
        };
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categories);
        spCategory.setAdapter(categoryAdapter);

        // Experience Level options
        String[] experienceLevels = {"Entry", "Intermediate", "Senior", "Executive"};
        ArrayAdapter<String> experienceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, experienceLevels);
        spExperienceLevel.setAdapter(experienceAdapter);
    }

    private void setupImageUpload() {
        // Initialize the file picker launcher
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        handleImageSelection(selectedImageUri);
                    }
                }
            }
        );
    }

    private void setupDatePicker() {
        selectedDate = Calendar.getInstance();
        selectedDate.add(Calendar.DAY_OF_MONTH, 30); // Default to 30 days from now
        updateDateDisplay();
    }

    private void setClickListeners() {
        ivBack.setOnClickListener(v -> finish());
        btnPostJob.setOnClickListener(v -> attemptPostJob());
        imageUploadContainer.setOnClickListener(v -> openImageFilePicker());
        etExpiryDate.setOnClickListener(v -> showDatePicker());
    }

    private void openImageFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/jpg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Job Image"));
    }

    private void handleImageSelection(Uri uri) {
        try {
            selectedImageFileName = getFileNameFromUri(uri);
            imageFile = createFileFromUri(uri);
            
            // Update UI to show selected image
            updateImageUI(selectedImageFileName, false);
            
            Toast.makeText(this, "Image selected: " + selectedImageFileName, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error selecting image file", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = "job_image";
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
        File tempFile = File.createTempFile("job_image_upload", extension, getCacheDir());
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
            if (mimeType.equals("image/jpeg") || mimeType.equals("image/jpg")) return ".jpg";
            if (mimeType.equals("image/png")) return ".png";
        }
        return ".jpg"; // default
    }

    private void updateImageUI(String fileName, boolean isUploaded) {
        // Hide loading state
        imageLoadingUI.setVisibility(View.GONE);
        
        if (fileName != null && !fileName.isEmpty()) {
            // Show image display UI
            imageUploadUI.setVisibility(View.GONE);
            imageDisplayUI.setVisibility(View.VISIBLE);
            
            tvImageName.setText(fileName);
            
            // Set image preview if available
            if (selectedImageUri != null) {
                ivJobImagePreview.setImageURI(selectedImageUri);
            }
        } else {
            // Show upload UI
            imageUploadUI.setVisibility(View.VISIBLE);
            imageDisplayUI.setVisibility(View.GONE);
        }
    }

    private void showImageLoading() {
        imageUploadUI.setVisibility(View.GONE);
        imageDisplayUI.setVisibility(View.GONE);
        imageLoadingUI.setVisibility(View.VISIBLE);
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                selectedDate.set(Calendar.YEAR, year);
                selectedDate.set(Calendar.MONTH, month);
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateDisplay();
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        
        // Set minimum date to tomorrow
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.DAY_OF_MONTH, 1);
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        
        datePickerDialog.show();
    }

    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        etExpiryDate.setText(sdf.format(selectedDate.getTime()));
    }

    private void attemptPostJob() {
        // Get all input values
        String title = etJobTitle.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String salary = etSalary.getText().toString().trim();
        String description = etJobDescription.getText().toString().trim();
        String requirements = etRequirements.getText().toString().trim();
        String benefits = etBenefits.getText().toString().trim();
        String skillsRequired = etSkillsRequired.getText().toString().trim();
        String jobType = spJobType.getText() != null ? spJobType.getText().toString().trim() : "";
        String category = spCategory.getText() != null ? spCategory.getText().toString().trim() : "";
        String experienceLevel = spExperienceLevel.getText() != null ? spExperienceLevel.getText().toString().trim() : "";
        String expiryDate = etExpiryDate.getText().toString().trim();

        // Validation
        if (!validateInputs(title, location, description, requirements, jobType, category, experienceLevel, expiryDate)) {
            return;
        }

        // Show progress
        setLoading(true);

        // Create job data
        Map<String, Object> jobData = new HashMap<>();
        jobData.put("title", title);
        jobData.put("location", location);
        jobData.put("description", description);
        jobData.put("requirements", requirements);
        jobData.put("job_type", jobType);
        jobData.put("category", category);
        jobData.put("experience_level", experienceLevel);
        jobData.put("expiry_date", expiryDate);
        jobData.put("is_active", true);

        // Add optional fields
        if (!TextUtils.isEmpty(salary)) {
            jobData.put("salary", salary);
        }
        if (!TextUtils.isEmpty(benefits)) {
            jobData.put("benefits", benefits);
        }
        if (!TextUtils.isEmpty(skillsRequired)) {
            jobData.put("skills_required", skillsRequired);
        }

        // Make API call to create job
        ApiClient.getRecruiterApiService().createJob(jobData).enqueue(new Callback<ApiResponse<Job>>() {
            @Override
            public void onResponse(Call<ApiResponse<Job>> call, Response<ApiResponse<Job>> response) {
                setLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Job> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(JobPostingActivity.this, "Job posted successfully! It will be reviewed by admin.", Toast.LENGTH_LONG).show();
                        
                        // Return to previous screen with success result
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("job_posted", true);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } else {
                        String errorMessage = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Job posting failed";
                        Toast.makeText(JobPostingActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                } else {
                    String errorMessage = "Job posting failed. Please try again.";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(JobPostingActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Job>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(JobPostingActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateInputs(String title, String location, String description, String requirements,
                                  String jobType, String category, String experienceLevel, String expiryDate) {
        if (TextUtils.isEmpty(title)) {
            etJobTitle.setError("Job title is required");
            etJobTitle.requestFocus();
            return false;
        }
        
        if (TextUtils.isEmpty(location)) {
            etLocation.setError("Location is required");
            etLocation.requestFocus();
            return false;
        }
        
        if (TextUtils.isEmpty(description)) {
            etJobDescription.setError("Job description is required");
            etJobDescription.requestFocus();
            return false;
        }
        
        if (TextUtils.isEmpty(requirements)) {
            etRequirements.setError("Requirements are required");
            etRequirements.requestFocus();
            return false;
        }
        
        if (TextUtils.isEmpty(jobType)) {
            spJobType.setError("Job type is required");
            spJobType.requestFocus();
            return false;
        }
        
        if (TextUtils.isEmpty(category)) {
            spCategory.setError("Category is required");
            spCategory.requestFocus();
            return false;
        }
        
        if (TextUtils.isEmpty(experienceLevel)) {
            spExperienceLevel.setError("Experience level is required");
            spExperienceLevel.requestFocus();
            return false;
        }
        
        if (TextUtils.isEmpty(expiryDate)) {
            etExpiryDate.setError("Application deadline is required");
            etExpiryDate.requestFocus();
            return false;
        }
        
        return true;
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnPostJob.setEnabled(!isLoading);
        btnPostJob.setText(isLoading ? "Posting..." : "Post Job");
    }
}