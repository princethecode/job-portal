<?php
/**
 * Simple Upload Test Script
 * 
 * This script tests if file uploads work on your server
 * Place this in your Admin directory and access via browser
 * 
 * URL: https://emps.co.in/test_upload.php
 * 
 * IMPORTANT: Delete this file after testing for security!
 */

// Enable error reporting
error_reporting(E_ALL);
ini_set('display_errors', 1);

?>
<!DOCTYPE html>
<html>
<head>
    <title>Upload Test</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 800px;
            margin: 50px auto;
            padding: 20px;
        }
        .success { color: green; }
        .error { color: red; }
        .info { color: blue; }
        .warning { color: orange; }
        pre {
            background: #f4f4f4;
            padding: 10px;
            border-radius: 5px;
            overflow-x: auto;
        }
        .section {
            margin: 20px 0;
            padding: 15px;
            border: 1px solid #ddd;
            border-radius: 5px;
        }
    </style>
</head>
<body>
    <h1>🔧 Upload Test Script</h1>
    
    <?php
    // Check if form was submitted
    if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_FILES['test_file'])) {
        echo '<div class="section">';
        echo '<h2>Upload Test Results</h2>';
        
        $file = $_FILES['test_file'];
        
        // Display file info
        echo '<h3>File Information:</h3>';
        echo '<pre>';
        print_r($file);
        echo '</pre>';
        
        // Check for upload errors
        if ($file['error'] !== UPLOAD_ERR_OK) {
            echo '<p class="error">❌ Upload Error Code: ' . $file['error'] . '</p>';
            
            $errors = [
                UPLOAD_ERR_INI_SIZE => 'File exceeds upload_max_filesize in php.ini',
                UPLOAD_ERR_FORM_SIZE => 'File exceeds MAX_FILE_SIZE in HTML form',
                UPLOAD_ERR_PARTIAL => 'File was only partially uploaded',
                UPLOAD_ERR_NO_FILE => 'No file was uploaded',
                UPLOAD_ERR_NO_TMP_DIR => 'Missing temporary folder',
                UPLOAD_ERR_CANT_WRITE => 'Failed to write file to disk',
                UPLOAD_ERR_EXTENSION => 'A PHP extension stopped the file upload',
            ];
            
            if (isset($errors[$file['error']])) {
                echo '<p class="error">Error: ' . $errors[$file['error']] . '</p>';
            }
        } else {
            echo '<p class="success">✓ File uploaded to temporary location</p>';
            
            // Try to move to storage
            $targetDir = __DIR__ . '/storage/app/public/job_images';
            $targetFile = $targetDir . '/test_' . time() . '_' . basename($file['name']);
            
            echo '<h3>Storage Test:</h3>';
            echo '<p><strong>Target directory:</strong> ' . $targetDir . '</p>';
            echo '<p><strong>Target file:</strong> ' . $targetFile . '</p>';
            
            // Check if directory exists
            if (!is_dir($targetDir)) {
                echo '<p class="error">❌ Directory does not exist: ' . $targetDir . '</p>';
                echo '<p class="info">Creating directory...</p>';
                
                if (mkdir($targetDir, 0775, true)) {
                    echo '<p class="success">✓ Directory created</p>';
                } else {
                    echo '<p class="error">❌ Failed to create directory</p>';
                }
            } else {
                echo '<p class="success">✓ Directory exists</p>';
            }
            
            // Check if directory is writable
            if (!is_writable($targetDir)) {
                echo '<p class="error">❌ Directory is not writable</p>';
                echo '<p class="info">Current permissions: ' . substr(sprintf('%o', fileperms($targetDir)), -4) . '</p>';
            } else {
                echo '<p class="success">✓ Directory is writable</p>';
            }
            
            // Try to move file
            if (move_uploaded_file($file['tmp_name'], $targetFile)) {
                echo '<p class="success">✓ File successfully moved to storage!</p>';
                echo '<p><strong>File location:</strong> ' . $targetFile . '</p>';
                
                // Check if file exists
                if (file_exists($targetFile)) {
                    echo '<p class="success">✓ File verified to exist</p>';
                    echo '<p><strong>File size:</strong> ' . filesize($targetFile) . ' bytes</p>';
                    echo '<p><strong>File permissions:</strong> ' . substr(sprintf('%o', fileperms($targetFile)), -4) . '</p>';
                    
                    // Generate public URL
                    $publicUrl = '/storage/job_images/' . basename($targetFile);
                    echo '<p><strong>Public URL:</strong> <a href="' . $publicUrl . '" target="_blank">' . $publicUrl . '</a></p>';
                    
                    // Check if symlink exists
                    $symlinkPath = __DIR__ . '/public/storage';
                    if (is_link($symlinkPath)) {
                        echo '<p class="success">✓ Storage symlink exists</p>';
                        echo '<p><strong>Symlink target:</strong> ' . readlink($symlinkPath) . '</p>';
                    } else {
                        echo '<p class="error">❌ Storage symlink does not exist</p>';
                        echo '<p class="warning">Run: php artisan storage:link</p>';
                    }
                } else {
                    echo '<p class="error">❌ File does not exist after move!</p>';
                }
            } else {
                echo '<p class="error">❌ Failed to move file to storage</p>';
                echo '<p class="info">This is likely a permissions issue</p>';
            }
        }
        
        echo '</div>';
    }
    ?>
    
    <div class="section">
        <h2>System Information</h2>
        
        <h3>PHP Configuration:</h3>
        <ul>
            <li><strong>upload_max_filesize:</strong> <?php echo ini_get('upload_max_filesize'); ?></li>
            <li><strong>post_max_size:</strong> <?php echo ini_get('post_max_size'); ?></li>
            <li><strong>max_execution_time:</strong> <?php echo ini_get('max_execution_time'); ?> seconds</li>
            <li><strong>memory_limit:</strong> <?php echo ini_get('memory_limit'); ?></li>
            <li><strong>file_uploads:</strong> <?php echo ini_get('file_uploads') ? 'Enabled' : 'Disabled'; ?></li>
            <li><strong>upload_tmp_dir:</strong> <?php echo ini_get('upload_tmp_dir') ?: 'Default'; ?></li>
        </ul>
        
        <h3>Directory Status:</h3>
        <?php
        $dirs = [
            'storage/app/public' => __DIR__ . '/storage/app/public',
            'storage/app/public/job_images' => __DIR__ . '/storage/app/public/job_images',
            'public/storage' => __DIR__ . '/public/storage',
        ];
        
        echo '<ul>';
        foreach ($dirs as $name => $path) {
            $exists = file_exists($path);
            $writable = is_writable($path);
            $isLink = is_link($path);
            
            echo '<li><strong>' . $name . ':</strong> ';
            
            if ($exists) {
                echo '<span class="success">Exists</span>';
                
                if ($isLink) {
                    echo ' <span class="info">(Symlink → ' . readlink($path) . ')</span>';
                }
                
                if ($writable) {
                    echo ' <span class="success">Writable</span>';
                } else {
                    echo ' <span class="error">Not Writable</span>';
                }
                
                $perms = substr(sprintf('%o', fileperms($path)), -4);
                echo ' <span class="info">(Permissions: ' . $perms . ')</span>';
            } else {
                echo '<span class="error">Does not exist</span>';
            }
            
            echo '</li>';
        }
        echo '</ul>';
        ?>
        
        <h3>Disk Space:</h3>
        <?php
        $free = disk_free_space(__DIR__);
        $total = disk_total_space(__DIR__);
        $used = $total - $free;
        $percent = round(($used / $total) * 100, 2);
        
        echo '<ul>';
        echo '<li><strong>Total:</strong> ' . number_format($total / 1024 / 1024 / 1024, 2) . ' GB</li>';
        echo '<li><strong>Used:</strong> ' . number_format($used / 1024 / 1024 / 1024, 2) . ' GB (' . $percent . '%)</li>';
        echo '<li><strong>Free:</strong> ' . number_format($free / 1024 / 1024 / 1024, 2) . ' GB</li>';
        echo '</ul>';
        ?>
    </div>
    
    <div class="section">
        <h2>Upload Test Form</h2>
        <p>Upload a test image to verify the upload functionality:</p>
        
        <form method="POST" enctype="multipart/form-data">
            <input type="file" name="test_file" accept="image/*" required>
            <button type="submit">Test Upload</button>
        </form>
    </div>
    
    <div class="section">
        <h2>⚠️ Security Warning</h2>
        <p class="error"><strong>DELETE THIS FILE AFTER TESTING!</strong></p>
        <p>This file should not be accessible in production for security reasons.</p>
        <p>Run: <code>rm <?php echo __FILE__; ?></code></p>
    </div>
</body>
</html>
