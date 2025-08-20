<?php

namespace App\Http\Controllers;

use App\Models\Job;
use App\Models\Category;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Storage;

class RecruiterJobController extends Controller
{
    public function __construct()
    {
        $this->middleware('auth:recruiter');
    }

    /**
     * Display a listing of recruiter's jobs
     */
    public function index(Request $request)
    {
        $recruiter = Auth::guard('recruiter')->user();
        
        $query = $recruiter->jobs()->withCount('applications');
        
        // Search functionality
        if ($request->filled('search')) {
            $query->where(function($q) use ($request) {
                $q->where('title', 'like', '%' . $request->search . '%')
                  ->orWhere('company', 'like', '%' . $request->search . '%')
                  ->orWhere('location', 'like', '%' . $request->search . '%');
            });
        }
        
        // Filter by status
        if ($request->filled('status')) {
            $query->where('is_active', $request->status === 'active');
        }
        
        // Filter by job type
        if ($request->filled('job_type')) {
            $query->where('job_type', $request->job_type);
        }
        
        $jobs = $query->latest()->paginate(10);
        
        return view('recruiter.jobs.index', compact('jobs'));
    }

    /**
     * Show the form for creating a new job
     */
    public function create()
    {
        $recruiter = Auth::guard('recruiter')->user();
        
        // Get predefined categories + categories used by this recruiter
        $categories = Category::where('is_active', true)
            ->where(function($query) use ($recruiter) {
                $query->where('sort_order', '<', 999) // Predefined categories
                      ->orWhereIn('name', $recruiter->jobs()->distinct()->pluck('category')); // Recruiter's used categories
            })
            ->orderBy('sort_order')
            ->orderBy('name')
            ->get();
            
        return view('recruiter.jobs.create', compact('categories'));
    }

    /**
     * Store a newly created job
     */
    public function store(Request $request)
    {
        $request->validate([
            'title' => 'required|string|max:255',
            'description' => 'required|string',
            'location' => 'required|string|max:255',
            'job_type' => 'required|in:Full-time,Part-time,Contract,Internship',
            'category' => 'required|string|max:255',
            'salary' => 'nullable|numeric|min:0',
            'expiry_date' => 'required|date|after:today',
            'requirements' => 'nullable|string',
            'benefits' => 'nullable|string',
            'experience_required' => 'nullable|string',
            'skills_required' => 'nullable|array',
            'image' => 'nullable|image|mimes:jpeg,png,jpg,gif|max:2048',
        ]);

        // Handle custom category creation
        $categoryName = trim($request->category);
        $isNewCategory = false;
        if (!empty($categoryName)) {
            // Check if category exists, if not create it
            $existingCategory = Category::where('name', $categoryName)->first();
            if (!$existingCategory) {
                Category::create([
                    'name' => $categoryName,
                    'slug' => \Illuminate\Support\Str::slug($categoryName),
                    'description' => 'Custom category created by recruiter',
                    'is_active' => true,
                    'sort_order' => 999, // Put custom categories at the end
                ]);
                $isNewCategory = true;
            }
        }

        $recruiter = Auth::guard('recruiter')->user();
        
        $jobData = $request->all();
        $jobData['recruiter_id'] = $recruiter->id;
        $jobData['company'] = $recruiter->company_name;
        $jobData['posting_date'] = now();
        $jobData['is_active'] = true;
        
        // Handle skills as JSON
        if ($request->filled('skills_required') && is_array($request->skills_required)) {
            $jobData['skills_required'] = json_encode($request->skills_required);
        } else {
            $jobData['skills_required'] = null;
        }
        
        // Handle image upload
        if ($request->hasFile('image')) {
            $jobData['image'] = $request->file('image')->store('job_images', 'public');
        }

        Job::create($jobData);

        $successMessage = 'Job posted successfully!';
        if ($isNewCategory) {
            $successMessage .= " New category '{$categoryName}' has been created and will be available for future job postings.";
        }

        return redirect()->route('recruiter.jobs.index')
            ->with('success', $successMessage);
    }

