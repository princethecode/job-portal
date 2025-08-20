<?php

namespace App\Http\Controllers;

use App\Models\Application;
use App\Models\Interview;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Storage;

class RecruiterApplicationController extends Controller
{
    public function __construct()
    {
        $this->middleware('auth:recruiter');
    }

    /**
     * Display applications for recruiter's jobs
     */
    public function index(Request $request)
    {
        $recruiter = Auth::guard('recruiter')->user();
        
        $query = $recruiter->applications()->with(['user', 'job']);
        
        // Filter by status
        if ($request->filled('status')) {
            $query->where('status', $request->status);
        }
        
        // Filter by job
        if ($request->filled('job_id')) {
            $query->where('job_id', $request->job_id);
        }
        
        // Search by candidate name or email
        if ($request->filled('search')) {
            $query->whereHas('user', function($q) use ($request) {
                $q->where('name', 'like', '%' . $request->search . '%')
                  ->orWhere('email', 'like', '%' . $request->search . '%');
            });
        }
        
        $applications = $query->latest()->paginate(15);
        
        // Get recruiter's jobs for filter dropdown
        $jobs = $recruiter->jobs()->select('id', 'title')->get();
        
        return view('recruiter.applications.index', compact('applications', 'jobs'));
    }

    /**
     * Show application details
     */
    public function show(Application $application)
    {
        $this->authorize('view', $application);
        
        $application->load(['user', 'job', 'interviews']);
        
        return view('recruiter.applications.show', compact('application'));
    }

    /**
     * Update application status
     */
    public function updateStatus(Request $request, Application $application)
    {
        $this->authorize('update', $application);
        
        $request->validate([
            'status' => 'required|in:Applied,Under Review,Shortlisted,Rejected,Hired',
            'notes' => 'nullable|string|max:1000',
        ]);
        
        $oldStatus = $application->status;
        
        $application->update([
            'status' => $request->status,
            'notes' => $request->notes,
        ]);
        
        // Send email notification to candidate
        try {
            \Illuminate\Support\Facades\Mail::to($application->user->email)
                ->send(new \App\Mail\ApplicationStatusUpdate($application, $oldStatus));
        } catch (\Exception $e) {
            \Illuminate\Support\Facades\Log::error('Failed to send status update email: ' . $e->getMessage());
        }
        
        return back()->with('success', 'Application status updated successfully! Email notification sent to candidate.');
    }

    /**
     * Download candidate resume
     */
    public function downloadResume(Application $application)
    {
        $this->authorize('view', $application);
        
        if (!$application->resume_path || !Storage::disk('public')->exists($application->resume_path)) {
            return back()->with('error', 'Resume file not found.');
        }
        
        return Storage::disk('public')->download($application->resume_path);
    }

    /**
     * Schedule interview
     */
    public function scheduleInterview(Request $request, Application $application)
    {
        $this->authorize('update', $application);
        
        $request->validate([
            'interview_date' => 'required|date|after:today',
            'interview_time' => 'required|date_format:H:i',
            'interview_type' => 'required|in:online,offline,phone',
            'meeting_link' => 'required_if:interview_type,online|nullable|url',
            'location' => 'required_if:interview_type,offline|nullable|string',
            'notes' => 'nullable|string|max:1000',
        ]);
        
        $recruiter = Auth::guard('recruiter')->user();
        
        $interview = Interview::create([
            'application_id' => $application->id,
            'recruiter_id' => $recruiter->id,
            'user_id' => $application->user_id,
            'job_id' => $application->job_id,
            'interview_date' => $request->interview_date,
            'interview_time' => $request->interview_date . ' ' . $request->interview_time,
            'interview_type' => $request->interview_type,
            'meeting_link' => $request->meeting_link,
            'location' => $request->location,
            'notes' => $request->notes,
        ]);
        
        // Update application status to under review
        $application->update(['status' => 'Under Review']);
        
        // Send interview invitation email to candidate
        try {
            \Illuminate\Support\Facades\Mail::to($application->user->email)
                ->send(new \App\Mail\InterviewInvitation($interview));
        } catch (\Exception $e) {
            \Illuminate\Support\Facades\Log::error('Failed to send interview invitation email: ' . $e->getMessage());
        }
        
        return back()->with('success', 'Interview scheduled successfully!');
    }

    /**
     * Bulk update application status
     */
    public function bulkUpdateStatus(Request $request)
    {
        $request->validate([
            'application_ids' => 'required|array',
            'application_ids.*' => 'exists:applications,id',
            'status' => 'required|in:applied,under_review,shortlisted,rejected,hired',
        ]);
        
        $recruiter = Auth::guard('recruiter')->user();
        
        // Ensure recruiter can only update their own job applications
        $applications = $recruiter->applications()
            ->whereIn('id', $request->application_ids)
            ->get();
        
        foreach ($applications as $application) {
            $application->update(['status' => $request->status]);
        }
        
        return back()->with('success', count($applications) . ' applications updated successfully!');
    }
}