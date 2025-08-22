<?php

namespace App\Http\Controllers\API;

use App\Http\Controllers\Controller;
use App\Models\Application;
use App\Models\Interview;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Validator;

class RecruiterApplicationController extends Controller
{
    /**
     * Get all applications for recruiter's jobs
     */
    public function index(Request $request)
    {
        $recruiter = $request->user();
        
        $query = $recruiter->applications()->with(['user', 'job']);

        // Apply filters
        if ($request->has('status')) {
            $query->where('status', $request->status);
        }

        if ($request->has('search')) {
            $search = $request->search;
            $query->whereHas('user', function ($q) use ($search) {
                $q->where('name', 'like', "%{$search}%")
                  ->orWhere('email', 'like', "%{$search}%");
            })->orWhereHas('job', function ($q) use ($search) {
                $q->where('title', 'like', "%{$search}%");
            });
        }

        $applications = $query->orderBy('created_at', 'desc')->paginate(10);

        return response()->json([
            'success' => true,
            'data' => [
                'applications' => $applications->items(),
                'pagination' => [
                    'current_page' => $applications->currentPage(),
                    'last_page' => $applications->lastPage(),
                    'per_page' => $applications->perPage(),
                    'total' => $applications->total(),
                ]
            ]
        ]);
    }

    /**
     * Get specific application details
     */
    public function show(Request $request, $id)
    {
        $recruiter = $request->user();
        $application = $recruiter->applications()->with(['user', 'job', 'user.experiences'])->findOrFail($id);

        return response()->json([
            'success' => true,
            'data' => [
                'application' => [
                    'id' => $application->id,
                    'status' => $application->status,
                    'cover_letter' => $application->cover_letter,
                    'notes' => $application->notes,
                    'applied_date' => $application->created_at,
                    'user' => [
                        'id' => $application->user->id,
                        'name' => $application->user->name,
                        'email' => $application->user->email,
                        'mobile' => $application->user->mobile,
                        'profile_photo' => $application->user->profile_photo,
                        'resume' => $application->user->resume,
                        'experiences' => $application->user->experiences,
                    ],
                    'job' => [
                        'id' => $application->job->id,
                        'title' => $application->job->title,
                        'company_name' => $application->job->company_name,
                        'location' => $application->job->location,
                    ]
                ]
            ]
        ]);
    }

    /**
     * Update application status
     */
    public function updateStatus(Request $request, $id)
    {
        $validator = Validator::make($request->all(), [
            'status' => 'required|in:Applied,Reviewing,Shortlisted,Rejected,Hired',
            'notes' => 'nullable|string',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation failed',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            $recruiter = $request->user();
            $application = $recruiter->applications()->findOrFail($id);
            
            $application->update([
                'status' => $request->status,
                'notes' => $request->notes,
            ]);

            return response()->json([
                'success' => true,
                'message' => 'Application status updated successfully',
                'data' => [
                    'application' => [
                        'id' => $application->id,
                        'status' => $application->status,
                        'notes' => $application->notes,
                    ]
                ]
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Status update failed',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Schedule interview for application
     */
    public function scheduleInterview(Request $request, $id)
    {
        $validator = Validator::make($request->all(), [
            'interview_date' => 'required|date|after:today',
            'interview_time' => 'required|date_format:H:i',
            'interview_type' => 'required|in:Phone,Video,In-person',
            'location' => 'nullable|string',
            'notes' => 'nullable|string',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation failed',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            $recruiter = $request->user();
            $application = $recruiter->applications()->with(['user', 'job'])->findOrFail($id);

            $interview = Interview::create([
                'recruiter_id' => $recruiter->id,
                'user_id' => $application->user_id,
                'job_id' => $application->job_id,
                'application_id' => $application->id,
                'interview_date' => $request->interview_date,
                'interview_time' => $request->interview_time,
                'interview_type' => $request->interview_type,
                'location' => $request->location,
                'notes' => $request->notes,
                'status' => 'scheduled',
            ]);

            // Update application status to shortlisted
            $application->update(['status' => 'Shortlisted']);

            return response()->json([
                'success' => true,
                'message' => 'Interview scheduled successfully',
                'data' => [
                    'interview' => [
                        'id' => $interview->id,
                        'interview_date' => $interview->interview_date,
                        'interview_time' => $interview->interview_time,
                        'interview_type' => $interview->interview_type,
                        'location' => $interview->location,
                        'status' => $interview->status,
                    ]
                ]
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Interview scheduling failed',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Download resume
     */
    public function downloadResume(Request $request, $id)
    {
        try {
            $recruiter = $request->user();
            $application = $recruiter->applications()->with('user')->findOrFail($id);

            if (!$application->user->resume) {
                return response()->json([
                    'success' => false,
                    'message' => 'No resume found'
                ], 404);
            }

            $path = storage_path('app/public/resumes/' . $application->user->resume);
            
            if (!file_exists($path)) {
                return response()->json([
                    'success' => false,
                    'message' => 'Resume file not found'
                ], 404);
            }

            return response()->download($path);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Resume download failed',
                'error' => $e->getMessage()
            ], 500);
        }
    }
}
