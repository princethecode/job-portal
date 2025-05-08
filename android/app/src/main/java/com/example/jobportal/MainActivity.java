package com.example.jobportal;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.jobportal.auth.LoginActivity;
import com.example.jobportal.ui.profile.ProfileFragment;
import com.example.jobportal.ui.jobs.JobsFragment;
import com.example.jobportal.ui.applications.ApplicationsFragment;
import com.example.jobportal.ui.notifications.NotificationsFragment;
import com.example.jobportal.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    
    private SessionManager sessionManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize session manager
        sessionManager = SessionManager.getInstance(getApplicationContext());
        
        // Check if user is already logged in
        if (!sessionManager.isSessionValid()) {
            // User is not logged in, redirect to login activity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish(); // Close this activity
            return;
        }
        
        // User is logged in, continue with normal flow
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();
            if (id == R.id.navigation_profile) {
                selectedFragment = new ProfileFragment();
            } else if (id == R.id.navigation_jobs) {
                selectedFragment = new JobsFragment();
            } else if (id == R.id.navigation_applications) {
                selectedFragment = new ApplicationsFragment();
            } else if (id == R.id.navigation_notifications) {
                selectedFragment = new NotificationsFragment();
            } else if (id == R.id.navigation_home) {
                selectedFragment = new JobsFragment();
            }
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
            }
            return true;
        });

        // Set default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new JobsFragment())
                .commit();
        }
    }
}