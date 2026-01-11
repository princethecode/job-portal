package com.emps.abroadjobs.ui.FeaturedJobs;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.emps.abroadjobs.data.repository.FeaturedJobRepository;
import com.emps.abroadjobs.models.FeaturedJob;

import java.util.List;

public class FeaturedJobsViewModel extends AndroidViewModel {
    private final FeaturedJobRepository repository;

    public FeaturedJobsViewModel(Application application) {
        super(application);
        repository = new FeaturedJobRepository(application);
        // Fetch data as soon as the ViewModel is created
        fetchFeaturedJobs();
    }

    public LiveData<List<FeaturedJob>> getFeaturedJobs() { 
        return repository.getFeaturedJobs(); 
    }
    
    public LiveData<Boolean> getIsLoading() { 
        return repository.getIsLoading(); 
    }
    
    public LiveData<String> getError() { 
        return repository.getError(); 
    }

    public void fetchFeaturedJobs() {
        repository.fetchFeaturedJobs();
    }
}
