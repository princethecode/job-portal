<?php
use Illuminate\Support\Facades\Route;

Route::get('/send-reset-password/{email}', function ($email) {
    $user = \App\Models\User::where('email', $email)->first();
    
    if (!$user) {
        return "User not found";
    }
    
    // Generate token
    $token = \Illuminate\Support\Str::random(64);
    $expiresAt = now()->addHour();
    
    // Store token
    \DB::table('password_reset_tokens')->updateOrInsert(
        ['email' => $user->email],
        [
            'email' => $user->email,
            'token' => $token,
            'created_at' => now(),
            'expires_at' => $expiresAt
        ]
    );
    
    // Build reset URL
    $resetUrl = "https://emps.co.in/reset-password?token={$token}&email=" . urlencode($user->email);
    
    // Send email using PHP's mail function directly
    $to = $user->email;
    $subject = "Password Reset Request";
    $message = "Hello {$user->name},\n\n";
    $message .= "We received a request to reset your password. Click the link below to reset your password:\n\n";
    $message .= $resetUrl . "\n\n";
    $message .= "This link will expire in 1 hour.\n\n";
    $message .= "If you didn't request this, please ignore this email.\n\n";
    $message .= "Regards,\nEMPS Team";
    
    $headers = "From: noreply@emps.co.in\r\n";
    $headers .= "Reply-To: noreply@emps.co.in\r\n";
    
    $result = mail($to, $subject, $message, $headers);
    
    if ($result) {
        return "Password reset email sent successfully to {$email}";
    } else {
        return "Failed to send email. Error: " . print_r(error_get_last(), true);
    }
});