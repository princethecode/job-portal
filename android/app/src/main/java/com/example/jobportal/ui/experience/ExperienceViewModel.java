package com.example.jobportal.ui.experience;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.jobportal.data.repository.ExperienceRepository;
import com.example.jobportal.models.Experience;
import com.example.jobportal.network.ApiCallback;
import com.example.jobportal.network.ApiResponse;

import java.util.List;

public class ExperienceViewModel extends AndroidViewModel {
    
    private final ExperienceRepository repository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    public ExperienceViewModel(@NonNull Application application) {
        super(application);
        repository = new ExperienceRepository(application);
        
        // Observe repository loading and error states
        observeRepositoryStates();
    }
    
    private void observeRepositoryStates() {
        // Link repository loading state to view model loading state
        repository.getIsLoading().observeForever(isLoading -> {
            this.isLoading.setValue(isLoading);
        });
        
        // Link repository error messages to view model error messages
        repository.getErrorMessage().observeForever(error -> {
            if (error != null && !error.isEmpty()) {
                this.errorMessage.setValue(error);
            }
        });
    }
    
    public LiveData<List<Experience>> getUserExperiences() {
        return repository.getUserExperiences();
    }
    
    public LiveData<Experience> getExperienceById(long experienceId) {
        return repository.getExperienceById(experienceId);
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public void saveExperience(Experience experience) {
        isLoading.setValue(true);
        
        if (experience.getId() > 0) {
            // Update existing experience
            repository.updateExperience(experience, new ApiCallback<ApiResponse<Experience>>() {
                @Override
                public void onSuccess(ApiResponse<Experience> response) {
                    isLoading.setValue(false);
                    if (!response.isSuccess()) {
                        errorMessage.setValue(response.getMessage());
                    }
                }
                
                @Override
                public void onError(String message) {
                    isLoading.setValue(false);
                    errorMessage.setValue(message);
                }
            });
        } else {
            // Insert new experience
            repository.insertExperience(experience, new ApiCallback<ApiResponse<Experience>>() {
                @Override
                public void onSuccess(ApiResponse<Experience> response) {
                    isLoading.setValue(false);
                    if (!response.isSuccess()) {
                        errorMessage.setValue(response.getMessage());
                    }
                }
                
                @Override
                public void onError(String message) {
                    isLoading.setValue(false);
                    errorMessage.setValue(message);
                }
            });
        }
    }
    
    public void deleteExperience(Experience experience) {
        isLoading.setValue(true);
        
        repository.deleteExperience(experience, new ApiCallback<ApiResponse<Void>>() {
            @Override
            public void onSuccess(ApiResponse<Void> response) {
                isLoading.setValue(false);
                if (!response.isSuccess()) {
                    errorMessage.setValue(response.getMessage());
                }
            }
            
            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }
    
    /**
     * Sync experiences with the server
     * This method fetches the latest experiences from the server and updates the local database
     */
    public void syncExperiences() {
        repository.syncExperiences();
    }
}
