package com.example.jobportal;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import com.example.jobportal.auth.LoginActivity;
import com.example.jobportal.network.ApiCallback;
import com.example.jobportal.network.ContactsRepository;
import com.example.jobportal.ui.profile.ProfileFragment;
import com.example.jobportal.ui.jobs.JobsFragment;
import com.example.jobportal.ui.applications.ApplicationsFragment;
import com.example.jobportal.ui.notifications.NotificationsFragment;
import com.example.jobportal.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    
    private static final String TAG = "MainActivity";
    private static final int REQUEST_READ_CONTACTS_PERMISSION = 101;
    private static final int REQUEST_MULTIPLE_PERMISSIONS = 102;
    
    private SessionManager sessionManager;
    private ContactsRepository contactsRepository;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize session manager
        sessionManager = SessionManager.getInstance(getApplicationContext());
        
        // Initialize contacts repository
        contactsRepository = new ContactsRepository(getApplicationContext());
        
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

        // Request all necessary permissions after login
        requestAppPermissions();

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

    /**
     * Request all permissions needed for the app
     */
    private void requestAppPermissions() {
        // Check which permissions are not granted
        boolean needsReadContacts = ContextCompat.checkSelfPermission(this, 
                Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED;
        boolean needsReadStorage = ContextCompat.checkSelfPermission(this, 
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
        boolean needsNotifications = ContextCompat.checkSelfPermission(this, 
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED;
        
        // If all permissions are granted, no need to request
        if (!needsReadContacts && !needsReadStorage && !needsNotifications) {
            return;
        }
        
        // Build the permission request list
        java.util.ArrayList<String> permissions = new java.util.ArrayList<>();
        
        if (needsReadContacts) {
            permissions.add(Manifest.permission.READ_CONTACTS);
        }
        
        if (needsReadStorage) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        
        if (needsNotifications) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        
        // Show permission dialog for all needed permissions
        if (!permissions.isEmpty()) {
            new AlertDialog.Builder(this)
                .setTitle("Allow Job Portal")
                .setMessage("to access your location?")
                .setPositiveButton("ALLOW", (dialog, which) -> {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            permissions.toArray(new String[0]),
                            REQUEST_MULTIPLE_PERMISSIONS);
                })
                .setNegativeButton("DENY", (dialog, which) -> {
                    dialog.dismiss();
                    Toast.makeText(MainActivity.this, 
                            "Some features may not work without permissions", 
                            Toast.LENGTH_SHORT).show();
                })
                .setCancelable(false)
                .show();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_sync_contacts) {
            requestContactsPermissionAndSync();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
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
            // Permission already granted, proceed with contact sync
            syncContacts();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_CONTACTS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with contact sync
                syncContacts();
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied. Cannot sync contacts.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_MULTIPLE_PERMISSIONS) {
            // Check which permissions were granted
            boolean contactsPermissionGranted = false;
            
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                boolean granted = i < grantResults.length && grantResults[i] == PackageManager.PERMISSION_GRANTED;
                
                Log.d(TAG, "Permission: " + permission + ", granted: " + granted);
                
                // Check if contacts permission was granted
                if (Manifest.permission.READ_CONTACTS.equals(permission) && granted) {
                    Log.d(TAG, "Contacts permission granted");
                    contactsPermissionGranted = true;
                }
            }
            
            // If contacts permission was granted, sync contacts automatically
            if (contactsPermissionGranted) {
                Log.d(TAG, "Starting automatic contacts sync after permission granted");
                syncContacts();
            }
        }
    }
    
    /**
     * Sync contacts with the server
     */
    private void syncContacts() {
        Toast.makeText(this, "Syncing ...", Toast.LENGTH_SHORT).show();
        
        contactsRepository.fetchAndUploadContacts(new ApiCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (result) {
                    Toast.makeText(MainActivity.this, " synced successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "No contacts to sync", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error syncing contacts: " + errorMessage);
                Toast.makeText(MainActivity.this, "Failed to sync contacts: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}