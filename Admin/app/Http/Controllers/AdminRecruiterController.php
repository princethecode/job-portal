<?php

namespace App\Http\Controllers;

use App\Models\Recruiter;
use App\Models\Job;
use App\Models\Application;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;

class AdminRecruiterController extends Controller
{
    public function __construct()
    {
        $this->middleware('admin.auth');
    }

    /**
     * Display a listing of recruiters
     */
    public function index(Request $request)
    {
        $query = Recruiter::query();

        // Search functionality
        if ($request->filled('search')) {
            $search = $request->search;
            $query->where(function($q) use ($search) {
                $q->where('name', 'like', "%{$search}%")
                  ->orWhere('email', 'like', "%{$search}%")
                  ->orWhere('company_name', 'like', "%{$search}%");
            });
        }

        // Filter by status
        if ($request->filled('status')) {
            if ($request->status === 'active') {
                $query->where('is_active', true);
            } elseif ($request->status === 'inactive') {
                $query->where('is_active', false);
            }
        }

        // Filter by verification status
        if ($request->filled('verified')) {
            if ($request->verified === 'verified') {
                $query->where('is_verified', true);
            } elseif ($request->verified === 'unverified') {
                $query->where('is_verified', false);
            }
        }

        // Filter by industry
        if ($request->filled('industry')) {
            $query->where('industry', $request->industry);
        }

        $recruiters = $query->withCount(['jobs', 'applications'])
                           ->orderBy('recruiters.created_at', 'desc')
                           ->paginate(15);

        // Get statistics
        $stats = [
            'total_recruiters' => Recruiter::count(),
            'active_recruiters' => Recruiter::where('is_active', true)->count(),
            'verified_recruiters' => Recruiter::where('is_verified', true)->count(),
            'total_jobs_posted' => Job::whereHas('recruiter')->count(),
            'total_applications' => Application::whereHas('job.recruiter')->count(),
        ];

        // Get industries for filter
        $industries = Recruiter::whereNotNull('industry')
                              ->distinct()
                              ->pluck('industry')
                              ->sort();

        return view('admin.recruiters.index', compact('recruiters', 'stats', 'industries'));
    }

    /**
     * Show recruiter details
     */
    public function show(Recruiter $recruiter)
    {
        $recruiter->load(['jobs.applications', 'interviews']);

        // Calculate metrics
        $metrics = [
            'total_jobs' => $recruiter->jobs()->count(),
            'active_jobs' => $recruiter->jobs()->where('is_active', true)->count(),
            'total_applications' => $recruiter->applications()->count(),
            'total_interviews' => $recruiter->interviews()->count(),
            'hired_candidates' => $recruiter->applications()->where('status', 'Hired')->count(),
        ];

        // Recent activity
        $recentJobs = $recruiter->jobs()->orderBy('jobs.created_at', 'desc')->take(5)->get();
        $recentApplications = $recruiter->applications()->with(['user', 'job'])->orderBy('applications.created_at', 'desc')->take(5)->get();

        // Monthly stats for chart
        $monthlyStats = [];
        for ($i = 5; $i >= 0; $i--) {
            $date = now()->subMonths($i);
            $monthKey = $date->format('M Y');
            
            $monthlyStats[$monthKey] = [
                'jobs' => $recruiter->jobs()
                    ->whereYear('jobs.created_at', $date->year)
                    ->whereMonth('jobs.created_at', $date->month)
                    ->count(),
                'applications' => $recruiter->applications()
                    ->whereYear('applications.created_at', $date->year)
                    ->whereMonth('applications.created_at', $date->month)
                    ->count(),
            ];
        }

        return view('admin.recruiters.show', compact('recruiter', 'metrics', 'recentJobs', 'recentApplications', 'monthlyStats'));
    }

    /**
     * Update recruiter status
     */
    public function updateStatus(Request $request, Recruiter $recruiter)
    {
        $request->validate([
            'is_active' => 'required|boolean',
        ]);

        $recruiter->update([
            'is_active' => $request->is_active
        ]);

        $status = $request->is_active ? 'activated' : 'deactivated';
        
        return back()->with('success', "Recruiter {$status} successfully!");
    }

