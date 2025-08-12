<?php

namespace App\Http\Controllers\API;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use App\Models\Application;
use App\Models\Job;
use App\Models\FeaturedJob;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\Storage;

class ApplicationController extends Controller
{
    /**
     * Apply for a job
     *
     * @param Request $request
     * @param int $jobId
     * @return \Illuminate\Http\JsonResponse
     */
    public function apply(Request $request, $jobId)
    {
        // Check if user is authenticated
        if (!$request->user()) {
            return response()->json([
                'success' => false,
                'message' => 'User not authenticated'
            ], 401);
        }

        // Validate request
        $validator = Validator::make($request->all(), [
            'cover_letter' => 'required|string|max:1000',
            'resume' => 'nullable|file|mimes:pdf,doc,docx|max:2048',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation error',
                'errors' => $validator->errors()
            ], 422);
        }

        // Check if this is a featured job application
        $isFeaturedJob = $request->has('is_featured_job') && $request->is_featured_job === 'true';
        
        if ($isFeaturedJob) {
            // Look for the job in the featured jobs table
            $featuredJob = FeaturedJob::where('id', $jobId)
                      ->where('is_active', true)
                      ->first();
                      
            if (!$featuredJob) {
                return response()->json([
                    'success' => false,
                    'message' => 'Featured job not found or no longer available'
                ], 404);
            }
            
            // Check if we already created a regular job entry for this featured job
            $job = Job::where('title', 'LIKE', "[Featured Job {$featuredJob->id}] %")->first();
            
            if (!$job) {
                // Create a copy of the featured job in the regular jobs table
                $job = new Job();
                $job->title = "[Featured Job {$featuredJob->id}] {$featuredJob->job_title}";
                $job->company = $featuredJob->company_name;
                $job->location = $featuredJob->location;
                $job->description = $featuredJob->description;
                $job->salary = $featuredJob->salary;
                $job->job_type = $featuredJob->job_type;
                $job->posting_date = now(); // Add the required posting_date field
                $job->is_active = true;
                $job->category = 'Featured'; // Set a default category for featured jobs
                $job->expiry_date = now()->addMonths(1); // Set an expiry date for the copied job
                $job->created_at = now();
                $job->updated_at = now();
                $job->save();
            }
            
            // Use the ID of the newly created or existing job
            $jobId = $job->id;
        } else {
            // Check if regular job exists and is active
            $job = Job::where('id', $jobId)
                      ->where('is_active', true)
                      ->where('expiry_date', '>=', now())
                      ->first();

            if (!$job) {
                return response()->json([
                    'success' => false,
                    'message' => 'Job not found or no longer available'
                ], 404);
            }
        }

        // Check if user has already applied
        // We need to be careful with the job ID since featured jobs might have overlapping IDs with regular jobs
        $query = Application::where('user_id', $request->user()->id)
                          ->where('job_id', $jobId);
                          
        // For featured job applications, we'll check the cover_letter field for our marker
        if ($isFeaturedJob) {
            $query->where(function($q) {
                $q->where('cover_letter', 'LIKE', '%[FEATURED JOB APPLICATION]%')
                  ->orWhereNull('cover_letter');
            });
        } else {
            // For regular jobs, exclude any applications marked as featured job applications
            $query->where(function($q) {
                $q->where('cover_letter', 'NOT LIKE', '%[FEATURED JOB APPLICATION]%')
                  ->orWhereNull('cover_letter');
            });
        }
        
        $existingApplication = $query->first();

        if ($existingApplication) {
            return response()->json([
                'success' => false,
                'message' => 'You have already applied for this job'
            ], 422);
        }

        // Handle resume upload
        $resumePath = null;
        if ($request->hasFile('resume')) {
            $resumePath = $request->file('resume')->store('resumes', 'public');
        } else if ($request->user()->resume_path) {
            $resumePath = $request->user()->resume_path;
        }

