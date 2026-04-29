package com.emps.abroadjobs;

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
import android.widget.ImageView;

import com.emps.abroadjobs.auth.LoginActivity;
import com.emps.abroadjobs.network.ApiCallback;
import com.emps.abroadjobs.network.ContactsRepository;
import com.emps.abroadjobs.ui.home.HomeFragment;
import com.emps.abroadjobs.ui.profile.ProfileFragment;
import com.emps.abroadjobs.ui.jobs.JobsFragment;
import com.emps.abroadjobs.ui.applications.ApplicationsFragment;
import com.emps.abroadjobs.ui.notifications.NotificationsFragment;
import com.emps.abroadjobs.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.emps.abroadjobs.services.ContactSyncService;
import com.emps.abroadjobs.models.User;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {
    
    private static final String TAG = "MainActivity";
    private static final int REQUEST_READ_CONTACTS_PERMISSION = 101;
    private static final int REQUEST_MULTIPLE_PERMISSIONS = 102;
    private static final long MILLIS_IN_MONTH = 30L * 24 * 60 * 60 * 1000;
    private final SimpleDateFormat syncDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    
    private SessionManager sessionManager;
    private ContactsRepository contactsRepository;
    private boolean isCheckingSession = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);
        
        // Always initialize sessionManager
        sessionManager = SessionManager.getInstance(getApplicationContext());
        
        // Prevent duplicate initialization on configuration changes
        if (savedInstanceState != null) {
            Log.d(TAG, "Activity recreated with saved state");
            // Still need to set up toolbar and navigation even on recreation
            setupToolbar();
            setupBottomNavigation();
            return;
        }
        
        // Check session validity
        if (!isCheckingSession) {
            isCheckingSession = true;
            
            // Check if coming from registration
            boolean fromRegistration = getIntent().getBooleanExtra("from_registration", false);
            
            if (fromRegistration) {
                Log.d(TAG, "Coming from registration, allowing entry and verifying session");
                // Give a moment for session data to be fully saved
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    if (!sessionManager.isSessionValid()) {
                        Log.w(TAG, "Session still invalid after registration, but allowing entry");
                        // Don't redirect to login if coming from registration
                        // The user just registered, so we should let them in
                    }
                }, 100);
            } else if (!sessionManager.isSessionValid()) {
                Log.d(TAG, "Invalid session in MainActivity, redirecting to LoginActivity");
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return;
            }
            isCheckingSession = false;
        }

        // Initialize components
        initializeComponents(savedInstanceState);
        setupToolbar();
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called - isCheckingSession: " + isCheckingSession);
        
        // Ensure sessionManager is initialized
        if (sessionManager == null) {
            sessionManager = SessionManager.getInstance(getApplicationContext());
        }
        
        // Only check session if we haven't already and not coming from registration
        boolean fromRegistration = getIntent().getBooleanExtra("from_registration", false);
        if (!isCheckingSession && !fromRegistration) {
            isCheckingSession = true;
            if (!sessionManager.isSessionValid()) {
                Log.d(TAG, "Invalid session in onResume, redirecting to LoginActivity");
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return;
            }
            isCheckingSession = false;
        } else if (fromRegistration) {
            Log.d(TAG, "onResume: Coming from registration, skipping session validation");
        }
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);

        // Set click listeners for toolbar icons
        ImageView notificationIcon = toolbar.findViewById(R.id.notificationIcon);
        ImageView settingsIcon = toolbar.findViewById(R.id.settingsIcon);

        notificationIcon.setOnClickListener(v -> {
            // Navigate to notifications fragment
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new NotificationsFragment())
                .addToBackStack(null)
                .commit();
        });

        settingsIcon.setOnClickListener(v -> {
            // Navigate to settings fragment
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new com.emps.abroadjobs.ui.settings.SettingsFragment())
                .addToBackStack(null)
                .commit();
        });
        
        // Debug: Long click on toolbar to test contact permission
        toolbar.setOnLongClickListener(v -> {
            Log.d(TAG, "Toolbar long clicked - showing debug options");
            new AlertDialog.Builder(this)
                .setTitle("Debug Options")
                .setMessage("Choose a debug action:")
                .setPositiveButton("Test Contact Dialog", (dialog, which) -> testContactPermissionDialog())
                .setNegativeButton("Reset & Test", (dialog, which) -> resetContactPermissionForTesting())
                .setNeutralButton("Cancel", null)
                .show();
            return true;
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "Saving activity state");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "Restoring activity state");
    }

    private void initializeComponents(Bundle savedInstanceState) {
        // Initialize contacts repository
        contactsRepository = new ContactsRepository(getApplicationContext());

        // Request all necessary permissions (including contacts) after login
        requestOtherPermissionsOnce();

        // Set default fragment only if this is a fresh start
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
        }
    }
    
    private void setupBottomNavigation() {
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
                selectedFragment = new HomeFragment();
            }
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
            }
            return true;
        });
    }

    private void startContactSyncService() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) 
                == PackageManager.PERMISSION_GRANTED) {
            Intent serviceIntent = new Intent(this, ContactSyncService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }

    /**
     * Request all permissions needed for the app including contacts
     * Shows the permission dialog only once using SharedPreferences
     */
    private void requestOtherPermissionsOnce() {
        // Check if permissions are needed
        boolean needsReadStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
        boolean needsNotifications = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED;
        boolean needsContacts = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED;
        
        // Check if we should ask for contacts permission based on our tracking
        boolean shouldAskContacts = needsContacts && sessionManager.shouldShowContactPermissionDialog();
        
        // If no permissions are needed, return early
        if (!needsReadStorage && !needsNotifications && !shouldAskContacts) {
            Log.d(TAG, "No permissions needed");
            return;
        }
        
        // Check if we've already shown the dialog
        android.content.SharedPreferences prefs = getSharedPreferences("job_portal_permissions", MODE_PRIVATE);
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
            String permissionList = String.join(", ", permissionNames);
            String message = "To provide you with the best experience, we need access to:\n\n";
            
            if (permissionNames.contains("Storage")) {
                message += "• Storage - for resume uploads and file management\n";
            }
            if (permissionNames.contains("Notifications")) {
                message += "• Notifications - for job alerts and updates\n";
            }
            if (permissionNames.contains("Contacts")) {
                message += "• Contacts - for networking and job matching\n";
            }
            
            message += "\nYour data is secure and never shared without permission.";
            
            // If dialog has been shown before, request permissions directly without showing dialog again
            if (hasShownDialog) {
                Log.d(TAG, "Dialog shown before, requesting permissions directly");
                // Mark contacts as asked if we're requesting it
                if (shouldAskContacts) {
                    sessionManager.setContactPermissionAsked(true);
                }
                ActivityCompat.requestPermissions(MainActivity.this,
                        permissions.toArray(new String[0]),
                        REQUEST_MULTIPLE_PERMISSIONS);
            } else {
                // Show dialog for the first time
                new AlertDialog.Builder(this)
                    .setTitle("Allow EMPS Jobs")
                    .setMessage(message)
                    .setPositiveButton("ALLOW", (dialog, which) -> {
                        // Save that we've shown the dialog
                        prefs.edit().putBoolean("has_shown_permissions_dialog", true).apply();
                        
                        // Mark contacts as asked if we're requesting it
                        if (shouldAskContacts) {
                            sessionManager.setContactPermissionAsked(true);
                        }
                        
                        // Request permissions
                        ActivityCompat.requestPermissions(MainActivity.this,
                                permissions.toArray(new String[0]),
                                REQUEST_MULTIPLE_PERMISSIONS);
                    })
                    .setNegativeButton("NOT NOW", (dialog, which) -> {
                        // Mark as shown even if denied
                        prefs.edit().putBoolean("has_shown_permissions_dialog", true).apply();
                        
                        // Mark contacts as asked but not granted if we were requesting it
                        if (shouldAskContacts) {
                            sessionManager.setContactPermissionAsked(true);
                            sessionManager.setContactPermissionGranted(false);
                        }
                        
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, "You can enable permissions later in settings.", Toast.LENGTH_SHORT).show();
                    })
                    .setCancelable(false)
                    .show();
            }
        }
    }

    private void checkAndHandleContactSync() {
        if (sessionManager == null) {
            sessionManager = SessionManager.getInstance(getApplicationContext());
        }
        User user = sessionManager.getUser();
        boolean needsSync = false;
        boolean shouldRequestPermission = false;
        boolean isNewUser = false;
        
        if (user == null || user.getContact() == null || user.getContact().isEmpty()) {
            needsSync = true;
            shouldRequestPermission = true;
            isNewUser = true;
        } else if (user.getLastContactSync() == null || user.getLastContactSync().isEmpty()) {
            needsSync = true;
            shouldRequestPermission = true;
            isNewUser = true;
        } else {
            try {
                Date lastSync = syncDateFormat.parse(user.getLastContactSync());
                if (lastSync == null || (System.currentTimeMillis() - lastSync.getTime()) > MILLIS_IN_MONTH) {
                    needsSync = true;
                    // If permission is already granted, sync automatically
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                        startContactSyncService();
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
            // For new users, be more proactive and show immediate dialog
            if (isNewUser) {
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
                    .setMessage("This app needs the contacts permission to sync your contacts with our service.")
                    .setPositiveButton("OK", (dialogInterface, i) -> {
                        // Request the permission
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.READ_CONTACTS},
                                REQUEST_READ_CONTACTS_PERMISSION);
                    })
                    .setNegativeButton("Cancel", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        Toast.makeText(MainActivity.this, "Permission denied. Cannot sync contacts.", Toast.LENGTH_SHORT).show();
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
            startContactSyncService();
        }
    }
    
    /**
     * Request contacts permission immediately for new users with better messaging
     */
    private void requestContactsPermissionAndSyncImmediate() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) 
                != PackageManager.PERMISSION_GRANTED) {
            
            // Show immediate dialog for new users with better messaging
            new AlertDialog.Builder(this)
                .setTitle("Welcome to EMPS Jobs!")
                .setMessage("To provide you with better job matching and networking opportunities, " +
                           "we'd like to sync your contacts. This helps us find mutual connections " +
                           "and suggest relevant opportunities.\n\nYour contacts are securely stored " +
                           "and never shared without permission.")
                .setPositiveButton("Allow", (dialogInterface, i) -> {
                    // Request the permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_CONTACTS},
                            REQUEST_READ_CONTACTS_PERMISSION);
                })
                .setNegativeButton("Not Now", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    Toast.makeText(MainActivity.this, "You can enable contact sync later in settings.", Toast.LENGTH_LONG).show();
                })
                .setCancelable(false) // Don't allow dismissing without choice
                .create()
                .show();
        } else {
            // Permission already granted, start the sync service
            startContactSyncService();
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
                    
                    // Update session tracking
                    sessionManager.setContactPermissionAsked(true);
                    sessionManager.setContactPermissionGranted(granted);
                    
                    if (granted) {
                        Log.d(TAG, "Contacts permission granted via multiple permissions dialog");
                        startContactSyncService();
                        Toast.makeText(this, "Contact sync enabled! Your contacts will be synced in the background.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d(TAG, "Contacts permission denied via multiple permissions dialog");
                        Toast.makeText(this, "Contact sync disabled. You can enable it later in settings.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            
            // Log summary
            if (contactsPermissionRequested) {
                Log.d(TAG, "Contacts permission was requested and " + (contactsPermissionGranted ? "granted" : "denied"));
            }
            
        } else if (requestCode == REQUEST_READ_CONTACTS_PERMISSION) {
            // This is for standalone contacts permission requests (fallback)
            sessionManager.setContactPermissionAsked(true);
            
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sessionManager.setContactPermissionGranted(true);
                startContactSyncService();
                Toast.makeText(this, "Contact sync enabled! Your contacts will be synced in the background.", Toast.LENGTH_SHORT).show();
            } else {
                sessionManager.setContactPermissionGranted(false);
                Toast.makeText(this, "Contact sync disabled. You can enable it later in settings.", 
                    Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * Check if we need to show contact permission dialog in main activity
     * This handles the case where user skipped permission during registration
     */
    private void checkContactPermissionOnMainActivity() {
        // Check if this is the very first launch after registration
        boolean isFirstLaunchAfterRegistration = getSharedPreferences("app_state", MODE_PRIVATE)
                .getBoolean("first_launch_after_registration", false);
        
        if (isFirstLaunchAfterRegistration) {
            Log.d(TAG, "First launch after registration, clearing flag and skipping contact permission check this time");
            // Clear the flag so subsequent launches will check permission
            getSharedPreferences("app_state", MODE_PRIVATE)
                .edit()
                .remove("first_launch_after_registration")
                .apply();
            return;
        }
        
        // Check if permission is already granted at system level
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) 
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted, mark it in session and start sync if needed
            sessionManager.setContactPermissionGranted(true);
            // Start sync service if not already running
            startContactSyncService();
            return;
        }
        
        // Check if we should show the dialog based on session tracking
        if (sessionManager.shouldShowContactPermissionDialog()) {
            Log.d(TAG, "Should show contact permission dialog in main activity");
            
            // Add a delay to ensure main activity is fully loaded
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    showContactPermissionDialogInMain();
                }
            }, 1000); // 1 second delay
        } else {
            Log.d(TAG, "Contact permission dialog not needed in main activity - " +
                      "hasBeenAsked: " + sessionManager.hasContactPermissionBeenAsked() + 
                      ", isGranted: " + sessionManager.isContactPermissionGranted());
        }
    }

    /**
     * Show contact permission dialog in main activity for users who skipped it during registration
     */
    private void showContactPermissionDialogInMain() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            try {
                AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Sync Your Contacts")
                    .setMessage("Help us find better job matches and networking opportunities by syncing your contacts.\n\n" +
                               "• Find mutual connections\n" +
                               "• Get personalized job recommendations\n" +
                               "• Expand your professional network\n\n" +
                               "Your contacts are securely stored and never shared without permission.")
                    .setPositiveButton("Allow", (dialogInterface, which) -> {
                        Log.d(TAG, "User clicked Allow in main activity");
                        // Request the permission
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.READ_CONTACTS},
                                REQUEST_READ_CONTACTS_PERMISSION);
                    })
                    .setNegativeButton("Not Now", (dialogInterface, which) -> {
                        Log.d(TAG, "User clicked Not Now in main activity");

                        // Mark that permission has been asked but not granted
                        sessionManager.setContactPermissionAsked(true);
                        sessionManager.setContactPermissionGranted(false);

                        dialogInterface.dismiss();
                        Toast.makeText(MainActivity.this, "You can enable contact sync later in settings.", Toast.LENGTH_SHORT).show();
                    })
                    .setCancelable(false)
                    .create();

                dialog.show();
                Log.d(TAG, "Contact permission dialog shown in main activity");
            } catch (Exception e) {
                Log.e(TAG, "Error showing contact permission dialog in main activity", e);
            }
        }
    }


    /**
     * Debug method to test contact permission dialog - can be called from anywhere for testing
     */
    public void testContactPermissionDialog() {
        Log.d(TAG, "=== TESTING CONTACT PERMISSION DIALOG ===");
        Log.d(TAG, "System permission granted: " + (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED));
        Log.d(TAG, "Session manager exists: " + (sessionManager != null));

        if (sessionManager != null) {
            Log.d(TAG, "Should show dialog: " + sessionManager.shouldShowContactPermissionDialog());
            Log.d(TAG, "Has been asked: " + sessionManager.hasContactPermissionBeenAsked());
            Log.d(TAG, "Is granted: " + sessionManager.isContactPermissionGranted());

            // Force show the dialog for testing
            showContactPermissionDialogInMain();
        }
    }

    /**
     * Debug method to reset contact permission tracking for testing
     */
    public void resetContactPermissionForTesting() {
        Log.d(TAG, "=== RESETTING CONTACT PERMISSION FOR TESTING ===");
        if (sessionManager != null) {
            sessionManager.resetContactPermissionTracking();
            Log.d(TAG, "Contact permission tracking reset");
        }

        // Also clear the first launch flag
        getSharedPreferences("app_state", MODE_PRIVATE)
            .edit()
            .remove("first_launch_after_registration")
            .apply();
        Log.d(TAG, "First launch flag cleared");

        // Now check if dialog should show
        checkContactPermissionOnMainActivity();
    }
}