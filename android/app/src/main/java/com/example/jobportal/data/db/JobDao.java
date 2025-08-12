package com.example.jobportal.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.jobportal.models.Job;

import java.util.List;

@Dao
public interface JobDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Job job);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Job> jobs);

    @Query("SELECT * FROM jobs")
    LiveData<List<Job>> getAllJobs();

    @Query("SELECT * FROM jobs WHERE id = :jobId")
    LiveData<Job> getJobById(String jobId);

    @Query("DELETE FROM jobs")
    void deleteAllJobs();

    @Query("SELECT * FROM jobs WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    LiveData<List<Job>> searchJobs(String query);
    
    @Query("SELECT * FROM jobs WHERE category LIKE :category")
    LiveData<List<Job>> getJobsByCategory(String category);
} 