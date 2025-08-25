<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;

class AdminAuth
{
    /**
     * Handle an incoming request.
     *
     * @param  \Illuminate\Http\Request  $request
     * @param  \Closure  $next
     * @return mixed
     */
    public function handle(Request $request, Closure $next)
    {
        // Check if user is authenticated
        if (!Auth::check()) {
            return redirect()->route('admin.login');
        }
        
        // Check if authenticated user has admin privileges
        $user = Auth::user();
        if (!$user->is_admin) {
            Auth::logout();
            return redirect()->route('admin.login')
                ->withErrors(['email' => 'Access denied. Admin privileges required.']);
        }
        
        return $next($request);
    }
} 