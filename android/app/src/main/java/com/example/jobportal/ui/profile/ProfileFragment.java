package com.example.jobportal.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.jobportal.R;
import com.example.jobportal.auth.LoginActivity;
import com.example.jobportal.auth.ProfileActivity;
import com.example.jobportal.models.User;
import com.example.jobportal.network.ApiCallback;
import com.example.jobportal.network.ApiClient;
import com.example.jobportal.network.ApiResponse;
import com.example.jobportal.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    
    private TextView tvName, tvEmail, tvMobile, tvLocation;
    private View progressBar;
    private Button editProfileButton;
    private Button logoutButton;
    
    private ApiClient apiClient;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                            @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize API client and session manager
        apiClient = ApiClient.getInstance(requireContext());
        sessionManager = SessionManager.getInstance(requireContext());

        // Initialize views
        tvName = view.findViewById(R.id.name_text);
        tvEmail = view.findViewById(R.id.email_text);
        tvMobile = view.findViewById(R.id.phone_text);
        tvLocation = view.findViewById(R.id.location_text);
        progressBar = view.findViewById(R.id.progress_profile);
        editProfileButton = view.findViewById(R.id.edit_profile_button);
        logoutButton = view.findViewById(R.id.logout_button);
        
        // Set up listeners
        editProfileButton.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), ProfileActivity.class));
        });
        
        // Set click listener for logout button
        logoutButton.setOnClickListener(v -> handleLogout());
        
        // Load profile data
        loadProfileData();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Reload profile data each time the fragment is shown
        loadProfileData();
    }
    
    private void loadProfileData() {
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            // Redirect to login if not logged in
            navigateToLogin();
            return;
        }
        
        // Show loading indicator
        progressBar.setVisibility(View.VISIBLE);
        
        // Fetch profile data from API
        apiClient.getUserProfile(new ApiCallback<ApiResponse<User>>() {
            @Override
            public void onSuccess(ApiResponse<User> response) {
                // Hide loading indicator
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccess() && response.getData() != null) {
                    User user = response.getData();
                    
                    // Debug logs to check mobile field
                    Log.d(TAG, "User data received from API - Name: " + user.getFullName());
                    Log.d(TAG, "User data received from API - Email: " + user.getEmail());
                    Log.d(TAG, "User data received from API - Mobile: " + user.getPhone());
                    
                    // Update UI with user data
                    tvName.setText(user.getFullName());
                    tvEmail.setText(user.getEmail());
                    
                    // Check if phone number is null or empty
                    String phoneNumber = user.getPhone();
                    if (phoneNumber != null && !phoneNumber.isEmpty()) {
                        tvMobile.setText(phoneNumber);
                        tvMobile.setVisibility(View.VISIBLE);
                        Log.d(TAG, "Setting mobile text to: " + phoneNumber);
                    } else {
                        tvMobile.setText("Not provided");
                        tvMobile.setVisibility(View.VISIBLE);
                        Log.d(TAG, "Mobile field is null or empty, set to 'Not provided'");
                    }
                    
                    // For location, use a default value since we don't have it
                    tvLocation.setText("India");
                    tvLocation.setVisibility(View.VISIBLE);
                    
                    // Save user data to session
                    sessionManager.saveUser(user);
                } else {
                    // Show error and fallback to saved user data
                    showError("Failed to load profile. Using saved data.");
                    loadSavedUserData();
                }
            }

            @Override
            public void onError(String errorMessage) {
                // Hide loading indicator
                progressBar.setVisibility(View.GONE);
                
                // Show error and fallback to saved user data
                showError("Error: " + errorMessage);
                loadSavedUserData();
            }
        });
    }
    
    private void loadSavedUserData() {
        // Try to load user data from session manager
        User user = sessionManager.getUser();
        if (user != null) {
            tvName.setText(user.getFullName());
            tvEmail.setText(user.getEmail());
            
            // Check if phone number is null or empty
            String phoneNumber = user.getPhone();
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                tvMobile.setText(phoneNumber);
                tvMobile.setVisibility(View.VISIBLE);
            } else {
                tvMobile.setText("Not provided");
                tvMobile.setVisibility(View.VISIBLE);
            }
            
            // For location, use a default value
            tvLocation.setText("Location not specified");
            tvLocation.setVisibility(View.VISIBLE);
        } else {
            // If no saved data, show placeholder
            tvName.setText("No user data available");
            tvEmail.setText("");
            tvMobile.setText("");
            tvLocation.setText("");
        }
    }

    private void handleLogout() {
        // Clear session and logout
        sessionManager.logout();
        
        // Navigate to login screen
        navigateToLogin();
    }
    
    private void navigateToLogin() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
    
    private void showError(String message) {
        if (isAdded()) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            Log.e(TAG, message);
        }
    }
}