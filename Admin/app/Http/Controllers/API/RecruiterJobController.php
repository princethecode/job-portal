<?php

namespace App\Http\Controllers\API;

use App\Http\Controllers\Controller;
use App\Models\Job;
use App\Models\Category;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Schema;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Str;

class RecruiterJobController extends Controller
{
    /**
     * Get all jobs for the recruiter
     */
    public function index(Request $request)
    {
        $recruiter = $request->user();
        
        $query = $recruiter->jobs()->with(['applications']);

        // Apply filters
        if ($request->has('status')) {
            switch ($request->status) {
                case 'active':
                    $query->where('is_active', true);
                    break;
                case 'inactive':
                    $query->where('is_active', false);
                    break;
                case 'expired':
                    $query->where('expiry_date', '<', now());
                    break;
            }
        }

        // Add approval status filter
        if ($request->has('approval_status')) {
            $query->where('approval_status', $request->approval_status);
        }

        if ($request->has('search')) {
            $search = $request->search;
            $query->where(function ($q) use ($search) {
                $q->where('title', 'like', "%{$search}%")
                  ->orWhere('description', 'like', "%{$search}%")
                  ->orWhere('location', 'like', "%{$search}%");
            });
        }

        $jobs = $query->orderBy('created_at', 'desc')->paginate(10);

        // Transform jobs data to include category safely
        $transformedJobs = $jobs->getCollection()->map(function ($job) use ($recruiter) {
            return [
                'id' => $job->id,
                'title' => $job->title,
                'description' => $job->description,
                'location' => $job->location,
                'job_type' => $job->job_type,
                'salary' => $job->salary,
                'category' => $job->category ?? 'General', // Use string category or default
                'is_active' => $job->is_active,
                'approval_status' => $job->approval_status,
                'expiry_date' => $job->expiry_date,
                'company_name' => $job->company_name ?? $recruiter->company_name, // Fallback to recruiter's company
                'applications_count' => $job->applications->count(),
                'created_at' => $job->created_at,
                'posted_date' => $job->created_at->diffForHumans(),
            ];
        });

        return response()->json([
            'success' => true,
            'data' => [
                'jobs' => $transformedJobs,
                'pagination' => [
                    'current_page' => $jobs->currentPage(),
                    'last_page' => $jobs->lastPage(),
                    'per_page' => $jobs->perPage(),
                    'total' => $jobs->total(),
                ]
            ]
        ]);
    }

    /**
     * Create a new job
     */
    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'title' => 'required|string|max:255',
            'description' => 'required|string',
            'requirements' => 'required|string',
            'location' => 'required|string|max:255',
            'job_type' => 'required|in:Full-time,Part-time,Contract,Freelance,Internship',
            'salary' => 'sometimes|nullable|string|max:255',
            'experience_level' => 'required|in:Entry,Intermediate,Senior,Executive',
            'category' => 'required|string|max:255',
            'expiry_date' => 'required|date|after:today',
            'benefits' => 'sometimes|nullable|string',
            'skills_required' => 'sometimes|nullable|string',
            'is_active' => 'boolean',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation failed',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            $recruiter = $request->user();

            // Handle category - create category record if it doesn't exist, but store as string in jobs table
            $category = Category::where('name', $request->category)->first();
            if (!$category) {
                $category = Category::create([
                    'name' => $request->category,
                    'slug' => Str::slug($request->category),
                    'description' => 'Custom category created by recruiter',
                    'is_active' => true,
                    'sort_order' => 999,
                ]);
            }

            // Prepare job data with required fields
            $jobData = [
                'recruiter_id' => $recruiter->id,
                'title' => $request->title,
                'description' => $request->description,
                'requirements' => $request->requirements,
                'location' => $request->location,
                'job_type' => $request->job_type,
                'salary' => $request->salary ?? 'Negotiable',
                'experience_required' => $request->experience_level,
                'category' => $request->category, // Store category as string
                'company' => $recruiter->company_name ?? 'Company', // Required field from original schema
                'expiry_date' => $request->expiry_date,
                'benefits' => $request->benefits,
                'skills_required' => $request->skills_required ? explode(',', $request->skills_required) : null,
                'is_active' => $request->is_active ?? true,
                'approval_status' => 'pending', // Set as pending by default for recruiter posts
                'posting_date' => now(), // Set posting date to current time
            ];

            // Add company fields only if they exist (after migration)
            if (Schema::hasColumn('jobs', 'company_name')) {
                $jobData['company_name'] = $recruiter->company_name;
                $jobData['company_website'] = $recruiter->company_website;
                $jobData['company_description'] = $recruiter->company_description;
            }

            $job = Job::create($jobData);

