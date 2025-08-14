<?php
/**
 * Test script for Google Authentication endpoints
 * 
 * Usage: php test_google_auth.php
 */

$baseUrl = 'https://emps.co.in/api';

// Test data
$testUser = [
    'name' => 'Test Google User',
    'email' => 'testgoogle@gmail.com',
    'google_id' => 'google_test_id_' . time(),
    'provider' => 'google'
];

echo "=== Google Authentication Test ===\n\n";

// Test 1: Register with Google
echo "1. Testing Google Registration...\n";
$registerData = json_encode($testUser);

$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, $baseUrl . '/register/google');
curl_setopt($ch, CURLOPT_POST, 1);
curl_setopt($ch, CURLOPT_POSTFIELDS, $registerData);
curl_setopt($ch, CURLOPT_HTTPHEADER, [
    'Content-Type: application/json',
    'Accept: application/json'
]);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);

$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);

echo "Response Code: $httpCode\n";
echo "Response: $response\n\n";

// Test 2: Login with Google
echo "2. Testing Google Login...\n";
$loginData = json_encode([
    'email' => $testUser['email'],
    'google_id' => $testUser['google_id'],
    'provider' => 'google'
]);

$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, $baseUrl . '/login/google');
curl_setopt($ch, CURLOPT_POST, 1);
curl_setopt($ch, CURLOPT_POSTFIELDS, $loginData);
curl_setopt($ch, CURLOPT_HTTPHEADER, [
    'Content-Type: application/json',
    'Accept: application/json'
]);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);

$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);

echo "Response Code: $httpCode\n";
echo "Response: $response\n\n";

echo "=== Test Complete ===\n";
echo "Note: You may need to manually clean up the test user from the database.\n";
?>