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
            $table->foreignId('recruiter_id')->nullable()->constrained()->onDelete('cascade');
            $table->text('requirements')->nullable();
            $table->text('benefits')->nullable();
            $table->string('experience_required')->nullable(); // 0-1, 1-3, 3-5, 5+ years
            $table->json('skills_required')->nullable();
            $table->integer('views_count')->default(0);
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('jobs', function (Blueprint $table) {
            $table->dropForeign(['recruiter_id']);
            $table->dropColumn([
                'recruiter_id',
                'requirements',
                'benefits',
                'experience_required',
                'skills_required',
                'views_count'
            ]);
        });
    }
};
