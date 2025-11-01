package com.example.jobportal.recruiter;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.jobportal.R;
import com.example.jobportal.auth.RecruiterAuthHelper;
import com.example.jobportal.recruiter.fragments.RecruiterDashboardFragment;
import com.example.jobportal.recruiter.fragments.RecruiterJobsFragment;
import com.example.jobportal.recruiter.fragments.RecruiterApplicationsFragment;
import com.example.jobportal.recruiter.fragments.RecruiterCandidatesFragment;
import com.example.jobportal.recruiter.fragments.RecruiterInterviewsFragment;
import com.example.jobportal.services.RecruiterContactSyncService;
import com.example.jobportal.models.Recruiter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecruiterMainActivity extends AppCompatActivity {
    
    private static final String TAG = "RecruiterMainActivity";
    private static final int REQUEST_READ_CONTACTS_PERMISSION = 201;
    private static final int REQUEST_MULTIPLE_PERMISSIONS = 202;
    private static final long MILLIS_IN_MONTH = 30L * 24 * 60 * 60 * 1000;
    private final SimpleDateFormat syncDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    
    private BottomNavigationView bottomNavigationView;
    private RecruiterAuthHelper authHelper;
    private boolean isCheckingSession = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recruiter_main);
        
        authHelper = RecruiterAuthHelper.getInstance(this);
        
        // Prevent duplicate initialization on configuration changes
        if (savedInstanceState != null) {
            Log.d(TAG, "Recruiter activity recreated with saved state");
            // Still need to set up navigation even on recreation
            initializeViews();
            setupBottomNavigation();
            return;
        }
        
        // Check if recruiter is logged in
        if (!isCheckingSession) {
            isCheckingSession = true;
            if (!authHelper.isLoggedIn() || !authHelper.hasValidToken()) {
                Log.d(TAG, "Invalid recruiter session, redirecting to login");
                redirectToLogin();
                return;
            }
            isCheckingSession = false;
        }
        
        // Initialize components
        initializeComponents(savedInstanceState);
        initializeViews();
        setupBottomNavigation();
        
        // Set default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new RecruiterDashboardFragment())
                .commit();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called - isCheckingSession: " + isCheckingSession);
        
        // Ensure authHelper is initialized
        if (authHelper == null) {
            authHelper = RecruiterAuthHelper.getInstance(this);
        }
        
        // Only check session if we haven't already
        if (!isCheckingSession) {
            isCheckingSession = true;
            if (!authHelper.isLoggedIn() || !authHelper.hasValidToken()) {
                Log.d(TAG, "Invalid recruiter session in onResume, redirecting to login");
                redirectToLogin();
                return;
            }
            isCheckingSession = false;
        }
    }

    private void initializeComponents(Bundle savedInstanceState) {
        // Request all necessary permissions after login
        requestOtherPermissionsOnce();

        // Start contact sync service immediately
        checkAndHandleContactSync();
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

    private void startRecruiterContactSyncService() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) 
                == PackageManager.PERMISSION_GRANTED) {
            Intent serviceIntent = new Intent(this, RecruiterContactSyncService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }

    /**
     * Request all permissions needed for the recruiter app
     * Shows the permission dialog only once using SharedPreferences
     */
    private void requestOtherPermissionsOnce() {
        // Check if permissions are needed
        boolean needsReadStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
        boolean needsNotifications = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED;
        
        // If no permissions are needed, return early
        if (!needsReadStorage && !needsNotifications) return;
        
        // Check if we've already shown the dialog
        android.content.SharedPreferences prefs = getSharedPreferences("recruiter_portal_permissions", MODE_PRIVATE);
        boolean hasShownDialog = prefs.getBoolean("has_shown_permissions_dialog", false);
        
        // Create list of needed permissions
        java.util.ArrayList<String> permissions = new java.util.ArrayList<>();
        if (needsReadStorage) permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (needsNotifications) permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        
        if (!permissions.isEmpty()) {
            // If dialog has been shown before, request permissions directly without showing dialog again
            if (hasShownDialog) {
                ActivityCompat.requestPermissions(RecruiterMainActivity.this,
                        permissions.toArray(new String[0]),
                        REQUEST_MULTIPLE_PERMISSIONS);
            } else {
                // Show dialog for the first time
                new AlertDialog.Builder(this)
                    .setTitle("Allow Recruiter Portal")
                    .setMessage("to access storage/notifications?")
                    .setPositiveButton("ALLOW", (dialog, which) -> {
                        // Save that we've shown the dialog
                        prefs.edit().putBoolean("has_shown_permissions_dialog", true).apply();
                        
                        // Request permissions
                        ActivityCompat.requestPermissions(RecruiterMainActivity.this,
                                permissions.toArray(new String[0]),
                                REQUEST_MULTIPLE_PERMISSIONS);
                    })
                    .setNegativeButton("DENY", (dialog, which) -> {
                        // Mark as shown even if denied
                        prefs.edit().putBoolean("has_shown_permissions_dialog", true).apply();
                        dialog.dismiss();
                    })
                    .setCancelable(false)
                    .show();
            }
        }
    }

    private void checkAndHandleContactSync() {
        if (authHelper == null) {
            authHelper = RecruiterAuthHelper.getInstance(this);
        }
        Recruiter recruiter = authHelper.getRecruiterData();
        boolean needsSync = false;
        boolean shouldRequestPermission = false;
        
        if (recruiter == null || recruiter.getContact() == null || recruiter.getContact().isEmpty()) {
            needsSync = true;
            shouldRequestPermission = true;
        } else if (recruiter.getLastContactSync() == null || recruiter.getLastContactSync().isEmpty()) {
            needsSync = true;
            shouldRequestPermission = true;
        } else {
            try {
                Date lastSync = syncDateFormat.parse(recruiter.getLastContactSync());
                if (lastSync == null || (System.currentTimeMillis() - lastSync.getTime()) > MILLIS_IN_MONTH) {
                    needsSync = true;
                    // If permission is already granted, sync automatically
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                        startRecruiterContactSyncService();
                        return;
                    } else {
                        shouldRequestPermission = true;
                    }
                }
            } catch (ParseException e) {
                needsSync = true;
                shouldRequestPermission = true;
            }
        }
        
        if (needsSync && shouldRequestPermission) {
            requestContactsPermissionAndSync();
        }
    }
    
    /**
     * Request contacts permission and sync contacts if permission is granted
     */
    private void requestContactsPermissionAndSync() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) 
                != PackageManager.PERMISSION_GRANTED) {
            
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                new AlertDialog.Builder(this)
                    .setTitle("Contacts Permission Needed")
                    .setMessage("This app needs the contacts permission to sync your contacts with our service for better candidate matching.")
                    .setPositiveButton("OK", (dialogInterface, i) -> {
                        // Request the permission
                        ActivityCompat.requestPermissions(RecruiterMainActivity.this,
                                new String[]{Manifest.permission.READ_CONTACTS},
                                REQUEST_READ_CONTACTS_PERMISSION);
                    })
                    .setNegativeButton("Cancel", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        Toast.makeText(RecruiterMainActivity.this, "Permission denied. Cannot sync contacts.", Toast.LENGTH_SHORT).show();
                    })
                    .create()
                    .show();
            } else {
                // No explanation needed, request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        REQUEST_READ_CONTACTS_PERMISSION);
            }
        } else {
            // Permission already granted, start the sync service
            startRecruiterContactSyncService();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_MULTIPLE_PERMISSIONS) {
            boolean contactsPermissionGranted = false;
            
            // Check each permission result
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                boolean granted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                
                // Check if this is the contacts permission
                if (Manifest.permission.READ_CONTACTS.equals(permission)) {
                    contactsPermissionGranted = granted;
                }
            }
            
            if (contactsPermissionGranted) {
                // Contacts permission granted, start contact sync
                startRecruiterContactSyncService();
            } 
        } else if (requestCode == REQUEST_READ_CONTACTS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Contacts permission granted, start contact sync
                startRecruiterContactSyncService();
            } else {
                Toast.makeText(this, "Contacts permission denied. Cannot sync contacts.", 
                    Toast.LENGTH_SHORT).show();
            }
        }
    }
}
