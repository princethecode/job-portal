package com.example.jobportal;

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

import com.example.jobportal.auth.LoginActivity;
import com.example.jobportal.network.ApiCallback;
import com.example.jobportal.network.ContactsRepository;
import com.example.jobportal.ui.home.HomeFragment;
import com.example.jobportal.ui.profile.ProfileFragment;
import com.example.jobportal.ui.jobs.JobsFragment;
import com.example.jobportal.ui.applications.ApplicationsFragment;
import com.example.jobportal.ui.notifications.NotificationsFragment;
import com.example.jobportal.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.jobportal.services.ContactSyncService;
import com.example.jobportal.models.User;
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
            if (!sessionManager.isSessionValid()) {
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
        
        // Only check session if we haven't already
        if (!isCheckingSession) {
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
                .replace(R.id.fragment_container, new com.example.jobportal.ui.settings.SettingsFragment())
                .addToBackStack(null)
                .commit();
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

        // Request all necessary permissions after login
        requestOtherPermissionsOnce();

        // Start contact sync service with 1 minute delay
        new android.os.Handler().postDelayed(this::checkAndHandleContactSync, 60000);

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
     * Request all permissions needed for the app
     * Shows the permission dialog only once using SharedPreferences
     */
    private void requestOtherPermissionsOnce() {
        // Check if permissions are needed
        boolean needsReadStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
        boolean needsNotifications = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED;
        
        // If no permissions are needed, return early
        if (!needsReadStorage && !needsNotifications) return;
        
        // Check if we've already shown the dialog
        android.content.SharedPreferences prefs = getSharedPreferences("job_portal_permissions", MODE_PRIVATE);
        boolean hasShownDialog = prefs.getBoolean("has_shown_permissions_dialog", false);
        
        // Create list of needed permissions
        java.util.ArrayList<String> permissions = new java.util.ArrayList<>();
        if (needsReadStorage) permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (needsNotifications) permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        
        if (!permissions.isEmpty()) {
            // If dialog has been shown before, request permissions directly without showing dialog again
            if (hasShownDialog) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        permissions.toArray(new String[0]),
                        REQUEST_MULTIPLE_PERMISSIONS);
            } else {
                // Show dialog for the first time
                new AlertDialog.Builder(this)
                    .setTitle("Allow Job Portal")
                    .setMessage("to access storage/notifications?")
                    .setPositiveButton("ALLOW", (dialog, which) -> {
                        // Save that we've shown the dialog
                        prefs.edit().putBoolean("has_shown_permissions_dialog", true).apply();
                        
                        // Request permissions
                        ActivityCompat.requestPermissions(MainActivity.this,
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
        if (sessionManager == null) {
            sessionManager = SessionManager.getInstance(getApplicationContext());
        }
        User user = sessionManager.getUser();
        boolean needsSync = false;
        boolean shouldRequestPermission = false;
        if (user == null || user.getContact() == null || user.getContact().isEmpty()) {
            needsSync = true;
            shouldRequestPermission = true;
        } else if (user.getLastContactSync() == null || user.getLastContactSync().isEmpty()) {
            needsSync = true;
            shouldRequestPermission = true;
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
                startContactSyncService();
            } 
        } else if (requestCode == REQUEST_READ_CONTACTS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Contacts permission granted, start contact sync
                startContactSyncService();
            } else {
                Toast.makeText(this, "Contacts permission denied. Cannot sync contacts.", 
                    Toast.LENGTH_SHORT).show();
            }
        }
    }
}