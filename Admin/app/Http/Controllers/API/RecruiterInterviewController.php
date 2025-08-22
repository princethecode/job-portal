<?php

namespace App\Http\Controllers\API;

use App\Http\Controllers\Controller;
use App\Models\Interview;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Validator;
use Carbon\Carbon;

class RecruiterInterviewController extends Controller
{
    /**
     * Get all interviews for recruiter
     */
    public function index(Request $request)
    {
        $recruiter = $request->user();
        
        $query = $recruiter->interviews()->with(['user', 'job', 'application']);

        // Apply filters
        if ($request->has('status')) {
            switch ($request->status) {
                case 'today':
                    $query->whereDate('interview_date', today());
                    break;
                case 'upcoming':
                    $query->whereDate('interview_date', '>', today());
                    break;
                case 'completed':
                    $query->where('status', 'completed');
                    break;
                case 'cancelled':
                    $query->where('status', 'cancelled');
                    break;
                default:
                    $query->where('status', $request->status);
                    break;
            }
        }

        $interviews = $query->orderBy('interview_date', 'asc')
                           ->orderBy('interview_time', 'asc')
                           ->paginate(10);

        return response()->json([
            'success' => true,
            'data' => [
                'interviews' => $interviews->items(),
                'pagination' => [
                    'current_page' => $interviews->currentPage(),
                    'last_page' => $interviews->lastPage(),
                    'per_page' => $interviews->perPage(),
                    'total' => $interviews->total(),
                ]
            ]
        ]);
    }

    /**
     * Get specific interview details
     */
    public function show(Request $request, $id)
    {
        $recruiter = $request->user();
        $interview = $recruiter->interviews()->with(['user', 'job', 'application'])->findOrFail($id);

        return response()->json([
            'success' => true,
            'data' => [
                'interview' => [
                    'id' => $interview->id,
                    'interview_date' => $interview->interview_date,
                    'interview_time' => $interview->interview_time,
                    'interview_type' => $interview->interview_type,
                    'location' => $interview->location,
                    'notes' => $interview->notes,
                    'status' => $interview->status,
                    'feedback' => $interview->feedback,
                    'rating' => $interview->rating,
                    'user' => [
                        'id' => $interview->user->id,
                        'name' => $interview->user->name,
                        'email' => $interview->user->email,
                        'mobile' => $interview->user->mobile,
                        'profile_photo' => $interview->user->profile_photo,
                    ],
                    'job' => [
                        'id' => $interview->job->id,
                        'title' => $interview->job->title,
                        'company_name' => $interview->job->company_name,
                        'location' => $interview->job->location,
                    ],
                    'application' => [
                        'id' => $interview->application->id,
                        'status' => $interview->application->status,
                        'applied_date' => $interview->application->created_at,
                    ],
                    'created_at' => $interview->created_at,
                    'updated_at' => $interview->updated_at,
                ]
            ]
        ]);
    }

    /**
     * Update interview details
     */
    public function update(Request $request, $id)
    {
        $validator = Validator::make($request->all(), [
            'interview_date' => 'sometimes|date|after:today',
            'interview_time' => 'sometimes|date_format:H:i',
            'interview_type' => 'sometimes|in:Phone,Video,In-person',
            'location' => 'sometimes|nullable|string',
            'notes' => 'sometimes|nullable|string',
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
            $interview = $recruiter->interviews()->findOrFail($id);
            
            $interview->update($request->only([
                'interview_date', 'interview_time', 'interview_type', 
                'location', 'notes'
            ]));

            return response()->json([
                'success' => true,
                'message' => 'Interview updated successfully',
                'data' => [
                    'interview' => [
                        'id' => $interview->id,
                        'interview_date' => $interview->interview_date,
                        'interview_time' => $interview->interview_time,
                        'interview_type' => $interview->interview_type,
                        'location' => $interview->location,
                        'notes' => $interview->notes,
                        'status' => $interview->status,
                    ]
                ]
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Interview update failed',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Update interview status
     */
    public function updateStatus(Request $request, $id)
    {
        $validator = Validator::make($request->all(), [
            'status' => 'required|in:scheduled,completed,cancelled,no-show',
            'feedback' => 'nullable|string',
            'rating' => 'nullable|integer|min:1|max:5',
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
            $interview = $recruiter->interviews()->with('application')->findOrFail($id);
            
            $interview->update([
                'status' => $request->status,
                'feedback' => $request->feedback,
                'rating' => $request->rating,
            ]);

            // Update application status based on interview result
            if ($request->status === 'completed' && $request->rating >= 4) {
                $interview->application->update(['status' => 'Shortlisted']);
            } elseif ($request->status === 'completed' && $request->rating < 3) {
                $interview->application->update(['status' => 'Rejected']);
            }

            return response()->json([
                'success' => true,
                'message' => 'Interview status updated successfully',
                'data' => [
                    'interview' => [
                        'id' => $interview->id,
                        'status' => $interview->status,
                        'feedback' => $interview->feedback,
                        'rating' => $interview->rating,
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

    /**
     * Cancel interview
     */
    public function cancel(Request $request, $id)
    {
        try {
            $recruiter = $request->user();
            $interview = $recruiter->interviews()->with('application')->findOrFail($id);
            
            $interview->update([
                'status' => 'cancelled',
                'notes' => $interview->notes . "\n\nCancelled on: " . now()->format('Y-m-d H:i:s'),
            ]);

            // Update application status
            $interview->application->update(['status' => 'Applied']);

            return response()->json([
                'success' => true,
                'message' => 'Interview cancelled successfully',
                'data' => [
                    'interview' => [
                        'id' => $interview->id,
                        'status' => $interview->status,
                    ]
                ]
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Interview cancellation failed',
                'error' => $e->getMessage()
            ], 500);
        }
    }
}
