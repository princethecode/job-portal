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
        Schema::create('featured_jobs', function (Blueprint $table) {
            $table->id();
            $table->string('company_logo')->nullable();
            $table->string('job_title');
            $table->string('company_name');
            $table->string('location');
            $table->string('salary');
            $table->string('job_type');
            $table->text('description');
            $table->timestamp('posted_date')->useCurrent();
            $table->boolean('is_active')->default(true);
            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('featured_jobs');
    }
};
