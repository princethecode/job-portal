package com.example.jobportal.ui.home;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.appcompat.widget.SearchView;

import com.example.jobportal.ui.FeaturedJobs.FeaturedJobsAdapter;
import  com.example.jobportal.ui.FeaturedJobs.FeaturedJobsViewModel;
// Import jobs related classes
import com.example.jobportal.models.FeaturedJob;
import com.example.jobportal.models.JobsListResponse;
import com.example.jobportal.network.ApiClient;
import com.example.jobportal.R;  // Add R class import
import com.example.jobportal.ui.jobs.JobsFragment;
import com.example.jobportal.ui.jobs.JobsViewModel;
import com.example.jobportal.ui.jobdetails.JobDetailsFragment;

import com.example.jobportal.models.Job;
import com.example.jobportal.databinding.FragmentHomeBinding;
import com.example.jobportal.network.ApiResponse;
import com.example.jobportal.ui.jobs.JobAdapter;
import com.example.jobportal.ui.JobCategory.JobCategoryAdapter;
import com.example.jobportal.models.JobCategory;
import com.example.jobportal.ui.search.SearchFragment;
import com.example.jobportal.utils.JobConverter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements FeaturedJobsAdapter.OnJobClickListener, JobAdapter.OnJobClickListener {
    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;
    private JobAdapter jobAdapter;
    private JobAdapter recentJobsAdapter;
    private FeaturedJobsAdapter featuredJobsAdapter;
    private JobCategoryAdapter jobCategoryAdapter;
    private JobsViewModel jobsViewModel;
    private FeaturedJobsViewModel featuredJobsViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                            @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize ViewModel
        jobsViewModel = new ViewModelProvider(this, 
            new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication()))
            .get(JobsViewModel.class);
        
        featuredJobsViewModel = new ViewModelProvider(this,
            new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication()))
            .get(FeaturedJobsViewModel.class);
        
        Log.d(TAG, "ViewModel initialized in onCreate");
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Setup all recycler views and view pagers
        setupFeaturedJobsViewPager();
        setupCategoryRecyclerView();
        setupRecentJobsRecyclerView();
        setupRecyclerView();
        setupSearchView();
        
        // Setup click listener for View All button
        binding.viewAllRecentJobs.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new JobsFragment())
                .addToBackStack(null)
                .commit();
        });
        
        // Observe ViewModel data for recent jobs
        jobsViewModel.getJobs().observe(getViewLifecycleOwner(), jobs -> {
            Log.d(TAG, "Recent jobs updated. Count: " + (jobs != null ? jobs.size() : 0));
            if (jobs != null && !jobs.isEmpty()) {
                // Only take the first 5 jobs for the recent jobs section
                List<Job> recentJobs = jobs.size() > 5 ? jobs.subList(0, 5) : jobs;
                recentJobsAdapter.submitList(recentJobs);
                binding.recentJobsRecyclerView.setVisibility(View.VISIBLE);
            } else {
                Log.d(TAG, "No recent jobs to display, showing mock data");
                // Show mock data if no jobs available
                recentJobsAdapter.submitList(createMockJobs());
                binding.recentJobsRecyclerView.setVisibility(View.VISIBLE);
            }
        });
        
        // Observe loading state
        jobsViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                binding.progressBar.setVisibility(View.VISIBLE);
            } else {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
        
        // Observe errors
        jobsViewModel.getError().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                // Show mock data as fallback
                showMockData();
            }
        });
        
        // Refresh jobs from repository
        jobsViewModel.refreshJobs();
        
        // Check connectivity as a fallback
        if (!isNetworkAvailable()) {
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(requireContext(), 
                "No internet connection available. Showing cached data.", 
                Toast.LENGTH_LONG).show();
        }

        // Observe LiveData
        featuredJobsViewModel.getFeaturedJobs().observe(getViewLifecycleOwner(), jobs -> {
            if (jobs != null && !jobs.isEmpty()) {
                featuredJobsAdapter.submitList(jobs);
                binding.featuredJobsViewPager.setVisibility(View.VISIBLE);
            } else {
                // Hide featured jobs section if no data
                binding.featuredJobsViewPager.setVisibility(View.GONE);
                Log.d(TAG, "No featured jobs available, hiding section");
            }
        });
        
        featuredJobsViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Only show progress bar if it's not already visible from other operations
            if (isLoading && binding.progressBar.getVisibility() != View.VISIBLE) {
                binding.progressBar.setVisibility(View.VISIBLE);
            } else if (!isLoading) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
        
        featuredJobsViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "Featured jobs error: " + error);
                
                // Show user-friendly error message
                String userMessage = error;
                if (error.contains("JsonSyntaxException") || error.contains("invalid data format")) {
                    userMessage = "Unable to load featured jobs. Please try again later.";
                } else if (error.contains("Network error") || error.contains("Unable to connect")) {
                    userMessage = "Please check your internet connection and try again.";
                }
                
                Toast.makeText(requireContext(), userMessage, Toast.LENGTH_SHORT).show();
                
                // Hide featured jobs section on error
                binding.featuredJobsViewPager.setVisibility(View.GONE);
            }
        });

        // Fetch data
        featuredJobsViewModel.fetchFeaturedJobs();
        
        // Add debug test for the API endpoint (only in debug builds)
        if (com.example.jobportal.BuildConfig.DEBUG) {
            testFeaturedJobsEndpoint();
        }
    }

    private void setupCategoryRecyclerView() {
        List<JobCategory> categories = new ArrayList<>();
        categories.add(new JobCategory("Delivery", R.drawable.delivery_guy));
        categories.add(new JobCategory("Electrician", R.drawable.electrician));
        categories.add(new JobCategory("Welder", R.drawable.welder));
        categories.add(new JobCategory("Labor/Helper", R.drawable.labor));
        categories.add(new JobCategory("Carpenter", R.drawable.carpenter));
        categories.add(new JobCategory("More", R.drawable.ic_more));
        
        jobCategoryAdapter = new JobCategoryAdapter(categories, category -> {
            if (category.getName().equals("More")) {
                // Navigate to the AllJobCategoriesFragment
                requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new com.example.jobportal.ui.JobCategory.AllJobCategoriesFragment())
                    .addToBackStack(null)
                    .commit();
            } else {
                // Navigate to JobsFragment with category filter
                navigateToJobsByCategory(category.getName());
            }
        });
        
        // Use GridLayoutManager with 3 columns for categories
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 3);
        binding.jobCategoryRecyclerView.setLayoutManager(gridLayoutManager);
        binding.jobCategoryRecyclerView.setAdapter(jobCategoryAdapter);
    }
    
    private void setupFeaturedJobsViewPager() {
        // Setup featured jobs ViewPager2
        // Use this fragment as the click listener for featured jobs
        featuredJobsAdapter = new FeaturedJobsAdapter(this);
        
        binding.featuredJobsViewPager.setAdapter(featuredJobsAdapter);
        binding.featuredJobsViewPager.setClipToPadding(false);
        binding.featuredJobsViewPager.setClipChildren(false);
        binding.featuredJobsViewPager.setOffscreenPageLimit(3);
        
        // Add page transformer for nice visual effect
        CompositePageTransformer compositeTransformer = new CompositePageTransformer();
        compositeTransformer.addTransformer(new MarginPageTransformer(40));
        compositeTransformer.addTransformer((page, position) -> {
            float r = 1 - Math.abs(position);
            page.setScaleY(0.85f + r * 0.15f);
        });
        
        binding.featuredJobsViewPager.setPageTransformer(compositeTransformer);
    }
    
    private void setupRecentJobsRecyclerView() {
        // Use this fragment as the click listener for jobs
        recentJobsAdapter = new JobAdapter(this);
        
        binding.recentJobsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recentJobsRecyclerView.setAdapter(recentJobsAdapter);
        binding.recentJobsRecyclerView.setNestedScrollingEnabled(false);
    }
    
    @Override
    public void onJobClick(Job job) {
        // Add logging
        Log.d(TAG, "Featured Job clicked: ID=" + job.getId() + ", Title=" + job.getTitle());
        
        // Navigate to job details fragment - using the same approach as JobsFragment
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, JobDetailsFragment.newInstance(String.valueOf(job.getId())))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onJobDetailsClick(Job job) {
        Log.d(TAG, "Job Details button clicked: ID=" + job.getId() + ", Title=" + job.getTitle());
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, JobDetailsFragment.newInstance(String.valueOf(job.getId())))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onWhatsAppShareClick(Job job) {
        Log.d(TAG, "WhatsApp share clicked for job: " + job.getTitle());
        shareJobOnWhatsApp(job);
    }

    private void shareJobOnWhatsApp(Job job) {
        try {
            String shareText = "🔥 *Job Opportunity* 🔥\n\n" +
                    "📋 *Position:* " + job.getTitle() + "\n" +
                    "🏢 *Company:* " + job.getCompany() + "\n" +
                    "📍 *Location:* " + job.getLocation() + "\n" +
                    "💰 *Salary:* " + job.getSalary() + "\n\n" +
                    "📝 *Description:* " + job.getDescription() + "\n\n" +
                    "Apply now through our Job Portal app! 📱\n\n" +
                    "📲 Download app from => https://emps.co.in/";

            Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
            whatsappIntent.setType("text/plain");
            whatsappIntent.setPackage("com.whatsapp");
            whatsappIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            
            try {
                startActivity(whatsappIntent);
            } catch (android.content.ActivityNotFoundException ex) {
                // WhatsApp not installed, try with general share
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Job Opportunity: " + job.getTitle());
                startActivity(Intent.createChooser(shareIntent, "Share Job"));
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Unable to share job", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error sharing job", e);
        }
    }

    @Override
    public void onJobClick(FeaturedJob job, int position) {
        // Add logging
        Log.d(TAG, "Featured Job clicked: ID=" + job.getId() + ", Title=" + job.getJobTitle());
        
        // Check if job data is valid
        if (job == null) {
            Toast.makeText(requireContext(), "Error loading job details", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show loading indicator
        binding.progressBar.setVisibility(View.VISIBLE);
        
        // Fetch complete featured job details from API
        ApiClient.getApiService().getFeaturedJobDetails(job.getId()).enqueue(new Callback<ApiResponse<FeaturedJob>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<FeaturedJob>> call, @NonNull Response<ApiResponse<FeaturedJob>> response) {
                binding.progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    FeaturedJob featuredJob = response.body().getData();
                    if (featuredJob != null) {
                        // Navigate to job details with the full data
                        navigateToJobDetails(featuredJob);
                    } else {
                        Toast.makeText(requireContext(), "Error: Job details not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle error
                    Toast.makeText(requireContext(), "Error loading job details", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<ApiResponse<FeaturedJob>> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Network error: " + t.getMessage(), t);
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                
                // As a fallback, try to navigate with the available data
                navigateToJobDetails(job);
            }
        });
    }
    
    /**
     * Navigate to the job details screen for a featured job
     * 
     * @param featuredJob The featured job to display
     */
    private void navigateToJobDetails(FeaturedJob featuredJob) {
        // Create job details fragment with the job ID, explicitly marking as a featured job
        JobDetailsFragment detailsFragment = JobDetailsFragment.newInstance(String.valueOf(featuredJob.getId()), true);
        
        // Pass additional data as arguments if needed
        Bundle args = detailsFragment.getArguments();
        if (args == null) {
            args = new Bundle();
        }
        
        // Add any extra data that might be needed
        args.putString("job_title", featuredJob.getJobTitle());
        args.putString("company_name", featuredJob.getCompanyName());
        args.putString("company_logo", featuredJob.getCompanyLogo());
        detailsFragment.setArguments(args);
        
        // Add transition animation
        requireActivity().getSupportFragmentManager().beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right, R.anim.slide_out_left,
                R.anim.slide_in_left, R.anim.slide_out_right)
            .replace(R.id.fragment_container, detailsFragment)
            .addToBackStack(null)
            .commit();
    }

    private void setupRecyclerView() {
        jobAdapter = new JobAdapter(this);
        binding.jobsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.jobsRecyclerView.setAdapter(jobAdapter);
    }
    
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) 
                requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }
    
    private boolean isHostReachable(String hostname, int timeout) {
        try {
            return InetAddress.getByName(hostname).isReachable(timeout);
        } catch (UnknownHostException e) {
            return false; // Host not found
        } catch (IOException e) {
            return false; // Error checking reachability
        }
    }

    private void fetchJobs() {
        binding.progressBar.setVisibility(View.VISIBLE);
        ApiClient.getApiService().getJobs().enqueue(new Callback<JobsListResponse>() {
            @Override
            public void onResponse(@NonNull Call<JobsListResponse> call,
                          @NonNull Response<JobsListResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    JobsListResponse jobsResponse = response.body();
                    if (jobsResponse.isSuccess() && jobsResponse.getData() != null && 
                        jobsResponse.getData().getJobs() != null) {
                        jobAdapter.submitList(jobsResponse.getData().getJobs());
                    } else {
                        Toast.makeText(requireContext(), 
                            "No jobs available from server", 
                            Toast.LENGTH_LONG).show();
                        showMockData();
                    }
                } else {
                    String errorMsg = "Error code: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
                    
                    // Show mock data as fallback
                    showMockData();
                }
            }
    
            @Override
            public void onFailure(@NonNull Call<JobsListResponse> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), 
                    "Network error: " + t.getMessage(), 
                    Toast.LENGTH_LONG).show();
                
                // Show mock data as fallback
                showMockData();
            }
        });
    }
    
    private void showMockData() {
        List<Job> mockJobs = createMockJobs();
        jobAdapter.submitList(mockJobs);
        recentJobsAdapter.submitList(mockJobs);
    }
    
    private List<Job> getCreateFeaturedJobs() {
        List<Job> featuredJobs = new ArrayList<>();
        
        // Featured job examples with company logos and attractive salaries
        featuredJobs.add(new Job(
            "f1",
            "Senior UX Designer",
            "Design beautiful and intuitive user interfaces for our flagship products. Work with cross-functional teams to deliver exceptional user experiences.",
            "Google",
            "San Francisco, CA",
            "$120K - $150K",
            "Full-time",
            "Design",
            "2025-05-23",
            "2025-06-23",
            true,
            "2025-05-23",
            "2025-05-23"
        ));
        
        featuredJobs.add(new Job(
            "f2",
            "Frontend Developer",
            "Build responsive web applications using modern frameworks. Strong experience with React and TypeScript required.",
            "Spotify",
            "New York, NY",
            "$90K - $120K",
            "Full-time",
            "Technology",
            "2025-05-22",
            "2025-06-22",
            true,
            "2025-05-22",
            "2025-05-22"
        ));
        
        featuredJobs.add(new Job(
            "f3",
            "UI/UX Designer",
            "Create delightful user experiences for our creative tools. Collaborate with product and engineering teams.",
            "Adobe",
            "San Jose, CA",
            "$95K - $120K",
            "Full-time",
            "Design",
            "2025-05-25",
            "2025-06-25",
            true,
            "2025-05-25",
            "2025-05-25"
        ));
        
        featuredJobs.add(new Job(
            "f4",
            "Frontend Developer",
            "Build and maintain high-quality web applications. Work with React, TypeScript, and CSS to create pixel-perfect interfaces.",
            "Airbnb",
            "Remote",
            "$100K - $130K",
            "Full-time",
            "Technology",
            "2025-05-23",
            "2025-06-23",
            true,
            "2025-05-23",
            "2025-05-23"
        ));
        
        featuredJobs.add(new Job(
            "f5",
            "Backend Engineer",
            "Design and implement scalable backend services. Experience with distributed systems and microservices architecture required.",
            "Netflix",
            "Los Gatos, CA",
            "$130K - $170K",
            "Full-time",
            "Technology",
            "2025-05-24",
            "2025-06-24",
            true,
            "2025-05-24",
            "2025-05-24"
        ));
        
        return featuredJobs;
    }
    
    private List<Job> createMockJobs() {
        List<Job> mockJobs = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            // Use the full constructor instead of setters
            Job job = new Job(
                String.valueOf(i),  // Convert int to String
                "Sample Job " + i,                             // title
                "This is a sample job description for job " + i, // description
                "Sample Company " + i,                         // company
                "Sample Location " + i,                        // location
                "$" + (50 + i * 10) + "k - $" + (80 + i * 10) + "k", // salary
                "Full-time",                                   // jobType
                "IT",                                          // category
                "2025-05-01",                                  // postingDate
                "2025-06-01",                                  // expiryDate
                true,                                          // isActive
                "2025-05-01",                                  // createdAt
                "2025-06-01"                                   // updatedAt
            );
            mockJobs.add(job);
        }
        return mockJobs;
    }

    private void setupSearchView() {
        binding.searchEditText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.length() >= 2) {
                    searchJobs(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() >= 2) {
                    searchJobs(newText);
                } else if (newText.isEmpty()) {
                    showHomeSections();
                }
                return true;
            }
        });

        // Listen for the X (close) button
        binding.searchEditText.setOnCloseListener(() -> {
            showHomeSections();
            return false; // Let SearchView handle default behavior (clearing text)
        });
    }

    private void showHomeSections() {
        // Show all main sections
        binding.featuredJobsViewPager.setVisibility(View.VISIBLE);
        binding.jobCategoryRecyclerView.setVisibility(View.VISIBLE);
        binding.recentJobsRecyclerView.setVisibility(View.VISIBLE);
        binding.viewAllRecentJobs.setVisibility(View.VISIBLE);
        binding.jobsRecyclerView.setVisibility(View.GONE); // Hide search results
        // Optionally, clear focus from SearchView
        binding.searchEditText.clearFocus();
    }

    private void searchJobs(String query) {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        ApiClient.getApiService().getJobs().enqueue(new Callback<JobsListResponse>() {
            @Override
            public void onResponse(@NonNull Call<JobsListResponse> call, 
                          @NonNull Response<JobsListResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    JobsListResponse jobsResponse = response.body();
                    if (jobsResponse.isSuccess() && jobsResponse.getData() != null && 
                        jobsResponse.getData().getJobs() != null) {
                        List<Job> jobs = jobsResponse.getData().getJobs();
                        List<Job> filteredJobs = filterJobsByQuery(jobs, query);
                        displaySearchResults(filteredJobs);
                    } else {
                        showEmptySearchResults();
                    }
                } else {
                    showEmptySearchResults();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JobsListResponse> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                showEmptySearchResults();
            }
        });
    }

    private List<Job> filterJobsByQuery(List<Job> jobs, String query) {
        List<Job> filteredJobs = new ArrayList<>();
        String lowercaseQuery = query.toLowerCase();
        
        for (Job job : jobs) {
            if (job.getTitle().toLowerCase().contains(lowercaseQuery) || 
                job.getCompany().toLowerCase().contains(lowercaseQuery) ||
                job.getLocation().toLowerCase().contains(lowercaseQuery) ||
                job.getDescription().toLowerCase().contains(lowercaseQuery) ||
                job.getCategory().toLowerCase().contains(lowercaseQuery)) {
                filteredJobs.add(job);
            }
        }
        
        return filteredJobs;
    }

    private void displaySearchResults(List<Job> jobs) {
        if (jobs.isEmpty()) {
            showEmptySearchResults();
        } else {
            // Hide other sections
            binding.featuredJobsViewPager.setVisibility(View.GONE);
            binding.jobCategoryRecyclerView.setVisibility(View.GONE);
            binding.recentJobsRecyclerView.setVisibility(View.GONE);
            binding.viewAllRecentJobs.setVisibility(View.GONE);
            
            // Show search results
            binding.jobsRecyclerView.setVisibility(View.VISIBLE);
            jobAdapter.submitList(jobs);
        }
    }

    private void showEmptySearchResults() {
        // Show empty state
        binding.featuredJobsViewPager.setVisibility(View.GONE);
        binding.jobCategoryRecyclerView.setVisibility(View.GONE);
        binding.recentJobsRecyclerView.setVisibility(View.GONE);
        binding.viewAllRecentJobs.setVisibility(View.GONE);
        binding.jobsRecyclerView.setVisibility(View.GONE);
        
        // Show empty state message
        Toast.makeText(requireContext(), "No jobs found matching your search", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Navigate to the JobsFragment with the selected category as a filter
     * @param categoryName The name of the category to filter jobs by
     */
    private void navigateToJobsByCategory(String categoryName) {
        // Show toast notification with selected category
        Toast.makeText(requireContext(), "Showing " + categoryName + " jobs", Toast.LENGTH_SHORT).show();
        
        // Create a bundle with the category parameter
        Bundle bundle = new Bundle();
        bundle.putString("category", categoryName.toLowerCase());
        
        // Create the JobsFragment and set the arguments
        Fragment jobsFragment = new com.example.jobportal.ui.jobs.JobsFragment();
        jobsFragment.setArguments(bundle);
        
        // Navigate to the JobsFragment
        requireActivity().getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, jobsFragment)
            .addToBackStack(null)
            .commit();
    }

    /**
     * Test method to debug the featured jobs API endpoint
     */
    private void testFeaturedJobsEndpoint() {
        Log.d(TAG, "Testing featured jobs API endpoint...");
        
        // Make a simple HTTP request to test the endpoint
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL("https://emps.co.in/api/featured-jobs");
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                
                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Direct API test - Response code: " + responseCode);
                
                java.io.InputStream inputStream = responseCode >= 200 && responseCode < 300 
                    ? connection.getInputStream() 
                    : connection.getErrorStream();
                
                if (inputStream != null) {
                    java.util.Scanner scanner = new java.util.Scanner(inputStream).useDelimiter("\\A");
                    String response = scanner.hasNext() ? scanner.next() : "";
                    Log.d(TAG, "Direct API test - Response body: " + response);
                    
                    if (response.trim().startsWith("<")) {
                        Log.e(TAG, "FOUND THE ISSUE: Server is returning HTML instead of JSON!");
                    }
                }
                
                connection.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Direct API test failed", e);
            }
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();  
        binding = null;
    }
}