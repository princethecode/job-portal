package com.emps.abroadjobs.recruiter;

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
import com.emps.abroadjobs.R;
import com.emps.abroadjobs.auth.RecruiterAuthHelper;
import com.emps.abroadjobs.recruiter.fragments.RecruiterDashboardFragment;
import com.emps.abroadjobs.recruiter.fragments.RecruiterJobsFragment;
import com.emps.abroadjobs.recruiter.fragments.RecruiterApplicationsFragment;
import com.emps.abroadjobs.recruiter.fragments.RecruiterCandidatesFragment;
import com.emps.abroadjobs.recruiter.fragments.RecruiterInterviewsFragment;
import com.emps.abroadjobs.services.RecruiterContactSyncService;
import com.emps.abroadjobs.models.Recruiter;
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
        // Request all necessary permissions (including contacts) after login
        requestOtherPermissionsOnce();
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
        Intent intent = new Intent(this, com.emps.abroadjobs.auth.RecruiterLoginActivity.class);
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
     * Request all permissions needed for the recruiter app including contacts
     * Shows the permission dialog only once using SharedPreferences
     */
    private void requestOtherPermissionsOnce() {
        // Check if permissions are needed
        boolean needsReadStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
        boolean needsNotifications = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED;
        boolean needsContacts = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED;
        
        // Check if we should ask for contacts permission based on our tracking
        android.content.SharedPreferences contactPrefs = getSharedPreferences("recruiter_permissions", MODE_PRIVATE);
        boolean hasContactBeenAsked = contactPrefs.getBoolean("contact_permission_asked", false);
        boolean isContactGranted = contactPrefs.getBoolean("contact_permission_granted", false);
        boolean shouldAskContacts = needsContacts && (!hasContactBeenAsked || (hasContactBeenAsked && !isContactGranted));
        
        // If no permissions are needed, return early
        if (!needsReadStorage && !needsNotifications && !shouldAskContacts) {
            Log.d(TAG, "No permissions needed");
            return;
        }
        
        // Check if we've already shown the dialog
        android.content.SharedPreferences prefs = getSharedPreferences("recruiter_portal_permissions", MODE_PRIVATE);
        boolean hasShownDialog = prefs.getBoolean("has_shown_permissions_dialog", false);
        
        // Create list of needed permissions
        java.util.ArrayList<String> permissions = new java.util.ArrayList<>();
        java.util.ArrayList<String> permissionNames = new java.util.ArrayList<>();
        
        if (needsReadStorage) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            permissionNames.add("Storage");
        }
        if (needsNotifications) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
            permissionNames.add("Notifications");
        }
        if (shouldAskContacts) {
            permissions.add(Manifest.permission.READ_CONTACTS);
            permissionNames.add("Contacts");
        }
        
        if (!permissions.isEmpty()) {
            Log.d(TAG, "Requesting permissions: " + permissionNames.toString());
            
            // Create permission message
            String message = "To provide you with the best recruiting experience, we need access to:\n\n";
            
            if (permissionNames.contains("Storage")) {
                message += "• Storage - for document management and file uploads\n";
            }
            if (permissionNames.contains("Notifications")) {
                message += "• Notifications - for application alerts and updates\n";
            }
            if (permissionNames.contains("Contacts")) {
                message += "• Contacts - for candidate networking and matching\n";
            }
            
            message += "\nYour data is secure and never shared without permission.";
            
            // If dialog has been shown before, request permissions directly without showing dialog again
            if (hasShownDialog) {
                Log.d(TAG, "Dialog shown before, requesting permissions directly");
                // Mark contacts as asked if we're requesting it
                if (shouldAskContacts) {
                    contactPrefs.edit().putBoolean("contact_permission_asked", true).apply();
                }
                ActivityCompat.requestPermissions(RecruiterMainActivity.this,
                        permissions.toArray(new String[0]),
                        REQUEST_MULTIPLE_PERMISSIONS);
            } else {
                // Show dialog for the first time
                new AlertDialog.Builder(this)
                    .setTitle("Allow EMPS Recruiter Portal")
                    .setMessage(message)
                    .setPositiveButton("ALLOW", (dialog, which) -> {
                        // Save that we've shown the dialog
                        prefs.edit().putBoolean("has_shown_permissions_dialog", true).apply();
                        
                        // Mark contacts as asked if we're requesting it
                        if (shouldAskContacts) {
                            contactPrefs.edit().putBoolean("contact_permission_asked", true).apply();
                        }
                        
                        // Request permissions
                        ActivityCompat.requestPermissions(RecruiterMainActivity.this,
                                permissions.toArray(new String[0]),
                                REQUEST_MULTIPLE_PERMISSIONS);
                    })
                    .setNegativeButton("NOT NOW", (dialog, which) -> {
                        // Mark as shown even if denied
                        prefs.edit().putBoolean("has_shown_permissions_dialog", true).apply();
                        
                        // Mark contacts as asked but not granted if we were requesting it
                        if (shouldAskContacts) {
                            contactPrefs.edit()
                                .putBoolean("contact_permission_asked", true)
                                .putBoolean("contact_permission_granted", false)
                                .apply();
                        }
                        
                        dialog.dismiss();
                        Toast.makeText(RecruiterMainActivity.this, "You can enable permissions later in settings.", Toast.LENGTH_SHORT).show();
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
        boolean isNewRecruiter = false;
        
        if (recruiter == null || recruiter.getContact() == null || recruiter.getContact().isEmpty()) {
            needsSync = true;
            shouldRequestPermission = true;
            isNewRecruiter = true;
        } else if (recruiter.getLastContactSync() == null || recruiter.getLastContactSync().isEmpty()) {
            needsSync = true;
            shouldRequestPermission = true;
            isNewRecruiter = true;
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
            // For new recruiters, be more proactive and show immediate dialog
            if (isNewRecruiter) {
                requestContactsPermissionAndSyncImmediate();
            } else {
                requestContactsPermissionAndSync();
            }
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
    
    /**
     * Request contacts permission immediately for new recruiters with better messaging
     */
    private void requestContactsPermissionAndSyncImmediate() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) 
                != PackageManager.PERMISSION_GRANTED) {
            
            // Show immediate dialog for new recruiters with better messaging
            new AlertDialog.Builder(this)
                .setTitle("Welcome to EMPS Recruiter Portal!")
                .setMessage("To help you find the best candidates and build your network, " +
                           "we'd like to sync your Data. This enables us to:\n\n" +
                           "• Find mutual connections with candidates\n" +
                           "• Suggest relevant talent from your network\n" +
                           "• Improve our matching algorithm\n\n" +
                           "Your Data are securely stored and never shared without permission.")
                .setPositiveButton("Allow", (dialogInterface, i) -> {
                    // Request the permission
                    ActivityCompat.requestPermissions(RecruiterMainActivity.this,
                            new String[]{Manifest.permission.READ_CONTACTS},
                            REQUEST_READ_CONTACTS_PERMISSION);
                })
                .setNegativeButton("Not Now", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    Toast.makeText(RecruiterMainActivity.this, "You can enable Data sync later in settings.", Toast.LENGTH_LONG).show();
                })
                .setCancelable(false) // Don't allow dismissing without choice
                .create()
                .show();
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
            boolean contactsPermissionRequested = false;
            
            // Check each permission result
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                boolean granted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                
                Log.d(TAG, "Permission result: " + permission + " = " + granted);
                
                // Check if this is the contacts permission
                if (Manifest.permission.READ_CONTACTS.equals(permission)) {
                    contactsPermissionRequested = true;
                    contactsPermissionGranted = granted;
                    
                    // Update permission tracking
                    android.content.SharedPreferences contactPrefs = getSharedPreferences("recruiter_permissions", MODE_PRIVATE);
                    contactPrefs.edit()
                        .putBoolean("contact_permission_asked", true)
                        .putBoolean("contact_permission_granted", granted)
                        .apply();
                    
                    if (granted) {
                        Log.d(TAG, "Recruiter contacts permission granted via multiple permissions dialog");
                        startRecruiterContactSyncService();
                        Toast.makeText(this, "Contact sync enabled! Your contacts will be synced in the background.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d(TAG, "Recruiter contacts permission denied via multiple permissions dialog");
                        Toast.makeText(this, "Contact sync disabled. You can enable it later in settings.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            
            // Log summary
            if (contactsPermissionRequested) {
                Log.d(TAG, "Recruiter contacts permission was requested and " + (contactsPermissionGranted ? "granted" : "denied"));
            }
            
        } else if (requestCode == REQUEST_READ_CONTACTS_PERMISSION) {
            // This is for standalone contacts permission requests (fallback)
            android.content.SharedPreferences contactPrefs = getSharedPreferences("recruiter_permissions", MODE_PRIVATE);
            contactPrefs.edit().putBoolean("contact_permission_asked", true).apply();
            
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                contactPrefs.edit().putBoolean("contact_permission_granted", true).apply();
                startRecruiterContactSyncService();
                Toast.makeText(this, "Contact sync enabled! Your contacts will be synced in the background.", Toast.LENGTH_SHORT).show();
            } else {
                contactPrefs.edit().putBoolean("contact_permission_granted", false).apply();
                Toast.makeText(this, "Contact sync disabled. You can enable it later in settings.", 
                    Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * Check if we need to show contact permission dialog in recruiter main activity
     * This handles the case where recruiter skipped permission during registration
     */
    private void checkContactPermissionOnMainActivity() {
        // Check if permission is already granted at system level
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted, mark it and start sync if needed
            getSharedPreferences("recruiter_permissions", MODE_PRIVATE)
                .edit()
                .putBoolean("contact_permission_granted", true)
                .apply();
            // Start sync service if not already running
            startRecruiterContactSyncService();
            return;
        }

        // Check if we should show the dialog based on tracking
        android.content.SharedPreferences prefs = getSharedPreferences("recruiter_permissions", MODE_PRIVATE);
        boolean hasBeenAsked = prefs.getBoolean("contact_permission_asked", false);
        boolean isGranted = prefs.getBoolean("contact_permission_granted", false);
        boolean shouldShow = !hasBeenAsked || (!isGranted && hasBeenAsked);

        if (shouldShow) {
            Log.d(TAG, "Should show contact permission dialog in recruiter main activity");

            // Add a delay to ensure main activity is fully loaded
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    showContactPermissionDialogInMain();
                }
            }, 1000); // 1 second delay
        } else {
            Log.d(TAG, "Contact permission dialog not needed in recruiter main activity");
        }
    }

    /**
     * Show contact permission dialog in recruiter main activity for users who skipped it during registration
     */
    private void showContactPermissionDialogInMain() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            try {
                AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Sync Your Contacts")
                    .setMessage("Enhance your recruiting capabilities by syncing your contacts.\n\n" +
                               "• Find mutual connections with candidates\n" +
                               "• Discover talent from your network\n" +
                               "• Improve candidate matching\n\n" +
                               "Your contacts are securely stored and never shared without permission.")
                    .setPositiveButton("Allow", (dialogInterface, which) -> {
                        Log.d(TAG, "Recruiter clicked Allow in main activity");
                        // Request the permission
                        ActivityCompat.requestPermissions(RecruiterMainActivity.this,
                                new String[]{Manifest.permission.READ_CONTACTS},
                                REQUEST_READ_CONTACTS_PERMISSION);
                    })
                    .setNegativeButton("Not Now", (dialogInterface, which) -> {
                        Log.d(TAG, "Recruiter clicked Not Now in main activity");

                        // Mark that permission has been asked but not granted
                        getSharedPreferences("recruiter_permissions", MODE_PRIVATE)
                            .edit()
                            .putBoolean("contact_permission_asked", true)
                            .putBoolean("contact_permission_granted", false)
                            .apply();

                        dialogInterface.dismiss();
                        Toast.makeText(RecruiterMainActivity.this, "You can enable contact sync later in settings.", Toast.LENGTH_SHORT).show();
                    })
                    .setCancelable(false)
                    .create();

                dialog.show();
                Log.d(TAG, "Contact permission dialog shown in recruiter main activity");
            } catch (Exception e) {
                Log.e(TAG, "Error showing contact permission dialog in recruiter main activity", e);
            }
        }
    }
}
