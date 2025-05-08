package com.example.jobportal.ui.home;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
// Change this import to use the network package
import com.example.jobportal.models.JobsListResponse;
import com.example.jobportal.network.ApiClient;

import com.example.jobportal.models.Job;
import com.example.jobportal.databinding.FragmentHomeBinding;
import com.example.jobportal.network.ApiResponse;
import com.example.jobportal.ui.jobs.JobAdapter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private JobAdapter jobAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                            @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        
        // Check if we have internet connectivity first
        if (isNetworkAvailable()) {
            // Try to ping the server directly to check DNS
            new Thread(() -> {
                try {
                    boolean reachable = isHostReachable("emps.co.in", 5000);
                    requireActivity().runOnUiThread(() -> {
                        if (reachable) {
                            fetchJobs();
                        } else {
                            binding.progressBar.setVisibility(View.GONE);
                            Toast.makeText(requireContext(), 
                                "Unable to reach server domain: emps.co.in. DNS resolution failed.", 
                                Toast.LENGTH_LONG).show();
                            
                            // Show mock data anyway
                            showMockData();
                        }
                    });
                } catch (Exception e) {
                    requireActivity().runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), 
                            "Network check error: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                        
                        // Show mock data anyway
                        showMockData();
                    });
                }
            }).start();
        } else {
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(requireContext(), 
                "No internet connection available. Please check your connectivity.", 
                Toast.LENGTH_LONG).show();
            
            // Show mock data anyway
            showMockData();
        }
    }

    private void setupRecyclerView() {
        jobAdapter = new JobAdapter(job -> {
            // TODO: Navigate to job details
        });
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}