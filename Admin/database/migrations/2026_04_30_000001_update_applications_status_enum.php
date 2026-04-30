<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;
use Illuminate\Support\Facades\DB;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        // For MySQL, we need to alter the enum column
        DB::statement("ALTER TABLE applications MODIFY COLUMN status ENUM('Applied', 'Pending', 'Under Review', 'Reviewing', 'Shortlisted', 'Rejected', 'Hired') DEFAULT 'Applied'");
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        // Revert to original enum values
        DB::statement("ALTER TABLE applications MODIFY COLUMN status ENUM('Applied', 'Under Review', 'Shortlisted', 'Rejected') DEFAULT 'Applied'");
    }
};
