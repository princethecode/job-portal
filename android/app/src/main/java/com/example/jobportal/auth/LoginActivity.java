package com.example.jobportal.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.jobportal.BuildConfig;
import com.example.jobportal.MainActivity;
import com.example.jobportal.R;
import com.example.jobportal.models.LoginResponse;
import com.example.jobportal.network.ApiClient;
import com.example.jobportal.databinding.ActivityLoginBinding;
import com.example.jobportal.network.ApiResponse;
import com.example.jobportal.models.User;
import com.example.jobportal.network.ApiCallback;
import com.example.jobportal.network.ApiService;
import com.example.jobportal.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.auth.FirebaseUser;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import okhttp3.ResponseBody;
import com.example.jobportal.recruiter.RecruiterMainActivity;
import com.example.jobportal.recruiter.fragments.RecruiterDashboardFragment;

public class LoginActivity extends AppCompatActivity {
    
    private static final String TAG = "LoginActivity";
    private TextInputLayout tilMobile, tilPassword;
    private TextInputEditText etMobile, etPassword;
    private Button btnLogin, btnGoogleSignIn;
    private TextView tvForgotPassword, tvRegister;
    private TextView recruiterLoginLink;
    private View progressBar;
    private ActivityLoginBinding binding;
    private boolean isCheckingSession = false;
    
    // User data to be passed after successful login
    private User authenticatedUser;
    private ApiClient apiClient;
    private SessionManager sessionManager;
    
    // Firebase Auth Helper
    private FirebaseAuthHelper firebaseAuthHelper;
    
