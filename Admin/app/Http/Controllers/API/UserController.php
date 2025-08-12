<?php

namespace App\Http\Controllers\API;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use App\Models\User;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\Storage;

class UserController extends Controller
{
    /**
     * Get user profile
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function profile(Request $request)
    {
        return response()->json([
            'success' => true,
            'data' => $request->user()
        ]);
    }

    /**
     * Update user profile
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function updateProfile(Request $request)
    {
        $user = $request->user();

        $validator = Validator::make($request->all(), [
            'name' => 'sometimes|required|string|max:255',
            'mobile' => 'nullable|string|max:15',
            'skills' => 'nullable|string',
            'experience' => 'nullable|string',
            'resume' => 'nullable|file|mimes:pdf,doc,docx|max:2048',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation error',
                'errors' => $validator->errors()
            ], 422);
        }

        // Update basic info
        if ($request->has('name')) {
            $user->name = $request->name;
        }
        
        if ($request->has('mobile')) {
            $user->mobile = $request->mobile;
        }
        
        if ($request->has('skills')) {
            $user->skills = $request->skills;
        }
        
        if ($request->has('experience')) {
            $user->experience = $request->experience;
        }
        
        // Update new profile fields
        if ($request->has('location')) {
            $user->location = $request->location;
        }
        
        if ($request->has('job_title')) {
            $user->job_title = $request->job_title;
        }
        
        if ($request->has('about_me')) {
            $user->about_me = $request->about_me;
        }

        // Handle resume upload
        if ($request->hasFile('resume')) {
            // Delete old resume if exists
            if ($user->resume_path) {
                Storage::disk('public')->delete($user->resume_path);
            }
            
            $user->resume_path = $request->file('resume')->store('resumes', 'public');
        }

        $user->save();

        return response()->json([
            'success' => true,
            'message' => 'Profile updated successfully',
            'data' => $user
        ]);
    }

    /**
     * Change user password
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function changePassword(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'current_password' => 'required|string',
            'new_password' => 'required|string|min:8|confirmed',
            'new_password_confirmation' => 'required|same:new_password',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation error',
                'errors' => $validator->errors()
            ], 422);
        }

        $user = $request->user();

        // Check current password
        if (!Hash::check($request->current_password, $user->password)) {
            return response()->json([
                'success' => false,
                'message' => 'Current password is incorrect'
            ], 401);
        }

        // Update password
        $user->password = Hash::make($request->new_password);
        $user->save();

        // Logout other sessions
        $user->tokens()->where('id', '!=', $request->user()->currentAccessToken()->id)->delete();

        return response()->json([
            'success' => true,
            'message' => 'Password changed successfully'
        ]);
    }

    /**
     * Update user contact information
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function updateContact(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'contact' => 'required|string',
            'last_contact_sync' => 'required|string|date',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation error',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            $user = $request->user();
            
            // Update contact fields
            $user->contact = $request->contact;
            $user->last_contact_sync = $request->last_contact_sync;
            $user->save();

            return response()->json([
                'success' => true,
                'message' => 'Contact information updated successfully',
                'data' => $user
            ]);
        } catch (\Exception $e) {
            \Log::error('Error updating contact information: ' . $e->getMessage());
            return response()->json([
                'success' => false,
                'message' => 'Failed to update contact information'
            ], 500);
        }
    }
    
    /**
     * Upload profile photo
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function uploadProfilePhoto(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'photo' => 'required|image|mimes:jpeg,png,jpg,gif|max:2048',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation error',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            $user = $request->user();
            
            // Delete old profile photo if exists
            if ($user->profile_photo) {
                Storage::disk('public')->delete($user->profile_photo);
            }
            
            // Store the new profile photo
            $path = $request->file('photo')->store('profile_photos', 'public');
            $user->profile_photo = $path;
            $user->save();

            return response()->json([
                'success' => true,
                'message' => 'Profile photo uploaded successfully',
                'data' => $user
            ]);
        } catch (\Exception $e) {
            \Log::error('Error uploading profile photo: ' . $e->getMessage());
            return response()->json([
                'success' => false,
                'message' => 'Failed to upload profile photo'
            ], 500);
        }
    }

    /**
     * Get all users (Admin only)
     *
     * @return \Illuminate\Http\JsonResponse
     */
    public function index()
    {
        $users = User::paginate(10);

        return response()->json([
            'success' => true,
            'data' => $users
        ]);
    }

    /**
     * Get user details (Admin only)
     *
     * @param int $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function show($id)
    {
        $user = User::find($id);

        if (!$user) {
            return response()->json([
                'success' => false,
                'message' => 'User not found'
            ], 404);
        }

        return response()->json([
            'success' => true,
            'data' => $user
        ]);
    }

    /**
     * Update user status (Admin only)
     *
     * @param Request $request
     * @param int $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function updateStatus(Request $request, $id)
    {
        $user = User::find($id);

        if (!$user) {
            return response()->json([
                'success' => false,
                'message' => 'User not found'
            ], 404);
        }

        $validator = Validator::make($request->all(), [
            'is_active' => 'required|boolean',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation error',
                'errors' => $validator->errors()
            ], 422);
        }

        $user->is_active = $request->is_active;
        $user->save();

        return response()->json([
            'success' => true,
            'message' => 'User status updated successfully',
            'data' => $user
        ]);
    }

    /**
     * Delete user (Admin only)
     *
     * @param int $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function destroy($id)
    {
        $user = User::find($id);

        if (!$user) {
            return response()->json([
                'success' => false,
                'message' => 'User not found'
            ], 404);
        }

        // Delete user's resume if exists
        if ($user->resume_path) {
            Storage::disk('public')->delete($user->resume_path);
        }

        $user->delete();

        return response()->json([
            'success' => true,
            'message' => 'User deleted successfully'
        ]);
    }
}
