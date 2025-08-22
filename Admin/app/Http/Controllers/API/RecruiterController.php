<?php

namespace App\Http\Controllers\API;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Validator;

class RecruiterController extends Controller
{
    /**
     * Get recruiter profile
     */
    public function profile(Request $request)
    {
        $recruiter = $request->user();

        return response()->json([
            'success' => true,
            'message' => 'Profile retrieved successfully',
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
                ]
            ]
        ]);
    }

    /**
     * Update recruiter profile
     */
    public function updateProfile(Request $request)
    {
        $recruiter = $request->user();

        $validator = Validator::make($request->all(), [
            'name' => 'sometimes|string|max:255',
            'mobile' => 'sometimes|string|max:20',
            'company_name' => 'sometimes|string|max:255',
            'company_website' => 'sometimes|nullable|url|max:255',
            'company_description' => 'sometimes|nullable|string|max:1000',
            'company_size' => 'sometimes|nullable|string|max:100',
            'industry' => 'sometimes|nullable|string|max:255',
            'location' => 'sometimes|string|max:255',
            'designation' => 'sometimes|nullable|string|max:255',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation failed',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            $recruiter->update($request->only([
                'name', 'mobile', 'company_name', 'company_website', 
                'company_description', 'company_size', 'industry', 
                'location', 'designation'
            ]));

            return response()->json([
                'success' => true,
                'message' => 'Profile updated successfully',
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
                    ]
                ]
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Profile update failed',
                'error' => $e->getMessage()
            ], 500);
        }
    }
}
