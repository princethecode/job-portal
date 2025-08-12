<?php

namespace App\Http\Controllers\Auth;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use App\Models\User;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;
use Illuminate\Support\Facades\Log;

class ResetPasswordController extends Controller
{
    /**
     * Reset the user's password.
     *
     * @param  \Illuminate\Http\Request  $request
     * @return \Illuminate\Http\RedirectResponse
     */
    public function reset(Request $request)
    {
        Log::info('Web reset password request received', ['email' => $request->email]);
        
        // Validate the request
        $request->validate([
            'token' => 'required',
            'email' => 'required|email|exists:users,email',
            'password' => 'required|string|min:8|confirmed',
        ]);

        // Validate password strength
        $password = $request->password;
        if (!preg_match('/[A-Za-z]/', $password) || !preg_match('/\d/', $password)) {
            return back()
                ->withInput($request->only('email'))
                ->withErrors(['password' => 'Password must contain at least one letter and one number']);
        }
        
        Log::info('Attempting to reset password via web form');
        
        // Get the reset record from database
        $resetRecord = DB::table('password_reset_tokens')
            ->where('email', $request->email)
            ->first();
            
        // Check if reset record exists
        if (!$resetRecord) {
            Log::warning('No reset record found for email', ['email' => $request->email]);
            return back()
                ->withInput($request->only('email'))
                ->withErrors(['email' => 'Invalid password reset token']);
        }
        
        // Check if token has expired
        if ($resetRecord->expires_at && now()->isAfter($resetRecord->expires_at)) {
            Log::warning('Token has expired', [
                'email' => $request->email, 
                'expires_at' => $resetRecord->expires_at
            ]);
            
            // Delete the expired token
            DB::table('password_reset_tokens')
                ->where('email', $request->email)
                ->delete();
                
            return back()
                ->withInput($request->only('email'))
                ->withErrors(['email' => 'Password reset token has expired. Please request a new one.']);
        }
        
        // Verify token
        if ($resetRecord->token !== $request->token) {
            Log::warning('Invalid token provided', ['email' => $request->email]);
            
            return back()
                ->withInput($request->only('email'))
                ->withErrors(['email' => 'Invalid password reset token']);
        }
        
        // Get user and update password
        $user = User::where('email', $request->email)->first();
        
        if (!$user) {
            Log::warning('User not found', ['email' => $request->email]);
            return back()
                ->withInput($request->only('email'))
                ->withErrors(['email' => 'User not found']);
        }
        
        // Update password
        $user->forceFill([
            'password' => Hash::make($request->password)
        ])->setRememberToken(Str::random(60));
        
        $user->save();
        Log::info('User password updated successfully via web form', ['user_id' => $user->id]);
        
        // Delete the used token
        DB::table('password_reset_tokens')
            ->where('email', $request->email)
            ->delete();
            
        Log::info('Password reset token deleted');
        
        // Revoke all existing tokens for security
        if (method_exists($user, 'tokens')) {
            $user->tokens()->delete();
            Log::info('All existing tokens revoked for security');
        }
        
        // Redirect to the success page
        return view('auth.password-reset-success');
    }
} 