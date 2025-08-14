<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Models\AppVersion;
use Exception;

class AppVersionController extends Controller
{
    /**
     * Display a listing of app versions
     *
     * @param Request $request
     * @return \Illuminate\View\View
     */
    public function index(Request $request)
    {
        try {
            $query = AppVersion::query();
            
            // Add platform filter if provided
            if ($request->has('platform') && !empty($request->platform)) {
                $query->where('platform', $request->platform);
            }
            
            // Add status filter if provided
            if ($request->has('is_active') && $request->is_active !== '') {
                $query->where('is_active', (bool) $request->is_active);
            }
            
            $versions = $query->orderBy('created_at', 'desc')->paginate(10);
            
            return view('app-versions.index', [
                'versions' => $versions,
                'filters' => $request->all()
            ]);
        } catch (Exception $e) {
            return view('app-versions.index', [
                'error' => 'Unable to fetch app versions. ' . $e->getMessage(),
                'filters' => $request->all()
            ]);
        }
    }

    /**
     * Show the form for creating a new app version
     *
     * @return \Illuminate\View\View
     */
    public function create()
    {
        return view('app-versions.create');
    }

    /**
     * Store a newly created app version
     *
     * @param Request $request
     * @return \Illuminate\Http\RedirectResponse
     */
    public function store(Request $request)
    {
        $request->validate([
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

        try {
            // If this version is being set as active, deactivate other versions for the same platform
            if ($request->is_active) {
                AppVersion::where('platform', $request->platform)
                          ->where('is_active', true)
                          ->update(['is_active' => false]);
            }

            AppVersion::create($request->all());

            return redirect()->route('admin.app-versions.index')
                ->with('success', 'App version created successfully');
        } catch (Exception $e) {
            return redirect()->back()
                ->withInput()
                ->with('error', 'Failed to create app version: ' . $e->getMessage());
        }
    }

    /**
     * Display the specified app version
     *
     * @param int $id
     * @return \Illuminate\View\View
     */
    public function show($id)
    {
        try {
            $version = AppVersion::findOrFail($id);
            
            return view('app-versions.show', [
                'version' => $version
            ]);
        } catch (Exception $e) {
            return redirect()->route('admin.app-versions.index')
                ->with('error', 'App version not found or error occurred.');
        }
    }

    /**
     * Show the form for editing the specified app version
     *
     * @param int $id
     * @return \Illuminate\View\View
     */
    public function edit($id)
    {
        try {
            $version = AppVersion::findOrFail($id);
            
            return view('app-versions.edit', [
                'version' => $version
            ]);
        } catch (Exception $e) {
            return redirect()->route('admin.app-versions.index')
                ->with('error', 'App version not found or error occurred.');
        }
    }

    /**
     * Update the specified app version
     *
     * @param Request $request
     * @param int $id
     * @return \Illuminate\Http\RedirectResponse
     */
    public function update(Request $request, $id)
    {
        $request->validate([
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

        try {
            $version = AppVersion::findOrFail($id);

            // If this version is being set as active, deactivate other versions for the same platform
            if ($request->is_active && !$version->is_active) {
                AppVersion::where('platform', $request->platform)
                          ->where('id', '!=', $id)
                          ->where('is_active', true)
                          ->update(['is_active' => false]);
            }

            $version->update($request->all());

            return redirect()->route('admin.app-versions.index')
                ->with('success', 'App version updated successfully');
        } catch (Exception $e) {
            return redirect()->back()
                ->withInput()
                ->with('error', 'Failed to update app version: ' . $e->getMessage());
        }
    }

    /**
     * Remove the specified app version
     *
     * @param int $id
     * @return \Illuminate\Http\RedirectResponse
     */
    public function destroy($id)
    {
        try {
            $version = AppVersion::findOrFail($id);
            $version->delete();

            return redirect()->route('admin.app-versions.index')
                ->with('success', 'App version deleted successfully');
        } catch (Exception $e) {
            return redirect()->back()
                ->with('error', 'Failed to delete app version: ' . $e->getMessage());
        }
    }

    /**
     * Toggle the active status of an app version
     *
     * @param int $id
     * @return \Illuminate\Http\RedirectResponse
     */
    public function toggleStatus($id)
    {
        try {
            $version = AppVersion::findOrFail($id);
            
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
            return redirect()->back()
                ->with('success', "App version {$status} successfully");
        } catch (Exception $e) {
            return redirect()->back()
                ->with('error', 'Failed to toggle app version status: ' . $e->getMessage());
        }
    }

    /**
     * Get the latest version for a platform (API endpoint)
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function getLatestVersion(Request $request)
    {
        $request->validate([
            'platform' => 'required|in:android,ios',
            'current_version_code' => 'nullable|integer'
        ]);

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
        } catch (Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Failed to fetch version information: ' . $e->getMessage()
            ], 500);
        }
    }
}