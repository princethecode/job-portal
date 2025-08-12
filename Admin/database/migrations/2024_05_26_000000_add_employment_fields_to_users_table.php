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
        Schema::table('users', function (Blueprint $table) {
            $table->string('current_company')->nullable()->after('skills');
            $table->string('department')->nullable()->after('current_company');
            $table->decimal('current_salary', 10, 2)->nullable()->after('department');
            $table->decimal('expected_salary', 10, 2)->nullable()->after('current_salary');
            $table->string('joining_period')->nullable()->after('expected_salary');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->dropColumn([
                'current_company',
                'department',
                'current_salary',
                'expected_salary',
                'joining_period'
            ]);
        });
    }
}; 