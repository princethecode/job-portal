package com.example.jobportal.ui.application;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.jobportal.R;
import com.example.jobportal.models.User;
import com.example.jobportal.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Map;

public class JobApplicationPersonalInfoFragment extends Fragment {

    private TextInputEditText fullNameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText phoneEditText;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_job_application_personal_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        fullNameEditText = view.findViewById(R.id.et_full_name);
        emailEditText = view.findViewById(R.id.et_email);
        phoneEditText = view.findViewById(R.id.et_phone);

        // Get current user data
        SessionManager sessionManager = SessionManager.getInstance(requireContext());
        currentUser = sessionManager.getUser();

        // Pre-populate fields with user data if available
        if (currentUser != null) {
            fullNameEditText.setText(currentUser.getFullName());
            emailEditText.setText(currentUser.getEmail());
            phoneEditText.setText(currentUser.getPhone());
        }
    }

    public boolean validateAndSaveData(Map<String, String> applicationData) {
        // Validate inputs
        String fullName = fullNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        if (fullName.isEmpty()) {
            fullNameEditText.setError("Full name is required");
            return false;
        }

        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            return false;
        }

        if (phone.isEmpty()) {
            phoneEditText.setError("Phone number is required");
            return false;
        }

        // Save data to application map
        applicationData.put("full_name", fullName);
        applicationData.put("email", email);
        applicationData.put("phone", phone);

        return true;
    }
}
