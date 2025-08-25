<?php

namespace App\Http\Controllers\API;

use App\Http\Controllers\Controller;
use App\Models\Job;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;
use App\Services\FirebaseNotificationService;
use App\Mail\JobApprovalUpdate;
use Illuminate\Support\Facades\Mail;

class AdminJobApprovalController extends Controller
{
    protected $notificationService;

    public function __construct(FirebaseNotificationService $notificationService)
    {
        $this->notificationService = $notificationService;
    }

    /**
     * Get all jobs pending approval
     */
    public function pendingJobs(Request $request)
    {
        $query = Job::with(['recruiter', 'applications'])->pending();

        // Apply search filter if provided
        if ($request->has('search') && !empty($request->search)) {
            $query->where(function($q) use ($request) {
                $q->where('title', 'like', '%' . $request->search . '%')
                  ->orWhere('company_name', 'like', '%' . $request->search . '%')
                  ->orWhere('description', 'like', '%' . $request->search . '%');
            });
        }

        // Apply filters
        if ($request->has('job_type') && !empty($request->job_type)) {
            $query->where('job_type', $request->job_type);
        }

        if ($request->has('category') && !empty($request->category)) {
            $query->where('category', 'like', '%' . $request->category . '%');
        }

        $jobs = $query->orderBy('created_at', 'desc')->paginate(15);

        return response()->json([
            'success' => true,
            'data' => [
                'jobs' => $jobs->items(),
                'pagination' => [
                    'current_page' => $jobs->currentPage(),
                    'last_page' => $jobs->lastPage(),
                    'per_page' => $jobs->perPage(),
                    'total' => $jobs->total(),
                ]
            ]
        ]);
    }

    /**
     * Get all jobs with approval status
     */
    public function allJobsWithStatus(Request $request)
    {
        $query = Job::with(['recruiter', 'applications', 'approvedBy']);

        // Apply approval status filter
        if ($request->has('approval_status') && !empty($request->approval_status)) {
            $query->where('approval_status', $request->approval_status);
        }

        // Apply search filter if provided
        if ($request->has('search') && !empty($request->search)) {
            $query->where(function($q) use ($request) {
                $q->where('title', 'like', '%' . $request->search . '%')
                  ->orWhere('company_name', 'like', '%' . $request->search . '%')
                  ->orWhere('description', 'like', '%' . $request->search . '%');
            });
        }

        $jobs = $query->orderBy('created_at', 'desc')->paginate(15);

        return response()->json([
            'success' => true,
            'data' => [
                'jobs' => $jobs->items(),
                'pagination' => [
                    'current_page' => $jobs->currentPage(),
                    'last_page' => $jobs->lastPage(),
                    'per_page' => $jobs->perPage(),
                    'total' => $jobs->total(),
                ]
            ]
        ]);
    }

    /**
     * Approve a job posting
     */
    public function approveJob(Request $request, $id)
    {
        try {
            $job = Job::findOrFail($id);
            
            // Check if job is pending
            if (!$job->isPending()) {
                return response()->json([
                    'success' => false,
                    'message' => 'Job is not pending approval'
                ], 400);
            }

            $admin = $request->user();
            
            // Update job status
            $job->update([
                'approval_status' => 'approved',
                'approved_by' => $admin->id,
                'approved_at' => now(),
                'decline_reason' => null // Clear any previous decline reason
            ]);

            // Send notification to recruiter
            $this->sendApprovalNotification($job, 'approved');

            // Get all users who should be notified about new jobs
            $users = User::whereNotNull('fcm_token')->get();

            // Send notification to all users about new approved job
            foreach ($users as $user) {
                $this->notificationService->sendNewJobNotification(
                    $user->fcm_token,
                    $job->title,
                    $job->id
                );
            }

            return response()->json([
                'success' => true,
                'message' => 'Job approved successfully',
                'data' => [
                    'job' => $job->load(['recruiter', 'approvedBy'])
                ]
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Failed to approve job',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Decline a job posting
     */
    public function declineJob(Request $request, $id)
    {
        $validator = Validator::make($request->all(), [
            'decline_reason' => 'required|string|max:1000'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation failed',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            $job = Job::findOrFail($id);
            
            // Check if job is pending
            if (!$job->isPending()) {
                return response()->json([
                    'success' => false,
                    'message' => 'Job is not pending approval'
                ], 400);
            }

            $admin = $request->user();
            
            // Update job status
            $job->update([
                'approval_status' => 'declined',
                'approved_by' => $admin->id,
                'approved_at' => now(),
                'decline_reason' => $request->decline_reason
            ]);

            // Send notification to recruiter
            $this->sendApprovalNotification($job, 'declined', $request->decline_reason);

            return response()->json([
                'success' => true,
                'message' => 'Job declined successfully',
                'data' => [
                    'job' => $job->load(['recruiter', 'approvedBy'])
                ]
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Failed to decline job',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Get job approval statistics
     */
    public function getApprovalStats()
    {
        $stats = [
            'pending' => Job::pending()->count(),
            'approved' => Job::approved()->count(),
            'declined' => Job::declined()->count(),
            'total' => Job::count(),
            'recent_pending' => Job::pending()->where('created_at', '>=', now()->subDays(7))->count(),
        ];

        return response()->json([
            'success' => true,
            'data' => $stats
        ]);
    }

    /**
     * Send approval notification to recruiter
     */
    private function sendApprovalNotification($job, $status, $reason = null)
    {
        try {
            // Send email notification
            if ($job->recruiter && $job->recruiter->email) {
                Mail::to($job->recruiter->email)->send(new JobApprovalUpdate($job, $status, $reason));
            }

            // Send FCM notification if recruiter has a token
            if ($job->recruiter && $job->recruiter->fcm_token) {
                $title = $status === 'approved' ? 'Job Approved' : 'Job Declined';
                $body = $status === 'approved' 
                    ? "Your job posting '{$job->title}' has been approved and is now live!"
                    : "Your job posting '{$job->title}' has been declined. Please check your email for details.";
                
                $this->notificationService->sendJobApprovalNotification(
                    $job->recruiter->fcm_token,
                    $title,
                    $body,
                    $job->id
                );
            }
        } catch (\Exception $e) {
            // Log error but don't fail the approval process
            \Log::error('Failed to send approval notification: ' . $e->getMessage());
        }
    }
}