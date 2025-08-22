<?php

namespace App\Http\Controllers\API;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Carbon\Carbon;

class RecruiterAnalyticsController extends Controller
{
    /**
     * Get analytics data for recruiter
     */
    public function index(Request $request)
    {
        $recruiter = $request->user();

        // Get date range (default: last 30 days)
        $startDate = $request->get('start_date', now()->subDays(30)->format('Y-m-d'));
        $endDate = $request->get('end_date', now()->format('Y-m-d'));

        // Basic statistics
        $stats = [
            'total_jobs' => $recruiter->jobs()->count(),
            'active_jobs' => $recruiter->jobs()->where('is_active', true)->count(),
            'total_applications' => $recruiter->applications()->count(),
            'total_interviews' => $recruiter->interviews()->count(),
            'hired_candidates' => $recruiter->applications()->where('status', 'Hired')->count(),
            'pending_applications' => $recruiter->applications()->where('status', 'Applied')->count(),
            'shortlisted_applications' => $recruiter->applications()->where('status', 'Shortlisted')->count(),
            'rejected_applications' => $recruiter->applications()->where('status', 'Rejected')->count(),
        ];

        // Monthly trends (last 6 months)
        $monthlyTrends = [];
        for ($i = 5; $i >= 0; $i--) {
            $date = Carbon::now()->subMonths($i);
            $monthlyTrends[] = [
                'month' => $date->format('M Y'),
                'jobs' => $recruiter->jobs()
                    ->whereYear('created_at', $date->year)
                    ->whereMonth('created_at', $date->month)
                    ->count(),
                'applications' => $recruiter->applications()
                    ->whereYear('created_at', $date->year)
                    ->whereMonth('created_at', $date->month)
                    ->count(),
                'interviews' => $recruiter->interviews()
                    ->whereYear('created_at', $date->year)
                    ->whereMonth('created_at', $date->month)
                    ->count(),
                'hires' => $recruiter->applications()
                    ->where('status', 'Hired')
                    ->whereYear('updated_at', $date->year)
                    ->whereMonth('updated_at', $date->month)
                    ->count(),
            ];
        }

        // Application status distribution
        $applicationStatusDistribution = [
            'Applied' => $recruiter->applications()->where('status', 'Applied')->count(),
            'Reviewing' => $recruiter->applications()->where('status', 'Reviewing')->count(),
            'Shortlisted' => $recruiter->applications()->where('status', 'Shortlisted')->count(),
            'Rejected' => $recruiter->applications()->where('status', 'Rejected')->count(),
            'Hired' => $recruiter->applications()->where('status', 'Hired')->count(),
        ];

        // Interview status distribution
        $interviewStatusDistribution = [
            'scheduled' => $recruiter->interviews()->where('status', 'scheduled')->count(),
            'completed' => $recruiter->interviews()->where('status', 'completed')->count(),
            'cancelled' => $recruiter->interviews()->where('status', 'cancelled')->count(),
            'no-show' => $recruiter->interviews()->where('status', 'no-show')->count(),
        ];

        // Top performing jobs (by application count)
        $topJobs = $recruiter->jobs()
            ->withCount('applications')
            ->orderBy('applications_count', 'desc')
            ->limit(5)
            ->get()
            ->map(function ($job) {
                return [
                    'id' => $job->id,
                    'title' => $job->title,
                    'applications_count' => $job->applications_count,
                    'is_active' => $job->is_active,
                ];
            });

        // Recent activity
        $recentActivity = collect();

        // Recent applications
        $recentApplications = $recruiter->applications()
            ->with(['user', 'job'])
            ->orderBy('created_at', 'desc')
            ->limit(5)
            ->get()
            ->map(function ($application) {
                return [
                    'type' => 'application',
                    'id' => $application->id,
                    'user_name' => $application->user->name,
                    'job_title' => $application->job->title,
                    'status' => $application->status,
                    'date' => $application->created_at,
                ];
            });

        // Recent interviews
        $recentInterviews = $recruiter->interviews()
            ->with(['user', 'job'])
            ->orderBy('created_at', 'desc')
            ->limit(5)
            ->get()
            ->map(function ($interview) {
                return [
                    'type' => 'interview',
                    'id' => $interview->id,
                    'user_name' => $interview->user->name,
                    'job_title' => $interview->job->title,
                    'status' => $interview->status,
                    'date' => $interview->created_at,
                ];
            });

        // Recent jobs
        $recentJobs = $recruiter->jobs()
            ->orderBy('created_at', 'desc')
            ->limit(5)
            ->get()
            ->map(function ($job) {
                return [
                    'type' => 'job',
                    'id' => $job->id,
                    'title' => $job->title,
                    'status' => $job->is_active ? 'Active' : 'Inactive',
                    'date' => $job->created_at,
                ];
            });

        // Combine and sort recent activity
        $recentActivity = $recentApplications->concat($recentInterviews)->concat($recentJobs)
            ->sortByDesc('date')
            ->take(10)
            ->values();

        // Conversion rates
        $totalApplications = $recruiter->applications()->count();
        $conversionRates = [
            'application_to_interview' => $totalApplications > 0 ? 
                round(($recruiter->interviews()->count() / $totalApplications) * 100, 2) : 0,
            'interview_to_hire' => $recruiter->interviews()->where('status', 'completed')->count() > 0 ? 
                round(($recruiter->applications()->where('status', 'Hired')->count() / $recruiter->interviews()->where('status', 'completed')->count()) * 100, 2) : 0,
            'overall_hire_rate' => $totalApplications > 0 ? 
                round(($recruiter->applications()->where('status', 'Hired')->count() / $totalApplications) * 100, 2) : 0,
        ];

        return response()->json([
            'success' => true,
            'data' => [
                'stats' => $stats,
                'monthly_trends' => $monthlyTrends,
                'application_status_distribution' => $applicationStatusDistribution,
                'interview_status_distribution' => $interviewStatusDistribution,
                'top_jobs' => $topJobs,
                'recent_activity' => $recentActivity,
                'conversion_rates' => $conversionRates,
                'date_range' => [
                    'start_date' => $startDate,
                    'end_date' => $endDate,
                ]
            ]
        ]);
    }
}
