package com.emps.abroadjobs.recruiter;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.emps.abroadjobs.R;
import com.emps.abroadjobs.models.Application;
import com.emps.abroadjobs.models.User;
import com.emps.abroadjobs.network.ApiClient;
import com.emps.abroadjobs.network.ApiResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecruiterApplicationDetailsActivity extends AppCompatActivity {
    
    public static final String EXTRA_APPLICATION_ID = "application_id";
    
    private int applicationId;
    private Application currentApplication;
    
    // UI Components
    private Toolbar toolbar;
    private View progressBar, contentLayout;
    private ImageView ivProfilePhoto;
    private TextView tvApplicantName, tvApplicantEmail, tvApplicantMobile;
    private TextView tvApplicantId, tvCurrentCompany, tvDepartment;
    private TextView tvCurrentSalary, tvExpectedSalary, tvJoiningPeriod;
    private TextView tvJobTitle, tvCompanyName, tvAppliedDate;
    private TextView tvExperience, tvSkills, tvLocation, tvAboutMe;
    private TextView tvCoverLetter;
    private Chip chipStatus;
    private MaterialButton btnViewResume, btnUpdateStatus, btnContactCandidate;
    private View cardEmployment, cardProfessional, cardCoverLetter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recruiter_application_details);
        
        // Get application ID from intent
        applicationId = getIntent().getIntExtra(EXTRA_APPLICATION_ID, -1);
        
        if (applicationId == -1) {
            Toast.makeText(this, "Invalid application ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initializeViews();
        setupToolbar();
        setupClickListeners();
        loadApplicationDetails();
    }
    
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progress_bar);
        contentLayout = findViewById(R.id.content_layout);
        
        ivProfilePhoto = findViewById(R.id.iv_profile_photo);
        tvApplicantName = findViewById(R.id.tv_applicant_name);
        tvApplicantEmail = findViewById(R.id.tv_applicant_email);
        tvApplicantMobile = findViewById(R.id.tv_applicant_mobile);
        tvApplicantId = findViewById(R.id.tv_applicant_id);
        
        tvCurrentCompany = findViewById(R.id.tv_current_company);
        tvDepartment = findViewById(R.id.tv_department);
        tvCurrentSalary = findViewById(R.id.tv_current_salary);
        tvExpectedSalary = findViewById(R.id.tv_expected_salary);
        tvJoiningPeriod = findViewById(R.id.tv_joining_period);
        
        tvJobTitle = findViewById(R.id.tv_job_title);
        tvCompanyName = findViewById(R.id.tv_company_name);
        tvAppliedDate = findViewById(R.id.tv_applied_date);
        
        tvExperience = findViewById(R.id.tv_experience);
        tvSkills = findViewById(R.id.tv_skills);
        tvLocation = findViewById(R.id.tv_location);
        tvAboutMe = findViewById(R.id.tv_about_me);
        tvCoverLetter = findViewById(R.id.tv_cover_letter);
        
        chipStatus = findViewById(R.id.chip_status);
        btnViewResume = findViewById(R.id.btn_view_resume);
        btnUpdateStatus = findViewById(R.id.btn_update_status);
        btnContactCandidate = findViewById(R.id.btn_contact_candidate);
        
        cardEmployment = findViewById(R.id.card_employment);
        cardProfessional = findViewById(R.id.card_professional);
        cardCoverLetter = findViewById(R.id.card_cover_letter);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Application Details");
        }
    }
    
    private void setupClickListeners() {
        btnViewResume.setOnClickListener(v -> viewResume());
        btnUpdateStatus.setOnClickListener(v -> showUpdateStatusDialog());
        btnContactCandidate.setOnClickListener(v -> contactCandidate());
    }
    
    private void loadApplicationDetails() {
        progressBar.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);
        
        ApiClient.getRecruiterApiService().getApplicationDetails(applicationId)
            .enqueue(new Callback<ApiResponse<Application>>() {
                @Override
                public void onResponse(Call<ApiResponse<Application>> call, Response<ApiResponse<Application>> response) {
                    progressBar.setVisibility(View.GONE);
                    
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        currentApplication = response.body().getData();
                        if (currentApplication != null) {
                            displayApplicationDetails(currentApplication);
                            contentLayout.setVisibility(View.VISIBLE);
                        } else {
                            showError("Application data not available");
                        }
                    } else {
                        showError("Failed to load application details");
                    }
                }
                
                @Override
                public void onFailure(Call<ApiResponse<Application>> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    showError("Network error: " + t.getMessage());
                }
            });
    }
    
    private void displayApplicationDetails(Application application) {
        User user = application.getUser();
        
        if (user != null) {
            // Basic Information
            tvApplicantName.setText(user.getName() != null ? user.getName() : "N/A");
            tvApplicantEmail.setText(user.getEmail() != null ? user.getEmail() : "N/A");
            tvApplicantMobile.setText(user.getPhone() != null ? user.getPhone() : "N/A");
            tvApplicantId.setText("ID: " + user.getId());
            
            // Employment Information
            boolean hasEmploymentInfo = false;
            if (isValidValue(user.getCurrentCompany())) {
                tvCurrentCompany.setText(user.getCurrentCompany());
                hasEmploymentInfo = true;
            } else {
                tvCurrentCompany.setText("Not specified");
            }
            
            if (isValidValue(user.getDepartment())) {
                tvDepartment.setText(user.getDepartment());
                hasEmploymentInfo = true;
            } else {
                tvDepartment.setText("Not specified");
            }
            
            if (isValidValue(user.getCurrentSalary())) {
                tvCurrentSalary.setText(user.getCurrentSalary());
                hasEmploymentInfo = true;
            } else {
                tvCurrentSalary.setText("Not specified");
            }
            
            if (isValidValue(user.getExpectedSalary())) {
                tvExpectedSalary.setText(user.getExpectedSalary());
                hasEmploymentInfo = true;
            } else {
                tvExpectedSalary.setText("Not specified");
            }
            
            if (isValidValue(user.getJoiningPeriod())) {
                tvJoiningPeriod.setText(user.getJoiningPeriod());
                hasEmploymentInfo = true;
            } else {
                tvJoiningPeriod.setText("Not specified");
            }
            
            cardEmployment.setVisibility(hasEmploymentInfo ? View.VISIBLE : View.GONE);
            
            // Professional Information
            boolean hasProfessionalInfo = false;
            if (isValidValue(user.getExperience())) {
                tvExperience.setText(user.getExperience());
                hasProfessionalInfo = true;
            } else {
                tvExperience.setText("Not specified");
            }
            
            if (isValidValue(user.getSkills())) {
                tvSkills.setText(user.getSkills());
                hasProfessionalInfo = true;
            } else {
                tvSkills.setText("Not specified");
            }
            
            if (isValidValue(user.getLocation())) {
                tvLocation.setText(user.getLocation());
                hasProfessionalInfo = true;
            } else {
                tvLocation.setText("Not specified");
            }
            
            if (isValidValue(user.getAboutMe())) {
                tvAboutMe.setText(user.getAboutMe());
                hasProfessionalInfo = true;
            } else {
                tvAboutMe.setText("Not specified");
            }
            
            cardProfessional.setVisibility(hasProfessionalInfo ? View.VISIBLE : View.GONE);
            
            // Load profile photo dynamically
            if (user.getProfilePhoto() != null && !user.getProfilePhoto().isEmpty()) {
                String photoUrl = user.getProfilePhoto();
                
                // Handle different URL formats
                if (!photoUrl.startsWith("http")) {
                    // Check if it starts with "profile_photos/" or "storage/"
                    if (photoUrl.startsWith("profile_photos/")) {
                        photoUrl = "https://emps.co.in/storage/" + photoUrl;
                    } else if (photoUrl.startsWith("storage/")) {
                        photoUrl = "https://emps.co.in/" + photoUrl;
                    } else {
                        photoUrl = "https://emps.co.in/" + photoUrl;
                    }
                }
                
                Log.d("RecruiterAppDetails", "Loading profile photo from: " + photoUrl);
                
                Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(ivProfilePhoto);
            } else {
                Log.d("RecruiterAppDetails", "No profile photo available, using default");
                ivProfilePhoto.setImageResource(R.drawable.ic_person);
            }
        }
        
        // Job Information
        tvJobTitle.setText(application.getJobTitle());
        tvCompanyName.setText(application.getCompany());
        
        // Applied Date
        String appliedDate = application.getAppliedDate() != null ? 
            application.getAppliedDate() : application.getCreatedAt();
        tvAppliedDate.setText("Applied: " + getRelativeDateString(appliedDate));
        
        // Application Status
        String status = application.getStatus() != null ? application.getStatus() : "Pending";
        chipStatus.setText(status);
        updateStatusChipColor(status);
        
        // Cover Letter
        if (isValidValue(application.getCoverLetter())) {
            tvCoverLetter.setText(application.getCoverLetter());
            cardCoverLetter.setVisibility(View.VISIBLE);
        } else {
            cardCoverLetter.setVisibility(View.GONE);
        }
        
        // Resume button
        if (application.getResumePath() != null && !application.getResumePath().isEmpty()) {
            btnViewResume.setEnabled(true);
        } else if (user != null && user.getResume() != null && !user.getResume().isEmpty()) {
            btnViewResume.setEnabled(true);
        } else {
            btnViewResume.setEnabled(false);
        }
    }
    
    private boolean isValidValue(String value) {
        return value != null && !value.isEmpty() && !value.equalsIgnoreCase("null");
    }
    
    private void updateStatusChipColor(String status) {
        int colorResId;
        switch (status.toLowerCase()) {
            case "pending":
            case "applied":
                colorResId = android.R.color.holo_orange_dark;
                break;
            case "reviewing":
            case "under review":
                colorResId = android.R.color.holo_blue_dark;
                break;
            case "shortlisted":
                colorResId = android.R.color.holo_purple;
                break;
            case "rejected":
                colorResId = android.R.color.holo_red_dark;
                break;
            case "hired":
            case "accepted":
                colorResId = android.R.color.holo_green_dark;
                break;
            default:
                colorResId = android.R.color.darker_gray;
                break;
        }
        chipStatus.setChipBackgroundColorResource(colorResId);
    }
    
    private void viewResume() {
        if (currentApplication == null) return;
        
        // Check if resume exists
        String resumePath = currentApplication.getResumePath();
        if (resumePath == null || resumePath.isEmpty()) {
            User user = currentApplication.getUser();
            if (user != null && user.getResume() != null && !user.getResume().isEmpty()) {
                resumePath = user.getResume();
            }
        }
        
        if (resumePath == null || resumePath.isEmpty()) {
            Toast.makeText(this, "Resume not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show loading state
        btnViewResume.setEnabled(false);
        btnViewResume.setText("Loading...");
        
        // Download resume using authenticated endpoint
        ApiClient.getRecruiterApiService().downloadApplicationResume(applicationId)
            .enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    btnViewResume.setEnabled(true);
                    btnViewResume.setText("View Resume");
                    
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            // Save the file to cache directory
                            File cacheDir = getCacheDir();
                            String fileName = "resume_" + applicationId + ".pdf";
                            File resumeFile = new File(cacheDir, fileName);
                            
                            // Write response body to file
                            InputStream inputStream = response.body().byteStream();
                            FileOutputStream outputStream = new FileOutputStream(resumeFile);
                            
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                            
                            outputStream.flush();
                            outputStream.close();
                            inputStream.close();
                            
                            // Open the file with appropriate app
                            openResumeFile(resumeFile);
                            
                        } catch (Exception e) {
                            Log.e("RecruiterAppDetails", "Error saving resume: " + e.getMessage());
                            Toast.makeText(RecruiterApplicationDetailsActivity.this, 
                                "Failed to open resume: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(RecruiterApplicationDetailsActivity.this, 
                            "Failed to download resume", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    btnViewResume.setEnabled(true);
                    btnViewResume.setText("View Resume");
                    Log.e("RecruiterAppDetails", "Resume download failed: " + t.getMessage());
                    Toast.makeText(RecruiterApplicationDetailsActivity.this, 
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void openResumeFile(File file) {
        try {
            Uri fileUri = FileProvider.getUriForFile(
                this,
                getApplicationContext().getPackageName() + ".fileprovider",
                file
            );
            
            String mimeType = "application/pdf";
            // Detect file type from extension
            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
                mimeType = "application/msword";
            } else if (fileName.endsWith(".pdf")) {
                mimeType = "application/pdf";
            }
            
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            try {
                startActivity(intent);
            } catch (android.content.ActivityNotFoundException e) {
                Toast.makeText(this, "No app found to open resume. Please install a PDF viewer.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e("RecruiterAppDetails", "Error opening resume: " + e.getMessage());
            Toast.makeText(this, "Failed to open resume: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showUpdateStatusDialog() {
        if (currentApplication == null) return;
        
        String[] statuses = {"Pending", "Under Review", "Shortlisted", "Rejected", "Hired"};
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Update Application Status")
            .setItems(statuses, (dialog, which) -> {
                updateApplicationStatus(statuses[which]);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void updateApplicationStatus(String newStatus) {
        btnUpdateStatus.setEnabled(false);
        
        java.util.Map<String, String> statusUpdate = new java.util.HashMap<>();
        statusUpdate.put("status", newStatus);
        
        ApiClient.getRecruiterApiService().updateApplicationStatus(applicationId, statusUpdate)
            .enqueue(new Callback<ApiResponse<Application>>() {
                @Override
                public void onResponse(Call<ApiResponse<Application>> call, Response<ApiResponse<Application>> response) {
                    btnUpdateStatus.setEnabled(true);
                    
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(RecruiterApplicationDetailsActivity.this, 
                            "Status updated successfully", Toast.LENGTH_SHORT).show();
                        // Reload application details
                        loadApplicationDetails();
                    } else {
                        Toast.makeText(RecruiterApplicationDetailsActivity.this, 
                            "Failed to update status", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<ApiResponse<Application>> call, Throwable t) {
                    btnUpdateStatus.setEnabled(true);
                    Toast.makeText(RecruiterApplicationDetailsActivity.this, 
                        "Network error", Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void contactCandidate() {
        if (currentApplication == null || currentApplication.getUser() == null) return;
        
        User user = currentApplication.getUser();
        String[] options = {"Call", "Email", "SMS"};
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Contact Candidate")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: // Call
                        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                            Intent callIntent = new Intent(Intent.ACTION_DIAL);
                            callIntent.setData(Uri.parse("tel:" + user.getPhone()));
                            startActivity(callIntent);
                        } else {
                            Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 1: // Email
                        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                            emailIntent.setData(Uri.parse("mailto:" + user.getEmail()));
                            emailIntent.putExtra(Intent.EXTRA_SUBJECT, 
                                "Regarding your application for " + currentApplication.getJobTitle());
                            startActivity(Intent.createChooser(emailIntent, "Send email"));
                        } else {
                            Toast.makeText(this, "Email not available", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 2: // SMS
                        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                            Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
                            smsIntent.setData(Uri.parse("smsto:" + user.getPhone()));
                            startActivity(smsIntent);
                        } else {
                            Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private String getRelativeDateString(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "recently";
        }

        try {
            SimpleDateFormat sdf;
            Date appliedDate = null;
            Date currentDate = new Date();
            
            // Try parsing with ISO 8601 format first
            try {
                sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault());
                appliedDate = sdf.parse(dateString);
            } catch (ParseException e1) {
                try {
                    sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                    appliedDate = sdf.parse(dateString);
                } catch (ParseException e2) {
                    try {
                        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        appliedDate = sdf.parse(dateString);
                    } catch (ParseException e3) {
                        sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        appliedDate = sdf.parse(dateString);
                    }
                }
            }

            if (appliedDate != null) {
                long diffInMillis = currentDate.getTime() - appliedDate.getTime();
                long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);
                long diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis);

                if (diffInDays > 30) {
                    SimpleDateFormat monthFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
                    return monthFormat.format(appliedDate);
                } else if (diffInDays > 0) {
                    return diffInDays + (diffInDays == 1 ? " day ago" : " days ago");
                } else if (diffInHours > 0) {
                    return diffInHours + (diffInHours == 1 ? " hour ago" : " hours ago");
                } else {
                    return "just now";
                }
            }
        } catch (ParseException e) {
            Log.e("RecruiterAppDetails", "Error parsing date: " + e.getMessage());
        }
        
        return "recently";
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
