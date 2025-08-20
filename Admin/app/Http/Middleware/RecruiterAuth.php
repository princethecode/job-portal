<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Symfony\Component\HttpFoundation\Response;

class RecruiterAuth
{
    /**
     * Handle an incoming request.
     */
    public function handle(Request $request, Closure $next): Response
    {
        if (!Auth::guard('recruiter')->check()) {
            return redirect()->route('recruiter.login')->with('error', 'Please login to access recruiter panel.');
        }

        return $next($request);
    }
}