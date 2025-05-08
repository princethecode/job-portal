<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Models\Application;
use App\Models\Job;
use Exception;

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
            'status' => 'required|in:Applied,Under Review,Shortlisted,Rejected',
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
}