    // Activity result launcher for Google Sign-In
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d(TAG, "Google Sign-In result received. Result code: " + result.getResultCode());
                
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Log.d(TAG, "Processing Google Sign-In result...");
                    firebaseAuthHelper.handleGoogleSignInResult(result.getData(), new FirebaseAuthHelper.AuthCallback() {
                        @Override
                        public void onSuccess(FirebaseUser user) {
                            Log.d(TAG, "Firebase authentication successful");
                            handleGoogleSignInSuccess(user);
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "Firebase authentication failed: " + error);
                            showProgress(false);
                            
                            // Show more user-friendly error messages
                            if (error.contains("Configuration error") || error.contains("SHA-1")) {
                                Toast.makeText(LoginActivity.this, 
                                    "Google Sign-In is not properly configured. Please contact support.", 
                                    Toast.LENGTH_LONG).show();
                            } else if (error.contains("cancelled")) {
                                Toast.makeText(LoginActivity.this, 
                                    "Sign-in was cancelled. Please try again.", 
                                    Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LoginActivity.this, 
                                    "Google Sign-In failed: " + error, 
                                    Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    Log.w(TAG, "Google Sign-In was cancelled or failed. Result code: " + result.getResultCode());
                    showProgress(false);
                    
                    if (result.getResultCode() == RESULT_CANCELED) {
                        Toast.makeText(this, "Google Sign-In was cancelled", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Google Sign-In failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Prevent activity recreation on configuration changes
        if (savedInstanceState != null) {
            Log.d(TAG, "Activity recreated with saved state");
            return;
        }
        
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize API client and SessionManager
        apiClient = new ApiClient(this);
        sessionManager = SessionManager.getInstance(getApplicationContext());
        
        // Initialize Firebase Auth Helper
        firebaseAuthHelper = new FirebaseAuthHelper(this);
        
        // Check if user is already logged in
        if (!isCheckingSession) {
            isCheckingSession = true;
            if (sessionManager.isSessionValid()) {
                Log.d(TAG, "Valid session found, redirecting to MainActivity");
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Log.d(TAG, "No valid session found, showing login screen");
            }
            isCheckingSession = false;
        }

        // Initialize views
        initViews();
        
        // Set click listeners
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called - isCheckingSession: " + isCheckingSession);
        
        // Only check session if we haven't already
        if (!isCheckingSession) {
            isCheckingSession = true;
            if (sessionManager.isSessionValid()) {
                Log.d(TAG, "Valid session found in onResume, redirecting to MainActivity");
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
            isCheckingSession = false;
        }
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

    private void initViews() {
        tilMobile = binding.emailLayout;
        tilPassword = binding.passwordLayout;
        etMobile = binding.emailInput;
        etPassword = binding.passwordInput;
        btnLogin = binding.loginButton;
        btnGoogleSignIn = binding.googleSignInButton;
        tvForgotPassword = binding.forgotPassword;
        tvRegister = binding.registerLink;
        recruiterLoginLink = findViewById(R.id.recruiterLoginLink);
        progressBar = findViewById(R.id.progress_bar); // Make sure to add this to your layout
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> {
            if (validateInputs()) {
                attemptLogin();
            }
        });

        btnGoogleSignIn.setOnClickListener(v -> {
            attemptGoogleSignIn();
        });

        tvForgotPassword.setOnClickListener(v -> {
            // Navigate to forgot password screen
            navigateToForgotPassword();
        });

        tvRegister.setOnClickListener(v -> {
            // Navigate to registration screen
            navigateToRegister();
        });

        if (recruiterLoginLink != null) {
            recruiterLoginLink.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, RecruiterLoginActivity.class);
                startActivity(intent);
            });
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validate mobile
        String mobile = etMobile.getText().toString().trim();
        if (TextUtils.isEmpty(mobile)) {
            tilMobile.setError("Mobile number is required");
            isValid = false;
        } else if (!android.util.Patterns.PHONE.matcher(mobile).matches()) {
            tilMobile.setError("Enter a valid mobile number");
            isValid = false;
        } else {
            tilMobile.setError(null);
        }

        // Validate password
        String password = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            isValid = false;
        } else {
            tilPassword.setError(null);
        }

        return isValid;
    }

    private void attemptLogin() {
        String mobile = etMobile.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
    
        // Show loading state
        showProgress(true);
        
        // Check for internet connectivity first
        if (!isNetworkAvailable()) {
            showProgress(false);
            Toast.makeText(this, "No internet connection. Please check your network settings and try again.", 
                    Toast.LENGTH_LONG).show();
            return;
        }
        
        // Call the API for login with mobile
        apiClient.loginWithMobile(mobile, password, new ApiCallback<ApiResponse<LoginResponse>>() {
            @Override
            public void onSuccess(ApiResponse<LoginResponse> response) {
                showProgress(false);
                handleLoginResponse(response);
            }
    
            @Override
            public void onError(String errorMessage) {
                showProgress(false);
                
                // Check if it's a DNS or connection error
                if (errorMessage.contains("UnknownHostException") || 
                    errorMessage.contains("Unable to resolve host")) {
                    Toast.makeText(LoginActivity.this, 
                        "Cannot connect to server. Please check your internet connection or try again later.", 
                        Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    
    /**
     * Check if the device has an active internet connection
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                return capabilities != null && (
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                );
            } else {
                // For older Android versions
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
        }
        return false;
    }
    
    private void navigateToForgotPassword() {
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    private void navigateToRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
    
    private void showProgress(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        btnLogin.setEnabled(!show);
        btnLogin.setText(show ? "Logging in..." : "Login");
    }

    private void handleLoginResponse(ApiResponse<LoginResponse> response) {
        if (response.isSuccess() && response.getData() != null) {
            LoginResponse loginResponse = response.getData();
            User user = loginResponse.getUser();
            String token = loginResponse.getAccessToken();
            
            Log.d("JobPortal", "Login response: " + response.toString());
            Log.d("JobPortal", "User data: " + user.toString());
            Log.d("JobPortal", "Token: " + token);
            
            if (user != null && token != null && !token.isEmpty()) {
                handleSuccessfulLogin(user, token);
            } else {
                String errorMsg = "Login failed: ";
                if (user == null) errorMsg += "User data is null. ";
                if (token == null || token.isEmpty()) errorMsg += "Token is missing. ";
                Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
            }
        } else {
            String errorMsg = response.getMessage() != null ? 
                response.getMessage() : "Login failed: Invalid response data";
            Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
        }
    }

    private void handleSuccessfulLogin(User user, String token) {
        // Save user session with token
        sessionManager.createLoginSession(
            Integer.parseInt(user.getId()),
            user.getFullName(),
            user.getEmail(),
            token
        );
        
        Log.d("JobPortal", "Login successful - Token: " + token);
        Log.d("JobPortal", "User ID: " + user.getId());
        
        // Register FCM token if available
        registerFcmTokenIfAvailable();
        
        // Navigate to main activity
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void registerFcmTokenIfAvailable() {
        String fcmToken = sessionManager.getFcmToken();
        if (fcmToken != null && !fcmToken.isEmpty()) {
            Log.d(TAG, "Registering FCM token after login");
            ApiService apiService = ApiClient.getApiService();
            if (apiService != null) {
                Map<String, String> tokenData = new HashMap<>();
                tokenData.put("fcm_token", fcmToken);
                
                // Create a custom OkHttpClient with additional logging
                OkHttpClient httpClient = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request request = original.newBuilder()
                            .header("Authorization", "Bearer " + sessionManager.getToken())
                            .header("Accept", "application/json")
                            .method(original.method(), original.body())
                            .build();
                        return chain.proceed(request);
                    })
                    .build();

                // Create a custom Retrofit instance
                Retrofit customRetrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient)
                    .build();

                // Create API service with custom Retrofit
                ApiService customApiService = customRetrofit.create(ApiService.class);
                
                customApiService.registerFcmToken(tokenData).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            try {
                                String responseBody = response.body() != null ? response.body().string() : "";
                                Log.d(TAG, "FCM token registration response: " + responseBody);
                                
                                // Try to parse as JSON first
                                try {
                                    Gson gson = new Gson();
                                    ApiResponse<Void> apiResponse = gson.fromJson(responseBody, 
                                        new TypeToken<ApiResponse<Void>>(){}.getType());
                                    if (apiResponse != null && apiResponse.isSuccess()) {
                                        Log.d(TAG, "FCM token registered successfully after login");
                                    } else {
                                        Log.e(TAG, "Failed to register FCM token: " + 
                                            (apiResponse != null ? apiResponse.getMessage() : "Unknown error"));
                                    }
                                } catch (JsonSyntaxException e) {
                                    // If JSON parsing fails, treat as a simple success message
                                    Log.d(TAG, "FCM token registered successfully (string response)");
                                }
                            } catch (IOException e) {
                                Log.e(TAG, "Error reading response body", e);
                            }
                        } else {
                            Log.e(TAG, "Failed to register FCM token after login: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e(TAG, "Error registering FCM token after login", t);
                    }
                });
            }
        } else {
            Log.d(TAG, "No FCM token available to register after login");
        }
    }
    
    private void attemptGoogleSignIn() {
        showProgress(true);
        Intent signInIntent = firebaseAuthHelper.getGoogleSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }
    
    private void handleGoogleSignInSuccess(FirebaseUser firebaseUser) {
        Log.d(TAG, "Google Sign-In successful: " + firebaseUser.getEmail());
        
        // Create a User object from Firebase user data
        User user = new User();
        user.setId("0"); // Temporary ID, will be updated from server
        user.setFullName(firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "");
        user.setEmail(firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "");
        
        // Check if user exists in your backend, if not register them
        checkOrRegisterGoogleUser(user, firebaseUser);
    }
    
    private void checkOrRegisterGoogleUser(User user, FirebaseUser firebaseUser) {
        // First, try to login with Google email
        apiClient.loginWithGoogle(user.getEmail(), firebaseUser.getUid(), new ApiCallback<ApiResponse<LoginResponse>>() {
            @Override
            public void onSuccess(ApiResponse<LoginResponse> response) {
                showProgress(false);
                if (response.isSuccess() && response.getData() != null) {
                    // User exists, handle successful login
                    handleLoginResponse(response);
                } else {
                    // User doesn't exist, register them
                    registerGoogleUser(user, firebaseUser);
                }
            }

            @Override
            public void onError(String errorMessage) {
                // If login fails, try to register the user
                Log.d(TAG, "Google login failed, attempting registration: " + errorMessage);
                registerGoogleUser(user, firebaseUser);
            }
        });
    }
    
    private void registerGoogleUser(User user, FirebaseUser firebaseUser) {
        // Register the Google user in your backend
        apiClient.registerWithGoogle(
            user.getFullName(),
            user.getEmail(),
            firebaseUser.getUid(),
            new ApiCallback<ApiResponse<User>>() {
                @Override
                public void onSuccess(ApiResponse<User> response) {
                    showProgress(false);
                    if (response.isSuccess() && response.getData() != null) {
                        // Registration successful, now login
                        Toast.makeText(LoginActivity.this, "Google account registered successfully!", Toast.LENGTH_SHORT).show();
                        
                        // Try to login again after registration
                        apiClient.loginWithGoogle(user.getEmail(), firebaseUser.getUid(), new ApiCallback<ApiResponse<LoginResponse>>() {
                            @Override
                            public void onSuccess(ApiResponse<LoginResponse> loginResponse) {
                                if (loginResponse.isSuccess() && loginResponse.getData() != null) {
                                    handleLoginResponse(loginResponse);
                                } else {
                                    Toast.makeText(LoginActivity.this, "Registration successful but login failed. Please try again.", Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onError(String errorMessage) {
                                Toast.makeText(LoginActivity.this, "Registration successful but login failed: " + errorMessage, Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        Toast.makeText(LoginActivity.this, "Google registration failed: " + 
                            (response.getMessage() != null ? response.getMessage() : "Unknown error"), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    showProgress(false);
                    Toast.makeText(LoginActivity.this, "Google registration failed: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        );
    }
}
