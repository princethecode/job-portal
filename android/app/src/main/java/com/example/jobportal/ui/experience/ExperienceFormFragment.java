package com.example.jobportal.ui.experience;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.jobportal.R;
import com.example.jobportal.models.Experience;
import com.example.jobportal.ui.experience.ExperienceViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ExperienceFormFragment extends Fragment {

    private ExperienceViewModel viewModel;
    private TextView formTitle;
    private TextInputEditText etJobTitle, etCompanyName, etStartDate, etEndDate, etDescription;
    private TextInputLayout endDateLayout;
    private CheckBox cbCurrentJob;
    private Button btnSaveExperience, btnDeleteExperience;
    private ProgressBar progressBar;
    
    private Experience currentExperience;
    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM yyyy", Locale.US);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_experience_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        formTitle = view.findViewById(R.id.formTitle);
        etJobTitle = view.findViewById(R.id.etJobTitle);
        etCompanyName = view.findViewById(R.id.etCompanyName);
        etStartDate = view.findViewById(R.id.etStartDate);
        etEndDate = view.findViewById(R.id.etEndDate);
        etDescription = view.findViewById(R.id.etDescription);
        cbCurrentJob = view.findViewById(R.id.cbCurrentJob);
        endDateLayout = view.findViewById(R.id.endDateLayout);
        btnSaveExperience = view.findViewById(R.id.btnSaveExperience);
        btnDeleteExperience = view.findViewById(R.id.btnDeleteExperience);
        progressBar = view.findViewById(R.id.progressBar);
        Toolbar toolbar = view.findViewById(R.id.toolbar);

        // Set up toolbar navigation
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        // Set up ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(ExperienceViewModel.class);

        // Set up date pickers
        setupDatePickers();

        // Set up current job checkbox
        cbCurrentJob.setOnCheckedChangeListener((buttonView, isChecked) -> {
            endDateLayout.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            if (isChecked) {
                etEndDate.setText("");
            }
        });

        // Set up save button
        btnSaveExperience.setOnClickListener(v -> saveExperience());

        // Set up delete button
        btnDeleteExperience.setOnClickListener(v -> deleteExperience());

        // Observe data changes
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), this::updateLoadingState);
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), this::showError);

        // Get experience ID from arguments if editing
        Bundle args = getArguments();
        if (args != null && args.containsKey("experience_id")) {
            long experienceId = args.getLong("experience_id");
            viewModel.getExperienceById(experienceId).observe(getViewLifecycleOwner(), this::populateForm);
        } else {
            // Creating a new experience
            formTitle.setText("Add Experience");
            btnDeleteExperience.setVisibility(View.GONE);
        }
    }

    private void setupDatePickers() {
        // Start date picker
        etStartDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        etStartDate.setText(dateFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    1
            );
            datePickerDialog.show();
        });

        // End date picker
        etEndDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        etEndDate.setText(dateFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    1
            );
            datePickerDialog.show();
        });
    }

    private void populateForm(Experience experience) {
        if (experience != null) {
            currentExperience = experience;
            formTitle.setText("Edit Experience");
            etJobTitle.setText(experience.getJobTitle());
            etCompanyName.setText(experience.getCompanyName());
            etStartDate.setText(experience.getStartDate());
            
            boolean isCurrent = experience.isCurrent();
            cbCurrentJob.setChecked(isCurrent);
            endDateLayout.setVisibility(isCurrent ? View.GONE : View.VISIBLE);
            
            if (!isCurrent && experience.getEndDate() != null) {
                etEndDate.setText(experience.getEndDate());
            }
            
            etDescription.setText(experience.getDescription());
            btnDeleteExperience.setVisibility(View.VISIBLE);
        }
    }

    private void saveExperience() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        // Create or update experience
        Experience experience = currentExperience != null ?
                currentExperience : new Experience();

        experience.setJobTitle(etJobTitle.getText().toString().trim());
        experience.setCompanyName(etCompanyName.getText().toString().trim());
        experience.setStartDate(etStartDate.getText().toString().trim());
        experience.setCurrent(cbCurrentJob.isChecked());
        
        if (!cbCurrentJob.isChecked()) {
            experience.setEndDate(etEndDate.getText().toString().trim());
        } else {
            experience.setEndDate(null);
        }
        
        experience.setDescription(etDescription.getText().toString().trim());

        // Save to repository
        viewModel.saveExperience(experience);
        
        // Navigate back
        requireActivity().onBackPressed();
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validate job title
        if (etJobTitle.getText().toString().trim().isEmpty()) {
            etJobTitle.setError("Job title is required");
            isValid = false;
        }

        // Validate company name
        if (etCompanyName.getText().toString().trim().isEmpty()) {
            etCompanyName.setError("Company name is required");
            isValid = false;
        }

        // Validate start date
        if (etStartDate.getText().toString().trim().isEmpty()) {
            etStartDate.setError("Start date is required");
            isValid = false;
        }

        // Validate end date if not current job
        if (!cbCurrentJob.isChecked() && etEndDate.getText().toString().trim().isEmpty()) {
            etEndDate.setError("End date is required");
            isValid = false;
        }

        return isValid;
    }

    private void deleteExperience() {
        if (currentExperience != null) {
            viewModel.deleteExperience(currentExperience);
            requireActivity().onBackPressed();
        }
    }

    private void updateLoadingState(Boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSaveExperience.setEnabled(!isLoading);
        btnDeleteExperience.setEnabled(!isLoading);
    }

    private void showError(String errorMessage) {
        if (errorMessage != null && !errorMessage.isEmpty()) {
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
        }
    }
}
