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
                // Convert model.Application to fragment's Application objects
                List<Application> appList = new ArrayList<>();
                for (com.example.jobportal.models.Application app : applications) {
                    appList.add(new Application(
                        Integer.parseInt(app.getId()),
                        app.getJobTitle(),
                        app.getCompany(),
                        app.getApplicationDate(),
                        app.getStatus()
                    ));
                }
                applicationAdapter.submitList(appList);
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
        
        // Trigger loading
        viewModel.loadApplications();
    }
    
    private void showEmptyView(String message) {
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        emptyText.setText(message);
    }
    
    // Application model class (would typically be in a separate file)
    public static class Application {
        private int id;
        private String jobTitle;
        private String company;
        private String appliedDate;
        private String status;
        
        public Application(int id, String jobTitle, String company, String appliedDate, String status) {
            this.id = id;
            this.jobTitle = jobTitle;
            this.company = company;
            this.appliedDate = appliedDate;
            this.status = status;
        }
        
        // Getters
        public int getId() { return id; }
        public String getJobTitle() { return jobTitle; }
        public String getCompany() { return company; }
        public String getAppliedDate() { return appliedDate; }
        public String getStatus() { return status; }
    }
    
    // Adapter for applications (would typically be in a separate file)
    private static class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ApplicationViewHolder> {
        private List<Application> applications = new ArrayList<>();
        private final OnApplicationClickListener listener;
        
        public interface OnApplicationClickListener {
            void onApplicationClick(Application application);
        }
        
        public ApplicationAdapter(OnApplicationClickListener listener) {
            this.listener = listener;
        }
        
        public void submitList(List<Application> newApplications) {
            this.applications = newApplications;
            notifyDataSetChanged();
        }
        
        @NonNull
        @Override
        public ApplicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_application, parent, false);
            return new ApplicationViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ApplicationViewHolder holder, int position) {
            holder.bind(applications.get(position));
        }
        
        @Override
        public int getItemCount() {
            return applications.size();
        }
        
        class ApplicationViewHolder extends RecyclerView.ViewHolder {
            private final TextView jobTitleTextView;
            private final TextView companyTextView;
            private final TextView dateTextView;
            private final TextView statusTextView;
            
            public ApplicationViewHolder(@NonNull View itemView) {
                super(itemView);
                jobTitleTextView = itemView.findViewById(R.id.job_title);
                companyTextView = itemView.findViewById(R.id.company_name);
                dateTextView = itemView.findViewById(R.id.applied_date);
                statusTextView = itemView.findViewById(R.id.status);
                
                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onApplicationClick(applications.get(position));
                    }
                });
            }
            
            public void bind(Application application) {
                jobTitleTextView.setText(application.getJobTitle());
                companyTextView.setText(application.getCompany());
                dateTextView.setText(application.getAppliedDate());
                statusTextView.setText(application.getStatus());
                
                // Set status background based on status
                if ("Accepted".equals(application.getStatus())) {
                    statusTextView.setBackgroundResource(R.drawable.status_accepted_background);
                } else if ("Rejected".equals(application.getStatus())) {
                    statusTextView.setBackgroundResource(R.drawable.status_rejected_background);
                } else {
                    statusTextView.setBackgroundResource(R.drawable.status_pending_background);
                }
            }
        }
    }
}