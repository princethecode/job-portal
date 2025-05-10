<?php
// Manual test script for change password functionality
require __DIR__ . '/vendor/autoload.php';

use App\Models\User;
use Illuminate\Support\Facades\Hash;
use Illuminate\Http\Request;
use Illuminate\Foundation\Bootstrap\LoadEnvironmentVariables;
use Illuminate\Foundation\Application;

// Create a simple Laravel app instance
$app = new Application(__DIR__);
$app->singleton(
    Illuminate\Contracts\Http\Kernel::class,
    App\Http\Kernel::class
);

// Load environment variables
(new LoadEnvironmentVariables)->bootstrap($app);

// Create a dummy user
$user = new User();
$user->name = 'Test User';
$user->email = 'test@example.com';

// Set an initial password
$initialPassword = 'password123';
$user->password = Hash::make($initialPassword);

echo "=== Manual Change Password Test ===\n";
echo "Initial Password Hash: " . $user->password . "\n";

// Get the controller class
$controller = new App\Http\Controllers\API\UserController();

// Test 1: Correct password change
echo "\nTest 1: Change password with correct current password\n";
$request = new Request([
    'current_password' => $initialPassword,
    'new_password' => 'newpassword123',
    'new_password_confirmation' => 'newpassword123'
]);
$request->setUserResolver(function () use ($user) {
    return $user;
});

// Call the change password method
try {
    $response = $controller->changePassword($request);
    echo "Response Status: " . $response->status() . "\n";
    echo "Response Content: " . $response->getContent() . "\n";
    
    // Check if the password was changed
    echo "Password changed? " . (Hash::check('newpassword123', $user->password) ? 'Yes' : 'No') . "\n";
} catch (Exception $e) {
    echo "Error: " . $e->getMessage() . "\n";
}

// Test 2: Incorrect current password
echo "\nTest 2: Try to change password with incorrect current password\n";
$request = new Request([
    'current_password' => 'wrongpassword',
    'new_password' => 'anotherpassword123',
    'new_password_confirmation' => 'anotherpassword123'
]);
$request->setUserResolver(function () use ($user) {
    return $user;
});

// Call the change password method
try {
    $response = $controller->changePassword($request);
    echo "Response Status: " . $response->status() . "\n";
    echo "Response Content: " . $response->getContent() . "\n";
} catch (Exception $e) {
    echo "Error: " . $e->getMessage() . "\n";
}

// Test 3: Password confirmation mismatch
echo "\nTest 3: Try to change password with non-matching confirmation\n";
$request = new Request([
    'current_password' => 'newpassword123', // This is now the current password after Test 1
    'new_password' => 'yetanotherpassword',
    'new_password_confirmation' => 'differentpassword'
]);
$request->setUserResolver(function () use ($user) {
    return $user;
});

// Call the change password method
try {
    $response = $controller->changePassword($request);
    echo "Response Status: " . $response->status() . "\n";
    echo "Response Content: " . $response->getContent() . "\n";
} catch (Exception $e) {
    echo "Error: " . $e->getMessage() . "\n";
}

echo "\n=== Test Completed ===\n"; 