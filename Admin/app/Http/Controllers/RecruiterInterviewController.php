<?php

namespace App\Http\Controllers;

use App\Models\Interview;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;

class RecruiterInterviewController extends Controller
{
    public function __construct()
    {
        $this->middleware('auth:recruiter');
    }

    /**
     * Display interviews scheduled by recruiter
     */
    public function index(Request $request)
    {
        $recruiter = Auth::guard('recruiter')->user();
        
        $query = $recruiter->interviews()->with(['user', 'job', 'application']);
        
        // Filter by status
        if ($request->filled('status')) {
            $query->where('status', $request->status);
        }
        
        // Filter by date range
        if ($request->filled('date_from')) {
            $query->whereDate('interview_date', '>=', $request->date_from);
        }
        
        if ($request->filled('date_to')) {
            $query->whereDate('interview_date', '<=', $request->date_to);
        }
        
        // Filter by interview type
        if ($request->filled('interview_type')) {
            $query->where('interview_type', $request->interview_type);
        }
        
        $interviews = $query->orderBy('interview_date')->paginate(15);
        
        return view('recruiter.interviews.index', compact('interviews'));
    }

    /**
     * Show interview details
     */
    public function show(Interview $interview)
    {
        $this->authorize('view', $interview);
        
        $interview->load(['user', 'job', 'application']);
        
        return view('recruiter.interviews.show', compact('interview'));
    }

    /**
     * Update interview details
     */
    public function update(Request $request, Interview $interview)
    {
        $this->authorize('update', $interview);
        
        $request->validate([
            'interview_date' => 'required|date',
            'interview_time' => 'required|date_format:H:i',
            'interview_type' => 'required|in:online,offline,phone',
            'meeting_link' => 'required_if:interview_type,online|nullable|url',
            'location' => 'required_if:interview_type,offline|nullable|string',
            'notes' => 'nullable|string|max:1000',
        ]);
        
        $interview->update([
            'interview_date' => $request->interview_date,
            'interview_time' => $request->interview_date . ' ' . $request->interview_time,
            'interview_type' => $request->interview_type,
            'meeting_link' => $request->meeting_link,
            'location' => $request->location,
            'notes' => $request->notes,
        ]);
        
        return back()->with('success', 'Interview updated successfully!');
    }

    /**
     * Update interview status
     */
    public function updateStatus(Request $request, Interview $interview)
    {
        $this->authorize('update', $interview);
        
        $request->validate([
            'status' => 'required|in:scheduled,completed,cancelled,rescheduled',
            'feedback' => 'nullable|string|max:2000',
            'rating' => 'nullable|integer|min:1|max:5',
        ]);
        
        $interview->update([
            'status' => $request->status,
            'feedback' => $request->feedback,
            'rating' => $request->rating,
        ]);
        
        // If interview is completed, you might want to update application status
        if ($request->status === 'completed') {
            // Optionally update application status based on rating
            if ($request->rating >= 4) {
                $interview->application->update(['status' => 'shortlisted']);
            }
        }
        
        return back()->with('success', 'Interview status updated successfully!');
    }

    /**
     * Cancel interview
     */
    public function cancel(Request $request, Interview $interview)
    {
        $this->authorize('update', $interview);
        
        $request->validate([
            'cancellation_reason' => 'required|string|max:500',
        ]);
        
        $interview->update([
            'status' => 'cancelled',
            'notes' => $request->cancellation_reason,
        ]);
        
        // Send cancellation notification to candidate
        // $this->sendCancellationNotification($interview);
        
        return back()->with('success', 'Interview cancelled successfully!');
    }

    /**
     * Get upcoming interviews for calendar view
     */
    public function calendar()
    {
        $recruiter = Auth::guard('recruiter')->user();
        
        // Get all interviews for calendar display (current month and surrounding months)
        $startDate = now()->startOfMonth()->subMonth();
        $endDate = now()->endOfMonth()->addMonth();
        
        $interviews = $recruiter->interviews()
            ->with(['user', 'job'])
            ->whereBetween('interview_date', [$startDate, $endDate])
            ->orderBy('interview_date')
            ->get()
            ->filter(function ($interview) {
                // Only include interviews with valid user and job relationships
                return $interview->user && $interview->job;
            });
        
        return view('recruiter.interviews.calendar', compact('interviews'));
    }
}