<?php

namespace App\Http\Controllers\API;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use App\Models\Job;
use Illuminate\Support\Facades\Validator;
use App\Services\FirebaseNotificationService;
use App\Models\User;

class JobController extends Controller
{
    protected $notificationService;

    public function __construct(FirebaseNotificationService $notificationService)
    {
        $this->notificationService = $notificationService;
    }

    /**
     * Display a listing of jobs with optional filters
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function index(Request $request)
    {
        $query = Job::where('is_active', true)
                    ->where('expiry_date', '>=', now())
                    ->where('approval_status', 'approved'); // Only show approved jobs

        // Apply location filter
        if ($request->has('location') && !empty($request->location)) {
            $query->where('location', 'like', '%' . $request->location . '%');
        }

        // Apply job type filter
        if ($request->has('job_type') && !empty($request->job_type)) {
            $query->where('job_type', $request->job_type);
        }

        // Apply category filter
        if ($request->has('category') && !empty($request->category)) {
            $query->where('category', 'like', '%' . $request->category . '%');
        }

        // Apply search query
        if ($request->has('search') && !empty($request->search)) {
            $query->where(function($q) use ($request) {
                $q->where('title', 'like', '%' . $request->search . '%')
                  ->orWhere('description', 'like', '%' . $request->search . '%')
                  ->orWhere('company', 'like', '%' . $request->search . '%');
            });
        }

        // Get all results
        $jobs = $query->orderBy('posting_date', 'desc')->paginate(10);

        return response()->json([
            'success' => true,
            'data' => $jobs
        ]);
    }

    /**
     * Display the specified job
     *
     * @param int $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function show($id)
    {
        $job = Job::where('id', $id)
                  ->where('approval_status', 'approved')
                  ->first();

        if (!$job) {
            return response()->json([
                'success' => false,
                'message' => 'Job not found or not available'
            ], 404);
        }

        return response()->json([
            'success' => true,
            'data' => $job
        ]);
    }

    /**
     * Store a newly created job (Admin only)
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
    'title' => 'required|string|max:255',
    'description' => 'required|string',
    'company' => 'required|string|max:255',
    'location' => 'required|string|max:255',
    'salary' => 'nullable|string|max:255',
    'job_type' => 'required|in:Full-time,Part-time,Contract',
    'category' => 'required|string|max:255',
    'posting_date' => 'required|date',
    'expiry_date' => 'required|date|after:posting_date',
    'image' => 'nullable|image|mimes:jpeg,png,jpg,gif,svg|max:2048',
]);

if ($validator->fails()) {
    return response()->json([
        'success' => false,
        'message' => 'Validation error',
        'errors' => $validator->errors()
    ], 422);
}

$data = $request->all();
if ($request->hasFile('image')) {
    $imagePath = $request->file('image')->store('job_images', 'public');
    $data['image'] = $imagePath;
}

// Set admin-created jobs as approved by default
$data['approval_status'] = 'approved';
$data['approved_by'] = $request->user() ? $request->user()->id : null;
$data['approved_at'] = now();

$job = Job::create($data);

        // Get all users who should be notified about new jobs
        $users = User::whereNotNull('fcm_token')->get();

        // Send notification to all users
        foreach ($users as $user) {
            $this->notificationService->sendNewJobNotification(
                $user->fcm_token,
                $job->title,
                $job->id
            );
        }

        return response()->json([
            'success' => true,
            'message' => 'Job created successfully',
            'data' => $job
        ], 201);
    }

    /**
     * Update the specified job (Admin only)
     *
     * @param Request $request
     * @param int $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function update(Request $request, $id)
    {
        $job = Job::find($id);

        if (!$job) {
            return response()->json([
                'success' => false,
                'message' => 'Job not found'
            ], 404);
        }

        $validator = Validator::make($request->all(), [
            'title' => 'sometimes|required|string|max:255',
            'description' => 'sometimes|required|string',
            'company' => 'sometimes|required|string|max:255',
            'location' => 'sometimes|required|string|max:255',
            'salary' => 'nullable|string|max:255',
            'job_type' => 'sometimes|required|in:Full-time,Part-time,Contract',
            'category' => 'sometimes|required|string|max:255',
            'posting_date' => 'sometimes|required|date',
            'expiry_date' => 'sometimes|required|date|after:posting_date',
            'is_active' => 'sometimes|required|boolean',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation error',
                'errors' => $validator->errors()
            ], 422);
        }

        $job->update($request->all());

        return response()->json([
            'success' => true,
            'message' => 'Job updated successfully',
            'data' => $job
        ]);
    }

    /**
     * Remove the specified job (Admin only)
     *
     * @param int $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function destroy($id)
    {
        $job = Job::find($id);

        if (!$job) {
            return response()->json([
                'success' => false,
                'message' => 'Job not found'
            ], 404);
        }

        $job->delete();

        return response()->json([
            'success' => true,
            'message' => 'Job deleted successfully'
        ]);
    }

    /**
     * Increment the share count for a job
     *
     * @param int $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function incrementShareCount($id)
    {
        $job = Job::find($id);

        if (!$job) {
            return response()->json([
                'success' => false,
                'message' => 'Job not found'
            ], 404);
        }

        // Increment the share count
        $job->increment('share_count');

        return response()->json([
            'success' => true,
            'message' => 'Share count incremented successfully',
            'data' => [
                'job_id' => $job->id,
                'share_count' => $job->share_count
            ]
        ]);
    }
}
