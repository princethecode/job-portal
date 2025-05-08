<?php

namespace App\Http\Controllers;

use App\Models\User;
use App\Models\Job;
use App\Models\Application;
use Exception;
use Illuminate\Support\Facades\DB;
use Carbon\Carbon;

class DashboardController extends Controller
{
    /**
     * Display the dashboard with statistics
     *
     * @return \Illuminate\View\View
     */
    public function index()
    {
        try {
            // Basic statistics
            $stats = [
                'total_users' => User::where('is_admin', 0)->count(),
                'total_jobs' => Job::count(),
                'total_applications' => Application::count(),
                'recent_applications' => Application::with(['user', 'job'])
                    ->orderBy('created_at', 'desc')
                    ->limit(10)
                    ->get()
            ];

            // Applications by status
            $applicationsByStatus = Application::select('status', DB::raw('count(*) as count'))
                ->groupBy('status')
                ->get()
                ->pluck('count', 'status')
                ->toArray();

            // Jobs by type
            $jobsByType = Job::select('job_type', DB::raw('count(*) as count'))
                ->groupBy('job_type')
                ->get()
                ->pluck('count', 'job_type')
                ->toArray();

            // Applications trend (last 30 days)
            $applicationTrend = Application::select(
                DB::raw('DATE(created_at) as date'),
                DB::raw('count(*) as count')
            )
                ->where('created_at', '>=', Carbon::now()->subDays(30))
                ->groupBy('date')
                ->orderBy('date')
                ->get()
                ->map(function ($item) {
                    return [
                        'date' => Carbon::parse($item->date)->format('M d'),
                        'count' => $item->count
                    ];
                });

            // Top job categories
            $topCategories = Job::select('category', DB::raw('count(*) as count'))
                ->groupBy('category')
                ->orderByDesc('count')
                ->limit(5)
                ->get()
                ->pluck('count', 'category')
                ->toArray();

            // Recent activities
            $recentActivities = collect();

            // Recent job postings
            Job::latest()
                ->take(5)
                ->get()
                ->each(function ($job) use (&$recentActivities) {
                    $recentActivities->push([
                        'type' => 'job',
                        'message' => "New job posted: {$job->title}",
                        'date' => $job->created_at,
                    ]);
                });

            // Recent applications
            Application::with(['user', 'job'])
                ->latest()
                ->take(5)
                ->get()
                ->each(function ($application) use (&$recentActivities) {
                    $recentActivities->push([
                        'type' => 'application',
                        'message' => "{$application->user->name} applied for {$application->job->title}",
                        'date' => $application->created_at,
                    ]);
                });

            // Sort activities by date
            $recentActivities = $recentActivities->sortByDesc('date')->values();

            return view('dashboard.index', compact(
                'stats',
                'applicationsByStatus',
                'jobsByType',
                'applicationTrend',
                'topCategories',
                'recentActivities'
            ));
        } catch (Exception $e) {
            return view('dashboard.index', [
                'error' => 'Error fetching dashboard statistics. ' . $e->getMessage()
            ]);
        }
    }
}
