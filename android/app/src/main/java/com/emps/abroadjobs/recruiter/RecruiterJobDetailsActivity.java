package com.emps.abroadjobs.recruiter;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.emps.abroadjobs.R;
import com.emps.abroadjobs.models.Job;
import com.emps.abroadjobs.models.JobListResponse;
import com.emps.abroadjobs.network.ApiClient;
import com.emps.abroadjobs.network.ApiResponse;
import com.emps.abroadjobs.recruiter.JobEditActivity;
import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecruiterJobDetailsActivity extends AppCompatActivity {
    
    public static final String EXTRA_JOB_ID = "job_id";
    public static final String EXTRA_JOB_TITLE = "job_title";
    
    private int jobId;
    private String jobTitle;
    private Job currentJob;
    private ActivityResultLauncher<Intent> editJobLauncher;
    
    // UI Components
    private TextView tvJobTitle, tvCompany, tvLocation, tvSalary, tvJobType, tvCategory;
    private TextView tvPostedDate, tvExpiryDate, tvDescription, tvRequirements, tvBenefits;
    private TextView tvApplicationsCount, tvStatus, tvApprovalStatus, tvSkills, tvExperience;
    private ImageView ivJobImage, ivCompanyLogo;
    private MaterialButton btnEditJob, btnToggleStatus;
    private View progressBar, contentLayout;
    private Toolbar toolbar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recruiter_job_details);
        
        // Initialize the edit job launcher
        editJobLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    boolean jobUpdated = result.getData().getBooleanExtra("job_updated", false);
                    if (jobUpdated) {
                        Toast.makeText(this, "Job updated successfully!", Toast.LENGTH_SHORT).show();
                        // Reload job details to show updated information
                        loadJobDetails();
                    }
                }
            }
        );
        
        // Get data from intent
        jobId = getIntent().getIntExtra(EXTRA_JOB_ID, -1);
        jobTitle = getIntent().getStringExtra(EXTRA_JOB_TITLE);
        
        if (jobId == -1) {
            Toast.makeText(this, "Invalid job ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initializeViews();
        setupToolbar();
        setupClickListeners();
        loadJobDetails();
    }
    
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progress_bar);
        contentLayout = findViewById(R.id.content_layout);
        
        tvJobTitle = findViewById(R.id.tv_job_title);
        tvCompany = findViewById(R.id.tv_company);
        tvLocation = findViewById(R.id.tv_location);
        tvSalary = findViewById(R.id.tv_salary);
        tvJobType = findViewById(R.id.tv_job_type);
        tvCategory = findViewById(R.id.tv_category);
        tvPostedDate = findViewById(R.id.tv_posted_date);
        tvExpiryDate = findViewById(R.id.tv_expiry_date);
        tvDescription = findViewById(R.id.tv_description);
        tvRequirements = findViewById(R.id.tv_requirements);
        tvBenefits = findViewById(R.id.tv_benefits);
        tvApplicationsCount = findViewById(R.id.tv_applications_count);
        tvStatus = findViewById(R.id.tv_status);
        tvApprovalStatus = findViewById(R.id.tv_approval_status);
        tvSkills = findViewById(R.id.tv_skills);
        tvExperience = findViewById(R.id.tv_experience);
        
        ivJobImage = findViewById(R.id.iv_job_image);
        ivCompanyLogo = findViewById(R.id.iv_company_logo);
        
        btnEditJob = findViewById(R.id.btn_edit_job);
        btnToggleStatus = findViewById(R.id.btn_toggle_status);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(jobTitle != null ? jobTitle : "Job Details");
        }
    }
    
    private void setupClickListeners() {
        btnEditJob.setOnClickListener(v -> {
            // Navigate to edit job activity
            Intent intent = new Intent(this, JobEditActivity.class);
            intent.putExtra(JobEditActivity.EXTRA_JOB_ID, jobId);
            intent.putExtra(JobEditActivity.EXTRA_JOB_DATA, currentJob);
            editJobLauncher.launch(intent);
        });
        
        btnToggleStatus.setOnClickListener(v -> {
            showStatusChangeDialog();
        });
    }
    
    private void loadJobDetails() {
        progressBar.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);
        
        // Use the specific job details API to get complete job information
        ApiClient.getRecruiterApiService().getJobDetails(jobId).enqueue(new Callback<ApiResponse<Job>>() {
            @Override
            public void onResponse(Call<ApiResponse<Job>> call, Response<ApiResponse<Job>> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Job job = response.body().getData();
                    if (job != null) {
                        currentJob = job; // Store the current job for editing
                        displayJobDetails(job);
                        contentLayout.setVisibility(View.VISIBLE);
                    } else {
                        showError("Job data not available");
                    }
                } else {
                    showError("Failed to load job details");
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Job>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showError("Network error: " + t.getMessage());
            }
        });
    }
    
    private void displayJobDetails(Job job) {
        // Basic job information
        tvJobTitle.setText(job.getTitle());
        tvCompany.setText(job.getCompanyName() != null && !job.getCompanyName().isEmpty() ? 
                         job.getCompanyName() : job.getCompany());
        tvLocation.setText(job.getLocation());
        tvJobType.setText(job.getJobType());
        tvCategory.setText(job.getCategory() != null ? job.getCategory() : "General");
        
        // Dynamic salary display
        String salary = job.getSalary();
        if (salary != null && !salary.isEmpty() && !salary.equalsIgnoreCase("null")) {
            tvSalary.setText(formatSalary(salary));
            tvSalary.setVisibility(View.VISIBLE);
        } else {
            tvSalary.setText("Salary not specified");
            tvSalary.setVisibility(View.VISIBLE);
        }
        
        // Dates
        if (job.getCreatedAt() != null) {
            tvPostedDate.setText("Posted: " + getRelativeDateString(job.getCreatedAt()));
        }
        if (job.getExpiryDate() != null) {
            tvExpiryDate.setText("Expires: " + formatDate(job.getExpiryDate()));
        }
        
        // Job description - always show
        String description = job.getDescription();
        if (description != null && !description.isEmpty()) {
            tvDescription.setText(description);
        } else {
            tvDescription.setText("No description available");
        }
        
        // Dynamic Requirements - hide card if empty
        String requirements = job.getRequirements();
        View requirementsCard = findViewById(R.id.card_requirements);
        if (requirements != null && !requirements.isEmpty() && !requirements.trim().equalsIgnoreCase("null")) {
            tvRequirements.setText(requirements);
            requirementsCard.setVisibility(View.VISIBLE);
        } else {
            requirementsCard.setVisibility(View.GONE);
        }
        
        // Dynamic Benefits - hide card if empty
        String benefits = job.getBenefits();
        View benefitsCard = findViewById(R.id.card_benefits);
        if (benefits != null && !benefits.isEmpty() && !benefits.trim().equalsIgnoreCase("null")) {
            tvBenefits.setText(benefits);
            benefitsCard.setVisibility(View.VISIBLE);
        } else {
            benefitsCard.setVisibility(View.GONE);
        }
        
        // Dynamic Skills - show card if available
        String skillsRequired = job.getSkillsRequired();
        View skillsCard = findViewById(R.id.card_skills);
        if (skillsRequired != null && !skillsRequired.isEmpty() && !skillsRequired.trim().equalsIgnoreCase("null")) {
            tvSkills.setText(skillsRequired);
            skillsCard.setVisibility(View.VISIBLE);
        } else {
            skillsCard.setVisibility(View.GONE);
        }
        
        // Dynamic Experience Level - show card if available
        String experienceRequired = job.getExperienceRequired();
        View experienceCard = findViewById(R.id.card_experience);
        if (experienceRequired != null && !experienceRequired.isEmpty() && !experienceRequired.trim().equalsIgnoreCase("null")) {
            tvExperience.setText(experienceRequired);
            experienceCard.setVisibility(View.VISIBLE);
        } else {
            experienceCard.setVisibility(View.GONE);
        }
        
        // Applications count
        tvApplicationsCount.setText(job.getApplicationsCount() + " Applications");
        
        // Job status
        if (job.isActive()) {
            tvStatus.setText("Active");
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            btnToggleStatus.setText("Deactivate");
        } else {
            tvStatus.setText("Inactive");
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            btnToggleStatus.setText("Activate");
        }
        
        // Approval status
        String approvalStatus = job.getApprovalStatus();
        if (approvalStatus != null) {
            switch (approvalStatus.toLowerCase()) {
                case "approved":
                    tvApprovalStatus.setText("Approved");
                    tvApprovalStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    break;
                case "pending":
                    tvApprovalStatus.setText("Pending Approval");
                    tvApprovalStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                    break;
                case "declined":
                    tvApprovalStatus.setText("Declined");
                    tvApprovalStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    break;
                default:
                    tvApprovalStatus.setText("Unknown Status");
                    break;
            }
        }
        
        // Load images - these might not be available in the list API, so we'll handle gracefully
        loadJobImage(job);
        loadCompanyLogo(job);
    }
    
    private String formatSalary(String salary) {
        if (salary == null || salary.isEmpty() || salary.equalsIgnoreCase("null")) {
            return "Not specified";
        }
        
        // Remove any existing currency symbols and clean up
        String cleanSalary = salary.replaceAll("[^\\d.,k-]", "").trim();
        
        if (cleanSalary.isEmpty()) {
            return "Not specified";
        }
        
        // Handle different salary formats
        if (cleanSalary.toLowerCase().contains("k")) {
            return "$" + cleanSalary.toUpperCase();
        } else if (cleanSalary.contains("-")) {
            return "$" + cleanSalary;
        } else {
            // Try to parse as number and format
            try {
                double amount = Double.parseDouble(cleanSalary.replace(",", ""));
                if (amount >= 1000) {
                    return String.format("$%.0fK", amount / 1000);
                } else {
                    return String.format("$%.0f", amount);
                }
            } catch (NumberFormatException e) {
                return "$" + cleanSalary;
            }
        }
    }
    
    private void loadJobImage(Job job) {
        View imageCard = findViewById(R.id.card_job_image);
        
        if (job.getImage() != null && !job.getImage().isEmpty()) {
            String imageUrl = job.getImage();
            if (!imageUrl.startsWith("http")) {
                imageUrl = "https://emps.co.in/" + imageUrl;
            }
            
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_jobs)
                .error(R.drawable.ic_jobs)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ivJobImage);
            imageCard.setVisibility(View.VISIBLE);
        } else {
            imageCard.setVisibility(View.GONE);
        }
    }
    
    private void loadCompanyLogo(Job job) {
        if (job.getCompanyLogo() != null && !job.getCompanyLogo().isEmpty()) {
            String logoUrl = job.getCompanyLogo();
            if (!logoUrl.startsWith("http")) {
                logoUrl = "https://emps.co.in/" + logoUrl;
            }
            
            Glide.with(this)
                .load(logoUrl)
                .placeholder(R.drawable.ic_company)
                .error(R.drawable.ic_company)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ivCompanyLogo);
        } else {
            ivCompanyLogo.setImageResource(R.drawable.ic_company);
        }
    }
    
    private void showStatusChangeDialog() {
        if (currentJob == null) return;
        
        String action = currentJob.isActive() ? "deactivate" : "activate";
        String message = currentJob.isActive() ? 
            "Deactivating this job will stop it from receiving new applications." :
            "Activating this job will allow it to receive new applications.";
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Confirm Action")
            .setMessage(message)
            .setPositiveButton(action.substring(0, 1).toUpperCase() + action.substring(1), (dialog, which) -> {
                if (currentJob.isActive()) {
                    deactivateJob();
                } else {
                    activateJob();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void deactivateJob() {
        btnToggleStatus.setEnabled(false);
        
        ApiClient.getRecruiterApiService().deactivateJob(jobId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                btnToggleStatus.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(RecruiterJobDetailsActivity.this, "Job deactivated successfully", Toast.LENGTH_SHORT).show();
                    // Reload job details to get updated status
                    loadJobDetails();
                } else {
                    Toast.makeText(RecruiterJobDetailsActivity.this, "Failed to deactivate job", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                btnToggleStatus.setEnabled(true);
                Toast.makeText(RecruiterJobDetailsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void activateJob() {
        btnToggleStatus.setEnabled(false);
        
        ApiClient.getRecruiterApiService().activateJob(jobId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                btnToggleStatus.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(RecruiterJobDetailsActivity.this, "Job activated successfully", Toast.LENGTH_SHORT).show();
                    // Reload job details to get updated status
                    loadJobDetails();
                } else {
                    Toast.makeText(RecruiterJobDetailsActivity.this, "Failed to activate job", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                btnToggleStatus.setEnabled(true);
                Toast.makeText(RecruiterJobDetailsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private String getRelativeDateString(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "recently";
        }

        try {
            SimpleDateFormat sdf;
            Date postedDate = null;
            Date currentDate = new Date();
            
            // Try parsing with ISO 8601 format first
            try {
                sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault());
                postedDate = sdf.parse(dateString);
            } catch (ParseException e1) {
                try {
                    sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                    postedDate = sdf.parse(dateString);
                } catch (ParseException e2) {
                    try {
                        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        postedDate = sdf.parse(dateString);
                    } catch (ParseException e3) {
                        sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        postedDate = sdf.parse(dateString);
                    }
                }
            }

            if (postedDate != null) {
                long diffInMillis = currentDate.getTime() - postedDate.getTime();
                long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);
                long diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis);

                if (diffInDays > 30) {
                    SimpleDateFormat monthFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
                    return monthFormat.format(postedDate);
                } else if (diffInDays > 0) {
                    return diffInDays + (diffInDays == 1 ? " day ago" : " days ago");
                } else if (diffInHours > 0) {
                    return diffInHours + (diffInHours == 1 ? " hour ago" : " hours ago");
                } else {
                    return "just now";
                }
            }
        } catch (ParseException e) {
            Log.e("RecruiterJobDetails", "Error parsing date: " + e.getMessage());
        }
        
        return "recently";
    }
    
    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "Not specified";
        }
        
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateString;
        }
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