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
        Schema::table('recruiters', function (Blueprint $table) {
            $table->string('company_license')->nullable()->after('designation');
            $table->timestamp('license_uploaded_at')->nullable()->after('company_license');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('recruiters', function (Blueprint $table) {
            $table->dropColumn(['company_license', 'license_uploaded_at']);
        });
    }
};