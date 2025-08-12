<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up()
    {
        Schema::create('labels', function (Blueprint $table) {
            $table->id();
            $table->string('name');
            $table->string('color')->default('#000000');
            $table->timestamps();
        });

        // Add label_id to contacts table
        Schema::table('contacts', function (Blueprint $table) {
            $table->foreignId('label_id')->nullable()->after('email')->constrained()->onDelete('set null');
            $table->string('import_tag')->nullable()->after('label_id'); // To store date or user info for imports
        });
    }

    public function down()
    {
        Schema::table('contacts', function (Blueprint $table) {
            $table->dropForeign(['label_id']);
            $table->dropColumn(['label_id', 'import_tag']);
        });
        Schema::dropIfExists('labels');
    }
}; 