        // Create application
        $application = Application::create([
            'user_id' => $request->user()->id,
            'job_id' => $jobId,
            'cover_letter' => $request->cover_letter,
            'resume_path' => $resumePath,
            'status' => 'Applied',
            'applied_date' => now(),
            'posting_date' => now(), // Added missing posting_date field
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Job application submitted successfully',
            'data' => $application
        ], 201);
    }

    /**
     * Get user's applications
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function userApplications(Request $request)
    {
        $applications = Application::with(['job' => function($query) {
                $query->select('id', 'title', 'company', 'location');
            }])
            ->where('user_id', $request->user()->id)
            ->orderBy('created_at', 'desc')
            ->paginate(10);

        return response()->json([
            'success' => true,
            'data' => $applications
        ]);
    }

    /**
     * Get all applications (Admin only)
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function index(Request $request)
    {
        $query = Application::with(['user', 'job']);

        // Filter by job if provided
        if ($request->has('job_id') && !empty($request->job_id)) {
            $query->where('job_id', $request->job_id);
        }

        // Filter by status if provided
        if ($request->has('status') && !empty($request->status)) {
            $query->where('status', $request->status);
        }

        $applications = $query->orderBy('created_at', 'desc')
                             ->paginate(10);

        return response()->json([
            'success' => true,
            'data' => $applications
        ]);
    }

    /**
     * Update application status (Admin only)
     *
     * @param Request $request
     * @param int $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function updateStatus(Request $request, $id)
    {
        $application = Application::find($id);

        if (!$application) {
            return response()->json([
                'success' => false,
                'message' => 'Application not found'
            ], 404);
        }

        $validator = Validator::make($request->all(), [
            'status' => 'required|in:Applied,Under Review,Shortlisted,Rejected,Accepted',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation error',
                'errors' => $validator->errors()
            ], 422);
        }

        $application->status = $request->status;
        $application->save();

        // TODO: Send notification to user about status change

        return response()->json([
            'success' => true,
            'message' => 'Application status updated successfully',
            'data' => $application
        ]);
    }

    /**
     * Get application details
     *
     * @param int $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function show($id)
    {
        $application = Application::with(['user', 'job'])->find($id);

        if (!$application) {
            return response()->json([
                'success' => false,
                'message' => 'Application not found'
            ], 404);
        }

        // Check if user has permission to view this application
        if ($application->user_id !== auth()->id() && !auth()->user()->isAdmin()) {
            return response()->json([
                'success' => false,
                'message' => 'Unauthorized'
            ], 403);
        }

        return response()->json([
            'success' => true,
            'data' => $application
        ]);
    }

    /**
     * Get jobs that the user has applied for
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function userAppliedJobs(Request $request)
    {
        // Check if user is authenticated
        if (!$request->user()) {
            return response()->json([
                'success' => false,
                'message' => 'User not authenticated'
            ], 401);
        }

        // Get applications with job details
        $applications = Application::with(['job' => function($query) {
                $query->select('id', 'title', 'company', 'location', 'salary', 'job_type', 'is_active');
            }])
            ->where('user_id', $request->user()->id)
            ->orderBy('created_at', 'desc')
            ->paginate(10);

        // Transform the data to include application status
        $appliedJobs = $applications->map(function($application) {
            return [
                'id' => $application->job->id,
                'title' => $application->job->title,
                'company' => $application->job->company,
                'location' => $application->job->location,
                'salary' => $application->job->salary,
                'job_type' => $application->job->job_type,
                'is_active' => $application->job->is_active,
                'application' => [
                    'id' => $application->id,
                    'status' => $application->status,
                    'applied_date' => $application->applied_date,
                    'cover_letter' => $application->cover_letter,
                    'resume_path' => $application->resume_path
                ]
            ];
        });

        return response()->json([
            'success' => true,
            'data' => [
                'jobs' => $appliedJobs,
                'pagination' => [
                    'total' => $applications->total(),
                    'per_page' => $applications->perPage(),
                    'current_page' => $applications->currentPage(),
                    'last_page' => $applications->lastPage()
                ]
            ]
        ]);
    }

    /**
     * Apply for a job with employment details
     *
     * @param Request $request
     * @param int $jobId
     * @return \Illuminate\Http\JsonResponse
     */
    public function applyWithDetails(Request $request, $jobId)
    {
        // Check if user is authenticated
        if (!$request->user()) {
            return response()->json([
                'success' => false,
                'message' => 'User not authenticated'
            ], 401);
        }

        // Validate request
        $validator = Validator::make($request->all(), [
            'current_company' => 'nullable|string|max:255',
            'department' => 'nullable|string|max:255',
            'current_salary' => 'nullable|string|max:50',
            'expected_salary' => 'nullable|string|max:50',
            'joining_period' => 'nullable|string|max:50',
            'skills' => 'nullable|string|max:500',
            'experience' => 'nullable|string|max:1000',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation error',
                'errors' => $validator->errors()
            ], 422);
        }

        // Check if this is a featured job application
        $isFeaturedJob = $request->has('is_featured_job') && $request->is_featured_job === 'true';
        
        if ($isFeaturedJob) {
            // Look for the job in the featured jobs table
            $featuredJob = FeaturedJob::where('id', $jobId)
                      ->where('is_active', true)
                      ->first();
                      
            if (!$featuredJob) {
                return response()->json([
                    'success' => false,
                    'message' => 'Featured job not found or no longer available'
                ], 404);
            }
            
            // Check if we already created a regular job entry for this featured job
            $job = Job::where('title', 'LIKE', "[Featured Job {$featuredJob->id}] %")->first();
            
            if (!$job) {
                // Create a copy of the featured job in the regular jobs table
                $job = new Job();
                $job->title = "[Featured Job {$featuredJob->id}] {$featuredJob->job_title}";
                $job->company = $featuredJob->company_name;
                $job->location = $featuredJob->location;
                $job->description = $featuredJob->description;
                $job->salary = $featuredJob->salary;
                $job->job_type = $featuredJob->job_type;
                $job->posting_date = now(); // Add the required posting_date field
                $job->is_active = true;
                $job->category = 'Featured'; // Set a default category for featured jobs
                $job->expiry_date = now()->addMonths(1); // Set an expiry date for the copied job
                $job->created_at = now();
                $job->updated_at = now();
                $job->save();
            }
            
            // Use the ID of the newly created or existing job
            $jobId = $job->id;
        } else {
            // Check if regular job exists and is active
            $job = Job::where('id', $jobId)
                      ->where('is_active', true)
                      ->where('expiry_date', '>=', now())
                      ->first();

            if (!$job) {
                return response()->json([
                    'success' => false,
                    'message' => 'Job not found or no longer available'
                ], 404);
            }
        }

        // Check if user has already applied
        // We need to be careful with the job ID since featured jobs might have overlapping IDs with regular jobs
        $query = Application::where('user_id', $request->user()->id)
                          ->where('job_id', $jobId);
                          
        // For featured job applications, we'll check the cover_letter field for our marker
        if ($isFeaturedJob) {
            $query->where(function($q) {
                $q->where('cover_letter', 'LIKE', '%[FEATURED JOB APPLICATION]%')
                  ->orWhereNull('cover_letter');
            });
        } else {
            // For regular jobs, exclude any applications marked as featured job applications
            $query->where(function($q) {
                $q->where('cover_letter', 'NOT LIKE', '%[FEATURED JOB APPLICATION]%')
                  ->orWhereNull('cover_letter');
            });
        }
        
        $existingApplication = $query->first();

        if ($existingApplication) {
            return response()->json([
                'success' => false,
                'message' => 'You have already applied for this job'
            ], 422);
        }

        // Update user employment details
        $user = $request->user();
        
        // Only update if values are provided
        if ($request->has('current_company')) {
            $user->current_company = $request->current_company;
        }
        
        if ($request->has('department')) {
            $user->department = $request->department;
        }
        
        if ($request->has('current_salary')) {
            $user->current_salary = $request->current_salary;
        }
        
        if ($request->has('expected_salary')) {
            $user->expected_salary = $request->expected_salary;
        }
        
        if ($request->has('joining_period')) {
            $user->joining_period = $request->joining_period;
        }
        
        // Update skills and experience if provided
        if ($request->has('skills')) {
            $user->skills = $request->skills;
        }
        
        if ($request->has('experience')) {
            $user->experience = $request->experience;
        }
        
        $user->save();

        // Create a cover letter from employment details
        $coverLetter = "Employment Details:\n";
        $coverLetter .= "Current Company: " . ($request->current_company ?? 'Not specified') . "\n";
        $coverLetter .= "Department: " . ($request->department ?? 'Not specified') . "\n";
        $coverLetter .= "Current Salary: " . ($request->current_salary ?? 'Not specified') . "\n";
        $coverLetter .= "Expected Salary: " . ($request->expected_salary ?? 'Not specified') . "\n";
        $coverLetter .= "Joining Period: " . ($request->joining_period ?? 'Not specified') . "\n\n";
        $coverLetter .= "Skills: " . ($request->skills ?? 'Not specified') . "\n";
        $coverLetter .= "Experience: " . ($request->experience ?? 'Not specified') . "\n";

        // Use existing resume if available
        $resumePath = $user->resume_path;

        // Prepare application data
        $applicationData = [
            'user_id' => $user->id,
            'job_id' => $jobId,
            'cover_letter' => $coverLetter,
            'resume_path' => $resumePath,
            'status' => 'Applied',
            'applied_date' => now(),
            'posting_date' => now(), // Added missing posting_date field
        ];
        
        // For featured job applications, we'll use the existing structure without the notes field
        // We'll store the job title in the cover letter instead since we don't have a notes column
        if ($isFeaturedJob) {
            // Add a marker at the beginning of the cover letter to identify featured job applications
            $applicationData['cover_letter'] = "[FEATURED JOB APPLICATION]\n\n" . $applicationData['cover_letter'];
        }
        
        // Create application
        $application = Application::create($applicationData);

        return response()->json([
            'success' => true,
            'message' => 'Job application with employment details submitted successfully',
            'data' => $application
        ], 201);
    }
}