            return response()->json([
                'success' => true,
                'message' => 'Job created successfully and submitted for admin approval',
                'data' => [
                    'job' => [
                        'id' => $job->id,
                        'title' => $job->title,
                        'description' => $job->description,
                        'requirements' => $job->requirements,
                        'location' => $job->location,
                        'job_type' => $job->job_type,
                        'salary' => $job->salary,
                        'experience_level' => $job->experience_required,
                        'category' => $job->category,
                        'expiry_date' => $job->expiry_date,
                        'benefits' => $job->benefits,
                        'skills_required' => is_array($job->skills_required) ? implode(', ', $job->skills_required) : $job->skills_required,
                        'is_active' => $job->is_active,
                        'approval_status' => $job->approval_status,
                        'company_name' => $job->company_name ?? $job->company,
                        'created_at' => $job->created_at,
                        'updated_at' => $job->updated_at,
                    ]
                ]
            ], 201);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Job creation failed',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Get a specific job
     */
    public function show(Request $request, $id)
    {
        $recruiter = $request->user();
        $job = $recruiter->jobs()->with(['applications.user'])->findOrFail($id);

        return response()->json([
            'success' => true,
            'data' => [
                'job' => [
                    'id' => $job->id,
                    'title' => $job->title,
                    'description' => $job->description,
                    'requirements' => $job->requirements,
                    'location' => $job->location,
                    'job_type' => $job->job_type,
                    'salary' => $job->salary,
                    'experience_level' => $job->experience_level,
                    'category' => $job->category ?? 'General',
                    'expiry_date' => $job->expiry_date,
                    'is_active' => $job->is_active,
                    'company_name' => $job->company_name ?? $recruiter->company_name, // Fallback to recruiter's company
                    'company_website' => $job->company_website,
                    'company_description' => $job->company_description,
                    'applications_count' => $job->applications->count(),
                    'applications' => $job->applications->map(function ($application) {
                        return [
                            'id' => $application->id,
                            'user_name' => $application->user->name,
                            'user_email' => $application->user->email,
                            'status' => $application->status,
                            'applied_date' => $application->created_at,
                        ];
                    }),
                    'created_at' => $job->created_at,
                    'updated_at' => $job->updated_at,
                ]
            ]
        ]);
    }

    /**
     * Update a job
     */
    public function update(Request $request, $id)
    {
        $validator = Validator::make($request->all(), [
            'title' => 'sometimes|string|max:255',
            'description' => 'sometimes|string',
            'requirements' => 'sometimes|string',
            'location' => 'sometimes|string|max:255',
            'job_type' => 'sometimes|in:Full-time,Part-time,Contract,Freelance,Internship',
            'salary' => 'sometimes|string|max:255',
            'experience_level' => 'sometimes|in:Entry,Intermediate,Senior,Executive',
            'category' => 'sometimes|string|max:255',
            'expiry_date' => 'sometimes|date|after:today',
            'is_active' => 'sometimes|boolean',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation failed',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            $recruiter = $request->user();
            $job = $recruiter->jobs()->findOrFail($id);

            // Handle category if provided
            $updateData = $request->except('category');
            if ($request->has('category')) {
                $category = Category::where('name', $request->category)->first();
                if (!$category) {
                    $category = Category::create([
                        'name' => $request->category,
                        'slug' => Str::slug($request->category),
                        'description' => 'Custom category created by recruiter',
                        'is_active' => true,
                        'sort_order' => 999,
                    ]);
                }
                $updateData['category'] = $request->category; // Store category as string
            }

            $job->update($updateData);

            // Get category name safely
            $categoryName = 'General'; // Default
            if ($job->category_id && $job->category) {
                $categoryName = $job->category->name;
            } elseif ($job->category) {
                $categoryName = $job->category;
            }

            return response()->json([
                'success' => true,
                'message' => 'Job updated successfully',
                'data' => [
                    'job' => [
                        'id' => $job->id,
                        'title' => $job->title,
                        'description' => $job->description,
                        'requirements' => $job->requirements,
                        'location' => $job->location,
                        'job_type' => $job->job_type,
                        'salary' => $job->salary,
                        'experience_level' => $job->experience_level,
                        'category' => $categoryName,
                        'expiry_date' => $job->expiry_date,
                        'is_active' => $job->is_active,
                        'company_name' => $job->company_name,
                        'updated_at' => $job->updated_at,
                    ]
                ]
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Job update failed',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Delete a job
     */
    public function destroy(Request $request, $id)
    {
        try {
            $recruiter = $request->user();
            $job = $recruiter->jobs()->findOrFail($id);
            $job->delete();

            return response()->json([
                'success' => true,
                'message' => 'Job deleted successfully'
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Job deletion failed',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Toggle job status
     */
    public function toggleStatus(Request $request, $id)
    {
        try {
            $recruiter = $request->user();
            $job = $recruiter->jobs()->findOrFail($id);
            
            $job->update(['is_active' => !$job->is_active]);

            return response()->json([
                'success' => true,
                'message' => 'Job status updated successfully',
                'data' => [
                    'job' => [
                        'id' => $job->id,
                        'title' => $job->title,
                        'is_active' => $job->is_active,
                    ]
                ]
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Status update failed',
                'error' => $e->getMessage()
            ], 500);
        }
    }
}
