package com.example.jobportal.ui.jobdetails;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.jobportal.R;
// Use only the data.model.Job class
//import com.example.jobportal.data.model.Job;
import com.example.jobportal.auth.LoginActivity;
import com.example.jobportal.network.ApiCallback;
import com.example.jobportal.network.ApiClient;
import com.example.jobportal.network.ApiResponse;
import com.example.jobportal.models.Job;
import com.example.jobportal.models.Application;
import com.example.jobportal.models.User;
import com.example.jobportal.ui.profile.ProfileFragment;
import com.example.jobportal.ui.application.JobApplicationNewFragment;
import com.example.jobportal.utils.SessionManager;
import com.example.jobportal.utils.JobConverter;
import com.example.jobportal.models.FeaturedJob;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class JobDetailsFragment extends Fragment {
    private static final String ARG_JOB_ID = "job_id";
    private static final String ARG_IS_FEATURED = "is_featured";
    
    private String jobId;
    private boolean isFeaturedJob = false;
    private TextView titleTextView;
    private TextView companyTextView;
    private TextView locationTextView;
    private TextView salaryTextView;
    private TextView typeTextView;
    private TextView dateTextView;
    private TextView descriptionTextView;
    private Button applyButton;
    private View progressBar;
    private ImageView jobImageView;
    private ImageView companyLogoView;
    private View contentLayout;

    private static final String BASE_URL = "https://emps.co.in/";
    
    public static JobDetailsFragment newInstance(String jobId) {
        return newInstance(jobId, false);
    }
    
    public static JobDetailsFragment newInstance(String jobId, boolean isFeaturedJob) {
        JobDetailsFragment fragment = new JobDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_JOB_ID, jobId);
        args.putBoolean(ARG_IS_FEATURED, isFeaturedJob);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            jobId = getArguments().getString(ARG_JOB_ID);
            isFeaturedJob = getArguments().getBoolean(ARG_IS_FEATURED, false);
            
            // Check if there are additional indicators that this is a featured job
            if (getArguments().getString("job_title") != null && 
                getArguments().getString("company_name") != null) {
                isFeaturedJob = true;
            }
        }
        
        Log.d("JobDetailsFragment", "Job ID: " + jobId + ", Is Featured: " + isFeaturedJob);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_job_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        titleTextView = view.findViewById(R.id.job_title);
        companyTextView = view.findViewById(R.id.company_name);
        companyLogoView = view.findViewById(R.id.company_logo);
        jobImageView = view.findViewById(R.id.job_image);
        locationTextView = view.findViewById(R.id.location);
        salaryTextView = view.findViewById(R.id.salary);
        typeTextView = view.findViewById(R.id.job_type);
        dateTextView = view.findViewById(R.id.posting_date);
        descriptionTextView = view.findViewById(R.id.description);
        applyButton = view.findViewById(R.id.apply_button);
        progressBar = view.findViewById(R.id.progress_bar);
        contentLayout = view.findViewById(R.id.content_layout);
        
        // Set up listeners
        applyButton.setOnClickListener(v -> {
            applyForJob();
        });
        
        // Load job details
        loadJobDetails();
    }
    
    private void loadJobDetails() {
        progressBar.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);
        
        Log.d("JobDetailsFragment", "Loading job details for ID: " + jobId + ", Is Featured: " + isFeaturedJob);
        
        // If the isFeaturedJob flag is set, prioritize loading as a featured job
        if (isFeaturedJob) {
            // Try to load as featured job first
            loadFeaturedJobDetails();
        } else {
            // Check if additional data was passed in arguments that indicates this is a featured job
            Bundle args = getArguments();
            String jobTitle = args != null ? args.getString("job_title") : null;
            String companyName = args != null ? args.getString("company_name") : null;
            
            // If we have job title and company name in args, this may be a featured job
            if (jobTitle != null && companyName != null) {
                // Try to load as featured job first
                loadFeaturedJobDetails();
            } else {
                // Load as regular job
                loadRegularJobDetails();
            }
        }
    }
    
    /**
     * Load job details for a featured job
     */
    private void loadFeaturedJobDetails() {
        ApiClient.getApiService().getFeaturedJobDetails(Integer.parseInt(jobId)).enqueue(new Callback<ApiResponse<FeaturedJob>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<FeaturedJob>> call, @NonNull Response<ApiResponse<FeaturedJob>> response) {
                progressBar.setVisibility(View.GONE);
                
                Log.d("JobDetailsFragment", "Featured job response received: " + response.code());
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    FeaturedJob featuredJob = response.body().getData();
                    if (featuredJob != null) {
                        Log.d("JobDetailsFragment", "Featured job details received: " + featuredJob.getJobTitle());
                        
                        // Convert featured job to regular job format and display
                        Job job = JobConverter.convertFeaturedJobToJob(featuredJob);
                        displayJobDetails(job);
                    } else {
                        Log.e("JobDetailsFragment", "Featured job data is null");
                        // Fall back to regular job loading
                        loadRegularJobDetails();
                    }
                } else {
                    Log.e("JobDetailsFragment", "Error response for featured job: " + response.code());
                    // Fall back to regular job loading
                    loadRegularJobDetails();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<FeaturedJob>> call, @NonNull Throwable t) {
                Log.e("JobDetailsFragment", "Network error for featured job: " + t.getMessage(), t);
                // Fall back to regular job loading
                loadRegularJobDetails();
            }
        });
    }
    
    /**
     * Load job details for a regular job
     */
    private void loadRegularJobDetails() {
        ApiClient.getApiService().getJobDetails(Integer.parseInt(jobId)).enqueue(new Callback<ApiResponse<Job>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Job>> call, @NonNull Response<ApiResponse<Job>> response) {
                progressBar.setVisibility(View.GONE);
                
                Log.d("JobDetailsFragment", "Response received: " + response.code());
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Job job = response.body().getData();
                    if (job != null) {
                        Log.d("JobDetailsFragment", "Job details received: " + job.getTitle());
                        displayJobDetails(job);
                    } else {
                        Log.e("JobDetailsFragment", "Job data is null");
                        showError("Failed to load job details: Data is null");
                    }
                } else {
                    Log.e("JobDetailsFragment", "Error response: " + response.code() + " " + response.message());
                    showError("Failed to load job details");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Job>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("JobDetailsFragment", "Network error: " + t.getMessage(), t);
                showError("Network error: " + t.getMessage());
            }
        });
    }
    

    private void displayJobDetails(Job job) {
        contentLayout.setVisibility(View.VISIBLE);
        
        titleTextView.setText(job.getTitle());
        companyTextView.setText(job.getCompany());
        locationTextView.setText(job.getLocation());
        
        // Format salary with currency symbol
        String formattedSalary = formatSalaryWithCurrency(job.getSalary());
        salaryTextView.setText(formattedSalary);
        
        // Set job type
        if (typeTextView != null) {
            String jobType = job.getJobType();
            if (jobType != null && !jobType.isEmpty()) {
                typeTextView.setText(jobType);
            } else {
                typeTextView.setText("Not specified");
            }
        }
        
        // Set posted date
        if (dateTextView != null) {
            String postedDate = job.getPostingDate();
            if (postedDate != null && !postedDate.isEmpty()) {
                String formattedDate = "Posted: " + getRelativeDateString(postedDate);
                dateTextView.setText(formattedDate);
            } else {
                dateTextView.setText("Posted: Recently");
            }
        }
        
        // Load company logo if available
        if (companyLogoView != null && job.getCompanyLogo() != null && !job.getCompanyLogo().isEmpty()) {
            String logoUrl = job.getCompanyLogo();
            // Ensure URL is absolute
            if (!logoUrl.startsWith("http")) {
                // If it's a relative URL, append to base URL
                String baseUrl = "https://emps.co.in/";
                if (logoUrl.startsWith("/")) {
                    logoUrl = baseUrl + logoUrl.substring(1);
                } else {
                    logoUrl = baseUrl + logoUrl;
                }
            }
            
            Log.d("JobDetailsFragment", "Loading company logo from: " + logoUrl);
            
            Glide.with(requireContext())
                .load(logoUrl)
                .placeholder(R.drawable.ic_company)
                .error(R.drawable.ic_company)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(companyLogoView);
        } else if (companyLogoView != null) {
            // Set default company icon
            companyLogoView.setImageResource(R.drawable.ic_company);
        }

        // Load job image if available
        if (jobImageView != null && job.getImage() != null && !job.getImage().isEmpty()) {
            jobImageView.setVisibility(View.VISIBLE);
            String imageUrl = job.getImage();
            if (!imageUrl.startsWith("http")) {
                String baseUrl = "https://emps.co.in/";
                if (imageUrl.startsWith("/")) {
                    imageUrl = baseUrl + imageUrl.substring(1);
                } else {
                    imageUrl = baseUrl + imageUrl;
                }
            }
            Log.d("JobDetailsFragment", "Loading job image from: " + imageUrl);
            Glide.with(requireContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_jobs)
                .error(R.drawable.ic_jobs)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(jobImageView);
        } else if (jobImageView != null) {
            jobImageView.setVisibility(View.GONE);
        }

        // Always show job description
        if (descriptionTextView != null) {
            descriptionTextView.setVisibility(View.VISIBLE);
            descriptionTextView.setText(job.getDescription());
        }
        // Enable apply button only if job is active
        applyButton.setEnabled(job.isActive());
        if (!job.isActive()) {
            applyButton.setText("Job Closed");
        }
    }
    
    /**
     * Format salary with currency symbol
     */
    private String formatSalaryWithCurrency(String salaryText) {
        if (salaryText == null || salaryText.isEmpty()) {
            return "0";
        }
        
        // Remove any existing currency symbols or formatting
        String cleanSalary = salaryText.replaceAll("[^\\d.,]|^[.,]", "").trim();
        
        // If empty after cleaning, return default
        if (cleanSalary.isEmpty()) {
            return "0";
        }
        
        // Handle different formats - K notation, ranges, etc.
        if (cleanSalary.contains("-")) {
            // Handle salary ranges
            String[] parts = cleanSalary.split("-");
            if (parts.length == 2) {
                String start = parts[0].trim();
                String end = parts[1].trim();
                return "" + start + " - " + end;
            }
        } else if (cleanSalary.toLowerCase().contains("k")) {
            // Handle K notation (thousands)
            return "" + cleanSalary;
        }
        
        // Default formatting with dollar sign
        return "" + cleanSalary;
    }
    
    private void applyForJob() {
        // Check if user is logged in
        SessionManager sessionManager = SessionManager.getInstance(requireContext());
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(requireContext(), 
                "Please login to apply for jobs", Toast.LENGTH_LONG).show();
            // Navigate to login screen
            startActivity(new Intent(requireContext(), LoginActivity.class));
            return;
        }

        // Show loading state
        applyButton.setEnabled(false);
        applyButton.setText("Checking...");
        
        // Log that we're applying for a job
        Log.d("JobDetailsFragment", "Applying for job ID: " + jobId + ", Is Featured: " + isFeaturedJob);
        
        // Create API client with context
        ApiClient apiClient = ApiClient.getInstance(requireContext());
        
        // First, check if the user has a resume by fetching the profile
        apiClient.getUserProfile(new ApiCallback<ApiResponse<User>>() {
            @Override
            public void onSuccess(ApiResponse<User> response) {
                if (isAdded()) {  // Check if fragment is still attached
                    User user = response.getData();
                    
                    if (user != null) {
                        String resumeUrl = user.getResume();
                        
                        if (resumeUrl != null && !resumeUrl.isEmpty()) {
                            // User has a resume, navigate to the JobApplicationFragment
                            applyButton.setEnabled(true);
                            applyButton.setText("Apply Now");
                            
                            // Get job details to pass to the application form
                            String jobTitle = titleTextView.getText().toString();
                            String company = companyTextView.getText().toString();
                            
                            // Navigate to application form fragment, passing the featured job flag
                            JobApplicationNewFragment applicationFragment = 
                                JobApplicationNewFragment.newInstance(jobId, jobTitle, company, isFeaturedJob);
                            requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, applicationFragment)
                                .addToBackStack(null)
                                .commit();
                        } else {
                            // User doesn't have a resume, prompt to upload
                            applyButton.setEnabled(true);
                            applyButton.setText("Apply Now");
                            
                            Toast.makeText(requireContext(), 
                                "Please upload your resume in profile before applying", Toast.LENGTH_LONG).show();
                            // Navigate to application form fragment anyway, as resume upload is now part of the flow
                            JobApplicationNewFragment applicationFragment = 
                                JobApplicationNewFragment.newInstance(jobId, titleTextView.getText().toString(), 
                                                                    companyTextView.getText().toString(), isFeaturedJob);
                            requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, applicationFragment)
                                .addToBackStack(null)
                                .commit();
                        }
                    } else {
                        // Error getting user data
                        applyButton.setEnabled(true);
                        applyButton.setText("Apply Now");
                        Toast.makeText(requireContext(), 
                            "Error fetching profile data. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            
            @Override
            public void onError(String errorMessage) {
                if (isAdded()) {  // Check if fragment is still attached
                    applyButton.setEnabled(true);
                    applyButton.setText("Apply Now");
                    
                    if (errorMessage.contains("not logged in")) {
                        // Clear session and redirect to login
                        sessionManager.logout();
                        startActivity(new Intent(requireContext(), LoginActivity.class));
                        requireActivity().finish();
                    } else {
                        Toast.makeText(requireContext(), 
                            "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
    
    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Convert date string to relative time format (e.g., "2 days ago")
     */
    private String getRelativeDateString(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "recently";
        }

        try {
            java.text.SimpleDateFormat sdf;
            java.util.Date postedDate = null;
            java.util.Date currentDate = new java.util.Date();
            
            // Try parsing with ISO 8601 format first (with microseconds)
            try {
                sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", java.util.Locale.getDefault());
                postedDate = sdf.parse(dateString);
            } catch (java.text.ParseException e1) {
                // Fallback: try without microseconds
                try {
                    sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault());
                    postedDate = sdf.parse(dateString);
                } catch (java.text.ParseException e2) {
                    // Fallback: try the old format
                    try {
                        sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
                        postedDate = sdf.parse(dateString);
                    } catch (java.text.ParseException e3) {
                        // Final fallback: try simple date format
                        sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                        postedDate = sdf.parse(dateString);
                    }
                }
            }

            if (postedDate != null) {
                // Calculate the difference in milliseconds
                long diffInMillis = currentDate.getTime() - postedDate.getTime();
                long diffInDays = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diffInMillis);
                long diffInHours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(diffInMillis);
                long diffInMinutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(diffInMillis);

                // Return a human-readable string
                if (diffInDays > 30) {
                    // If more than a month, show the actual date
                    java.text.SimpleDateFormat monthFormat = new java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault());
                    return monthFormat.format(postedDate);
                } else if (diffInDays > 0) {
                    return diffInDays + (diffInDays == 1 ? " day ago" : " days ago");
                } else if (diffInHours > 0) {
                    return diffInHours + (diffInHours == 1 ? " hour ago" : " hours ago");
                } else if (diffInMinutes > 0) {
                    return diffInMinutes + (diffInMinutes == 1 ? " minute ago" : " minutes ago");
                } else {
                    return "just now";
                }
            }
        } catch (java.text.ParseException e) {
            Log.e("JobDetailsFragment", "Error parsing date: " + e.getMessage());
        }
        
        return "recently"; // Fallback
    }
}