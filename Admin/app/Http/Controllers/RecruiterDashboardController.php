<?php

namespace App\Http\Controllers;

use App\Models\Job;
use App\Models\Application;
use App\Models\Interview;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Carbon\Carbon;

class RecruiterDashboardController extends Controller
{
    public function __construct()
    {
        $this->middleware('auth:recruiter');
    }

    /**
     * Show recruiter dashboard
     */
    public function index()
    {
        $recruiter = Auth::guard('recruiter')->user();
        
        // Get statistics
        $totalJobs = $recruiter->jobs()->count();
        $activeJobs = $recruiter->jobs()->where('is_active', true)->count();
        $totalApplications = $recruiter->applications()->count();
        $pendingApplications = $recruiter->applications()->where('status', 'applied')->count();
        $scheduledInterviews = $recruiter->interviews()->where('status', 'scheduled')->count();
        
        // Job approval statistics
        $pendingApprovalJobs = $recruiter->jobs()->where('approval_status', 'pending')->count();
        $approvedJobs = $recruiter->jobs()->where('approval_status', 'approved')->count();
        $declinedJobs = $recruiter->jobs()->where('approval_status', 'declined')->count();
        
        // Recent applications
        $recentApplications = $recruiter->applications()
            ->with(['user', 'job'])
            ->orderBy('applications.created_at', 'desc')
            ->take(5)
            ->get();
        
        // Job performance data
        $jobStats = $recruiter->jobs()
            ->withCount('applications')
            ->orderBy('applications_count', 'desc')
            ->take(5)
            ->get();
        
        // Monthly application trends
        $monthlyApplications = $recruiter->applications()
            ->selectRaw('MONTH(applications.created_at) as month, COUNT(*) as count')
            ->whereYear('applications.created_at', Carbon::now()->year)
            ->groupBy('month')
            ->pluck('count', 'month')
            ->toArray();
        
        return view('recruiter.dashboard', compact(
            'totalJobs',
            'activeJobs', 
            'totalApplications',
            'pendingApplications',
            'scheduledInterviews',
            'pendingApprovalJobs',
            'approvedJobs',
            'declinedJobs',
            'recentApplications',
            'jobStats',
            'monthlyApplications'
        ));
    }
}