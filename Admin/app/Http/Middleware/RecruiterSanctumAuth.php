<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Laravel\Sanctum\PersonalAccessToken;
use App\Models\Recruiter;

class RecruiterSanctumAuth
{
    /**
     * Handle an incoming request.
     *
     * @param  \Illuminate\Http\Request  $request
     * @param  \Closure(\Illuminate\Http\Request): (\Illuminate\Http\Response|\Illuminate\Http\RedirectResponse)  $next
     * @return \Illuminate\Http\Response|\Illuminate\Http\RedirectResponse
     */
    public function handle(Request $request, Closure $next)
    {
        // Get the Bearer token from the request
        $token = $request->bearerToken();
        
        if (!$token) {
            return response()->json([
                'success' => false,
                'message' => 'Unauthenticated - No token provided'
            ], 401);
        }

        // Find the token in the personal access tokens table
        $accessToken = PersonalAccessToken::findToken($token);
        
        if (!$accessToken) {
            return response()->json([
                'success' => false,
                'message' => 'Unauthenticated - Invalid token'
            ], 401);
        }

        // Check if the token belongs to a recruiter
        $tokenable = $accessToken->tokenable;
        
        if (!($tokenable instanceof Recruiter)) {
            return response()->json([
                'success' => false,
                'message' => 'Unauthenticated - Token not for recruiter'
            ], 401);
        }

        // Check if recruiter is active
        if (!$tokenable->is_active) {
            return response()->json([
                'success' => false,
                'message' => 'Account is deactivated'
            ], 401);
        }

        // Set the authenticated user for this request
        $request->setUserResolver(function () use ($tokenable) {
            return $tokenable;
        });

        // Update last used timestamp
        $accessToken->last_used_at = now();
        $accessToken->save();

        return $next($request);
    }
}