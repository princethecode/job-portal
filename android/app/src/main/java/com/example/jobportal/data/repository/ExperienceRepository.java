package com.example.jobportal.data.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.jobportal.models.Experience;
import com.example.jobportal.models.User;
import com.example.jobportal.network.ApiCallback;
import com.example.jobportal.network.ApiClient;
import com.example.jobportal.network.ApiClientExperience;
import com.example.jobportal.network.ApiResponse;
import com.example.jobportal.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class ExperienceRepository {
    private static final String TAG = "ExperienceRepository";
    
    private final ApiClient apiClient;
    private final ApiClientExperience experienceApiClient;
    private final SessionManager sessionManager;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<List<Experience>> userExperiences = new MutableLiveData<>();
    
    public ExperienceRepository(Application application) {
        apiClient = ApiClient.getInstance(application);
        experienceApiClient = ApiClientExperience.getInstance();
        sessionManager = SessionManager.getInstance(application);
    }
    
    // Get loading state
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    // Get error message
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    // Get the cached experiences
    public LiveData<List<Experience>> getUserExperiences() {
        // If we don't have experiences, fetch them
        if (userExperiences.getValue() == null || userExperiences.getValue().isEmpty()) {
            fetchExperiencesFromApi();
        }
        return userExperiences;
    }
    
    // Fetch experiences from API
    public void fetchExperiencesFromApi() {
        User user = sessionManager.getUser();
        if (user == null) {
            errorMessage.postValue("User not logged in");
            return;
        }
        
        isLoading.postValue(true);
        
        experienceApiClient.getUserExperiences(new ApiCallback<List<Experience>>() {
            @Override
            public void onSuccess(List<Experience> experiences) {
                // Process and validate experiences
                List<Experience> validExperiences = new ArrayList<>();
                for (Experience exp : experiences) {
                    // Make sure essential fields are not null
                    if (exp.getJobTitle() != null && exp.getCompanyName() != null && 
                        exp.getStartDate() != null) {
                        // Set userId if needed for display purposes
                        if (user.getId() != null && !user.getId().isEmpty()) {
                            exp.setUserId(user.getId());
                        }
                        validExperiences.add(exp);
                    } else {
                        Log.w(TAG, "Skipping experience with null essential fields");
                    }
                }
                
                // Update the LiveData
                userExperiences.postValue(validExperiences);
                Log.d(TAG, "Successfully loaded " + validExperiences.size() + " experiences from API");
                isLoading.postValue(false);
            }
            
            @Override
            public void onError(String errorMsg) {
                Log.e(TAG, "Error fetching experiences: " + errorMsg);
                errorMessage.postValue(errorMsg);
                isLoading.postValue(false);
            }
        });
    }
    
    // Get a specific experience by ID
    public LiveData<Experience> getExperienceById(long experienceId) {
        MutableLiveData<Experience> result = new MutableLiveData<>();
        
        // Check if we already have experiences loaded
        List<Experience> experiences = userExperiences.getValue();
        if (experiences != null) {
            // Find the experience with matching ID
            for (Experience exp : experiences) {
                if (exp.getId() == experienceId) {
                    result.setValue(exp);
                    return result;
                }
            }
        }
        
        // If not found or no experiences loaded, fetch from API
        fetchExperiencesFromApi();
        return result;
    }
    
    // Insert a new experience
    public void insertExperience(Experience experience, ApiCallback<ApiResponse<Experience>> callback) {
        User user = sessionManager.getUser();
        if (user == null) {
            callback.onError("User not logged in");
            return;
        }
        
        // Set the user ID for the experience
        experience.setUserId(user.getId());
        
        // Send to API
        experienceApiClient.addExperience(experience, new ApiCallback<Experience>() {
            @Override
            public void onSuccess(Experience serverExperience) {
                // Add to our cached list
                List<Experience> currentExperiences = userExperiences.getValue();
                if (currentExperiences != null) {
                    List<Experience> updatedList = new ArrayList<>(currentExperiences);
                    updatedList.add(serverExperience);
                    userExperiences.postValue(updatedList);
                } else {
                    List<Experience> newList = new ArrayList<>();
                    newList.add(serverExperience);
                    userExperiences.postValue(newList);
                }
                callback.onSuccess(new ApiResponse<>(true, "Experience added successfully", serverExperience));
            }
            
            @Override
            public void onError(String errorMsg) {
                callback.onError(errorMsg);
            }
        });
    }
    
    // Update an existing experience
    public void updateExperience(Experience experience, ApiCallback<ApiResponse<Experience>> callback) {
        User user = sessionManager.getUser();
        if (user == null) {
            callback.onError("User not logged in");
            return;
        }
        
        // Ensure the user ID is set
        experience.setUserId(user.getId());
        
        // Send to API
        experienceApiClient.updateExperience(experience.getId(), experience, new ApiCallback<Experience>() {
            @Override
            public void onSuccess(Experience updatedExperience) {
                // Update in our cached list
                List<Experience> currentExperiences = userExperiences.getValue();
                if (currentExperiences != null) {
                    List<Experience> updatedList = new ArrayList<>();
                    for (Experience exp : currentExperiences) {
                        if (exp.getId() == updatedExperience.getId()) {
                            updatedList.add(updatedExperience); // Replace with updated version
                        } else {
                            updatedList.add(exp); // Keep existing
                        }
                    }
                    userExperiences.postValue(updatedList);
                }
                callback.onSuccess(new ApiResponse<>(true, "Experience updated successfully", updatedExperience));
            }
            
            @Override
            public void onError(String errorMsg) {
                callback.onError(errorMsg);
            }
        });
    }
    
    // Delete an experience
    public void deleteExperience(Experience experience, ApiCallback<ApiResponse<Void>> callback) {
        User user = sessionManager.getUser();
        if (user == null) {
            callback.onError("User not logged in");
            return;
        }
        
        // Send delete request to API
        experienceApiClient.deleteExperience(experience.getId(), new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Remove from our cached list
                List<Experience> currentExperiences = userExperiences.getValue();
                if (currentExperiences != null) {
                    List<Experience> updatedList = new ArrayList<>();
                    for (Experience exp : currentExperiences) {
                        if (exp.getId() != experience.getId()) {
                            updatedList.add(exp); // Keep all except the deleted one
                        }
                    }
                    userExperiences.postValue(updatedList);
                }
                callback.onSuccess(new ApiResponse<>(true, "Experience deleted successfully", null));
            }
            
            @Override
            public void onError(String errorMsg) {
                callback.onError(errorMsg);
            }
        });
    }
    
    // Convenience method for ViewModel to save an experience (insert or update)
    public void saveExperience(Experience experience, ApiCallback<ApiResponse<Experience>> callback) {
        if (experience.getId() > 0) {
            updateExperience(experience, callback);
        } else {
            insertExperience(experience, callback);
        }
    }
    
    // Refresh experiences from API (to be called from UI when refresh is needed)
    public void refreshExperiences() {
        fetchExperiencesFromApi();
    }
    
    /**
     * Sync experiences with the server (no callback version for backward compatibility)
     * This method fetches the latest experiences from the server
     */
    public void syncExperiences() {
        fetchExperiencesFromApi();
    }
    
    // Load experiences from server with callback
    public void syncExperiences(ApiCallback<ApiResponse<List<Experience>>> callback) {
        User user = sessionManager.getUser();
        if (user == null) {
            callback.onError("User not logged in");
            return;
        }
        
        isLoading.postValue(true);
        
        // Fetch directly from API
        experienceApiClient.getUserExperiences(new ApiCallback<List<Experience>>() {
            @Override
            public void onSuccess(List<Experience> experiences) {
                // Process and validate experiences
                List<Experience> validExperiences = new ArrayList<>();
                for (Experience exp : experiences) {
                    if (exp.getJobTitle() != null && exp.getCompanyName() != null && 
                        exp.getStartDate() != null) {
                        // Set userId if needed for display purposes
                        if (user.getId() != null && !user.getId().isEmpty()) {
                            exp.setUserId(user.getId());
                        }
                        validExperiences.add(exp);
                    }
                }
                
                // Update the cache
                userExperiences.postValue(validExperiences);
                
                isLoading.postValue(false);
                callback.onSuccess(new ApiResponse<>(true, "Experiences loaded successfully", validExperiences));
            }
            
            @Override
            public void onError(String errorMsg) {
                Log.e(TAG, "Error loading experiences: " + errorMsg);
                isLoading.postValue(false);
                errorMessage.postValue(errorMsg);
                callback.onError(errorMsg);
            }
        });
    }
}
