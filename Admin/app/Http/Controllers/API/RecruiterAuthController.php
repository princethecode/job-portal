<?php

namespace App\Http\Controllers\API;

use App\Http\Controllers\Controller;
use App\Models\Recruiter;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Validator;
use Illuminate\Validation\ValidationException;

class RecruiterAuthController extends Controller
{
    /**
     * Recruiter login
     */
    public function login(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'email' => 'required|email',
            'password' => 'required|string|min:6',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation failed',
                'errors' => $validator->errors()
            ], 422);
        }

        $credentials = $request->only('email', 'password');
        
        if (Auth::guard('recruiter')->attempt($credentials)) {
            $recruiter = Auth::guard('recruiter')->user();
            
            if (!$recruiter->is_active) {
                return response()->json([
                    'success' => false,
                    'message' => 'Account is deactivated. Please contact admin.'
                ], 401);
            }

            $token = $recruiter->createToken('recruiter-token')->plainTextToken;

            return response()->json([
                'success' => true,
                'message' => 'Login successful',
                'data' => [
                    'recruiter' => [
                        'id' => $recruiter->id,
                        'name' => $recruiter->name,
                        'email' => $recruiter->email,
                        'company_name' => $recruiter->company_name,
                        'company_website' => $recruiter->company_website,
                        'company_description' => $recruiter->company_description,
                        'company_size' => $recruiter->company_size,
                        'industry' => $recruiter->industry,
                        'location' => $recruiter->location,
                        'designation' => $recruiter->designation,
                        'mobile' => $recruiter->mobile,
                        'is_verified' => $recruiter->is_verified,
                        'is_active' => $recruiter->is_active,
                        'created_at' => $recruiter->created_at,
                        'updated_at' => $recruiter->updated_at,
                    ],
                    'token' => $token,
                    'token_type' => 'Bearer'
                ]
            ]);
        }

        return response()->json([
            'success' => false,
            'message' => 'Invalid credentials'
        ], 401);
    }

    /**
     * Recruiter registration
     */
    public function register(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'name' => 'required|string|max:255',
            'email' => 'required|email|unique:recruiters,email',
            'password' => 'required|string|min:6|confirmed',
            'mobile' => 'required|string|max:20',
            'company_name' => 'required|string|max:255',
            'company_website' => 'nullable|url|max:255',
            'company_description' => 'nullable|string|max:1000',
            'company_size' => 'nullable|string|max:100',
            'industry' => 'nullable|string|max:255',
            'location' => 'required|string|max:255',
            'designation' => 'nullable|string|max:255',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation failed',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            $recruiter = Recruiter::create([
                'name' => $request->name,
                'email' => $request->email,
                'password' => Hash::make($request->password),
                'mobile' => $request->mobile,
                'company_name' => $request->company_name,
                'company_website' => $request->company_website,
                'company_description' => $request->company_description,
                'company_size' => $request->company_size,
                'industry' => $request->industry,
                'location' => $request->location,
                'designation' => $request->designation,
                'is_active' => true,
                'is_verified' => false,
            ]);

            $token = $recruiter->createToken('recruiter-token')->plainTextToken;

            return response()->json([
                'success' => true,
                'message' => 'Registration successful',
                'data' => [
                    'recruiter' => [
                        'id' => $recruiter->id,
                        'name' => $recruiter->name,
                        'email' => $recruiter->email,
                        'company_name' => $recruiter->company_name,
                        'company_website' => $recruiter->company_website,
                        'company_description' => $recruiter->company_description,
                        'company_size' => $recruiter->company_size,
                        'industry' => $recruiter->industry,
                        'location' => $recruiter->location,
                        'designation' => $recruiter->designation,
                        'mobile' => $recruiter->mobile,
                        'is_verified' => $recruiter->is_verified,
                        'is_active' => $recruiter->is_active,
                        'created_at' => $recruiter->created_at,
                        'updated_at' => $recruiter->updated_at,
                    ],
                    'token' => $token,
                    'token_type' => 'Bearer'
                ]
            ], 201);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Registration failed',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Logout recruiter and revoke token
     */
    public function logout(Request $request)
    {
        try {
            $recruiter = $request->user();
            
            if ($recruiter) {
                // Revoke the current token
                $recruiter->currentAccessToken()->delete();
            }

            return response()->json([
                'success' => true,
                'message' => 'Logged out successfully'
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Logout failed',
                'error' => $e->getMessage()
            ], 500);
        }
    }

   
}
