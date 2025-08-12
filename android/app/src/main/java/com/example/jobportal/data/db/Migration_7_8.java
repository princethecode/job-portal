package com.example.jobportal.data.db;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * Migration from schema version 7 to 8
 * Makes all string columns in Experience table nullable to prevent binding errors
 */
public class Migration_7_8 extends Migration {
    
    public Migration_7_8() {
        super(7, 8);
    }
    
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
        // Create a new table with all columns nullable
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS experiences_new (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "userId TEXT, " +  // Nullable foreign key
            "job_title TEXT, " +  // Nullable
            "company_name TEXT, " +  // Nullable
            "start_date TEXT, " +  // Nullable
            "end_date TEXT, " +  // Nullable
            "is_current INTEGER NOT NULL DEFAULT 0, " +
            "description TEXT, " +  // Nullable
            "FOREIGN KEY (userId) REFERENCES users(id) ON DELETE SET NULL" +
            ")"
        );
        
        // Copy data from old table to new table
        database.execSQL(
            "INSERT INTO experiences_new (id, userId, job_title, company_name, start_date, end_date, is_current, description) " +
            "SELECT id, userId, job_title, company_name, start_date, end_date, is_current, description FROM experiences"
        );
        
        // Drop old table
        database.execSQL("DROP TABLE experiences");
        
        // Rename new table to original name
        database.execSQL("ALTER TABLE experiences_new RENAME TO experiences");
        
        // Create index for userId
        database.execSQL("CREATE INDEX index_experiences_userId ON experiences (userId)");
    }
}
