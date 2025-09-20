<?php

namespace App\Http\Controllers\API;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Storage;
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
                    'company_license_url' => $recruiter->company_license_url,
                    'license_uploaded_at' => $recruiter->license_uploaded_at ? $recruiter->license_uploaded_at->toISOString() : null,
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
                        'company_license_url' => $recruiter->company_license_url,
                        'license_uploaded_at' => $recruiter->license_uploaded_at ? $recruiter->license_uploaded_at->toISOString() : null,
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

    /**
     * Upload company license
     */
    public function uploadCompanyLicense(Request $request)
    {
        $recruiter = $request->user();

        $validator = Validator::make($request->all(), [
            'license' => 'required|file|mimes:pdf,jpg,jpeg,png|max:5120', // 5MB max
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation failed',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            $file = $request->file('license');
            
            // Check if storage directory exists and is writable
            $storageDir = storage_path('app/public/licenses');
            if (!is_dir($storageDir)) {
                mkdir($storageDir, 0775, true);
            }
            
            if (!is_writable($storageDir)) {
                return response()->json([
                    'success' => false,
                    'message' => 'Storage directory is not writable. Please check permissions.',
                ], 500);
            }
            
            // Delete old license file if exists
            if ($recruiter->company_license) {
                Storage::disk('public')->delete($recruiter->company_license);
            }

            // Generate unique filename
            $filename = 'license_' . $recruiter->id . '_' . time() . '.' . $file->getClientOriginalExtension();
            
            // Store file in public disk under licenses directory
            $path = $file->storeAs('licenses', $filename, 'public');
            
            // Verify file was actually stored
            if (!$path || !Storage::disk('public')->exists($path)) {
                return response()->json([
                    'success' => false,
                    'message' => 'Failed to store file. Please check storage configuration.',
                ], 500);
            }

            // Update recruiter record
            $recruiter->update([
                'company_license' => $path,
                'license_uploaded_at' => now(),
            ]);

            return response()->json([
                'success' => true,
                'message' => 'Company license uploaded successfully',
                'data' => [
                    'license_url' => asset('storage/' . $path),
                    'license_name' => $file->getClientOriginalName(),
                    'uploaded_at' => $recruiter->license_uploaded_at->toISOString(),
                ]
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'License upload failed',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Get company license info
     */
    public function getCompanyLicense(Request $request)
    {
        $recruiter = $request->user();

        if (!$recruiter->hasCompanyLicense()) {
            return response()->json([
                'success' => false,
                'message' => 'No company license found',
            ], 404);
        }

        return response()->json([
            'success' => true,
            'message' => 'Company license retrieved successfully',
            'data' => [
                'license_url' => $recruiter->company_license_url,
                'uploaded_at' => $recruiter->license_uploaded_at ? $recruiter->license_uploaded_at->toISOString() : null,
            ]
        ]);
    }

    /**
     * Delete company license
     */
    public function deleteCompanyLicense(Request $request)
    {
        $recruiter = $request->user();

        if (!$recruiter->hasCompanyLicense()) {
            return response()->json([
                'success' => false,
                'message' => 'No company license found to delete',
            ], 404);
        }

        try {
            // Delete file from storage
            Storage::disk('public')->delete($recruiter->company_license);

            // Update recruiter record
            $recruiter->update([
                'company_license' => null,
                'license_uploaded_at' => null,
            ]);

            return response()->json([
                'success' => true,
                'message' => 'Company license deleted successfully',
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'License deletion failed',
                'error' => $e->getMessage()
            ], 500);
        }
    }
}
