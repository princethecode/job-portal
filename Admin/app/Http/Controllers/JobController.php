<?php

namespace App\Http\Controllers;

use App\Models\Job;
use App\Models\Application;
use Illuminate\Http\Request;
use Exception;

class JobController extends Controller
{
    /**
     * Display a listing of jobs
     *
     * @param Request $request
     * @return \Illuminate\View\View
     */
    public function index(Request $request)
    {
        try {
            $query = Job::query();
            
            // Add filters if provided
            if ($request->has('search') && !empty($request->search)) {
                $query->where(function($q) use ($request) {
                    $q->where('title', 'like', '%' . $request->search . '%')
                      ->orWhere('company', 'like', '%' . $request->search . '%')
                      ->orWhere('description', 'like', '%' . $request->search . '%');
                });
            }
            
            if ($request->has('location') && !empty($request->location)) {
                $query->where('location', 'like', '%' . $request->location . '%');
            }
            
            if ($request->has('job_type') && !empty($request->job_type)) {
                $query->where('job_type', $request->job_type);
            }
            
            if ($request->has('category') && !empty($request->category)) {
                $query->where('category', 'like', '%' . $request->category . '%');
            }
            
            $jobs = $query->orderBy('created_at', 'desc')->paginate(10);

            return view('jobs.index', [
                'jobs' => $jobs,
                'filters' => $request->all()
            ]);
        } catch (Exception $e) {
            return view('jobs.index', [
                'error' => 'Unable to fetch jobs. ' . $e->getMessage(),
                'filters' => $request->all()
            ]);
        }
    }

    /**
     * Show the form for creating a new job
     *
     * @return \Illuminate\View\View
     */
    public function create()
    {
        return view('jobs.create');
    }

    /**
     * Store a newly created job
     *
     * @param Request $request
     * @return \Illuminate\Http\RedirectResponse
     */
    public function store(Request $request)
    {
        $request->validate([
            'title' => 'required|string|max:255',
            'description' => 'required|string',
            'company' => 'required|string|max:255',
            'location' => 'required|string|max:255',
            'salary' => 'nullable|string|max:255',
            'job_type' => 'required|in:Full-time,Part-time,Contract',
            'category' => 'required|string|max:255',
            'posting_date' => 'required|date',
            'expiry_date' => 'required|date|after:posting_date',
        ]);

        try {
            $job = Job::create($request->all());

            return redirect()->route('admin.jobs.index')
                ->with('success', 'Job created successfully');
        } catch (Exception $e) {
            return back()->withInput()->withErrors([
                'error' => 'Failed to create job. ' . $e->getMessage()
            ]);
        }
    }

    /**
     * Display the specified job
     *
     * @param int $id
     * @return \Illuminate\View\View
     */
    public function show($id)
    {
        try {
            $job = Job::with(['applications.user'])->findOrFail($id);
            
            return view('jobs.show', [
                'job' => $job,
                'applications' => $job->applications
            ]);
        } catch (Exception $e) {
            return redirect()->route('admin.jobs.index')
                ->with('error', 'Job not found or error occurred.');
        }
    }

    /**
     * Show the form for editing the specified job
     *
     * @param int $id
     * @return \Illuminate\View\View
     */
    public function edit($id)
    {
        try {
            $job = Job::findOrFail($id);
            
            return view('jobs.edit', [
                'job' => $job
            ]);
        } catch (Exception $e) {
            return redirect()->route('admin.jobs.index')
                ->with('error', 'Job not found or error occurred.');
        }
    }

    /**
     * Update the specified job
     *
     * @param Request $request
     * @param int $id
     * @return \Illuminate\Http\RedirectResponse
     */
    public function update(Request $request, $id)
    {
        $request->validate([
            'title' => 'required|string|max:255',
            'description' => 'required|string',
            'company' => 'required|string|max:255',
            'location' => 'required|string|max:255',
            'salary' => 'nullable|string|max:255',
            'job_type' => 'required|in:Full-time,Part-time,Contract',
            'category' => 'required|string|max:255',
            'posting_date' => 'required|date',
            'expiry_date' => 'required|date|after:posting_date',
            'is_active' => 'boolean'
        ]);

        try {
            $job = Job::findOrFail($id);
            $job->update($request->all());

            return redirect()->route('admin.jobs.index')
                ->with('success', 'Job updated successfully');
        } catch (Exception $e) {
            return back()->withInput()->withErrors([
                'error' => 'Failed to update job. ' . $e->getMessage()
            ]);
        }
    }

    /**
     * Remove the specified job
     *
     * @param int $id
     * @return \Illuminate\Http\RedirectResponse
     */
    public function destroy($id)
    {
        try {
            $job = Job::findOrFail($id);
            $job->delete();

            return redirect()->route('admin.jobs.index')
                ->with('success', 'Job deleted successfully');
        } catch (Exception $e) {
            return redirect()->route('admin.jobs.index')
                ->with('error', 'Failed to delete job. ' . $e->getMessage());
        }
    }
}
