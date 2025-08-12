package com.example.jobportal.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.jobportal.models.Experience;

import java.util.List;

@Dao
public interface ExperienceDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Experience experience);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Experience> experiences);
    
    @Update
    void update(Experience experience);
    
    @Delete
    void delete(Experience experience);
    
    @Query("SELECT * FROM experiences WHERE userId = :userId ORDER BY startDate DESC")
    LiveData<List<Experience>> getUserExperiences(String userId);
    
    @Query("SELECT * FROM experiences WHERE id = :id")
    LiveData<Experience> getExperienceById(long id);
    
    @Query("DELETE FROM experiences WHERE userId = :userId")
    void deleteAllUserExperiences(String userId);
}
