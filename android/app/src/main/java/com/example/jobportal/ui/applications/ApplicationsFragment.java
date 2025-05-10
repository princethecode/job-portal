package com.example.jobportal.ui.applications;

import android.os.Bundle;
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
import com.example.jobportal.R;
import com.example.jobportal.models.Application;
import com.example.jobportal.ui.applications.ApplicationAdapter.OnApplicationClickListener;

import java.util.ArrayList;
import java.util.List;

public class ApplicationsFragment extends Fragment {
    private RecyclerView recyclerView;
    private ApplicationAdapter applicationAdapter;
    private View progressBar;
    private View emptyView;
    private TextView emptyText;
    private ApplicationsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_applications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        recyclerView = view.findViewById(R.id.applications_recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyView = view.findViewById(R.id.empty_view);
        emptyText = view.findViewById(R.id.empty_text);
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Load applications
        loadApplications();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication()))
                .get(ApplicationsViewModel.class);
    }
    
    private void setupRecyclerView() {
        applicationAdapter = new ApplicationAdapter(application -> {
            // TODO: Navigate to application details or show options
            Toast.makeText(requireContext(), "Application: " + application.getJobTitle(), Toast.LENGTH_SHORT).show();
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(applicationAdapter);
    }
    
    private void loadApplications() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        
        // Observe applications from ViewModel
        viewModel.getApplications().observe(getViewLifecycleOwner(), applications -> {
            progressBar.setVisibility(View.GONE);
            
            if (applications == null || applications.isEmpty()) {
                showEmptyView("You haven't applied to any jobs yet");
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                applicationAdapter.setApplications(applications);
            }
        });
        
        // Observe loading state
        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                progressBar.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.GONE);
            }
        });
        
        // Observe error state
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                progressBar.setVisibility(View.GONE);
                showEmptyView("Error: " + error);
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showEmptyView(String message) {
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        emptyText.setText(message);
    }
}