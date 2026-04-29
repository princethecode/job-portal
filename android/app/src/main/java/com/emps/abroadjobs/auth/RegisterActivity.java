package com.emps.abroadjobs.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.emps.abroadjobs.R;
import com.emps.abroadjobs.MainActivity;
import com.emps.abroadjobs.network.ApiResponse;
import com.emps.abroadjobs.network.ApiCallback;
import com.emps.abroadjobs.services.ContactSyncService;
import com.emps.abroadjobs.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.emps.abroadjobs.databinding.ActivityRegisterBinding;
import com.emps.abroadjobs.models.User;
import com.emps.abroadjobs.network.ApiClient;
import com.google.firebase.auth.FirebaseUser;
public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilFullName, tilEmail, tilMobile, tilPassword, tilConfirmPassword;
    private TextInputEditText etFullName, etEmail, etMobile, etPassword, etConfirmPassword;
    private Button btnUploadResume, btnRegister, btnGoogleSignIn;
    private TextView tvResumeStatus, tvLogin;
    private TextView recruiterRegisterLink;
    
    private Uri resumeUri = null;
    
    // Firebase Auth Helper
    private FirebaseAuthHelper firebaseAuthHelper;
    
    // Activity result launcher for resume file picking
    private final ActivityResultLauncher<String[]> resumePicker = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    // Store URI for later use - no need for persistent permission for this demo
                    resumeUri = uri;
                    String fileName = getFileNameFromUri(uri);
                    tvResumeStatus.setText(fileName);
                    tvResumeStatus.setTextColor(getResources().getColor(R.color.success, null));
                }
            }
    );
    
    // Activity result launcher for Google Sign-In
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    firebaseAuthHelper.handleGoogleSignInResult(result.getData(), new FirebaseAuthHelper.AuthCallback() {
                        @Override
                        public void onSuccess(FirebaseUser user) {
                            handleGoogleSignInSuccess(user);
                        }

                        @Override
                        public void onError(String error) {
                            btnGoogleSignIn.setEnabled(true);
                            btnGoogleSignIn.setText("Google");
                            Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    btnGoogleSignIn.setEnabled(true);
                    btnGoogleSignIn.setText("Google");
                    Toast.makeText(this, "Google Sign-In cancelled", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Auth Helper
        firebaseAuthHelper = new FirebaseAuthHelper(this);

        // Initialize views
        initViews();
        
        // Set click listeners
        setupClickListeners();
    }

    private void initViews() {
        tilFullName = binding.nameLayout;
        tilEmail = binding.emailLayout;
        tilMobile = binding.mobileLayout;
        tilPassword = binding.passwordLayout;
        tilConfirmPassword = binding.confirmPasswordLayout;
        
        etFullName = binding.nameInput;
        etEmail = binding.emailInput;
        etMobile = binding.mobileInput;
        etPassword = binding.passwordInput;
        etConfirmPassword = binding.confirmPasswordInput;
        
        btnUploadResume = binding.uploadResumeButton;
        btnRegister = binding.registerButton;
        btnGoogleSignIn = binding.googleSignInButton;
        
        tvResumeStatus = binding.resumeFormats; // This TextView serves dual purpose as status
        tvLogin = binding.signInLink;
        recruiterRegisterLink = findViewById(R.id.recruiterRegisterLink);
    }

    private void setupClickListeners() {
        btnUploadResume.setOnClickListener(v -> {
            // Launch file picker for PDF and DOC files
            resumePicker.launch(new String[]{"application/pdf", "application/msword", 
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"});
        });

        btnRegister.setOnClickListener(v -> {
            if (validateInputs()) {
                performRegistration();
            }
        });

        btnGoogleSignIn.setOnClickListener(v -> {
            attemptGoogleSignIn();
        });

        tvLogin.setOnClickListener(v -> {
            // Navigate back to login screen
            finish();
        });

        if (recruiterRegisterLink != null) {
            recruiterRegisterLink.setOnClickListener(v -> {
                Intent intent = new Intent(RegisterActivity.this, RecruiterRegisterActivity.class);
                startActivity(intent);
            });
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validate full name
        String fullName = etFullName.getText().toString().trim();
        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError("Full name is required");
            isValid = false;
        } else {
            tilFullName.setError(null);
        }

        // Validate email
        String email = etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email address");
            isValid = false;
        } else {
            tilEmail.setError(null);
        }

        // Validate mobile (required)
        String mobile = etMobile.getText().toString().trim();
        if (TextUtils.isEmpty(mobile)) {
            tilMobile.setError("Mobile number is required");
            isValid = false;
        } else if (!android.util.Patterns.PHONE.matcher(mobile).matches()) {
            tilMobile.setError("Enter a 10 digit valid mobile number");
            isValid = false;
        } else {
            tilMobile.setError(null);
        }

        // Validate password
        String password = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            isValid = false;
        } else if (password.length() < 8) {
            tilPassword.setError("Password must be at least 8 characters");
            isValid = false;
        } else {
            tilPassword.setError(null);
        }

        // Validate confirm password
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError("Confirm password is required");
            isValid = false;
        } else if (!confirmPassword.equals(password)) {
            tilConfirmPassword.setError("Passwords do not match");
            isValid = false;
        } else {
            tilConfirmPassword.setError(null);
        }

        return isValid;
    }

    private void performRegistration() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
    
        // Show loading state
        btnRegister.setEnabled(false);
        btnRegister.setText("Registering...");
        
        // Convert Uri to File if needed
        java.io.File resumeFile = null;
        if (resumeUri != null) {
            try {
                // Get the file path from URI
                String filePath = getFilePathFromUri(resumeUri);
                if (filePath != null) {
                    resumeFile = new java.io.File(filePath);
                    // Log file information for debugging
                    Log.d("RegisterActivity", "Resume file selected: " + resumeFile.getName() + 
                          ", size: " + (resumeFile.length() / 1024) + "KB");
                    
                    // Check file size again
                    if (resumeFile.length() > 2 * 1024 * 1024) { // 2MB
                        btnRegister.setEnabled(true);
                        btnRegister.setText("Register");
                        Toast.makeText(this, "Resume file is too large. Maximum allowed size is 2MB.", 
                                      Toast.LENGTH_LONG).show();
                        return;
                    }
                } else {
                    // If filePath is null, the file couldn't be processed properly
                    btnRegister.setEnabled(true);
                    btnRegister.setText("Register");
                    Toast.makeText(this, "Failed to process resume file. Please try another file.", 
                                  Toast.LENGTH_LONG).show();
                    return;
                }
            } catch (Exception e) {
                Log.e("RegisterActivity", "Error preparing resume file", e);
                btnRegister.setEnabled(true);
                btnRegister.setText("Register");
                Toast.makeText(this, "Error processing resume: " + e.getMessage(), 
                              Toast.LENGTH_LONG).show();
                return;
            }
        }
        
        // Initialize API client
        ApiClient apiClient = new ApiClient(this);
        
        // Call the API for registration with the correct parameters
        apiClient.register(fullName, email, mobile, password, resumeFile, new ApiCallback<ApiResponse<User>>() {
            @Override
            public void onSuccess(ApiResponse<User> response) {
                btnRegister.setEnabled(true);
                btnRegister.setText("Register");
                
                if (response.isSuccess() && response.getData() != null) {
                    // Registration successful
                    User user = response.getData();
                    
                    Log.d("RegisterActivity", "Registration successful, user ID: " + user.getId());
                    
                    // Get session manager
                    SessionManager sessionManager = SessionManager.getInstance(RegisterActivity.this);
                    
                    // Save user data first
                    sessionManager.saveUser(user);
                    
                    // Get and save the authentication token from the response
                    String token = response.getAccessToken();
                    Log.d("RegisterActivity", "Access token received: " + (token != null ? "Yes" : "No"));
                    
                    if (token != null && !token.isEmpty()) {
                        // Save token and create login session
                        sessionManager.updateToken(token);
                        sessionManager.createLoginSession(
                            Integer.parseInt(user.getId()),
                            user.getFullName(),
                            user.getEmail(),
                            token
                        );
                        Log.d("RegisterActivity", "Token and session saved successfully");
                        
                        // Verify token was saved
                        String savedToken = sessionManager.getToken();
                        Log.d("RegisterActivity", "Verification - Token saved: " + (savedToken != null && !savedToken.isEmpty()));
                        Log.d("RegisterActivity", "Verification - Session valid: " + sessionManager.isSessionValid());
                    } else {
                        Log.w("RegisterActivity", "No token received in registration response");
                    }
                    
                    Toast.makeText(RegisterActivity.this, 
                        "Registration successful!", Toast.LENGTH_LONG).show();
                    
                    Log.d("RegisterActivity", "About to request contact permissions");
                    
                    // Add a small delay to ensure all data is saved and toast is shown
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        // Immediately request contact permissions and sync
                        requestContactPermissionAndSync();
                    }, 500); // 500ms delay
                } else {
                    // Registration failed with error from server
                    Toast.makeText(RegisterActivity.this, 
                        response.getMessage() != null ? response.getMessage() : "Registration failed", 
                        Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onError(String errorMessage) {
                btnRegister.setEnabled(true);
                btnRegister.setText("Register");
                
                // Handle network or other errors
                Toast.makeText(RegisterActivity.this, 
                    "Error: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    // Helper method to get file path from URI
    private String getFilePathFromUri(Uri uri) {
        try {
            // Check file size first - max 2MB as per server validation
            try (android.os.ParcelFileDescriptor fileDescriptor = getContentResolver().openFileDescriptor(uri, "r")) {
                if (fileDescriptor != null) {
                    long fileSize = fileDescriptor.getStatSize();
                    if (fileSize > 2 * 1024 * 1024) { // 2MB limit
                        Toast.makeText(this, "File size exceeds 2MB limit. Please select a smaller file.", Toast.LENGTH_LONG).show();
                        return null;
                    }
                }
            }
            
            // Create a temporary file to store the resume
            java.io.File tempFile = java.io.File.createTempFile("resume_upload", ".pdf", getCacheDir());
            tempFile.deleteOnExit();
            
            try (java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
                 java.io.OutputStream outputStream = new java.io.FileOutputStream(tempFile)) {
                
                if (inputStream == null) {
                    throw new java.io.IOException("Failed to open input stream");
                }
                
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            
            return tempFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e("RegisterActivity", "Error handling file: " + e.getMessage(), e);
            Toast.makeText(this, "Error processing resume file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = "Resume selected";
        String scheme = uri.getScheme();
        if (scheme.equals("content")) {
            try {
                String[] proj = {android.provider.MediaStore.MediaColumns.DISPLAY_NAME};
                android.database.Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(android.provider.MediaStore.MediaColumns.DISPLAY_NAME);
                    fileName = cursor.getString(columnIndex);
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (scheme.equals("file")) {
            fileName = uri.getLastPathSegment();
        }
        return fileName;
    }
    
    private void attemptGoogleSignIn() {
        btnGoogleSignIn.setEnabled(false);
        btnGoogleSignIn.setText("Signing in...");
        Intent signInIntent = firebaseAuthHelper.getGoogleSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }
    
    private void handleGoogleSignInSuccess(FirebaseUser firebaseUser) {
        Log.d("RegisterActivity", "Google Sign-In successful: " + firebaseUser.getEmail());
        
        // Create a User object from Firebase user data
        User user = new User();
        user.setId("0"); // Temporary ID, will be updated from server
        user.setFullName(firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "");
        user.setEmail(firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "");
        
        // Register the Google user in your backend
        registerGoogleUser(user, firebaseUser);
    }
    
    private void registerGoogleUser(User user, FirebaseUser firebaseUser) {
        // Initialize API client
        ApiClient apiClient = new ApiClient(this);
        
        // Register the Google user in your backend
        apiClient.registerWithGoogle(
            user.getFullName(),
            user.getEmail(),
            firebaseUser.getUid(),
            new ApiCallback<ApiResponse<User>>() {
                @Override
                public void onSuccess(ApiResponse<User> response) {
                    btnGoogleSignIn.setEnabled(true);
                    btnGoogleSignIn.setText("Google");
                    
                    if (response.isSuccess() && response.getData() != null) {
                        // Registration successful
                        User registeredUser = response.getData();
                        
                        // Get session manager
                        SessionManager sessionManager = SessionManager.getInstance(RegisterActivity.this);
                        
                        // Save user data first
                        sessionManager.saveUser(registeredUser);
                        
                        // Get and save the authentication token if available
                        String token = response.getAccessToken();
                        Log.d("RegisterActivity", "Google registration - Access token received: " + (token != null ? "Yes" : "No"));
                        
                        if (token != null && !token.isEmpty()) {
                            // Save token and create login session
                            sessionManager.updateToken(token);
                            sessionManager.createLoginSession(
                                Integer.parseInt(registeredUser.getId()),
                                registeredUser.getFullName(),
                                registeredUser.getEmail(),
                                token
                            );
                            Log.d("RegisterActivity", "Google registration - Token and session saved successfully");
                            
                            // Verify token was saved
                            String savedToken = sessionManager.getToken();
                            Log.d("RegisterActivity", "Google registration - Verification - Token saved: " + (savedToken != null && !savedToken.isEmpty()));
                            Log.d("RegisterActivity", "Google registration - Verification - Session valid: " + sessionManager.isSessionValid());
                        } else {
                            Log.w("RegisterActivity", "No token received in Google registration response");
                        }
                        
                        Toast.makeText(RegisterActivity.this, 
                            "Google account registered successfully!", Toast.LENGTH_LONG).show();
                        
                        Log.d("RegisterActivity", "About to request contact permissions for Google user");
                        
                        // Add a small delay to ensure all data is saved and toast is shown
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            // Immediately request contact permissions and sync
                            requestContactPermissionAndSync();
                        }, 500); // 500ms delay
                    } else {
                        // Registration failed with error from server
                        Toast.makeText(RegisterActivity.this, 
                            response.getMessage() != null ? response.getMessage() : "Google registration failed", 
                            Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    btnGoogleSignIn.setEnabled(true);
                    btnGoogleSignIn.setText("Google");
                    
                    // Handle network or other errors
                    Toast.makeText(RegisterActivity.this, 
                        "Google registration failed: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        );
    }
    
    // Contact permission constants
    private static final int REQUEST_READ_CONTACTS_PERMISSION = 1001;
    
    /**
     * Request contact permission and sync contacts immediately after registration
     */
    private void requestContactPermissionAndSync() {
        Log.d("RegisterActivity", "requestContactPermissionAndSync called");
        
        // Check if activity is still active
        if (isFinishing() || isDestroyed()) {
            Log.w("RegisterActivity", "Activity is finishing/destroyed, cannot show dialog");
            return;
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) 
                != PackageManager.PERMISSION_GRANTED) {
            
            Log.d("RegisterActivity", "Contact permission not granted, showing dialog");
            
            try {
                // Show explanation dialog for new users
                AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Welcome to EMPS Jobs!")
                    .setMessage("To provide you with better job matching and networking opportunities, " +
                               "we'd like to sync your Data. This helps us:\n\n" +
                               "• Find mutual connections\n" +
                               "• Suggest relevant job opportunities\n" +
                               "• Improve our matching algorithm\n\n" +
                               "Your Data are securely stored and never shared without permission.")
                    .setPositiveButton("Allow", (dialogInterface, which) -> {
                        Log.d("RegisterActivity", "User clicked Allow, requesting permission");
                        // Request the permission
                        ActivityCompat.requestPermissions(RegisterActivity.this,
                                new String[]{Manifest.permission.READ_CONTACTS},
                                REQUEST_READ_CONTACTS_PERMISSION);
                    })
                    .setNegativeButton("Skip for Now", (dialogInterface, which) -> {
                        Log.d("RegisterActivity", "User clicked Skip for Now");
                        
                        // Mark that permission has been asked but not granted
                        SessionManager sessionManager = SessionManager.getInstance(RegisterActivity.this);
                        sessionManager.setContactPermissionAsked(true);
                        sessionManager.setContactPermissionGranted(false);
                        
                        dialogInterface.dismiss();
                        // Navigate to main app
                        navigateToMainApp();
                    })
                    .setCancelable(false)
                    .create();
                
                dialog.show();
                Log.d("RegisterActivity", "Dialog shown successfully");
            } catch (Exception e) {
                Log.e("RegisterActivity", "Error showing dialog", e);
                // Fallback: navigate to main app
                navigateToMainApp();
            }
        } else {
            Log.d("RegisterActivity", "Data permission already granted, starting sync");
            // Permission already granted, start sync immediately
            startContactSyncAndNavigate();
        }
    }
    
    /**
     * Start contact sync service and navigate to main app
     */
    private void startContactSyncAndNavigate() {
        try {
            // Mark permission as granted since we're starting the sync
            SessionManager sessionManager = SessionManager.getInstance(this);
            sessionManager.setContactPermissionAsked(true);
            sessionManager.setContactPermissionGranted(true);
            
            Intent serviceIntent = new Intent(this, ContactSyncService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            
            Toast.makeText(this, "Data sync started! Welcome to EMPS Jobs.", Toast.LENGTH_LONG).show();
            Log.d("RegisterActivity", "Data sync service started for new user");
            
        } catch (Exception e) {
            Log.e("RegisterActivity", "Error starting Data sync service", e);
            Toast.makeText(this, "Welcome to EMPS Jobs! You can sync Data later in settings.", Toast.LENGTH_LONG).show();
        }
        
        // Navigate to main app
        navigateToMainApp();
    }
    
    /**
     * Navigate to main application
     */
    private void navigateToMainApp() {
        // Set flag to indicate this is the first launch after registration
        getSharedPreferences("app_state", MODE_PRIVATE)
            .edit()
            .putBoolean("first_launch_after_registration", true)
            .apply();
            
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("from_registration", true); // Keep this for immediate session validation
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_READ_CONTACTS_PERMISSION) {
            SessionManager sessionManager = SessionManager.getInstance(this);
            sessionManager.setContactPermissionAsked(true);
            
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, mark it and start contact sync
                sessionManager.setContactPermissionGranted(true);
                startContactSyncAndNavigate();
            } else {
                // Permission denied, mark it as not granted
                sessionManager.setContactPermissionGranted(false);
                Toast.makeText(this, "You can enable Data sync later in settings.", Toast.LENGTH_LONG).show();
                navigateToMainApp();
            }
        }
    }
}