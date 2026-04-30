<?php
/**
 * Script to fix job image filenames that contain spaces or special characters
 * This will rename the physical files and update the database records
 * 
 * Run this script once to fix existing images:
 * php fix_image_filenames.php
 */

require __DIR__.'/vendor/autoload.php';

$app = require_once __DIR__.'/bootstrap/app.php';
$app->make(Illuminate\Contracts\Console\Kernel::class)->bootstrap();

use App\Models\Job;
use Illuminate\Support\Facades\Storage;

echo "Starting image filename sanitization...\n\n";

$jobs = Job::whereNotNull('image')->get();
$fixedCount = 0;
$errorCount = 0;
$skippedCount = 0;

foreach ($jobs as $job) {
    $oldImagePath = $job->image;
    
    // Check if the filename contains spaces or special characters
    $filename = basename($oldImagePath);
    $sanitizedFilename = preg_replace('/[^A-Za-z0-9\-_\.]/', '_', $filename);
    $sanitizedFilename = preg_replace('/_+/', '_', $sanitizedFilename);
    
    // If filename is already clean, skip
    if ($filename === $sanitizedFilename) {
        echo "✓ Skipped (already clean): {$oldImagePath}\n";
        $skippedCount++;
        continue;
    }
    
    $directory = dirname($oldImagePath);
    $newImagePath = $directory . '/' . $sanitizedFilename;
    
    try {
        // Check if old file exists
        if (!Storage::disk('public')->exists($oldImagePath)) {
            echo "✗ Error: File not found: {$oldImagePath}\n";
            $errorCount++;
            continue;
        }
        
        // Check if new filename already exists
        if (Storage::disk('public')->exists($newImagePath)) {
            echo "⚠ Warning: Target file already exists: {$newImagePath}\n";
            // Still update the database to point to the existing sanitized file
            $job->update(['image' => $newImagePath]);
            $fixedCount++;
            continue;
        }
        
        // Rename the file
        Storage::disk('public')->move($oldImagePath, $newImagePath);
        
        // Update the database
        $job->update(['image' => $newImagePath]);
        
        echo "✓ Fixed: {$oldImagePath} → {$newImagePath}\n";
        $fixedCount++;
        
    } catch (Exception $e) {
        echo "✗ Error processing {$oldImagePath}: " . $e->getMessage() . "\n";
        $errorCount++;
    }
}

echo "\n";
echo "========================================\n";
echo "Summary:\n";
echo "  Fixed: {$fixedCount}\n";
echo "  Skipped (already clean): {$skippedCount}\n";
echo "  Errors: {$errorCount}\n";
echo "  Total processed: " . ($fixedCount + $skippedCount + $errorCount) . "\n";
echo "========================================\n";
echo "\nDone!\n";
