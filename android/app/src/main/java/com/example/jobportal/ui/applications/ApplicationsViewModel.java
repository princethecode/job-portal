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
                
                try {
                    if (response.isSuccess() && response.getData() != null) {
                        Log.d(TAG, "Response data: " + new Gson().toJson(response.getData()));
                        
                        // Process the nested structure of response
                        Map<String, Object> data = response.getData();
                        if (data.containsKey("jobs")) {
                            Object jobsObj = data.get("jobs");
                            List<com.example.jobportal.models.Application> applicationList = new ArrayList<>();
                            
                            if (jobsObj instanceof List) {
                                Log.d(TAG, "Jobs is a List with " + ((List<?>) jobsObj).size() + " items");
                                processJobsList((List<?>) jobsObj, applicationList);
                            } else if (jobsObj instanceof Map) {
                                Log.d(TAG, "Jobs is a Map, trying to handle as array-like object");
                                processJobsMap((Map<?, ?>) jobsObj, applicationList);
                            } else {
                                Log.e(TAG, "Jobs is an unknown type: " + (jobsObj != null ? jobsObj.getClass().getName() : "null"));
                                error.postValue("Cannot parse jobs data format");
                                return;
                            }
                            
                            applications.postValue(applicationList);
                            if (applicationList.isEmpty()) {
                                error.postValue("No applications found");
                            }
                        } else {
                            Log.e(TAG, "No 'jobs' key in response data: " + new Gson().toJson(data));
                            error.postValue("No applications data found");
                        }
                    } else {
                        String message = response.getMessage() != null ? response.getMessage() : "Failed to load applications";
                        Log.e(TAG, "API error: " + message);
                        error.postValue(message);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing application data", e);
                    error.postValue("Error parsing data: " + e.getMessage());
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "onError: " + errorMessage);
                loading.postValue(false);
                error.postValue(errorMessage);
            }
        });
    }
    
    private void processJobsList(List<?> jobs, List<com.example.jobportal.models.Application> applicationList) {
        for (Object jobObj : jobs) {
            if (!(jobObj instanceof Map)) {
                Log.e(TAG, "Job item is not a Map: " + jobObj.getClass().getName());
                continue;
            }
            
            Map<?, ?> jobMap = (Map<?, ?>) jobObj;
            if (jobMap.containsKey("application")) {
                try {
                    // Extract job data
                    String jobId = String.valueOf(jobMap.get("id"));
                    String title = String.valueOf(jobMap.get("title"));
                    String company = String.valueOf(jobMap.get("company"));
                    String location = String.valueOf(jobMap.get("location"));
                    
                    // Extract application data
                    Object appObj = jobMap.get("application");
                    if (!(appObj instanceof Map)) {
                        Log.e(TAG, "Application is not a Map: " + appObj.getClass().getName());
                        continue;
                    }
                    
                    Map<?, ?> appData = (Map<?, ?>) appObj;
                    String appId = String.valueOf(appData.get("id"));
                    String status = String.valueOf(appData.get("status"));
                    String appliedDate = String.valueOf(appData.get("applied_date"));
                    
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
                } catch (Exception e) {
                    Log.e(TAG, "Error processing job data", e);
                }
            }
        }
    }
    
    private void processJobsMap(Map<?, ?> jobsMap, List<com.example.jobportal.models.Application> applicationList) {
        // Sometimes the API might return a map with numeric keys instead of a list
        for (Object key : jobsMap.keySet()) {
            Object jobObj = jobsMap.get(key);
            if (jobObj instanceof Map) {
                Map<?, ?> jobMap = (Map<?, ?>) jobObj;
                
                try {
                    // Extract job data
                    String jobId = String.valueOf(jobMap.get("id"));
                    String title = String.valueOf(jobMap.get("title"));
                    String company = String.valueOf(jobMap.get("company"));
                    String location = String.valueOf(jobMap.get("location"));
                    
                    // Extract application data if available
                    if (jobMap.containsKey("application")) {
                        Object appObj = jobMap.get("application");
                        if (!(appObj instanceof Map)) {
                            continue;
                        }
                        
                        Map<?, ?> appData = (Map<?, ?>) appObj;
                        String appId = String.valueOf(appData.get("id"));
                        String status = String.valueOf(appData.get("status"));
                        String appliedDate = String.valueOf(appData.get("applied_date"));
                        
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
                } catch (Exception e) {
                    Log.e(TAG, "Error processing job data from map", e);
                }
            }
        }
    }
}