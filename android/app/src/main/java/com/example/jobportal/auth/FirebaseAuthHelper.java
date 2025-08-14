package com.example.jobportal.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class FirebaseAuthHelper {
    
    private static final String TAG = "FirebaseAuthHelper";
    private static final int RC_SIGN_IN = 9001;
    
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private Context context;
    
    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onError(String error);
    }
    
    public FirebaseAuthHelper(Context context) {
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getWebClientId())
                .requestEmail()
                .build();
        
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
    }
    
    private String getWebClientId() {
        // You need to get this from your google-services.json file
        // For now, we'll use a placeholder - you'll need to replace this with your actual web client ID
        return context.getString(com.example.jobportal.R.string.default_web_client_id);
    }
    
    public Intent getGoogleSignInIntent() {
        return mGoogleSignInClient.getSignInIntent();
    }
    
    public void handleGoogleSignInResult(Intent data, AuthCallback callback) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            Log.d(TAG, "Google sign in successful: " + account.getId());
            Log.d(TAG, "Account email: " + account.getEmail());
            Log.d(TAG, "Account name: " + account.getDisplayName());
            firebaseAuthWithGoogle(account.getIdToken(), callback);
        } catch (ApiException e) {
            Log.e(TAG, "Google sign in failed with status code: " + e.getStatusCode());
            Log.e(TAG, "Error message: " + e.getMessage());
            
            String errorMessage;
            switch (e.getStatusCode()) {
                case 12501: // GoogleSignInStatusCodes.SIGN_IN_CANCELLED
                    errorMessage = "Sign in was cancelled";
                    break;
                case 12502: // GoogleSignInStatusCodes.SIGN_IN_CURRENTLY_IN_PROGRESS
                    errorMessage = "Sign in is already in progress";
                    break;
                case 12500: // GoogleSignInStatusCodes.SIGN_IN_FAILED
                    errorMessage = "Sign in failed. Please check your internet connection and try again.";
                    break;
                case 10: // GoogleSignInStatusCodes.DEVELOPER_ERROR
                    errorMessage = "Configuration error. Please check SHA-1 certificate in Firebase Console.";
                    break;
                default:
                    errorMessage = "Google sign in failed: " + e.getMessage();
                    break;
            }
            
            callback.onError(errorMessage);
        }
    }
    
    private void firebaseAuthWithGoogle(String idToken, AuthCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            callback.onSuccess(user);
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            callback.onError("Authentication failed: " + 
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                        }
                    }
                });
    }
    
    public void signOut() {
        mAuth.signOut();
        mGoogleSignInClient.signOut();
    }
    
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }
    
    public static int getSignInRequestCode() {
        return RC_SIGN_IN;
    }
}