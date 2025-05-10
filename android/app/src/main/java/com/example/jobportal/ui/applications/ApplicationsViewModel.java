package com.example.jobportal.ui.applications;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.jobportal.models.Job;
import com.example.jobportal.network.ApiCallback;
import com.example.jobportal.network.ApiClient;
import com.example.jobportal.network.ApiResponse;
import com.example.jobportal.network.ApiService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApplicationsViewModel extends AndroidViewModel {
    private static final String TAG = "ApplicationsViewModel";
    private final MutableLiveData<List<com.example.jobportal.models.Application>> applications = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final ApiClient apiClient;

    public ApplicationsViewModel(Application application) {
        super(application);
        apiClient = ApiClient.getInstance(application.getApplicationContext());
        loadApplications();
    }

    public LiveData<List<com.example.jobportal.models.Application>> getApplications() {
        return applications;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public void loadApplications() {
        loading.setValue(true);
        
        // Use the new endpoint
        apiClient.getUserAppliedJobs(new ApiCallback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onSuccess(ApiResponse<Map<String, Object>> response) {
                loading.postValue(false);
                if (response.isSuccess() && response.getData() != null) {
                    try {
                        // Process the nested structure of response
                        Map<String, Object> data = response.getData();
                        if (data.containsKey("jobs")) {
                            Object jobsObj = data.get("jobs");
                            List<com.example.jobportal.models.Application> applicationList = new ArrayList<>();
                            
                            if (jobsObj instanceof List) {
                                List<Map<String, Object>> jobs = (List<Map<String, Object>>) jobsObj;
                                
                                for (Map<String, Object> jobMap : jobs) {
                                    if (jobMap.containsKey("application")) {
                                        // Extract job data
                                        String jobId = String.valueOf(jobMap.get("id"));
                                        String title = (String) jobMap.get("title");
                                        String company = (String) jobMap.get("company");
                                        String location = (String) jobMap.get("location");
                                        
                                        // Extract application data
                                        Map<String, Object> appData = (Map<String, Object>) jobMap.get("application");
                                        String appId = String.valueOf(appData.get("id"));
                                        String status = (String) appData.get("status");
                                        String appliedDate = (String) appData.get("applied_date");
                                        
                                        // Create application object
                                        com.example.jobportal.models.Application application = 
                                            new com.example.jobportal.models.Application();
                                        application.setId(appId);
                                        application.setJobTitle(title);
                                        application.setCompany(company);
                                        application.setStatus(status);
                                        application.setApplicationDate(appliedDate);
                                        
                                        // Create job object and associate with application
                                        Job job = new Job();
                                        job.setId(jobId);
                                        job.setTitle(title);
                                        job.setCompany(company);
                                        job.setLocation(location);
                                        application.setJob(job);
                                        
                                        applicationList.add(application);
                                    }
                                }
                                
                                applications.postValue(applicationList);
                            }
                        } else {
                            error.postValue("No applications data found");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing application data", e);
                        error.postValue("Error parsing data: " + e.getMessage());
                    }
                } else {
                    error.postValue("Failed to load applications");
                }
            }

            @Override
            public void onError(String errorMessage) {
                loading.postValue(false);
                error.postValue(errorMessage);
            }
        });
    }
}