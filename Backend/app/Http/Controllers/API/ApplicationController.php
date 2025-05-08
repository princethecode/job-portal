<?php

namespace App\Http\Controllers\API;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use App\Models\Application;
use App\Models\Job;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\Storage;

class ApplicationController extends Controller
{
    /**
     * Apply for a job
     *
     * @param Request $request
     * @param int $jobId
     * @return \Illuminate\Http\JsonResponse
     */
    public function apply(Request $request, $jobId)
    {
        // Check if job exists and is active
        $job = Job::where('id', $jobId)
                  ->where('is_active', true)
                  ->where('expiry_date', '>=', now())
                  ->first();

        if (!$job) {
            return response()->json([
                'success' => false,
                'message' => 'Job not found or no longer available'
            ], 404);
        }

        // Check if user has already applied for this job
        $existingApplication = Application::where('user_id', $request->user()->id)
                                         ->where('job_id', $jobId)
                                         ->first();

        if ($existingApplication) {
            return response()->json([
                'success' => false,
                'message' => 'You have already applied for this job'
            ], 422);
        }

        $validator = Validator::make($request->all(), [
            'resume' => 'nullable|file|mimes:pdf,doc,docx|max:2048',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation error',
                'errors' => $validator->errors()
            ], 422);
        }

        // Handle resume upload
        $resumePath = null;
        if ($request->hasFile('resume')) {
            $resumePath = $request->file('resume')->store('resumes', 'public');
        } else if ($request->user()->resume_path) {
            // Use existing resume if available
            $resumePath = $request->user()->resume_path;
        }

        // Create application
        $application = Application::create([
            'user_id' => $request->user()->id,
            'job_id' => $jobId,
            'resume_path' => $resumePath,
            'status' => 'Applied',
            'applied_date' => now(),
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Job application submitted successfully',
            'data' => $application
        ], 201);
    }

    /**
     * Get user's applications
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function userApplications(Request $request)
    {
        $applications = Application::with('job')
                                  ->where('user_id', $request->user()->id)
                                  ->orderBy('created_at', 'desc')
                                  ->paginate(10);

        return response()->json([
            'success' => true,
            'data' => $applications
        ]);
    }

    /**
     * Get all applications (Admin only)
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function index(Request $request)
    {
        $query = Application::with(['user', 'job']);

        // Filter by job if provided
        if ($request->has('job_id') && !empty($request->job_id)) {
            $query->where('job_id', $request->job_id);
        }

        // Filter by status if provided
        if ($request->has('status') && !empty($request->status)) {
            $query->where('status', $request->status);
        }

        $applications = $query->orderBy('created_at', 'desc')
                             ->paginate(10);

        return response()->json([
            'success' => true,
            'data' => $applications
        ]);
    }

    /**
     * Update application status (Admin only)
     *
     * @param Request $request
     * @param int $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function updateStatus(Request $request, $id)
    {
        $application = Application::find($id);

        if (!$application) {
            return response()->json([
                'success' => false,
                'message' => 'Application not found'
            ], 404);
        }

        $validator = Validator::make($request->all(), [
            'status' => 'required|in:Applied,Under Review,Shortlisted,Rejected',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation error',
                'errors' => $validator->errors()
            ], 422);
        }

        $application->status = $request->status;
        $application->save();

        // TODO: Send notification to user about status change

        return response()->json([
            'success' => true,
            'message' => 'Application status updated successfully',
            'data' => $application
        ]);
    }

    /**
     * Get application details
     *
     * @param int $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function show($id)
    {
        $application = Application::with(['user', 'job'])->find($id);

        if (!$application) {
            return response()->json([
                'success' => false,
                'message' => 'Application not found'
            ], 404);
        }

        return response()->json([
            'success' => true,
            'data' => $application
        ]);
    }
}
