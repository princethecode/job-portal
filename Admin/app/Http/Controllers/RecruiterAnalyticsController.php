<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\DB;
use Carbon\Carbon;

class RecruiterAnalyticsController extends Controller
{
    public function __construct()
    {
        $this->middleware('recruiter.auth');
    }

    /**
     * Show analytics dashboard
     */
    public function index()
    {
        $recruiter = Auth::guard('recruiter')->user();
        
        // Basic metrics
        $metrics = [
            'total_jobs' => $recruiter->jobs()->count(),
            'active_jobs' => $recruiter->jobs()->where('is_active', true)->count(),
            'total_applications' => $recruiter->applications()->count(),
            'total_interviews' => $recruiter->interviews()->count(),
            'hired_candidates' => $recruiter->applications()->where('status', 'Hired')->count(),
        ];
        
        // Application funnel
        $applicationFunnel = [
            'Applied' => $recruiter->applications()->where('status', 'Applied')->count(),
            'Under Review' => $recruiter->applications()->where('status', 'Under Review')->count(),
            'Shortlisted' => $recruiter->applications()->where('status', 'Shortlisted')->count(),
            'Rejected' => $recruiter->applications()->where('status', 'Rejected')->count(),
            'Hired' => $recruiter->applications()->where('status', 'Hired')->count(),
        ];
        
        // Monthly trends (last 12 months)
        $monthlyData = [];
        for ($i = 11; $i >= 0; $i--) {
            $date = Carbon::now()->subMonths($i);
            $monthKey = $date->format('M Y');
            
            $monthlyData[$monthKey] = [
                'jobs_posted' => $recruiter->jobs()
                    ->whereYear('jobs.created_at', $date->year)
                    ->whereMonth('jobs.created_at', $date->month)
                    ->count(),
                'applications_received' => $recruiter->applications()
                    ->whereYear('applications.created_at', $date->year)
                    ->whereMonth('applications.created_at', $date->month)
                    ->count(),
                'interviews_conducted' => $recruiter->interviews()
                    ->whereYear('interviews.created_at', $date->year)
                    ->whereMonth('interviews.created_at', $date->month)
                    ->count(),
                'hires_made' => $recruiter->applications()
                    ->where('status', 'Hired')
                    ->whereYear('applications.updated_at', $date->year)
                    ->whereMonth('applications.updated_at', $date->month)
                    ->count(),
            ];
        }
        
        // Top performing jobs
        $topJobs = $recruiter->jobs()
            ->withCount('applications')
            ->orderBy('applications_count', 'desc')
            ->take(5)
            ->get();
        
        // Interview success rate
        $completedInterviews = $recruiter->interviews()->where('status', 'completed')->count();
        $totalInterviews = $recruiter->interviews()->count();
        $interviewSuccessRate = $totalInterviews > 0 ? round(($completedInterviews / $totalInterviews) * 100, 1) : 0;
        
        // Average time to hire
        $hiredApplications = $recruiter->applications()
            ->where('status', 'Hired')
            ->get();
        
        $avgTimeToHire = 0;
        if ($hiredApplications->count() > 0) {
            $totalDays = $hiredApplications->sum(function ($app) {
                return $app->created_at->diffInDays($app->updated_at);
            });
            $avgTimeToHire = round($totalDays / $hiredApplications->count(), 1);
        }
        
        // Source analysis (where applications come from)
        $sourceAnalysis = [
            'Direct Applications' => $recruiter->applications()->count(),
            'Referrals' => 0, // Placeholder for future implementation
            'Job Boards' => 0, // Placeholder for future implementation
        ];
        
        return view('recruiter.analytics.index', compact(
            'metrics',
            'applicationFunnel',
            'monthlyData',
            'topJobs',
            'interviewSuccessRate',
            'avgTimeToHire',
            'sourceAnalysis'
        ));
    }

    /**
     * Export analytics report
     */
    public function export(Request $request)
    {
        $recruiter = Auth::guard('recruiter')->user();
        $format = $request->get('format', 'csv');
        
        // Generate report data
        $reportData = [
            'recruiter' => $recruiter,
            'generated_at' => now(),
            'jobs' => $recruiter->jobs()->with('applications')->get(),
            'applications' => $recruiter->applications()->with(['user', 'job'])->get(),
            'interviews' => $recruiter->interviews()->with(['user', 'job'])->get(),
        ];
        
        if ($format === 'pdf') {
            return $this->exportPDF($reportData);
        } else {
            return $this->exportCSV($reportData);
        }
    }
    
    private function exportCSV($data)
    {
        $filename = 'recruiting_report_' . now()->format('Y-m-d') . '.csv';
        
        $headers = [
            'Content-Type' => 'text/csv',
            'Content-Disposition' => 'attachment; filename="' . $filename . '"',
        ];
        
        return response()->stream(function () use ($data) {
            $handle = fopen('php://output', 'w');
            
            // Applications CSV
            fputcsv($handle, ['Applications Report - Generated: ' . $data['generated_at']]);
            fputcsv($handle, ['Candidate Name', 'Job Title', 'Status', 'Applied Date', 'Email']);
            
            foreach ($data['applications'] as $app) {
                fputcsv($handle, [
                    $app->user->name,
                    $app->job->title,
                    $app->status,
                    $app->created_at->format('Y-m-d'),
                    $app->user->email,
                ]);
            }
            
            fputcsv($handle, []); // Empty row
            
            // Jobs CSV
            fputcsv($handle, ['Jobs Report']);
            fputcsv($handle, ['Job Title', 'Status', 'Applications Count', 'Posted Date']);
            
            foreach ($data['jobs'] as $job) {
                fputcsv($handle, [
                    $job->title,
                    $job->is_active ? 'Active' : 'Inactive',
                    $job->applications->count(),
                    $job->created_at->format('Y-m-d'),
                ]);
            }
            
            fclose($handle);
        }, 200, $headers);
    }
    
    private function exportPDF($data)
    {
        // For now, return a simple text response
        // In a real implementation, you'd use a PDF library like DomPDF
        return response()->json([
            'message' => 'PDF export feature coming soon! Please use CSV export for now.',
            'data' => $data
        ]);
    }
}