    /**
     * Display the specified job
     */
    public function show(Job $job)
    {
        $this->authorize('view', $job);
        
        $job->load(['applications.user', 'interviews']);
        $job->increment('views_count');
        
        return view('recruiter.jobs.show', compact('job'));
    }

    /**
     * Show the form for editing the specified job
     */
    public function edit(Job $job)
    {
        $this->authorize('update', $job);
        
        $recruiter = Auth::guard('recruiter')->user();
        
        // Get predefined categories + categories used by this recruiter
        $categories = Category::where('is_active', true)
            ->where(function($query) use ($recruiter) {
                $query->where('sort_order', '<', 999) // Predefined categories
                      ->orWhereIn('name', $recruiter->jobs()->distinct()->pluck('category')); // Recruiter's used categories
            })
            ->orderBy('sort_order')
            ->orderBy('name')
            ->get();
        return view('recruiter.jobs.edit', compact('job', 'categories'));
    }

    /**
     * Update the specified job
     */
    public function update(Request $request, Job $job)
    {
        $this->authorize('update', $job);
        
        $request->validate([
            'title' => 'required|string|max:255',
            'description' => 'required|string',
            'location' => 'required|string|max:255',
            'job_type' => 'required|in:Full-time,Part-time,Contract,Internship',
            'category' => 'required|string|max:255',
            'salary' => 'nullable|numeric|min:0',
            'expiry_date' => 'required|date|after:today',
            'requirements' => 'nullable|string',
            'benefits' => 'nullable|string',
            'experience_required' => 'nullable|string',
            'skills_required' => 'nullable|array',
            'image' => 'nullable|image|mimes:jpeg,png,jpg,gif|max:2048',
        ]);

        // Handle custom category creation
        $categoryName = trim($request->category);
        $isNewCategory = false;
        if (!empty($categoryName)) {
            // Check if category exists, if not create it
            $existingCategory = Category::where('name', $categoryName)->first();
            if (!$existingCategory) {
                Category::create([
                    'name' => $categoryName,
                    'slug' => \Illuminate\Support\Str::slug($categoryName),
                    'description' => 'Custom category created by recruiter',
                    'is_active' => true,
                    'sort_order' => 999, // Put custom categories at the end
                ]);
                $isNewCategory = true;
            }
        }

        $jobData = $request->all();
        
        // Handle skills as JSON
        if ($request->filled('skills_required') && is_array($request->skills_required)) {
            $jobData['skills_required'] = json_encode($request->skills_required);
        } else {
            $jobData['skills_required'] = null;
        }
        
        // Handle image upload
        if ($request->hasFile('image')) {
            // Delete old image
            if ($job->image) {
                Storage::disk('public')->delete($job->image);
            }
            $jobData['image'] = $request->file('image')->store('job_images', 'public');
        }

        $job->update($jobData);

        $successMessage = 'Job updated successfully!';
        if ($isNewCategory) {
            $successMessage .= " New category '{$categoryName}' has been created and will be available for future job postings.";
        }

        return redirect()->route('recruiter.jobs.show', $job)
            ->with('success', $successMessage);
    }

    /**
     * Remove the specified job
     */
    public function destroy(Job $job)
    {
        $this->authorize('delete', $job);
        
        // Delete image if exists
        if ($job->image) {
            Storage::disk('public')->delete($job->image);
        }
        
        $job->delete();

        return redirect()->route('recruiter.jobs.index')
            ->with('success', 'Job deleted successfully!');
    }

    /**
     * Toggle job status (active/inactive)
     */
    public function toggleStatus(Job $job)
    {
        $this->authorize('update', $job);
        
        $job->update(['is_active' => !$job->is_active]);
        
        $status = $job->is_active ? 'activated' : 'deactivated';
        return back()->with('success', "Job {$status} successfully!");
    }
}