<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;
use Illuminate\Support\Facades\DB;

return new class extends Migration
{
    public function up()
    {
        // Create the pivot table
        Schema::create('contact_label', function (Blueprint $table) {
            $table->id();
            $table->foreignId('contact_id')->constrained()->onDelete('cascade');
            $table->foreignId('label_id')->constrained()->onDelete('cascade');
            $table->timestamps();

            // Ensure a contact can't have the same label twice
            $table->unique(['contact_id', 'label_id']);
        });

        // Migrate existing label_id to the pivot table
        DB::statement('
            INSERT INTO contact_label (contact_id, label_id, created_at, updated_at)
            SELECT id, label_id, NOW(), NOW()
            FROM contacts
            WHERE label_id IS NOT NULL
        ');

        // Remove the label_id column from contacts table
        Schema::table('contacts', function (Blueprint $table) {
            $table->dropForeign(['label_id']);
            $table->dropColumn('label_id');
        });
    }

    public function down()
    {
        // Add back the label_id column to contacts
        Schema::table('contacts', function (Blueprint $table) {
            $table->foreignId('label_id')->nullable()->after('email')->constrained()->onDelete('set null');
        });

        // Migrate data back from pivot table
        DB::statement('
            UPDATE contacts c
            SET label_id = (
                SELECT label_id 
                FROM contact_label cl 
                WHERE cl.contact_id = c.id 
                ORDER BY cl.created_at DESC 
                LIMIT 1
            )
        ');

        // Drop the pivot table
        Schema::dropIfExists('contact_label');
    }
}; 