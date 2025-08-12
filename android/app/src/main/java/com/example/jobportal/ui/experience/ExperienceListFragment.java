package com.example.jobportal.ui.experience;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobportal.ui.experience.ExperienceAdapter;
import com.example.jobportal.R;
import com.example.jobportal.models.Experience;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class ExperienceListFragment extends Fragment implements ExperienceAdapter.OnExperienceClickListener {

    private ExperienceViewModel viewModel;
    private RecyclerView recyclerView;
    private ExperienceAdapter adapter;
    private ProgressBar progressBar;
    private LinearLayout emptyStateView;
    private FloatingActionButton fabAddExperience;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_experience_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        recyclerView = view.findViewById(R.id.experienceRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateView = view.findViewById(R.id.emptyStateView);
        fabAddExperience = view.findViewById(R.id.fabAddExperience);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ExperienceAdapter(this);
        recyclerView.setAdapter(adapter);

        // Set up ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(ExperienceViewModel.class);

        // Observe data changes
        viewModel.getUserExperiences().observe(getViewLifecycleOwner(), this::updateExperienceList);
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), this::updateLoadingState);
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), this::showError);

        // Set up FAB
        fabAddExperience.setOnClickListener(v -> navigateToExperienceForm(null));

        // Load data
        viewModel.syncExperiences();
    }

    private void updateExperienceList(List<Experience> experiences) {
        if (experiences != null && !experiences.isEmpty()) {
            adapter.submitList(experiences);
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
        }
    }

    private void updateLoadingState(Boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void showError(String errorMessage) {
        if (errorMessage != null && !errorMessage.isEmpty()) {
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onExperienceClick(Experience experience) {
        navigateToExperienceForm(experience);
    }

    @Override
    public void onEditClick(Experience experience) {
        navigateToExperienceForm(experience);
    }
    
    /**
     * Navigate to the Experience Form Fragment to add or edit an experience
     * @param experience The experience to edit, or null if adding a new experience
     */
    private void navigateToExperienceForm(Experience experience) {
        try {
            // Create a new instance of the experience form fragment
            ExperienceFormFragment formFragment = new ExperienceFormFragment();
            
            // If editing an existing experience, pass the ID as an argument
            if (experience != null) {
                Bundle args = new Bundle();
                args.putLong("experience_id", experience.getId());
                formFragment.setArguments(args);
            }
            
            // Navigate to the experience form fragment
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, formFragment)
                    .addToBackStack(null)
                    .commit();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Unable to open experience form", Toast.LENGTH_SHORT).show();
        }
    }
}
