package com.example.jobportal.data.db;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * Migration class to handle database schema changes from version 5 to 6
 * Adds the experience table to store user work experiences
 */
public class Migration_5_6 extends Migration {

    public Migration_5_6() {
        super(5, 6);
    }

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
        // Create the experiences table
        database.execSQL(
                "CREATE TABLE IF NOT EXISTS `experiences` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`user_id` INTEGER NOT NULL, " +
                        "`job_title` TEXT NOT NULL, " +
                        "`company_name` TEXT NOT NULL, " +
                        "`start_date` TEXT NOT NULL, " +
                        "`end_date` TEXT, " +
                        "`is_current` INTEGER NOT NULL DEFAULT 0, " +
                        "`description` TEXT, " +
                        "`created_at` TEXT, " +
                        "`updated_at` TEXT, " +
                        "FOREIGN KEY(`user_id`) REFERENCES `users`(`id`) ON UPDATE CASCADE ON DELETE CASCADE)"
        );

        // Create an index on the user_id column for faster lookups
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_experiences_user_id` ON `experiences` (`user_id`)");
    }
}
