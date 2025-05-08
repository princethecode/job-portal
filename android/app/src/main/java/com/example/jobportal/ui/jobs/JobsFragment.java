package com.example.jobportal.ui.jobs;

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
import com.example.jobportal.ui.jobdetails.JobDetailsFragment;

import com.example.jobportal.R;
// Update to use the correct Job class
import com.example.jobportal.models.Job;

public class JobsFragment extends Fragment implements JobAdapter.OnJobClickListener {

    private JobsViewModel viewModel;
    private JobAdapter jobAdapter;
    private RecyclerView recyclerView;
    private View progressBar;
    private TextView emptyView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize ViewModel first, before any view operations
        viewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication()))
                .get(JobsViewModel.class);
        
        // Add debug logging
        Log.d("JobsFragment", "ViewModel initialized in onCreate");
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_jobs, container, false);
        
        // Initialize views
        recyclerView = root.findViewById(R.id.jobs_recycler_view);
        TextView titleTextView = root.findViewById(R.id.text_jobs);
        titleTextView.setText("Available Jobs");
        
        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        jobAdapter = new JobAdapter(this);
        recyclerView.setAdapter(jobAdapter);
        
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        progressBar = view.findViewById(R.id.progress_bar);
        emptyView = view.findViewById(R.id.empty_view);
        
        // Add debug logging
        Log.d("JobsFragment", "onViewCreated called");
        
        // Observe ViewModel data
        viewModel.getJobs().observe(getViewLifecycleOwner(), jobs -> {
            Log.d("JobsFragment", "Jobs updated. Count: " + (jobs != null ? jobs.size() : 0));
            if (jobs != null && !jobs.isEmpty()) {
                Log.d("JobsFragment", "First job: " + jobs.get(0).getTitle());
                jobAdapter.submitList(jobs);
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            } else {
                Log.d("JobsFragment", "No jobs to display");
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            }
        });
        
        viewModel.getError().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
        
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        });
        
        // Call loadJobs() after setting up observers
        viewModel.refreshJobs();
    }
    
    @Override
    public void onJobClick(Job job) {
        // Add logging
        Log.d("JobsFragment", "Job clicked: ID=" + job.getId() + ", Title=" + job.getTitle());
        
        // Navigate to job details fragment
        JobDetailsFragment detailsFragment = JobDetailsFragment.newInstance(job.getId());
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detailsFragment)
                .addToBackStack(null)
                .commit();
    }
}