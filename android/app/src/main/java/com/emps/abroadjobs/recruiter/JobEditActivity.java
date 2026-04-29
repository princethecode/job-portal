package com.emps.abroadjobs.recruiter;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
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
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.emps.abroadjobs.R;
import com.emps.abroadjobs.models.Job;
import com.emps.abroadjobs.network.ApiClient;
import com.emps.abroadjobs.network.ApiResponse;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JobEditActivity extends AppCompatActivity {

    public static final String EXTRA_JOB_ID = "job_id";
    public static final String EXTRA_JOB_DATA = "job_data";
    
    private int jobId;
    private Job currentJob;

    // UI Components
    private EditText etJobTitle, etLocation, etSalary, etJobDescription, etRequirements, etBenefits, etSkillsRequired, etExpiryDate;
    private AutoCompleteTextView spJobType, spCategory, spExperienceLevel;
    private MaterialButton btnUpdateJob, btnDeleteJob;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    // Image Upload Components
    private FrameLayout imageUploadContainer;
    private LinearLayout imageUploadUI, imageLoadingUI, imageDisplayUI;
    private TextView tvImageName;
    private ImageView ivJobImagePreview;

    // Data
    private Uri selectedImageUri;
    private String selectedImageFileName;
    private File imageFile;
    private ActivityResultLauncher<PickVisualMediaRequest> imagePickerLauncher;
    private Calendar selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_edit);

        // Get data from intent
        jobId = getIntent().getIntExtra(EXTRA_JOB_ID, -1);
        currentJob = (Job) getIntent().getSerializableExtra(EXTRA_JOB_DATA);
        
        if (jobId == -1 || currentJob == null) {
            Toast.makeText(this, "Invalid job data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupToolbar();
        setupSpinners();
        setupImageUpload();
        setupDatePicker();
        setClickListeners();
        populateFields();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        
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

        btnUpdateJob = findViewById(R.id.btn_update_job);
        btnDeleteJob = findViewById(R.id.btn_delete_job);
        progressBar = findViewById(R.id.progress_bar);

        // Image upload UI
        imageUploadContainer = findViewById(R.id.image_upload_container);
        imageUploadUI = findViewById(R.id.image_upload_ui);
        imageLoadingUI = findViewById(R.id.image_loading_ui);
        imageDisplayUI = findViewById(R.id.image_display_ui);
        tvImageName = findViewById(R.id.tv_image_name);
        ivJobImagePreview = findViewById(R.id.iv_job_image_preview);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Edit Job");
        }
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
        // Initialize the Photo Picker launcher
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.PickVisualMedia(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    handleImageSelection(uri);
                }
            }
        );
    }

    private void setupDatePicker() {
        selectedDate = Calendar.getInstance();
    }

    private void setClickListeners() {
        btnUpdateJob.setOnClickListener(v -> attemptUpdateJob());
        btnDeleteJob.setOnClickListener(v -> showDeleteConfirmation());
        imageUploadContainer.setOnClickListener(v -> openImageFilePicker());
        etExpiryDate.setOnClickListener(v -> showDatePicker());
    }

    private void populateFields() {
        // Populate all fields with current job data
        etJobTitle.setText(currentJob.getTitle());
        etLocation.setText(currentJob.getLocation());
        etJobDescription.setText(currentJob.getDescription());
        etRequirements.setText(currentJob.getRequirements());
        etBenefits.setText(currentJob.getBenefits());
        etSkillsRequired.setText(currentJob.getSkillsRequired());
        
        // Handle salary
        if (currentJob.getSalary() != null && !currentJob.getSalary().isEmpty() && 
            !currentJob.getSalary().equalsIgnoreCase("null")) {
            etSalary.setText(currentJob.getSalary());
        }
        
        // Set spinners
        spJobType.setText(currentJob.getJobType(), false);
        spCategory.setText(currentJob.getCategory() != null ? currentJob.getCategory() : "Other", false);
        spExperienceLevel.setText(currentJob.getExperienceRequired() != null ? 
                                 currentJob.getExperienceRequired() : "Entry", false);
        
        // Set expiry date
        if (currentJob.getExpiryDate() != null) {
            etExpiryDate.setText(currentJob.getExpiryDate());
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                selectedDate.setTime(sdf.parse(currentJob.getExpiryDate()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        
        // Handle existing image
        if (currentJob.getImage() != null && !currentJob.getImage().isEmpty()) {
            updateImageUI(currentJob.getImage(), true);
        }
    }

    private void openImageFilePicker() {
        // Launch the Photo Picker for images only
        imagePickerLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
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

    private void updateImageUI(String fileName, boolean isExisting) {
        // Hide loading state
        imageLoadingUI.setVisibility(View.GONE);
        
        if (fileName != null && !fileName.isEmpty()) {
            // Show image display UI
            imageUploadUI.setVisibility(View.GONE);
            imageDisplayUI.setVisibility(View.VISIBLE);
            
            tvImageName.setText(isExisting ? "Current image" : fileName);
            
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

    private void attemptUpdateJob() {
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

        // Check if image is selected for multipart upload
        if (imageFile != null && selectedImageUri != null) {
            updateJobWithImage(title, location, salary, description, requirements, benefits, 
                             skillsRequired, jobType, category, experienceLevel, expiryDate);
        } else {
            updateJobWithoutImage(title, location, salary, description, requirements, benefits, 
                                skillsRequired, jobType, category, experienceLevel, expiryDate);
        }
    }

    private void updateJobWithImage(String title, String location, String salary, String description,
                                  String requirements, String benefits, String skillsRequired,
                                  String jobType, String category, String experienceLevel, String expiryDate) {
        
        try {
            // Create RequestBody instances for text fields
            okhttp3.RequestBody titleBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), title);
            okhttp3.RequestBody locationBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), location);
            okhttp3.RequestBody descriptionBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), description);
            okhttp3.RequestBody requirementsBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), requirements);
            okhttp3.RequestBody jobTypeBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), jobType);
            okhttp3.RequestBody categoryBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), category);
            okhttp3.RequestBody experienceLevelBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), experienceLevel);
            okhttp3.RequestBody expiryDateBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), expiryDate);
            okhttp3.RequestBody salaryBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), salary != null ? salary : "");
            okhttp3.RequestBody benefitsBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), benefits != null ? benefits : "");
            okhttp3.RequestBody skillsRequiredBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), skillsRequired != null ? skillsRequired : "");
            okhttp3.RequestBody methodBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), "PUT");

            // Create image part
            okhttp3.RequestBody imageRequestBody = okhttp3.RequestBody.create(
                okhttp3.MediaType.parse("image/*"), imageFile);
            okhttp3.MultipartBody.Part imagePart = okhttp3.MultipartBody.Part.createFormData(
                "image", imageFile.getName(), imageRequestBody);

            // Make API call
            ApiClient.getRecruiterApiService().updateJobWithImage(
                jobId, titleBody, locationBody, descriptionBody, requirementsBody,
                jobTypeBody, categoryBody, experienceLevelBody, expiryDateBody,
                salaryBody, benefitsBody, skillsRequiredBody, imagePart, methodBody
            ).enqueue(new Callback<ApiResponse<Job>>() {
                @Override
                public void onResponse(Call<ApiResponse<Job>> call, Response<ApiResponse<Job>> response) {
                    handleUpdateResponse(response);
                }

                @Override
                public void onFailure(Call<ApiResponse<Job>> call, Throwable t) {
                    handleUpdateFailure(t);
                }
            });

        } catch (Exception e) {
            setLoading(false);
            Toast.makeText(this, "Error preparing image upload: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateJobWithoutImage(String title, String location, String salary, String description,
                                     String requirements, String benefits, String skillsRequired,
                                     String jobType, String category, String experienceLevel, String expiryDate) {
        
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

        // Make API call to update job
        ApiClient.getRecruiterApiService().updateJob(jobId, jobData).enqueue(new Callback<ApiResponse<Job>>() {
            @Override
            public void onResponse(Call<ApiResponse<Job>> call, Response<ApiResponse<Job>> response) {
                handleUpdateResponse(response);
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Job>> call, Throwable t) {
                handleUpdateFailure(t);
            }
        });
    }

    private void handleUpdateResponse(Response<ApiResponse<Job>> response) {
        setLoading(false);
        
        if (response.isSuccessful() && response.body() != null) {
            ApiResponse<Job> apiResponse = response.body();
            
            if (apiResponse.isSuccess()) {
                Toast.makeText(JobEditActivity.this, "Job updated successfully!", Toast.LENGTH_LONG).show();
                
                // Return to previous screen with success result
                Intent resultIntent = new Intent();
                resultIntent.putExtra("job_updated", true);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                String errorMessage = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Job update failed";
                Toast.makeText(JobEditActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        } else {
            String errorMessage = "Job update failed. Please try again.";
            if (response.errorBody() != null) {
                try {
                    errorMessage = response.errorBody().string();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Toast.makeText(JobEditActivity.this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    private void handleUpdateFailure(Throwable t) {
        setLoading(false);
        Toast.makeText(JobEditActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
    }

    private void showDeleteConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Job")
            .setMessage("Are you sure you want to delete this job? This action cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> deleteJob())
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteJob() {
        setLoading(true);
        
        // Note: You'll need to add a delete endpoint to the API service
        // For now, we'll show a message
        setLoading(false);
        Toast.makeText(this, "Delete functionality will be implemented with backend support", Toast.LENGTH_LONG).show();
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
        btnUpdateJob.setEnabled(!isLoading);
        btnDeleteJob.setEnabled(!isLoading);
        btnUpdateJob.setText(isLoading ? "Updating..." : "Update Job");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}