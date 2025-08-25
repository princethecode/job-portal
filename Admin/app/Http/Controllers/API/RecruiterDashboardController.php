<?php

namespace App\Http\Controllers\API;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Carbon\Carbon;

class RecruiterDashboardController extends Controller
{
    /**
     * Get dashboard data for recruiter
     */
    public function index(Request $request)
    {
        $recruiter = $request->user();

        // Get basic statistics
        $stats = [
            'total_jobs' => $recruiter->jobs()->count(),
            'active_jobs' => $recruiter->jobs()->where('is_active', true)->count(),
            'total_applications' => $recruiter->applications()->count(),
            'scheduled_interviews' => $recruiter->interviews()->where('status', 'scheduled')->count(),
            'pending_applications' => $recruiter->applications()->where('status', 'Applied')->count(),
            'hired_candidates' => $recruiter->applications()->where('status', 'Hired')->count(),
            // Job approval status
            'pending_approval_jobs' => $recruiter->jobs()->where('approval_status', 'pending')->count(),
            'approved_jobs' => $recruiter->jobs()->where('approval_status', 'approved')->count(),
            'declined_jobs' => $recruiter->jobs()->where('approval_status', 'declined')->count(),
        ];

        // Get recent jobs with application counts
        $recentJobs = $recruiter->jobs()
            ->withCount('applications')
            ->orderBy('created_at', 'desc')
            ->limit(5)
            ->get()
            ->map(function ($job) use ($recruiter) {
                return [
                    'id' => $job->id,
                    'title' => $job->title,
                    'company_name' => $job->company_name ?? $recruiter->company_name, // Fallback to recruiter's company
                    'location' => $job->location,
                    'job_type' => $job->job_type,
                    'salary' => $job->salary,
                    'is_active' => $job->is_active,
                    'approval_status' => $job->approval_status,
                    'applications_count' => $job->applications_count,
                    'created_at' => $job->created_at,
                    'posted_date' => $job->created_at->diffForHumans(),
                ];
            });

        // Get monthly trends (last 6 months)
        $monthlyTrends = [];
        for ($i = 5; $i >= 0; $i--) {
            $date = Carbon::now()->subMonths($i);
            $monthlyTrends[] = [
                'month' => $date->format('M Y'),
                'jobs' => $recruiter->jobs()
                    ->whereYear('jobs.created_at', $date->year)
                    ->whereMonth('jobs.created_at', $date->month)
                    ->count(),
                'applications' => $recruiter->applications()
                    ->whereYear('applications.created_at', $date->year)
                    ->whereMonth('applications.created_at', $date->month)
                    ->count(),
                'interviews' => $recruiter->interviews()
                    ->whereYear('interviews.created_at', $date->year)
                    ->whereMonth('interviews.created_at', $date->month)
                    ->count(),
            ];
        }

        return response()->json([
            'success' => true,
            'data' => [
                'stats' => $stats,
                'recent_jobs' => $recentJobs,
                'monthly_trends' => $monthlyTrends,
                'recruiter' => [
                    'id' => $recruiter->id,
                    'name' => $recruiter->name,
                    'company_name' => $recruiter->company_name,
                ]
            ]
        ]);
    }
}
