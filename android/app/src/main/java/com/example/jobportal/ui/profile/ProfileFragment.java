package com.example.jobportal.ui.profile;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.jobportal.BuildConfig;
import com.example.jobportal.R;
import com.example.jobportal.auth.LoginActivity;
import com.example.jobportal.auth.ProfileActivity;
import com.example.jobportal.models.Experience;
import com.example.jobportal.models.User;
import com.example.jobportal.network.ApiCallback;
import com.example.jobportal.network.ApiClient;
import com.example.jobportal.network.ApiClientExperience;
import com.example.jobportal.network.ApiResponse;
import com.example.jobportal.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    
    // User profile information text views
    private TextView tvName, tvEmail, tvMobile, tvLocation;
    private TextView tvJobTitle; // Added for job title
    private TextView tvAboutMe; // Added for about me section
    private CircleImageView profileImageView; // Profile image view
    
    // Experience section UI elements
    private LinearLayout experiencesContainer;
    private View emptyExperiencesView;
    private Button viewAllExperiencesButton;
    private int MAX_EXPERIENCES_TO_SHOW = 2; // Maximum number of experiences to show in profile
    
    // Skills section UI elements
    private com.google.android.material.chip.ChipGroup skillsChipGroup;
    private View addSkillButton;
    
    // Resume section UI elements
    private View resumeCard;
    private View resumeItem;
    private TextView resumeFileName;
    private TextView resumeUpdateDate;
    private View downloadResumeButton;
    private View editResumeButton;
    
    // UI elements
    private View progressBar;
    private MaterialButton editProfileButton;
    private MaterialButton logoutButton;
    private View addExperienceButton; // Add experience button
    
    // API and data management
    private ApiClient apiClient;
    private ApiClientExperience experienceApiClient;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                            @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @SuppressLint("WrongViewCast")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize API client and session manager
        apiClient = ApiClient.getInstance(requireContext());
        experienceApiClient = ApiClientExperience.getInstance();
        sessionManager = SessionManager.getInstance(requireContext());

        // Initialize profile information views
        tvName = view.findViewById(R.id.name_text);
        tvEmail = view.findViewById(R.id.email_text);
        tvMobile = view.findViewById(R.id.phone_text);
        tvLocation = view.findViewById(R.id.location_text);
        progressBar = view.findViewById(R.id.progress_profile);
        profileImageView = view.findViewById(R.id.profile_image);
        addExperienceButton = view.findViewById(R.id.add_experience_button);
        
        // Initialize experience views
        experiencesContainer = view.findViewById(R.id.experiences_container);
        emptyExperiencesView = view.findViewById(R.id.empty_experiences_view);
        viewAllExperiencesButton = view.findViewById(R.id.view_all_experiences_button);
        
        // Set click listener for view all experiences button
        if (viewAllExperiencesButton != null) {
            viewAllExperiencesButton.setOnClickListener(v -> navigateToExperienceList());
        }
        
        // Initialize skills views
        skillsChipGroup = view.findViewById(R.id.skills_chip_group);
        addSkillButton = view.findViewById(R.id.add_skill_button);
        
        // Set click listener for add skill button
        if (addSkillButton != null) {
            addSkillButton.setOnClickListener(v -> {
                // Navigate to edit profile or show a dialog to add skills
                if (editProfileButton != null) {
                    editProfileButton.performClick();
                }
            });
        }
        
        // Initialize resume views
        resumeCard = view.findViewById(R.id.resume_card);
        resumeItem = view.findViewById(R.id.resume_item);
        resumeFileName = view.findViewById(R.id.resume_file_name);
        resumeUpdateDate = view.findViewById(R.id.resume_update_date);
        downloadResumeButton = view.findViewById(R.id.download_resume_button);
        editResumeButton = view.findViewById(R.id.edit_resume_button);
        
        // Set click listeners for resume buttons
        if (downloadResumeButton != null) {
            downloadResumeButton.setOnClickListener(v -> downloadResume());
        }
        
        if (editResumeButton != null) {
            editResumeButton.setOnClickListener(v -> {
                // Navigate to edit profile or show a dialog to upload resume
                if (editProfileButton != null) {
                    editProfileButton.performClick();
                }
            });
        }
        
        // Initialize new UI elements (if they exist)
        try {
            tvJobTitle = view.findViewById(R.id.job_title_text);
            tvAboutMe = view.findViewById(R.id.about_me_text);
        } catch (Exception e) {
            Log.w(TAG, "Some new UI elements could not be found: " + e.getMessage());
        }
        
        // Initialize buttons - try both old and new button IDs
        try {
            // First try to find the button with the original ID
            editProfileButton = view.findViewById(R.id.edit_profile_button);
            if (editProfileButton == null) {
                // If not found, try the new button ID
                editProfileButton = view.findViewById(R.id.edit_profile_button);
                Log.d(TAG, "Using profile_card_edit_button instead of edit_profile_button");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error finding edit profile button: " + e.getMessage());
        }
        
        logoutButton = view.findViewById(R.id.logout_button);
        
        // Set up listeners if buttons are found
        if (editProfileButton != null) {
            editProfileButton.setOnClickListener(v -> {
                startActivity(new Intent(requireContext(), ProfileActivity.class));
            });
        } else {
            Log.e(TAG, "Edit profile button not found!");
        }
        
        // Set click listener for logout button
        logoutButton.setOnClickListener(v -> handleLogout());
        
        // Set click listener for add experience button
        if (addExperienceButton != null) {
            addExperienceButton.setOnClickListener(v -> navigateToExperienceList());
        }
        
        // Load profile data
        loadProfileData();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Reload profile data each time the fragment is shown
        loadProfileData();
        // Load experience data
        loadExperienceData();
    }
    
    /**
     * Load the user's experiences from the API
     */
    private void loadExperienceData() {
        if (!sessionManager.isLoggedIn() || experiencesContainer == null) {
            return; // Don't load if not logged in or container is null
        }
        
        // Clear any existing experience views
        experiencesContainer.removeAllViews();
        
        // Show loading state
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        experienceApiClient.getUserExperiences(new ApiCallback<List<Experience>>() {
            @Override
            public void onSuccess(List<Experience> experiences) {
                if (!isAdded()) return; // Fragment not attached to activity
                
                // Hide loading state
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                
                if (experiences != null && !experiences.isEmpty()) {
                    // Show experiences container and hide empty state
                    experiencesContainer.setVisibility(View.VISIBLE);
                    emptyExperiencesView.setVisibility(View.GONE);
                    
                    // Determine how many experiences to show
                    int experiencesToShow = Math.min(experiences.size(), MAX_EXPERIENCES_TO_SHOW);
                    boolean hasMoreExperiences = experiences.size() > MAX_EXPERIENCES_TO_SHOW;
                    
                    // Add experience items to the container
                    for (int i = 0; i < experiencesToShow; i++) {
                        addExperienceItem(experiences.get(i));
                    }
                    
                    // Show or hide the "View All" button based on whether there are more experiences
                    if (viewAllExperiencesButton != null) {
                        viewAllExperiencesButton.setVisibility(hasMoreExperiences ? View.VISIBLE : View.GONE);
                    }
                } else {
                    // Show empty state and hide experiences container
                    experiencesContainer.setVisibility(View.GONE);
                    emptyExperiencesView.setVisibility(View.VISIBLE);
                    
                    if (viewAllExperiencesButton != null) {
                        viewAllExperiencesButton.setVisibility(View.GONE);
                    }
                }
            }
            
            @Override
            public void onError(String errorMsg) {
                if (!isAdded()) return; // Fragment not attached to activity
                
                // Hide loading state
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                
                // Show empty state on error
                experiencesContainer.setVisibility(View.GONE);
                emptyExperiencesView.setVisibility(View.VISIBLE);
                
                if (viewAllExperiencesButton != null) {
                    viewAllExperiencesButton.setVisibility(View.GONE);
                }
                
                Log.e(TAG, "Error loading experiences: " + errorMsg);
            }
        });
    }
    
    private void addExperienceItem(Experience experience) {
        if (experience == null || experiencesContainer == null || getContext() == null) {
            return;
        }
        
        try {
            // Inflate the experience item layout
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View experienceView = inflater.inflate(R.layout.item_experience, experiencesContainer, false);
            
            // Get references to views
            TextView tvJobTitle = experienceView.findViewById(R.id.tvJobTitle);
            TextView tvCompanyName = experienceView.findViewById(R.id.tvCompanyName);
            TextView tvDateRange = experienceView.findViewById(R.id.tvDateRange);
            TextView tvDescription = experienceView.findViewById(R.id.tvDescription);
            ImageButton btnEditExperience = experienceView.findViewById(R.id.btnEditExperience);
            ImageView ivCompanyLogo = experienceView.findViewById(R.id.ivCompanyLogo);
            
            // Set experience details
            tvJobTitle.setText(experience.getJobTitle());
            tvCompanyName.setText(experience.getCompanyName());
            
            // Set company logo based on company name
            setCompanyLogo(ivCompanyLogo, experience.getCompanyName());
            
            // Format date range
            String dateRange = experience.getStartDate();
            if (experience.isCurrent()) {
                dateRange += " - Present";
            } else if (experience.getEndDate() != null && !experience.getEndDate().isEmpty()) {
                dateRange += " - " + experience.getEndDate();
            }
            tvDateRange.setText(dateRange);
            
            // Set description
            tvDescription.setText(experience.getDescription());
            
            // Set up edit button click listener
            btnEditExperience.setOnClickListener(v -> navigateToExperienceForm(experience.getId()));
            
            // Add the view to the container
            experiencesContainer.addView(experienceView);
        } catch (Exception e) {
            Log.e(TAG, "Error adding experience item: " + e.getMessage());
        }
    }
    
    /**
     * Set company logo based on company name
     */
    private void setCompanyLogo(ImageView imageView, String companyName) {
        if (imageView == null || companyName == null) {
            return;
        }
        
        // Default background color
        int backgroundColor = getResources().getColor(android.R.color.holo_blue_light);
        
        // Set different background colors based on company name
        if (companyName.toLowerCase().contains("airbnb")) {
            backgroundColor = 0xFFFF5A5F; // Airbnb red
            // You could also set an image resource if available
            // imageView.setImageResource(R.drawable.ic_airbnb);
        } else if (companyName.toLowerCase().contains("dropbox")) {
            backgroundColor = 0xFF0061FF; // Dropbox blue
            // imageView.setImageResource(R.drawable.ic_dropbox);
        } else if (companyName.toLowerCase().contains("google")) {
            backgroundColor = 0xFF4285F4; // Google blue
            // imageView.setImageResource(R.drawable.ic_google);
        } else if (companyName.toLowerCase().contains("facebook") || 
                  companyName.toLowerCase().contains("meta")) {
            backgroundColor = 0xFF1877F2; // Facebook blue
            // imageView.setImageResource(R.drawable.ic_facebook);
        } else if (companyName.toLowerCase().contains("amazon")) {
            backgroundColor = 0xFFFF9900; // Amazon orange
            // imageView.setImageResource(R.drawable.ic_amazon);
        } else if (companyName.toLowerCase().contains("apple")) {
            backgroundColor = 0xFF999999; // Apple gray
            // imageView.setImageResource(R.drawable.ic_apple);
        } else if (companyName.toLowerCase().contains("microsoft")) {
            backgroundColor = 0xFF00A4EF; // Microsoft blue
            // imageView.setImageResource(R.drawable.ic_microsoft);
        } else {
            // Generate a consistent color based on the first letter of the company name
            if (!companyName.isEmpty()) {
                char firstChar = companyName.charAt(0);
                int hash = firstChar % 10;
                switch (hash) {
                    case 0: backgroundColor = 0xFF9C27B0; break; // Purple
                    case 1: backgroundColor = 0xFF2196F3; break; // Blue
                    case 2: backgroundColor = 0xFF4CAF50; break; // Green
                    case 3: backgroundColor = 0xFFFF5722; break; // Deep Orange
                    case 4: backgroundColor = 0xFF795548; break; // Brown
                    case 5: backgroundColor = 0xFF607D8B; break; // Blue Grey
                    case 6: backgroundColor = 0xFFE91E63; break; // Pink
                    case 7: backgroundColor = 0xFF009688; break; // Teal
                    case 8: backgroundColor = 0xFFFF9800; break; // Orange
                    case 9: backgroundColor = 0xFF673AB7; break; // Deep Purple
                }
            }
        }
        
        // Set the background color
        imageView.setBackgroundColor(backgroundColor);
        
        // Set the first letter of the company as text if no image
        if (!companyName.isEmpty()) {
            // This would require a custom view or using a TextView instead of ImageView
            // For now, we'll just use the colored background
        }
    }
    
    /**
     * Navigate to experience form fragment with the given experience ID
     */
    private void navigateToExperienceForm(long experienceId) {
        try {
            // Create a bundle to pass the experience ID
            Bundle args = new Bundle();
            args.putLong("experience_id", experienceId);
            
            // Navigate to experience form fragment
            Fragment experienceFormFragment = new com.example.jobportal.ui.experience.ExperienceFormFragment();
            experienceFormFragment.setArguments(args);
            
            // Navigate to form fragment
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, experienceFormFragment)
                    .addToBackStack(null)
                    .commit();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to experience form: " + e.getMessage());
            Toast.makeText(requireContext(), "Unable to edit experience", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void loadProfileData() {
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            // Redirect to login if not logged in
            navigateToLogin();
            return;
        }
        
        // Show loading indicator
        progressBar.setVisibility(View.VISIBLE);
        
        // Fetch profile data from API
        apiClient.getUserProfile(new ApiCallback<ApiResponse<User>>() {
            @Override
            public void onSuccess(ApiResponse<User> response) {
                // Hide loading indicator
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccess() && response.getData() != null) {
                    User user = response.getData();
                    
                    // Debug logs to check mobile field
                    Log.d(TAG, "User data received from API - Name: " + user.getFullName());
                    Log.d(TAG, "User data received from API - Email: " + user.getEmail());
                    Log.d(TAG, "User data received from API - Mobile: " + user.getPhone());
                    
                    // Update UI with user data
                    tvName.setText(user.getFullName());
                    tvEmail.setText(user.getEmail());
                    
                    // Check if phone number is null or empty
                    String phoneNumber = user.getPhone();
                    if (phoneNumber != null && !phoneNumber.isEmpty()) {
                        tvMobile.setText(phoneNumber);
                        tvMobile.setVisibility(View.VISIBLE);
                        Log.d(TAG, "Setting mobile text to: " + phoneNumber);
                    } else {
                        tvMobile.setText("Not provided");
                        tvMobile.setVisibility(View.VISIBLE);
                        Log.d(TAG, "Mobile field is null or empty, set to 'Not provided'");
                    }
                    
                    // Display location if available from the user model
                    if (user.getLocation() != null && !user.getLocation().isEmpty()) {
                        tvLocation.setText(user.getLocation());
                    } else {
                        tvLocation.setText("Location not provided");
                    }
                    tvLocation.setVisibility(View.VISIBLE);
                    
                    // Update new UI elements if they exist
                    updateExtendedProfileUI(user);
                    
                    // Save user data to session
                    sessionManager.saveUser(user);
                } else {
                    // Show error and fallback to saved user data
                    showError("Failed to load profile. Using saved data.");
                    loadSavedUserData();
                }
            }

            @Override
            public void onError(String errorMessage) {
                // Hide loading indicator
                progressBar.setVisibility(View.GONE);
                
                // Show error and fallback to saved user data
                showError("Error: " + errorMessage);
                loadSavedUserData();
            }
        });
    }
    /**
     * Updates the extended profile UI elements if they exist
     * @param user The user object containing profile data
     */
    private void updateExtendedProfileUI(User user) {
        if (user == null) return;
        
        try {
            // Update job title if available
            if (tvJobTitle != null && user.getJobTitle() != null && !user.getJobTitle().isEmpty()) {
                tvJobTitle.setText(user.getJobTitle());
                tvJobTitle.setVisibility(View.VISIBLE);
            } else if (tvJobTitle != null) {
                tvJobTitle.setVisibility(View.GONE);
            }
            
            // Update about me section if available
            if (tvAboutMe != null && user.getAboutMe() != null && !user.getAboutMe().isEmpty()) {
                tvAboutMe.setText(user.getAboutMe());
            } else if (tvAboutMe != null) {
                tvAboutMe.setText("Add a short bio to tell people about yourself.");
            }
            
            // Update profile photo if available
            if (profileImageView != null) {
                if (user.getProfilePhoto() != null && !user.getProfilePhoto().isEmpty()) {
                    // Use Glide to load the image from URL
                    Glide.with(requireContext())
                        .load(BuildConfig.API_BASE_URL + user.getProfilePhoto())
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .error(R.drawable.ic_profile_placeholder)
                        .centerCrop()
                        .into(profileImageView);
                } else {
                    // Set default placeholder
                    profileImageView.setImageResource(R.drawable.ic_profile_placeholder);
                }
            }
            
            // Update skills UI if skills are available
            if (user.getSkills() != null && !user.getSkills().isEmpty()) {
                updateSkillsUI(user.getSkills());
            }
            
            // Update resume UI if resume is available
            updateResumeUI(user);
        } catch (Exception e) {
            Log.e(TAG, "Error updating extended profile UI: " + e.getMessage());
        }
    }
    
    /**
     * Updates the resume UI with the user's resume data
     * @param user The user object containing resume data
     */
    private void updateResumeUI(User user) {
        if (resumeItem == null || resumeFileName == null || resumeUpdateDate == null) {
            return;
        }
        
        try {
            if (user.getResume() != null && !user.getResume().isEmpty()) {
                // Show resume item and set file name
                resumeItem.setVisibility(View.VISIBLE);
                
                // Extract file name from path
                String resumePath = user.getResume();
                String fileName = resumePath.substring(resumePath.lastIndexOf("/") + 1);
                resumeFileName.setText(fileName);
                
                // Set update date (using a placeholder for now as we don't have the actual date)
                resumeUpdateDate.setText("Updated recently");
                
                // Enable download button
                if (downloadResumeButton != null) {
                    downloadResumeButton.setEnabled(true);
                }
            } else {
                // Hide resume item if no resume is available
                resumeItem.setVisibility(View.GONE);
                
                // Disable download button
                if (downloadResumeButton != null) {
                    downloadResumeButton.setEnabled(false);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating resume UI: " + e.getMessage());
            resumeItem.setVisibility(View.GONE);
        }
    }
    
    /**
     * Downloads the user's resume
     */
    private void downloadResume() {
        // Get the current user from session
        User currentUser = sessionManager.getUser();
        if (currentUser == null || currentUser.getResume() == null || currentUser.getResume().isEmpty()) {
            Toast.makeText(requireContext(), "No resume available to download", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // Create download URL
            String resumeUrl = BuildConfig.API_BASE_URL + currentUser.getResume();
            
            // Create download manager request
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(resumeUrl));
            request.setTitle("Resume Download");
            request.setDescription("Downloading resume file");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, 
                    resumeUrl.substring(resumeUrl.lastIndexOf("/") + 1));
            
            // Get download service and enqueue the request
            DownloadManager downloadManager = (DownloadManager) requireContext().getSystemService(Context.DOWNLOAD_SERVICE);
            downloadManager.enqueue(request);
            
            Toast.makeText(requireContext(), "Resume download started", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error downloading resume: " + e.getMessage());
            Toast.makeText(requireContext(), "Failed to download resume: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Updates the skills UI with the user's skills
     * @param skillsString Comma-separated string of skills
     */
    private void updateSkillsUI(String skillsString) {
        if (skillsChipGroup == null || skillsString == null || skillsString.isEmpty()) {
            return;
        }
        
        try {
            // Clear existing chips
            skillsChipGroup.removeAllViews();
            
            // Split skills string by comma
            String[] skills = skillsString.split(",");
            
            // Add a chip for each skill
            for (String skill : skills) {
                String trimmedSkill = skill.trim();
                if (!trimmedSkill.isEmpty()) {
                    addSkillChip(trimmedSkill);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating skills UI: " + e.getMessage());
        }
    }
    
    /**
     * Adds a single skill chip to the ChipGroup
     * @param skill The skill text to display
     */
    private void addSkillChip(String skill) {
        if (skillsChipGroup == null || skill == null || skill.isEmpty() || getContext() == null) {
            return;
        }
        
        try {
            // Create a new chip
            com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(getContext());
            chip.setText(skill);
            chip.setTextColor(getResources().getColor(android.R.color.black));
            chip.setChipBackgroundColorResource(android.R.color.darker_gray);
            chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(0xFFF0F0F0));
            
            // Add the chip to the ChipGroup
            skillsChipGroup.addView(chip);
        } catch (Exception e) {
            Log.e(TAG, "Error adding skill chip: " + e.getMessage());
        }
    }
    
    private void loadSavedUserData() {
        // Try to load user data from session manager
        User user = sessionManager.getUser();
        if (user != null) {
            tvName.setText(user.getFullName());
            tvEmail.setText(user.getEmail());
            
            // Check if phone number is null or empty
            String phoneNumber = user.getPhone();
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                tvMobile.setText(phoneNumber);
                tvMobile.setVisibility(View.VISIBLE);
            } else {
                tvMobile.setText("Not provided");
                tvMobile.setVisibility(View.VISIBLE);
            }
            
            // Display location if available
            if (user.getLocation() != null && !user.getLocation().isEmpty()) {
                tvLocation.setText(user.getLocation());
            } else {
                tvLocation.setText("Location not provided");
            }
            tvLocation.setVisibility(View.VISIBLE);
            
            // Update extended profile UI
            updateExtendedProfileUI(user);
        } else {
            // If no saved data, show placeholder
            tvName.setText("No user data available");
            tvEmail.setText("");
            tvMobile.setText("");
            tvLocation.setText("");
            
            // Clear extended profile data if available
            if (tvJobTitle != null) tvJobTitle.setText("");
            if (tvAboutMe != null) tvAboutMe.setText("");
        }
    }

    private void handleLogout() {
        // Clear user session
        sessionManager.logout();
        
        // Navigate to login screen
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    
    /**
     * Navigate to the Experience List Fragment
     */
    private void navigateToExperienceList() {
        try {
            // Navigate to experience list fragment
            Fragment experienceListFragment = new com.example.jobportal.ui.experience.ExperienceListFragment();
            
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, experienceListFragment)
                    .addToBackStack(null)
                    .commit();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to experience list: " + e.getMessage());
            Toast.makeText(requireContext(), "Unable to open experiences", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void navigateToLogin() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
    
    private void showError(String message) {
        if (isAdded()) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            Log.e(TAG, message);
        }
    }
}