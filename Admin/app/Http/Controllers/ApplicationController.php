<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Models\Application;
use App\Models\Job;
use Exception;
use App\Exports\ApplicationsExport;
use Maatwebsite\Excel\Facades\Excel;

class ApplicationController extends Controller
{
    /**
     * Display a listing of applications
     *
     * @param Request $request
     * @return \Illuminate\View\View
     */
    public function index(Request $request)
    {
        try {
            $query = Application::with(['user', 'job']);
            
            // Add filters if provided
            if ($request->has('job_id') && !empty($request->job_id)) {
                $query->where('job_id', $request->job_id);
            }
            
            if ($request->has('status') && !empty($request->status)) {
                $query->where('status', $request->status);
            }
            
            $applications = $query->orderBy('created_at', 'desc')->paginate(10);
            
            // Get all jobs for the filter dropdown
            $jobs = Job::orderBy('title')->get();

            return view('applications.index', [
                'applications' => $applications,
                'jobs' => $jobs,
                'filters' => $request->all()
            ]);
        } catch (Exception $e) {
            return view('applications.index', [
                'error' => 'Unable to fetch applications. ' . $e->getMessage(),
                'filters' => $request->all()
            ]);
        }
    }

    /**
     * Display the specified application
     *
     * @param int $id
     * @return \Illuminate\View\View
     */
    public function show($id)
    {
        try {
            $application = Application::with(['user', 'job'])->findOrFail($id);
            
            return view('applications.show', [
                'application' => $application
            ]);
        } catch (Exception $e) {
            return redirect()->route('admin.applications.index')
                ->with('error', 'Application not found or error occurred.');
        }
    }

    /**
     * Update application status
     *
     * @param Request $request
     * @param int $id
     * @return \Illuminate\Http\RedirectResponse
     */
    public function updateStatus(Request $request, $id)
    {
        $request->validate([
            'status' => 'required|in:Applied,Under Review,Shortlisted,Rejected,Accepted',
        ]);

        try {
            $application = Application::findOrFail($id);
            $application->status = $request->status;
            $application->save();

            return redirect()->back()
                ->with('success', 'Application status updated successfully');
        } catch (Exception $e) {
            return redirect()->back()
                ->with('error', 'Failed to update application status.');
        }
    }

    /**
     * View application resume
     *
     * @param int $id
     * @return \Illuminate\Http\Response
     */
    public function viewResume($id)
    {
        try {
            $application = Application::findOrFail($id);
            
            if (!$application->resume_path) {
                return redirect()->back()->with('error', 'No resume found for this application.');
            }

            $path = storage_path('app/public/resumes/' . $application->resume_path);
            
            if (!file_exists($path)) {
                return redirect()->back()->with('error', 'Resume file not found.');
            }

            return response()->file($path, [
                'Content-Type' => 'application/pdf',
                'Content-Disposition' => 'inline; filename="' . $application->resume_path . '"'
            ]);
        } catch (Exception $e) {
            return redirect()->back()->with('error', 'Failed to view resume.');
        }
    }

    /**
     * Download application resume
     *
     * @param int $id
     * @return \Illuminate\Http\Response
     */
    public function downloadResume($id)
    {
        try {
            $application = Application::findOrFail($id);
            
            if (!$application->resume_path) {
                return redirect()->back()->with('error', 'No resume found for this application.');
            }

            $path = storage_path('app/public/resumes/' . $application->resume_path);
            
            if (!file_exists($path)) {
                return redirect()->back()->with('error', 'Resume file not found.');
            }

            return response()->download($path, $application->resume_path, [
                'Content-Type' => 'application/pdf',
                'Content-Disposition' => 'attachment; filename="' . $application->resume_path . '"'
            ]);
        } catch (Exception $e) {
            return redirect()->back()->with('error', 'Failed to download resume.');
        }
    }

    /**
     * Export applications to Excel
     *
     * @param Request $request
     * @return \Symfony\Component\HttpFoundation\BinaryFileResponse
     */
    public function export(Request $request)
    {
        $filters = $request->only(['job_id', 'status']);
        
        // Generate filename based on filters
        $filename = 'applications';
        if (!empty($filters['job_id'])) {
            $job = Job::find($filters['job_id']);
            $filename .= '-' . str_replace(' ', '-', strtolower($job ? $job->title : 'unknown-job'));
        }
        if (!empty($filters['status'])) {
            $filename .= '-' . str_replace(' ', '-', strtolower($filters['status']));
        }
        $filename .= '-' . now()->format('Y-m-d') . '.xlsx';
        
        return Excel::download(new ApplicationsExport($filters), $filename);
    }
}
