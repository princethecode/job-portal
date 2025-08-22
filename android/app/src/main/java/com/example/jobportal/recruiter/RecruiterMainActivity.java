package com.example.jobportal.recruiter;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.jobportal.R;
import com.example.jobportal.auth.RecruiterAuthHelper;
import com.example.jobportal.recruiter.fragments.RecruiterDashboardFragment;
import com.example.jobportal.recruiter.fragments.RecruiterJobsFragment;
import com.example.jobportal.recruiter.fragments.RecruiterApplicationsFragment;
import com.example.jobportal.recruiter.fragments.RecruiterCandidatesFragment;
import com.example.jobportal.recruiter.fragments.RecruiterInterviewsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class RecruiterMainActivity extends AppCompatActivity {
    
    private BottomNavigationView bottomNavigationView;
    private RecruiterAuthHelper authHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recruiter_main);
        
        authHelper = RecruiterAuthHelper.getInstance(this);
        
        // Check if recruiter is logged in
        if (!authHelper.isLoggedIn() || !authHelper.hasValidToken()) {
            redirectToLogin();
            return;
        }
        
        initializeViews();
        setupBottomNavigation();
        
        // Set default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new RecruiterDashboardFragment())
                .commit();
        }
    }
    
    private void initializeViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }
    
    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                selectedFragment = new RecruiterDashboardFragment();
            } else if (itemId == R.id.nav_jobs) {
                selectedFragment = new RecruiterJobsFragment();
            } else if (itemId == R.id.nav_applications) {
                selectedFragment = new RecruiterApplicationsFragment();
            } else if (itemId == R.id.nav_interviews) {
                selectedFragment = new RecruiterInterviewsFragment();
            } else if (itemId == R.id.nav_profile) {
                // Navigate to profile activity instead of fragment
                Intent intent = new Intent(this, RecruiterProfileActivity.class);
                startActivity(intent);
                return true;
            }
            
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
                return true;
            }
            
            return false;
        });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.recruiter_main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_profile) {
            Intent intent = new Intent(this, RecruiterProfileActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_logout) {
            showLogoutConfirmation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes", (dialog, which) -> logout())
            .setNegativeButton("No", null)
            .show();
    }
    
    private void logout() {
        authHelper.logout();
        redirectToLogin();
    }
    
    private void redirectToLogin() {
        Intent intent = new Intent(this, com.example.jobportal.auth.RecruiterLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    public void onBackPressed() {
        // Handle back navigation between fragments if needed
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
