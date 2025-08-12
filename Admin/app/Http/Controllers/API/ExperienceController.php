<?php

namespace App\Http\Controllers\API;

use App\Http\Controllers\Controller;
use App\Models\Experience;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\Auth;

class ExperienceController extends Controller
{
    /**
     * Get all experiences for the authenticated user
     *
     * @return \Illuminate\Http\JsonResponse
     */
    public function index()
    {
        $user = Auth::user();
        $experiences = $user->experiences()->orderBy('created_at', 'desc')->get();
        
        return response()->json([
            'success' => true,
            'data' => $experiences
        ]);
    }

    /**
     * Store a new experience
     *
     * @param  \Illuminate\Http\Request  $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'job_title' => 'required|string|max:255',
            'company_name' => 'required|string|max:255',
            'start_date' => 'required|string|max:255',
            'end_date' => 'nullable|string|max:255',
            'is_current' => 'boolean',
            'description' => 'nullable|string',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation error',
                'errors' => $validator->errors()
            ], 422);
        }

        $user = Auth::user();
        $experience = new Experience($request->all());
        $experience->user_id = $user->id;
        $experience->save();

        return response()->json([
            'success' => true,
            'message' => 'Experience created successfully',
            'data' => $experience
        ], 201);
    }

    /**
     * Get a specific experience
     *
     * @param  int  $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function show($id)
    {
        $user = Auth::user();
        $experience = $user->experiences()->find($id);

        if (!$experience) {
            return response()->json([
                'success' => false,
                'message' => 'Experience not found'
            ], 404);
        }

        return response()->json([
            'success' => true,
            'data' => $experience
        ]);
    }

    /**
     * Update an experience
     *
     * @param  \Illuminate\Http\Request  $request
     * @param  int  $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function update(Request $request, $id)
    {
        $validator = Validator::make($request->all(), [
            'job_title' => 'required|string|max:255',
            'company_name' => 'required|string|max:255',
            'start_date' => 'required|string|max:255',
            'end_date' => 'nullable|string|max:255',
            'is_current' => 'boolean',
            'description' => 'nullable|string',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation error',
                'errors' => $validator->errors()
            ], 422);
        }

        $user = Auth::user();
        $experience = $user->experiences()->find($id);

        if (!$experience) {
            return response()->json([
                'success' => false,
                'message' => 'Experience not found'
            ], 404);
        }

        $experience->update($request->all());

        return response()->json([
            'success' => true,
            'message' => 'Experience updated successfully',
            'data' => $experience
        ]);
    }

    /**
     * Delete an experience
     *
     * @param  int  $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function destroy($id)
    {
        $user = Auth::user();
        $experience = $user->experiences()->find($id);

        if (!$experience) {
            return response()->json([
                'success' => false,
                'message' => 'Experience not found'
            ], 404);
        }

        $experience->delete();

        return response()->json([
            'success' => true,
            'message' => 'Experience deleted successfully'
        ]);
    }
}
