package com.example.jobportal.ui.profile;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.jobportal.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {
    private TextView tvName, tvEmail, tvMobile, tvLocation;
    private View progressBar;
    private Button editProfileButton;
    private Button logoutButton;

    private FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                            @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

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
    
    private void loadProfileData() {
        // In a real app, fetch user data from API or local storage
        // For now, we'll use dummy data
        tvName.setText("John Doe");
        tvEmail.setText("john.doe@example.com");
        tvMobile.setText("+1 123-456-7890");
        tvLocation.setText("New York, NY");
    }

    private void handleLogout() {
        // Get SessionManager instance
        SessionManager sessionManager = SessionManager.getInstance(requireContext().getApplicationContext());
        
        // Clear session and logout
        sessionManager.logout();
        
        // Navigate to login screen
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}