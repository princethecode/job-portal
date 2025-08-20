<?php

namespace App\Http\Controllers;

use App\Models\Recruiter;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Validator;

class RecruiterAuthController extends Controller
{
    /**
     * Show recruiter login form
     */
    public function showLoginForm()
    {
        return view('recruiter.auth.login');
    }

    /**
     * Handle recruiter login
     */
    public function login(Request $request)
    {
        $credentials = $request->validate([
            'email' => 'required|email',
            'password' => 'required',
        ]);

        if (Auth::guard('recruiter')->attempt($credentials, $request->filled('remember'))) {
            $request->session()->regenerate();
            return redirect()->intended(route('recruiter.dashboard'));
        }

        return back()->withErrors([
            'email' => 'The provided credentials do not match our records.',
        ])->onlyInput('email');
    }

    /**
     * Show recruiter registration form
     */
    public function showRegistrationForm()
    {
        return view('recruiter.auth.register');
    }

    /**
     * Handle recruiter registration
     */
    public function register(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'name' => 'required|string|max:255',
            'email' => 'required|string|email|max:255|unique:recruiters',
            'password' => 'required|string|min:8|confirmed',
            'mobile' => 'nullable|string|max:15',
            'company_name' => 'required|string|max:255',
            'company_website' => 'nullable|url',
            'industry' => 'nullable|string|max:255',
            'location' => 'nullable|string|max:255',
            'designation' => 'nullable|string|max:255',
        ]);

        if ($validator->fails()) {
            return back()->withErrors($validator)->withInput();
        }

        $recruiter = Recruiter::create([
            'name' => $request->name,
            'email' => $request->email,
            'password' => Hash::make($request->password),
            'mobile' => $request->mobile,
            'company_name' => $request->company_name,
            'company_website' => $request->company_website,
            'industry' => $request->industry,
            'location' => $request->location,
            'designation' => $request->designation,
        ]);

        Auth::guard('recruiter')->login($recruiter);

        return redirect()->route('recruiter.dashboard')->with('success', 'Registration successful! Welcome to the platform.');
    }

    /**
     * Handle recruiter logout
     */
    public function logout(Request $request)
    {
        Auth::guard('recruiter')->logout();
        $request->session()->invalidate();
        $request->session()->regenerateToken();

        return redirect()->route('recruiter.login');
    }
}