package com.example.jobportal.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.jobportal.R;
import com.example.jobportal.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {
    private EditText etName, etEmail, etMobile;
    private Button saveButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views with correct IDs from layout
        etName = findViewById(R.id.nameInput);
        etEmail = findViewById(R.id.emailInput);
        etMobile = findViewById(R.id.mobileInput);
        saveButton = findViewById(R.id.saveChangesButton);

        // Load current profile data (dummy for now)
        loadProfileData();

        saveButton.setOnClickListener(v -> saveProfile());
    
        // Initialize the logout button
        Button logoutButton = findViewById(R.id.logoutButton);
        
        // Set click listener for logout button
        logoutButton.setOnClickListener(v -> handleLogout());
    }

    private void loadProfileData() {
        // In a real app, fetch user data from API or local storage
        // For now, we'll use dummy data
        etName.setText("John Doe");
        etEmail.setText("john.doe@example.com");
        etMobile.setText("+1 123-456-7890");
    }

    private void saveProfile() {
        // In a real app, validate and save the data to API or local storage
        // For now, just show a toast
        Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void handleLogout() {
        // Get SessionManager instance using singleton pattern
        SessionManager sessionManager = SessionManager.getInstance(getApplicationContext());
        
        // Clear session and logout
        sessionManager.logout();
        
        // Navigate to login screen
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}