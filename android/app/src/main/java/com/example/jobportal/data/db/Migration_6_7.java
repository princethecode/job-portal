package com.example.jobportal.data.db;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * Migration class to handle database schema changes from version 6 to 7
 * Updates the User table with additional employment-related fields
 */
public class Migration_6_7 extends Migration {

    public Migration_6_7() {
        super(6, 7);
    }

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
        // Add new columns to the users table that were added in version 7
        database.execSQL("ALTER TABLE users ADD COLUMN current_company TEXT");
        database.execSQL("ALTER TABLE users ADD COLUMN department TEXT");
        database.execSQL("ALTER TABLE users ADD COLUMN current_salary TEXT");
        database.execSQL("ALTER TABLE users ADD COLUMN expected_salary TEXT");
        database.execSQL("ALTER TABLE users ADD COLUMN joining_period TEXT");
        database.execSQL("ALTER TABLE users ADD COLUMN contact TEXT");
        database.execSQL("ALTER TABLE users ADD COLUMN last_contact_sync TEXT");
    }
}
