package com.example.jobportal.ui.application;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobportal.R;
import com.example.jobportal.models.Experience;
import com.example.jobportal.ui.experience.ExperienceAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobApplicationReviewFragment extends Fragment {
    private static final String TAG = "JobAppReviewFragment";

    private TextView fullNameValue;
    private TextView emailValue;
    private TextView phoneValue;
    private TextView currentCompanyValue;
    private TextView departmentValue;
    private TextView currentSalaryValue;
    private TextView expectedSalaryValue;
    private TextView totalExperienceValue;
    private TextView joiningPeriodValue;
    private TextView skillsValue;
    private TextView resumeValue;
    private View personalInfoEdit;
    private View employmentDetailsEdit;
    private View experienceDetailsEdit;
    private Button submitApplicationButton;
    private RecyclerView experienceReviewRecyclerView;
    private TextView noExperienceText;
    private ExperienceAdapter experienceAdapter;
    private Map<String, String> applicationData;
    private String jobId;
    private String jobTitle;
    private String company;
    private List<Experience> experiences = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_job_application_review, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        fullNameValue = view.findViewById(R.id.full_name_value);
        emailValue = view.findViewById(R.id.email_value);
        phoneValue = view.findViewById(R.id.phone_value);
        currentCompanyValue = view.findViewById(R.id.current_company_value);
        departmentValue = view.findViewById(R.id.department_value);
        currentSalaryValue = view.findViewById(R.id.current_salary_value);
        expectedSalaryValue = view.findViewById(R.id.expected_salary_value);
        totalExperienceValue = view.findViewById(R.id.total_experience_value);
        joiningPeriodValue = view.findViewById(R.id.joining_period_value);
        skillsValue = view.findViewById(R.id.skills_value);
        resumeValue = view.findViewById(R.id.resume_value);
        personalInfoEdit = view.findViewById(R.id.personal_info_edit);
        employmentDetailsEdit = view.findViewById(R.id.employment_details_edit);
/*
        experienceDetailsEdit = view.findViewById(R.id.experience_details_edit);
*/
        submitApplicationButton = view.findViewById(R.id.submit_application_button);
        /*experienceReviewRecyclerView = view.findViewById(R.id.experience_review_recycler_view);
        noExperienceText = view.findViewById(R.id.no_experience_text);*/

        // Set up RecyclerView
        experienceReviewRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        experienceAdapter = new ExperienceAdapter(null);
        experienceReviewRecyclerView.setAdapter(experienceAdapter);

        // Get data from arguments
        Bundle args = getArguments();
        if (args != null) {
            applicationData = (HashMap<String, String>) args.getSerializable("application_data");
            jobId = args.getString("job_id");
            jobTitle = args.getString("job_title");
            company = args.getString("company");
            
            // Populate UI with application data
            populateReviewUI();
        }

        // Set up edit button listeners
        personalInfoEdit.setOnClickListener(v -> navigateToStep(1));
        employmentDetailsEdit.setOnClickListener(v -> navigateToStep(2));
        experienceDetailsEdit.setOnClickListener(v -> navigateToStep(3));

        // Set up submit button
        submitApplicationButton.setOnClickListener(v -> {
            if (getParentFragment() instanceof JobApplicationNewFragment) {
                ((JobApplicationNewFragment) getParentFragment()).navigateToNextStep();
            }
        });
    }

    private void populateReviewUI() {
        if (applicationData != null) {
            // Personal information
            fullNameValue.setText(applicationData.get("full_name"));
            emailValue.setText(applicationData.get("email"));
            phoneValue.setText(applicationData.get("phone"));

            // Employment details
            currentCompanyValue.setText(applicationData.get("current_company"));
            departmentValue.setText(applicationData.get("department"));
            currentSalaryValue.setText(applicationData.get("current_salary"));
            expectedSalaryValue.setText(applicationData.get("expected_salary"));
            totalExperienceValue.setText(applicationData.get("experience_display"));
            joiningPeriodValue.setText(applicationData.get("joining_period") + " days");
            skillsValue.setText(applicationData.get("skills"));
            
            // Resume
            String resumeName = applicationData.get("resume_name");
            if (resumeName != null && !resumeName.isEmpty()) {
                resumeValue.setText(resumeName);
            } else {
                resumeValue.setText("No resume uploaded");
            }
            
            // Experience data
            String experienceCountStr = applicationData.get("experience_count");
            if (experienceCountStr != null && !experienceCountStr.isEmpty()) {
                int experienceCount = Integer.parseInt(experienceCountStr);
                if (experienceCount > 0) {
                    // Create experience objects from application data
                    List<Experience> experienceList = new ArrayList<>();
                    for (int i = 0; i < experienceCount; i++) {
                        Experience exp = new Experience();
                        exp.setJobTitle(applicationData.get("experience_" + i + "_title"));
                        exp.setCompanyName(applicationData.get("experience_" + i + "_company"));
                        
                        // The period is stored as a combined string, but we'll set it to startDate for display
                        String period = applicationData.get("experience_" + i + "_period");
                        exp.setStartDate(period != null ? period : "");
                        
                        experienceList.add(exp);
                    }
                    
                    // Update the UI with the experience list
                    updateExperiencesList(experienceList);
                } else {
                    // No experiences
                    experienceReviewRecyclerView.setVisibility(View.GONE);
                    noExperienceText.setVisibility(View.VISIBLE);
                }
            } else {
                // No experience count data
                experienceReviewRecyclerView.setVisibility(View.GONE);
                noExperienceText.setVisibility(View.VISIBLE);
            }
        }
    }

    private void navigateToStep(int step) {
        if (getParentFragment() instanceof JobApplicationNewFragment) {
            JobApplicationNewFragment parent = (JobApplicationNewFragment) getParentFragment();
            // Update current step in parent fragment and load appropriate fragment
            parent.currentStep = step;
            parent.updateStepUI(step);
            parent.loadStepFragment(step);
        }
    }

    public void updateExperiencesList(List<Experience> experiences) {
        this.experiences = experiences;
        
        if (experiences != null && !experiences.isEmpty()) {
            experienceAdapter.submitList(experiences);
            experienceReviewRecyclerView.setVisibility(View.VISIBLE);
            noExperienceText.setVisibility(View.GONE);
        } else {
            experienceReviewRecyclerView.setVisibility(View.GONE);
            noExperienceText.setVisibility(View.VISIBLE);
        }
    }
}
