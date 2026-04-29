<?php
/**
 * Diagnostic script to check resume paths in database vs actual files
 * Run with: php check_resume_paths.php
 */

require __DIR__.'/vendor/autoload.php';

$app = require_once __DIR__.'/bootstrap/app.php';
$app->make('Illuminate\Contracts\Console\Kernel')->bootstrap();

use App\Models\Application;

echo "=== Resume Path Diagnostic ===\n\n";

// Get all applications with resume paths
$applications = Application::whereNotNull('resume_path')->get();

echo "Found " . $applications->count() . " applications with resumes\n\n";

$storagePath = storage_path('app/public/resumes/');
echo "Storage path: $storagePath\n\n";

foreach ($applications as $app) {
    echo "Application ID: {$app->id}\n";
    echo "Database path: {$app->resume_path}\n";
    
    $filename = basename($app->resume_path);
    echo "Extracted filename: $filename\n";
    
    $fullPath = $storagePath . $filename;
    echo "Full path: $fullPath\n";
    echo "File exists: " . (file_exists($fullPath) ? "YES" : "NO") . "\n";
    
    if (!file_exists($fullPath)) {
        echo "⚠️  WARNING: File not found!\n";
    }
    
    echo "\n" . str_repeat("-", 50) . "\n\n";
}

// List actual files in storage
echo "\n=== Actual files in storage ===\n";
$files = scandir($storagePath);
foreach ($files as $file) {
    if ($file !== '.' && $file !== '..') {
        echo "- $file\n";
    }
}
