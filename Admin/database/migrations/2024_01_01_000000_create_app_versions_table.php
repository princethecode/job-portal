<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up()
    {
        Schema::create('app_versions', function (Blueprint $table) {
            $table->id();
            $table->string('platform')->default('android'); // android, ios
            $table->string('version_name'); // 1.0.1
            $table->integer('version_code'); // 2
            $table->string('minimum_version_name')->nullable(); // minimum required version
            $table->integer('minimum_version_code')->nullable(); // minimum required version code
            $table->boolean('force_update')->default(false);
            $table->text('update_message')->nullable();
            $table->string('download_url')->nullable();
            $table->boolean('is_active')->default(true);
            $table->timestamps();
        });
    }

    public function down()
    {
        Schema::dropIfExists('app_versions');
    }
};