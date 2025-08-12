package com.example.jobportal.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobportal.R;
import com.example.jobportal.models.JobsListResponse;
import com.example.jobportal.network.ApiClient;
import com.example.jobportal.models.Job;
import com.example.jobportal.ui.jobs.JobAdapter;
import com.example.jobportal.ui.jobdetails.JobDetailsFragment;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment implements JobAdapter.OnJobClickListener {
    private RecyclerView recyclerView;
    private JobAdapter jobAdapter;
    private SearchView searchView;
    private View progressBar;
    private View emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        recyclerView = view.findViewById(R.id.search_recycler_view);
        searchView = view.findViewById(R.id.search_view);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyView = view.findViewById(R.id.empty_view);
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Setup SearchView
        setupSearchView();
        
        // Set focus to search view and show keyboard
        searchView.setIconified(false);
        searchView.requestFocus();
    }
    
    private void setupRecyclerView() {
        jobAdapter = new JobAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(jobAdapter);
    }
    
    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
                    showEmptyView();
                }
                return true;
            }
        });
    }
    
    private void searchJobs(String query) {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        
        ApiClient.getApiService().getJobs().enqueue(new Callback<JobsListResponse>() {
            @Override
            public void onResponse(@NonNull Call<JobsListResponse> call, 
                          @NonNull Response<JobsListResponse> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    JobsListResponse jobsResponse = response.body();
                    if (jobsResponse.isSuccess() && jobsResponse.getData() != null && 
                        jobsResponse.getData().getJobs() != null) {
                        List<Job> jobs = jobsResponse.getData().getJobs();
                        List<Job> filteredJobs = filterJobsByQuery(jobs, query);
                        displayJobs(filteredJobs);
                    } else {
                        showEmptyView();
                    }
                } else {
                    showEmptyView();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JobsListResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                showEmptyView();
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
    
    private void displayJobs(List<Job> jobs) {
        if (jobs.isEmpty()) {
            showEmptyView();
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            jobAdapter.submitList(jobs);
        }
    }
    
    private void showEmptyView() {
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onJobClick(Job job) {
        // Navigate to job details fragment
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, JobDetailsFragment.newInstance(job.getId()))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onJobDetailsClick(Job job) {
        Log.d("SearchFragment", "Job Details button clicked: ID=" + job.getId() + ", Title=" + job.getTitle());
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, JobDetailsFragment.newInstance(job.getId()))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onWhatsAppShareClick(Job job) {
        Log.d("SearchFragment", "WhatsApp share clicked for job: " + job.getTitle());
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
            Log.e("SearchFragment", "Error sharing job", e);
        }
    }
}