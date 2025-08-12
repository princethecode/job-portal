<?php

namespace App\Http\Controllers\API;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use App\Models\User;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Password;
use Illuminate\Support\Str;
use Illuminate\Support\Facades\Mail;


class AuthController extends Controller
{
    /**
     * Register a new user
     * 
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function register(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'name' => 'required|string|max:255',
            'email' => 'required|string|email|max:255|unique:users',
            'mobile' => 'nullable|string|max:15',
            'password' => 'required|string|min:8|confirmed',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation error',
                'errors' => $validator->errors()
            ], 422);
        }

        // Create user with explicitly hashed password
        $user = User::create([
            'name' => $request->name,
            'email' => $request->email,
            'mobile' => $request->mobile,
            'password' => Hash::make($request->password),  // Explicitly hash the password
            'is_active' => true,  // Set user as active by default
        ]);
    
        $token = $user->createToken('auth_token')->plainTextToken;
    
        return response()->json([
            'success' => true,
            'message' => 'User registered successfully',
            'data' => [
                'user' => $user,
                'access_token' => $token,
                'token_type' => 'Bearer',
            ]
        ], 201);
    }

    /**
     * Login user and create token
     * 
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function login(Request $request)
    {
        $validator = Validator::make($request->all(), [
            // 'email' => 'required|string|email',  // Original email validation
            'mobile' => 'required|string',         // New mobile validation
            'password' => 'required|string',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation error',
                'errors' => $validator->errors()
            ], 422);
        }

        // Login with mobile number
        $credentials = [
            'mobile' => $request->mobile,
            'password' => $request->password
        ];

        if (!Auth::attempt($credentials)) {
            return response()->json([
                'success' => false,
                'message' => 'Invalid login credentials'
            ], 401);
        }

        $user = User::where('mobile', $request->mobile)->firstOrFail();
        
        // Check if user is active
        if (!$user->is_active) {
            return response()->json([
                'success' => false,
                'message' => 'Your account has been deactivated. Please contact admin.'
            ], 403);
        }

        $token = $user->createToken('auth_token')->plainTextToken;

        return response()->json([
            'success' => true,
            'message' => 'Login successful',
            'data' => [
                'user' => $user,
                'access_token' => $token,
                'token_type' => 'Bearer',
            ]
        ]);
        
        /* 
        // Original email login implementation
        // To switch back to email login:
        // 1. Comment out the mobile validation and uncomment the email validation above
        // 2. Comment out the mobile login code above and uncomment this section
        
        if (!Auth::attempt($request->only('email', 'password'))) {
            return response()->json([
                'success' => false,
                'message' => 'Invalid login credentials'
            ], 401);
        }

        $user = User::where('email', $request->email)->firstOrFail();
        
        // Check if user is active
        if (!$user->is_active) {
            return response()->json([
                'success' => false,
                'message' => 'Your account has been deactivated. Please contact admin.'
            ], 403);
        }

        $token = $user->createToken('auth_token')->plainTextToken;

        return response()->json([
            'success' => true,
            'message' => 'Login successful',
            'data' => [
                'user' => $user,
                'access_token' => $token,
                'token_type' => 'Bearer',
            ]
        ]);
        */
    }

    /**
     * Logout user (Revoke the token)
     * 
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function logout(Request $request)
    {
        $request->user()->currentAccessToken()->delete();

        return response()->json([
            'success' => true,
            'message' => 'Successfully logged out'
        ]);
    }

    /**
     * Send password reset link
     * 
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function forgotPassword(Request $request)
{
        try {
            \Log::info('Forgot password request received', ['request' => $request->all()]);
            
            $validator = Validator::make($request->all(), [
                'email' => 'required|string|email|exists:users',
            ]);

            if ($validator->fails()) {
                \Log::info('Validation failed', ['errors' => $validator->errors()]);
                return response()->json([
                    'success' => false,
                    'message' => 'Validation error',
                    'errors' => $validator->errors()
                ], 422);
            }

            $email = $request->email;
            \Log::info('Processing email', ['email' => $email]);
            
            // Get the user by email
            $user = User::where('email', $email)->first();
            
            if (!$user) {
                \Log::info('User not found', ['email' => $email]);
                return response()->json([
                    'success' => false,
                    'message' => 'User not found with the provided email address'
                ], 404);
            }
            
            \Log::info('User found', ['user_id' => $user->id, 'email' => $user->email]);
            
            // Generate a secure reset token with more entropy
            $token = Str::random(64);
            \Log::info('Token generated');
            
            // Set token expiry time (1 hour from now)
            $expiresAt = now()->addHour();
            
            try {
                // Store the token in the password_reset_tokens table
                \DB::table('password_reset_tokens')->updateOrInsert(
                    ['email' => $user->email],
                    [
                        'email' => $user->email,
                        'token' => $token,
                        'created_at' => now(),
                        'expires_at' => $expiresAt
                    ]
                );
                \Log::info('Token stored in database with expiry', ['expires_at' => $expiresAt]);
                
            /*    // Generate the reset URL with more secure parameters
                $resetUrl = config('app.frontend_url', 'http://emps.co.in/api/') . '/reset-password?token=' . $token . '&email=' . urlencode($user->email);
                \Log::info('Reset URL generated', ['url' => $resetUrl]);*/
                
                   // Generate the reset URL with more secure parameters
                $resetUrl = config('app.url', 'https://emps.co.in') . 'reset-password?token=' . $token . '&email=' . urlencode($user->email);
                \Log::info('Reset URL generated', ['url' => $resetUrl]);
                
            } catch (\Exception $e) {
                \Log::error('Error storing token in database', ['error' => $e->getMessage()]);
                return response()->json([
                    'success' => false,
                    'message' => 'Database error: ' . $e->getMessage()
                ], 500);
            }
            
            try {
                \Log::info('Attempting to send email reset link');
                
                // Send email using PHP's mail function directly since we've confirmed it works
                $to = $user->email;
                $subject = "Password Reset Request";
                
                // Create a proper HTML email that will pass spam filters
                $headers = "From: EMPS <noreply@emps.co.in>\r\n";
                $headers .= "Reply-To: noreply@emps.co.in\r\n";
                $headers .= "MIME-Version: 1.0\r\n";
                $headers .= "Content-Type: text/html; charset=UTF-8\r\n";
                $headers .= "X-Mailer: PHP/" . phpversion() . "\r\n";
                
                // Create HTML message with proper formatting for better deliverability
                $message = '<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Password Reset</title>
</head>
<body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
    <div style="border: 1px solid #ddd; border-radius: 5px; padding: 25px; background-color: #f9f9f9;">
        <div style="text-align: center; margin-bottom: 20px;">
            <h2>EMPS</h2>
        </div>
        
        <div style="background-color: #4A6CF7; color: white; padding: 15px; text-align: center; border-radius: 4px; margin-bottom: 20px;">
            <h2>Password Reset Request</h2>
        </div>
        
        <div style="background-color: white; padding: 20px; border-radius: 4px; margin-bottom: 20px;">
            <p>Hello <strong>' . $user->name . '</strong>,</p>
            
            <p>We received a request to reset your password for your EMPS account. If you didn\'t make this request, you can safely ignore this email.</p>
            
            <p>To reset your password, click on the button below:</p>
            
            <div style="text-align: center;">
                <a href="' . $resetUrl . '" style="display: inline-block; background-color: #4A6CF7; color: white; text-decoration: none; padding: 12px 25px; border-radius: 4px; margin: 15px 0; font-weight: bold;">Reset Password</a>
            </div>
            
            <p>Or copy and paste the following URL into your browser:</p>
            <p style="word-break: break-all; font-size: 14px;">' . $resetUrl . '</p>
            
            <p>This password reset link will expire on <strong>' . $expiresAt->format('F j, Y, g:i a') . '</strong>.</p>
            
            <div style="color: #e74c3c; margin-top: 20px; font-style: italic;">
                <p>If you did not request a password reset, please ignore this email or contact support if you have concerns about your account security.</p>
            </div>
        </div>
        
        <div style="background-color: #f1f1f1; padding: 15px; border-radius: 4px; font-size: 14px; margin-bottom: 20px;">
            <p><strong>Account Information:</strong></p>
            <p>Username/Email: ' . $user->email . '</p>
            <p>Mobile No: ' . $user->mobile . '</p>
        </div>
        
        <div style="text-align: center; color: #777; font-size: 12px; margin-top: 30px;">
            <p>&copy; ' . date('Y') . ' EMPS. All rights reserved.</p>
            <p>This is an automated email, please do not reply to this message.</p>
        </div>
    </div>
</body>
</html>';
                
                // Send the email
                $result = mail($to, $subject, $message, $headers);
                
                if ($result) {
                    \Log::info('Password reset email sent successfully');
                    
                    return response()->json([
                        'success' => true,
                        'message' => 'Password reset link sent to your email'
                    ]);
                } else {
                    $error = error_get_last();
                    throw new \Exception('Failed to send email: ' . ($error ? $error['message'] : 'Unknown error'));
                }
                
            } catch (\Exception $e) {
                \Log::error('Error sending password reset email', ['error' => $e->getMessage()]);
                return response()->json([
                    'success' => false,
                    'message' => 'Error sending email: ' . $e->getMessage()
                ], 500);
            }
            
        } catch (\Exception $e) {
            \Log::error('Exception in forgotPassword method', [
                'message' => $e->getMessage(),
                'trace' => $e->getTraceAsString()
            ]);
            
            return response()->json([
                'success' => false,
                'message' => 'Server error: ' . $e->getMessage()
            ], 500);
        }
    }

    /**
     * Reset password
     * 
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function resetPassword(Request $request)
    {
        try {
            \Log::info('Password reset request received', ['request' => $request->all()]);
            
            $validator = Validator::make($request->all(), [
                'email' => 'required|email|exists:users',
                'token' => 'required|string',
                'password' => 'required|string|min:8|confirmed',
            ]);

            if ($validator->fails()) {
                \Log::info('Validation failed', ['errors' => $validator->errors()]);
                return response()->json([
                    'success' => false,
                    'message' => 'Validation error',
                    'errors' => $validator->errors()
                ], 422);
            }

            // Validate password strength
            $password = $request->password;
            if (!preg_match('/[A-Za-z]/', $password) || !preg_match('/\d/', $password)) {
                \Log::info('Password strength validation failed');
                return response()->json([
                    'success' => false,
                    'message' => 'Password must contain at least one letter and one number',
                    'errors' => ['password' => ['Password must contain at least one letter and one number']]
                ], 422);
            }
            
            \Log::info('Attempting to reset password');
            
            // Get the reset record from database
            $resetRecord = \DB::table('password_reset_tokens')
                ->where('email', $request->email)
                ->first();
                
            // Check if reset record exists
            if (!$resetRecord) {
                \Log::warning('No reset record found for email', ['email' => $request->email]);
                return response()->json([
                    'success' => false,
                    'message' => 'Invalid password reset token'
                ], 400);
            }
            
            // Check if token has expired
            if ($resetRecord->expires_at && now()->isAfter($resetRecord->expires_at)) {
                \Log::warning('Token has expired', [
                    'email' => $request->email, 
                    'expires_at' => $resetRecord->expires_at
                ]);
                
                // Delete the expired token
                \DB::table('password_reset_tokens')
                    ->where('email', $request->email)
                    ->delete();
                    
                return response()->json([
                    'success' => false,
                    'message' => 'Password reset token has expired. Please request a new one.'
                ], 400);
            }
            
            // Verify token
            if ($resetRecord->token !== $request->token) {
                \Log::warning('Invalid token provided', ['email' => $request->email]);
                
                // Security measure: Increment failed attempts in session or track in DB
                // This could be implemented to prevent brute force attacks
                
                return response()->json([
                    'success' => false,
                    'message' => 'Invalid password reset token'
                ], 400);
            }
            
            // Get user and update password
            $user = User::where('email', $request->email)->first();
            
            if (!$user) {
                \Log::warning('User not found', ['email' => $request->email]);
                return response()->json([
                    'success' => false,
                    'message' => 'User not found'
                ], 404);
            }
            
            // Update password
            $user->forceFill([
                'password' => Hash::make($request->password)
            ])->setRememberToken(Str::random(60));
            
            $user->save();
            \Log::info('User password updated successfully', ['user_id' => $user->id]);
            
            // Delete the used token
            \DB::table('password_reset_tokens')
                ->where('email', $request->email)
                ->delete();
                
            \Log::info('Password reset token deleted');
            
            // Revoke all existing tokens for security
            if (method_exists($user, 'tokens')) {
                $user->tokens()->delete();
                \Log::info('All existing tokens revoked for security');
            }
            
            return response()->json([
                'success' => true,
                'message' => 'Password has been reset successfully'
            ]);
            
        } catch (\Exception $e) {
            \Log::error('Exception in resetPassword method', [
                'message' => $e->getMessage(),
                'trace' => $e->getTraceAsString()
            ]);
            
            return response()->json([
                'success' => false,
                'message' => 'Server error: ' . $e->getMessage()
            ], 500);
        }
    }

    /**
     * Get the authenticated User
     * 
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function user(Request $request)
    {
        return response()->json([
            'success' => true,
            'data' => [
                'user' => $request->user()
            ]
        ]);
    }
}
