package com.example.jobportal.recruiter.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobportal.R;
import com.example.jobportal.models.InterviewResponse;
import com.example.jobportal.network.ApiClient;
import com.example.jobportal.recruiter.adapters.InterviewsAdapter;
import retrofit2.Call;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class RecruiterInterviewsFragment extends Fragment {
    
    private RecyclerView rvInterviews;
    private TextView tvInterviewsCount;
    private View progressBar;
    private InterviewsAdapter interviewsAdapter;
    private List<InterviewResponse.Interview> interviewsList = new ArrayList<>();
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recruiter_interviews, container, false);
        
        initializeViews(view);
        setupRecyclerView();
        loadInterviews();
        
        return view;
    }
    
    private void initializeViews(View view) {
        // Use the correct view IDs from the layout
        rvInterviews = view.findViewById(R.id.rv_today_interviews);
        
        tvInterviewsCount = view.findViewById(R.id.tv_total_interviews);
        
        progressBar = view.findViewById(R.id.ll_loading);
        if (progressBar == null) {
            // Try alternative ID
            progressBar = view.findViewById(R.id.progress_bar);
        }
    }
    
    private void setupRecyclerView() {
        // Initialize adapter with click listener
        interviewsAdapter = new InterviewsAdapter(interviewsList, interview -> {
            // Handle interview click - could open details or edit dialog
            // For now, just show a toast with interview info
            if (getContext() != null) {
                String message = "Interview with " + interview.getUser().getName() + 
                               " for " + interview.getJob().getTitle();
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
        
        rvInterviews.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvInterviews.setAdapter(interviewsAdapter);
    }
    
    private void loadInterviews() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        ApiClient.getRecruiterApiService().getInterviews(null, null)
            .enqueue(new retrofit2.Callback<InterviewResponse>() {
            @Override
            public void onResponse(Call<InterviewResponse> call, Response<InterviewResponse> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    InterviewResponse interviewResponse = response.body();
                    if (interviewResponse.isSuccess() && interviewResponse.getData() != null) {
                        List<InterviewResponse.Interview> interviews = interviewResponse.getData().getInterviews();
                        if (interviews != null) {
                            interviewsList.clear();
                            interviewsList.addAll(interviews);
                            updateInterviewsCount(interviews.size());
                            
                            // Update adapter
                            if (interviewsAdapter != null) {
                                interviewsAdapter.updateInterviews(interviewsList);
                            }
                        }
                    } else {
                        showError("Failed to load interviews: " + interviewResponse.getMessage());
                    }
                } else {
                    showError("Failed to load interviews");
                }
            }
            
            @Override
            public void onFailure(Call<InterviewResponse> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                showError("Network error: " + t.getMessage());
            }
        });
    }
    
    private void updateInterviewsCount(int count) {
        if (tvInterviewsCount != null) {
            tvInterviewsCount.setText("Interviews (" + count + ")");
        }
    }
    
    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
