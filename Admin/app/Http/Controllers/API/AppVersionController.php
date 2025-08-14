<?php

namespace App\Http\Controllers\API;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use App\Models\AppVersion;
use Illuminate\Support\Facades\Validator;

class AppVersionController extends Controller
{
    /**
     * Display a listing of app versions
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function index(Request $request)
    {
        $query = AppVersion::query();
        
        // Apply platform filter
        if ($request->has('platform') && !empty($request->platform)) {
            $query->where('platform', $request->platform);
        }
        
        // Apply status filter
        if ($request->has('is_active') && $request->is_active !== '') {
            $query->where('is_active', (bool) $request->is_active);
        }
        
        $versions = $query->orderBy('created_at', 'desc')->paginate(10);

        return response()->json([
            'success' => true,
            'data' => $versions
        ]);
    }

    /**
     * Display the specified app version
     *
     * @param int $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function show($id)
    {
        $version = AppVersion::find($id);

        if (!$version) {
            return response()->json([
                'success' => false,
                'message' => 'App version not found'
            ], 404);
        }

        return response()->json([
            'success' => true,
            'data' => $version
        ]);
    }

    /**
     * Store a newly created app version (Admin only)
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'platform' => 'required|in:android,ios',
            'version_name' => 'required|string|max:50',
            'version_code' => 'required|integer|min:1',
            'minimum_version_name' => 'nullable|string|max:50',
            'minimum_version_code' => 'nullable|integer|min:1',
            'force_update' => 'boolean',
            'update_message' => 'nullable|string|max:500',
            'download_url' => 'nullable|url|max:255',
            'is_active' => 'boolean'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation error',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            // If this version is being set as active, deactivate other versions for the same platform
            if ($request->is_active) {
                AppVersion::where('platform', $request->platform)
                          ->where('is_active', true)
                          ->update(['is_active' => false]);
            }

            $version = AppVersion::create($request->all());

            return response()->json([
                'success' => true,
                'message' => 'App version created successfully',
                'data' => $version
            ], 201);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Failed to create app version: ' . $e->getMessage()
            ], 500);
        }
    }

    /**
     * Update the specified app version (Admin only)
     *
     * @param Request $request
     * @param int $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function update(Request $request, $id)
    {
        $version = AppVersion::find($id);

        if (!$version) {
            return response()->json([
                'success' => false,
                'message' => 'App version not found'
            ], 404);
        }

        $validator = Validator::make($request->all(), [
            'platform' => 'sometimes|required|in:android,ios',
            'version_name' => 'sometimes|required|string|max:50',
            'version_code' => 'sometimes|required|integer|min:1',
            'minimum_version_name' => 'nullable|string|max:50',
            'minimum_version_code' => 'nullable|integer|min:1',
            'force_update' => 'boolean',
            'update_message' => 'nullable|string|max:500',
            'download_url' => 'nullable|url|max:255',
            'is_active' => 'boolean'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation error',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            // If this version is being set as active, deactivate other versions for the same platform
            if ($request->has('is_active') && $request->is_active && !$version->is_active) {
                $platform = $request->platform ?? $version->platform;
                AppVersion::where('platform', $platform)
                          ->where('id', '!=', $id)
                          ->where('is_active', true)
                          ->update(['is_active' => false]);
            }

            $version->update($request->all());

            return response()->json([
                'success' => true,
                'message' => 'App version updated successfully',
                'data' => $version
            ]);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Failed to update app version: ' . $e->getMessage()
            ], 500);
        }
    }

    /**
     * Remove the specified app version (Admin only)
     *
     * @param int $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function destroy($id)
    {
        $version = AppVersion::find($id);

        if (!$version) {
            return response()->json([
                'success' => false,
                'message' => 'App version not found'
            ], 404);
        }

        try {
            $version->delete();

            return response()->json([
                'success' => true,
                'message' => 'App version deleted successfully'
            ]);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Failed to delete app version: ' . $e->getMessage()
            ], 500);
        }
    }

    /**
     * Toggle the active status of an app version
     *
     * @param int $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function toggleStatus($id)
    {
        $version = AppVersion::find($id);

        if (!$version) {
            return response()->json([
                'success' => false,
                'message' => 'App version not found'
            ], 404);
        }

        try {
            // If activating this version, deactivate others for the same platform
            if (!$version->is_active) {
                AppVersion::where('platform', $version->platform)
                          ->where('id', '!=', $id)
                          ->where('is_active', true)
                          ->update(['is_active' => false]);
            }
            
            $version->is_active = !$version->is_active;
            $version->save();

            $status = $version->is_active ? 'activated' : 'deactivated';
            
            return response()->json([
                'success' => true,
                'message' => "App version {$status} successfully",
                'data' => $version
            ]);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Failed to toggle app version status: ' . $e->getMessage()
            ], 500);
        }
    }

    /**
     * Get the latest version for a platform
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function getLatestVersion(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'platform' => 'required|in:android,ios',
            'current_version_code' => 'nullable|integer'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation error',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            $platform = $request->platform;
            $currentVersionCode = $request->current_version_code;
            
            $latestVersion = AppVersion::getLatestVersion($platform);
            
            if (!$latestVersion) {
                return response()->json([
                    'success' => false,
                    'message' => 'No active version found for this platform'
                ], 404);
            }

            $updateRequired = false;
            $forceUpdate = false;
            
            if ($currentVersionCode) {
                $updateRequired = $latestVersion->isUpdateRequired($currentVersionCode);
                $forceUpdate = $latestVersion->force_update && 
                              $currentVersionCode < $latestVersion->version_code;
            }

            return response()->json([
                'success' => true,
                'data' => [
                    'platform' => $latestVersion->platform,
                    'version_name' => $latestVersion->version_name,
                    'version_code' => $latestVersion->version_code,
                    'minimum_version_name' => $latestVersion->minimum_version_name,
                    'minimum_version_code' => $latestVersion->minimum_version_code,
                    'update_required' => $updateRequired,
                    'force_update' => $forceUpdate,
                    'update_message' => $latestVersion->update_message,
                    'download_url' => $latestVersion->download_url
                ]
            ]);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Failed to fetch version information: ' . $e->getMessage()
            ], 500);
        }
    }

    /**
     * Check if update is required for current version
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function checkUpdate(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'platform' => 'required|in:android,ios',
            'current_version_code' => 'required|integer'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation error',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            $platform = $request->platform;
            $currentVersionCode = $request->current_version_code;
            
            $latestVersion = AppVersion::getLatestVersion($platform);
            
            if (!$latestVersion) {
                return response()->json([
                    'success' => false,
                    'message' => 'No active version found for this platform'
                ], 404);
            }

            $updateRequired = $latestVersion->isUpdateRequired($currentVersionCode);
            $forceUpdate = $latestVersion->force_update && 
                          $currentVersionCode < $latestVersion->version_code;
            $updateAvailable = $currentVersionCode < $latestVersion->version_code;

            return response()->json([
                'success' => true,
                'data' => [
                    'update_available' => $updateAvailable,
                    'update_required' => $updateRequired,
                    'force_update' => $forceUpdate,
                    'latest_version' => [
                        'version_name' => $latestVersion->version_name,
                        'version_code' => $latestVersion->version_code,
                        'update_message' => $latestVersion->update_message,
                        'download_url' => $latestVersion->download_url
                    ]
                ]
            ]);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Failed to check update: ' . $e->getMessage()
            ], 500);
        }
    }
}