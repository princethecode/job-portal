<?php

namespace App\Http\Controllers\API;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;

class RecruiterJobAPIController extends Controller
{
    public function __construct()
    {
        $this->middleware('recruiter.auth');
    }

    /**
     * Get active jobs for the authenticated recruiter
     */
    public function getActiveJobs()
    {
        $recruiter = Auth::guard('recruiter')->user();
        
        $jobs = $recruiter->jobs()
            ->where('is_active', true)
            ->where('expiry_date', '>', now())
            ->select('id', 'title', 'location', 'job_type', 'salary')
            ->orderBy('created_at', 'desc')
            ->get();
        
        return response()->json([
            'success' => true,
            'jobs' => $jobs
        ]);
    }

    /**
     * Get job statistics for dashboard
     */
    public function getJobStats()
    {
        $recruiter = Auth::guard('recruiter')->user();
        
        $stats = [
            'total_jobs' => $recruiter->jobs()->count(),
            'active_jobs' => $recruiter->jobs()->where('is_active', true)->count(),
            'expired_jobs' => $recruiter->jobs()->where('expiry_date', '<', now())->count(),
            'total_applications' => $recruiter->applications()->count(),
            'pending_applications' => $recruiter->applications()->where('status', 'Applied')->count(),
            'shortlisted_applications' => $recruiter->applications()->where('status', 'Shortlisted')->count(),
            'scheduled_interviews' => $recruiter->interviews()->where('status', 'scheduled')->count(),
        ];
        
        return response()->json([
            'success' => true,
            'stats' => $stats
        ]);
    }
}