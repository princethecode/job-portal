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

            // Applications by status - ensure we have default statuses even if no applications exist
            $applicationsByStatus = Application::select('status', DB::raw('count(*) as count'))
                ->groupBy('status')
                ->get()
                ->pluck('count', 'status')
                ->toArray();
                
            // Ensure all statuses are present even if they have no applications
            $defaultStatuses = ['Applied', 'Under Review', 'Shortlisted', 'Rejected'];
            foreach ($defaultStatuses as $status) {
                if (!array_key_exists($status, $applicationsByStatus)) {
                    $applicationsByStatus[$status] = 0;
                }
            }

            // Jobs by type - ensure we have default types even if no jobs exist
            $jobsByType = Job::select('job_type', DB::raw('count(*) as count'))
                ->groupBy('job_type')
                ->get()
                ->pluck('count', 'job_type')
                ->toArray();
            
            // Ensure we have some default job types even if none exist
            if (empty($jobsByType)) {
                $jobsByType = [
                    'Full-time' => 0,
                    'Part-time' => 0,
                    'Contract' => 0,
                    'Freelance' => 0,
                    'Internship' => 0
                ];
            }

            // Applications trend (last 30 days)
            $applicationTrend = Application::select(
                DB::raw('DATE(created_at) as date'),
                DB::raw('count(*) as count')
            )
                ->where('created_at', '>=', Carbon::now()->subDays(30))
                ->groupBy('date')
                ->orderBy('date')
                ->get();
            
            // Fill in missing dates with zero counts
            $dateRange = [];
            for ($i = 30; $i >= 0; $i--) {
                $date = Carbon::now()->subDays($i);
                $dateRange[$date->format('Y-m-d')] = 0;
            }
            
            foreach ($applicationTrend as $item) {
                $dateRange[$item->date] = $item->count;
            }
            
            $applicationTrend = collect(array_map(function ($date, $count) {
                return [
                    'date' => Carbon::parse($date)->format('M d'),
                    'count' => $count
                ];
            }, array_keys($dateRange), array_values($dateRange)));

            // Top job categories
            $topCategories = Job::select('category', DB::raw('count(*) as count'))
                ->groupBy('category')
                ->orderByDesc('count')
                ->limit(5)
                ->get()
                ->pluck('count', 'category')
                ->toArray();
                
            // If we don't have any categories, provide defaults
            if (empty($topCategories)) {
                $topCategories = [
                    'Technology' => 0,
                    'Healthcare' => 0,
                    'Finance' => 0,
                    'Education' => 0,
                    'Marketing' => 0
                ];
            }

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
                    $userName = $application->user ? $application->user->name : 'Deleted User';
                    $jobTitle = $application->job ? $application->job->title : 'Deleted Job';
                    $recentActivities->push([
                        'type' => 'application',
                        'message' => "{$userName} applied for {$jobTitle}",
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
