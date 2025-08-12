package com.example.jobportal.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.io.File;

import com.example.jobportal.models.Job;
import com.example.jobportal.models.User;
import com.example.jobportal.models.Experience;

@Database(entities = {Job.class, User.class, Experience.class}, version = 8, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "jobportal_db";
    private static AppDatabase instance;

    public abstract JobDao jobDao();
    public abstract UserDao userDao();
    public abstract ExperienceDao experienceDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            // Get the database file path for deletion if needed
            File dbFile = context.getDatabasePath(DATABASE_NAME);
            
            // If database exists and we're having issues, delete it to force a fresh start
            if (dbFile.exists()) {
                // Attempt to delete the database file
                context.deleteDatabase(DATABASE_NAME);
            }
            
            instance = Room.databaseBuilder(
                context.getApplicationContext(),
                AppDatabase.class,
                DATABASE_NAME
            )
            .addMigrations(new Migration_5_6(), new Migration_6_7(), new Migration_7_8()) // Add explicit migrations
            .fallbackToDestructiveMigration() // Allow rebuilding the database if schema changes
            .build();
        }
        return instance;
    }
}