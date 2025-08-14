package com.example.jobportal.ui.update;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jobportal.databinding.ActivityUpdateBinding;

public class UpdateActivity extends AppCompatActivity {
    
    public static final String EXTRA_UPDATE_MESSAGE = "update_message";
    public static final String EXTRA_DOWNLOAD_URL = "download_url";
    public static final String EXTRA_FORCE_UPDATE = "force_update";
    public static final String EXTRA_VERSION_NAME = "version_name";
    
    private ActivityUpdateBinding binding;
    private String downloadUrl;
    private boolean isForceUpdate;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        android.util.Log.d("UpdateActivity", "UpdateActivity created!");
        
        binding = ActivityUpdateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        setupUI();
        setupClickListeners();
    }
    
    private void setupUI() {
        String updateMessage = getIntent().getStringExtra(EXTRA_UPDATE_MESSAGE);
        if (updateMessage == null) {
            updateMessage = "A new version is available. Please update to continue using the app.";
        }
        
        String versionName = getIntent().getStringExtra(EXTRA_VERSION_NAME);
        if (versionName == null) {
            versionName = "";
        }
        
        downloadUrl = getIntent().getStringExtra(EXTRA_DOWNLOAD_URL);
        isForceUpdate = getIntent().getBooleanExtra(EXTRA_FORCE_UPDATE, false);
        
        android.util.Log.d("UpdateActivity", "Setting up UI:");
        android.util.Log.d("UpdateActivity", "- Update message: " + updateMessage);
        android.util.Log.d("UpdateActivity", "- Version name: " + versionName);
        android.util.Log.d("UpdateActivity", "- Download URL: " + downloadUrl);
        android.util.Log.d("UpdateActivity", "- Force update: " + isForceUpdate);
        
        binding.tvUpdateMessage.setText(updateMessage);
        binding.tvVersionName.setText("Version " + versionName);
        
        // Hide skip button for force updates
        if (isForceUpdate) {
            android.util.Log.d("UpdateActivity", "Force update - hiding skip button");
            binding.btnSkip.setVisibility(View.GONE);
            binding.tvForceUpdateNote.setVisibility(View.VISIBLE);
        } else {
            android.util.Log.d("UpdateActivity", "Optional update - showing skip button");
            binding.btnSkip.setVisibility(View.VISIBLE);
            binding.tvForceUpdateNote.setVisibility(View.GONE);
        }
    }
    
    private void setupClickListeners() {
        binding.btnUpdate.setOnClickListener(v -> {
            if (downloadUrl != null && !downloadUrl.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
                startActivity(intent);
            }
        });
        
        binding.btnSkip.setOnClickListener(v -> {
            if (!isForceUpdate) {
                finish();
            }
        });
    }
    
    @Override
    public void onBackPressed() {
        if (!isForceUpdate) {
            super.onBackPressed();
        }
        // Do nothing for force updates - user cannot go back
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}