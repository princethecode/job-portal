<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::table('jobs', function (Blueprint $table) {
            // Add approval status enum: pending, approved, declined
            $table->enum('approval_status', ['pending', 'approved', 'declined'])->default('pending')->after('is_active');
            
            // Add admin who approved/declined the job
            $table->unsignedBigInteger('approved_by')->nullable()->after('approval_status');
            
            // Add timestamp when approval/decline occurred
            $table->timestamp('approved_at')->nullable()->after('approved_by');
            
            // Add reason for decline (optional)
            $table->text('decline_reason')->nullable()->after('approved_at');
            
            // Add foreign key constraint for approved_by (referring to users table for admin)
            $table->foreign('approved_by')->references('id')->on('users')->onDelete('set null');
            
            // Add index for faster querying by approval status
            $table->index('approval_status');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('jobs', function (Blueprint $table) {
            // Drop foreign key constraint first
            $table->dropForeign(['approved_by']);
            
            // Drop the index
            $table->dropIndex(['approval_status']);
            
            // Drop the columns
            $table->dropColumn([
                'approval_status',
                'approved_by', 
                'approved_at',
                'decline_reason'
            ]);
        });
    }
};