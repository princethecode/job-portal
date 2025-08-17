package com.example.jobportal.ui.jobs;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import com.example.jobportal.ui.jobdetails.JobDetailsFragment;
import com.example.jobportal.R;
import com.example.jobportal.models.Job;
import com.example.jobportal.databinding.FragmentJobsBinding;
import com.example.jobportal.network.ApiCallback;
import java.util.ArrayList;
import java.util.List;

public class JobsFragment extends Fragment implements JobAdapter.OnJobClickListener {

    private JobsViewModel viewModel;
    private JobAdapter jobAdapter;
    private List<Job> allJobs = new ArrayList<>();
    private FragmentJobsBinding binding;
    private String categoryFilter = null; // To store category filter if provided

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication()))
                .get(JobsViewModel.class);
        
        // Check if we have a category filter from arguments
        if (getArguments() != null && getArguments().containsKey("category")) {
            categoryFilter = getArguments().getString("category");
            Log.d("JobsFragment", "Category filter received: " + categoryFilter);
        }
        
        Log.d("JobsFragment", "ViewModel initialized in onCreate");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentJobsBinding.inflate(inflater, container, false);
        // Setup RecyclerView
        binding.jobsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        jobAdapter = new JobAdapter(this);
        binding.jobsRecyclerView.setAdapter(jobAdapter);
        // Setup SearchView
        setupSearchView();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Set the title based on category if available
        if (categoryFilter != null && !categoryFilter.isEmpty()) {
            // Capitalize the first letter of category for display
            String displayCategory = categoryFilter.substring(0, 1).toUpperCase() + categoryFilter.substring(1);
            binding.textJobs.setText(displayCategory + " Jobs");
        }
        
        // Configure observers based on whether we have a category filter
        if (categoryFilter != null && !categoryFilter.isEmpty()) {
            // Observe jobs filtered by category
            viewModel.getJobsByCategory(categoryFilter).observe(getViewLifecycleOwner(), jobs -> {
                if (jobs != null && !jobs.isEmpty()) {
                    allJobs = new ArrayList<>(jobs); // Keep a local copy for filtering
                    jobAdapter.submitList(jobs);
                    binding.jobsRecyclerView.setVisibility(View.VISIBLE);
                    binding.emptyView.setVisibility(View.GONE);
                } else {
                    binding.jobsRecyclerView.setVisibility(View.GONE);
                    binding.emptyView.setVisibility(View.VISIBLE);
                }
            });
        } else {
            // Observe all jobs if no category filter
            viewModel.getJobs().observe(getViewLifecycleOwner(), jobs -> {
                if (jobs != null && !jobs.isEmpty()) {
                    allJobs = new ArrayList<>(jobs); // Keep a local copy for filtering
                    jobAdapter.submitList(jobs);
                    binding.jobsRecyclerView.setVisibility(View.VISIBLE);
                    binding.emptyView.setVisibility(View.GONE);
                } else {
                    binding.jobsRecyclerView.setVisibility(View.GONE);
                    binding.emptyView.setVisibility(View.VISIBLE);
                }
            });
        }
        
        // Error handling
        viewModel.getError().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
        
        // Loading indicator
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                binding.progressBar.setVisibility(View.VISIBLE);
            } else {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
        
        // Only refresh jobs if not filtering by category (category filtering already refreshes)
        if (categoryFilter == null) {
            viewModel.refreshJobs();
        }
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
                    showAllJobs();
                }
                return true;
            }
        });
        binding.searchEditText.setOnCloseListener(() -> {
            showAllJobs();
            return false;
        });
    }

    private void searchJobs(String query) {
        List<Job> filtered = filterJobsByQuery(allJobs, query);
        jobAdapter.submitList(filtered);
        if (filtered.isEmpty()) {
            binding.jobsRecyclerView.setVisibility(View.GONE);
            binding.emptyView.setVisibility(View.VISIBLE);
        } else {
            binding.jobsRecyclerView.setVisibility(View.VISIBLE);
            binding.emptyView.setVisibility(View.GONE);
        }
    }

    private void showAllJobs() {
        jobAdapter.submitList(allJobs);
        binding.jobsRecyclerView.setVisibility(View.VISIBLE);
        binding.emptyView.setVisibility(allJobs.isEmpty() ? View.VISIBLE : View.GONE);
        binding.searchEditText.clearFocus();
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

    @Override
    public void onJobClick(Job job) {
        Log.d("JobsFragment", "Job clicked: ID=" + job.getId() + ", Title=" + job.getTitle());
        JobDetailsFragment detailsFragment = JobDetailsFragment.newInstance(job.getId());
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detailsFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onJobDetailsClick(Job job) {
        Log.d("JobsFragment", "Job Details button clicked: ID=" + job.getId() + ", Title=" + job.getTitle());
        JobDetailsFragment detailsFragment = JobDetailsFragment.newInstance(job.getId());
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detailsFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onWhatsAppShareClick(Job job) {
        Log.d("JobsFragment", "WhatsApp share clicked for job: " + job.getTitle() + " (ID: " + job.getId() + ")");
        shareJobOnWhatsApp(job);
    }

    private void shareJobOnWhatsApp(Job job) {
        Log.d("JobsFragment", "📱 Starting WhatsApp share for job: " + job.getTitle() + " (ID: " + job.getId() + ")");
        
        // Increment share count immediately when user clicks share
        incrementShareCount(job);
        
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
                Log.d("JobsFragment", "✅ WhatsApp intent started successfully");
            } catch (android.content.ActivityNotFoundException ex) {
                Log.d("JobsFragment", "⚠️ WhatsApp not found, trying general share");
                // WhatsApp not installed, try with general share
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Job Opportunity: " + job.getTitle());
                
                startActivity(Intent.createChooser(shareIntent, "Share Job"));
                Log.d("JobsFragment", "✅ General share intent started successfully");
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Unable to share job", Toast.LENGTH_SHORT).show();
            Log.e("JobsFragment", "❌ Error sharing job: " + e.getMessage(), e);
        }
    }

    private void incrementShareCount(Job job) {
        Log.d("JobsFragment", "�  INCREMENT SHARE COUNT CALLED for job: " + job.getTitle() + " (ID: " + job.getId() + ")");
        

        // Simple direct API call with share_count parameter
        Thread apiThread = new Thread(() -> {
            Log.d("JobsFragment", "🧵 API Thread started");
            try {
                String apiUrl = "https://emps.co.in/api/jobs/" + job.getId() + "/share?share_count=1";
                Log.d("JobsFragment", "🌐 Calling API: " + apiUrl);
                
                java.net.URL url = new java.net.URL(apiUrl);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setConnectTimeout(10000); // 10 seconds
                connection.setReadTimeout(10000); // 10 seconds
                
                Log.d("JobsFragment", "📡 Making HTTP request...");
                int responseCode = connection.getResponseCode();
                Log.d("JobsFragment", "📡 API Response Code: " + responseCode);
                
                if (responseCode == 200) {
                    // Success - show toast on main thread
                    requireActivity().runOnUiThread(() -> {
                        Log.d("JobsFragment", "✅ Share count incremented successfully!");
                    });
                } else {
                    // Error - show error on main thread
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Share count failed ❌ Code: " + responseCode, Toast.LENGTH_LONG).show();
                        Log.e("JobsFragment", "❌ API call failed with code: " + responseCode);
                    });
                }
                
                connection.disconnect();
                Log.d("JobsFragment", "🔌 Connection closed");
                
            } catch (Exception e) {
                Log.e("JobsFragment", "💥 Error calling share API: " + e.getMessage(), e);
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
        
        apiThread.start();
        Log.d("JobsFragment", "🧵 API Thread started successfully");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}