    /**
     * Update verification status
     */
    public function updateVerification(Request $request, Recruiter $recruiter)
    {
        $request->validate([
            'is_verified' => 'required|boolean',
        ]);

        $recruiter->update([
            'is_verified' => $request->is_verified
        ]);

        $status = $request->is_verified ? 'verified' : 'unverified';
        
        return back()->with('success', "Recruiter {$status} successfully!");
    }

    /**
     * Delete recruiter
     */
    public function destroy(Recruiter $recruiter)
    {
        // Check if recruiter has active jobs or applications
        $activeJobs = $recruiter->jobs()->where('is_active', true)->count();
        $pendingApplications = $recruiter->applications()->whereIn('status', ['Applied', 'Under Review', 'Shortlisted'])->count();

        if ($activeJobs > 0 || $pendingApplications > 0) {
            return back()->with('error', 'Cannot delete recruiter with active jobs or pending applications. Please deactivate first.');
        }

        $recruiterName = $recruiter->name;
        $recruiter->delete();

        return redirect()->route('admin.recruiters.index')
                        ->with('success', "Recruiter '{$recruiterName}' deleted successfully!");
    }

    /**
     * Bulk actions
     */
    public function bulkAction(Request $request)
    {
        $request->validate([
            'action' => 'required|in:activate,deactivate,verify,unverify,delete',
            'recruiter_ids' => 'required|array',
            'recruiter_ids.*' => 'exists:recruiters,id',
        ]);

        $recruiters = Recruiter::whereIn('id', $request->recruiter_ids);
        $count = $recruiters->count();

        switch ($request->action) {
            case 'activate':
                $recruiters->update(['is_active' => true]);
                $message = "{$count} recruiter(s) activated successfully!";
                break;
            case 'deactivate':
                $recruiters->update(['is_active' => false]);
                $message = "{$count} recruiter(s) deactivated successfully!";
                break;
            case 'verify':
                $recruiters->update(['is_verified' => true]);
                $message = "{$count} recruiter(s) verified successfully!";
                break;
            case 'unverify':
                $recruiters->update(['is_verified' => false]);
                $message = "{$count} recruiter(s) unverified successfully!";
                break;
            case 'delete':
                // Check for active jobs/applications before deleting
                $canDelete = true;
                foreach ($recruiters->get() as $recruiter) {
                    $activeJobs = $recruiter->jobs()->where('is_active', true)->count();
                    $pendingApplications = $recruiter->applications()->whereIn('status', ['Applied', 'Under Review', 'Shortlisted'])->count();
                    
                    if ($activeJobs > 0 || $pendingApplications > 0) {
                        $canDelete = false;
                        break;
                    }
                }
                
                if (!$canDelete) {
                    return back()->with('error', 'Cannot delete recruiters with active jobs or pending applications.');
                }
                
                $recruiters->delete();
                $message = "{$count} recruiter(s) deleted successfully!";
                break;
        }

        return back()->with('success', $message);
    }

    /**
     * Export recruiters data
     */
    public function export(Request $request)
    {
        $recruiters = Recruiter::withCount(['jobs', 'applications'])->get();

        $filename = 'recruiters_export_' . now()->format('Y-m-d') . '.csv';
        
        $headers = [
            'Content-Type' => 'text/csv',
            'Content-Disposition' => 'attachment; filename="' . $filename . '"',
        ];

        return response()->stream(function () use ($recruiters) {
            $handle = fopen('php://output', 'w');
            
            // CSV Headers
            fputcsv($handle, [
                'ID', 'Name', 'Email', 'Company', 'Industry', 'Location', 
                'Jobs Posted', 'Applications Received', 'Status', 'Verified', 
                'Registration Date', 'Last Login'
            ]);

            // CSV Data
            foreach ($recruiters as $recruiter) {
                fputcsv($handle, [
                    $recruiter->id,
                    $recruiter->name,
                    $recruiter->email,
                    $recruiter->company_name,
                    $recruiter->industry,
                    $recruiter->location,
                    $recruiter->jobs_count,
                    $recruiter->applications_count,
                    $recruiter->is_active ? 'Active' : 'Inactive',
                    $recruiter->is_verified ? 'Verified' : 'Unverified',
                    $recruiter->created_at->format('Y-m-d'),
                    $recruiter->updated_at->format('Y-m-d'),
                ]);
            }
            
            fclose($handle);
        }, 200, $headers);
    }
}