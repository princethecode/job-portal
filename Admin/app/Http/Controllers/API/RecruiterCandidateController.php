<?php

namespace App\Http\Controllers\API;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Models\SavedCandidate;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Validator;

class RecruiterCandidateController extends Controller
{
    /**
     * Get candidates who applied to recruiter's jobs
     */
    public function index(Request $request)
    {
        $recruiter = $request->user();
        
        $query = User::where('is_active', true)
            ->whereHas('applications.job', function($q) use ($recruiter) {
                $q->where('recruiter_id', $recruiter->id);
            })
            ->with(['experiences', 'applications.job' => function($q) use ($recruiter) {
                $q->where('recruiter_id', $recruiter->id);
            }]);

        // Apply filters
        if ($request->has('filter')) {
            switch ($request->filter) {
                case 'saved':
                    $query->whereHas('savedCandidates', function($q) use ($recruiter) {
                        $q->where('recruiter_id', $recruiter->id);
                    });
                    break;
                case 'recent':
                    $query->whereHas('applications', function($q) use ($recruiter) {
                        $q->whereHas('job', function($jobQ) use ($recruiter) {
                            $jobQ->where('recruiter_id', $recruiter->id);
                        })->where('created_at', '>=', now()->subDays(30));
                    });
                    break;
                case 'experienced':
                    $query->whereHas('experiences', function($q) {
                        $q->where('years_of_experience', '>=', 3);
                    });
                    break;
            }
        }

        if ($request->has('search')) {
            $search = $request->search;
            $query->where(function ($q) use ($search) {
                $q->where('name', 'like', "%{$search}%")
                  ->orWhere('email', 'like', "%{$search}%")
                  ->orWhereHas('experiences', function($expQ) use ($search) {
                      $expQ->where('title', 'like', "%{$search}%")
                           ->orWhere('company', 'like', "%{$search}%");
                  });
            });
        }

        $candidates = $query->orderBy('created_at', 'desc')->paginate(10);

        return response()->json([
            'success' => true,
            'data' => [
                'candidates' => $candidates->items(),
                'pagination' => [
                    'current_page' => $candidates->currentPage(),
                    'last_page' => $candidates->lastPage(),
                    'per_page' => $candidates->perPage(),
                    'total' => $candidates->total(),
                ]
            ]
        ]);
    }

    /**
     * Get specific candidate details
     */
    public function show(Request $request, $id)
    {
        $recruiter = $request->user();
        
        $candidate = User::where('is_active', true)
            ->whereHas('applications.job', function($q) use ($recruiter) {
                $q->where('recruiter_id', $recruiter->id);
            })
            ->with(['experiences', 'applications.job' => function($q) use ($recruiter) {
                $q->where('recruiter_id', $recruiter->id);
            }])
            ->findOrFail($id);

        $isSaved = $candidate->savedCandidates()
            ->where('recruiter_id', $recruiter->id)
            ->exists();

        return response()->json([
            'success' => true,
            'data' => [
                'candidate' => [
                    'id' => $candidate->id,
                    'name' => $candidate->name,
                    'email' => $candidate->email,
                    'mobile' => $candidate->mobile,
                    'profile_photo' => $candidate->profile_photo,
                    'resume' => $candidate->resume,
                    'bio' => $candidate->bio,
                    'is_saved' => $isSaved,
                    'experiences' => $candidate->experiences,
                    'applications' => $candidate->applications->map(function ($application) {
                        return [
                            'id' => $application->id,
                            'job_title' => $application->job->title,
                            'status' => $application->status,
                            'applied_date' => $application->created_at,
                        ];
                    }),
                    'created_at' => $candidate->created_at,
                ]
            ]
        ]);
    }

    /**
     * Toggle save candidate
     */
    public function toggleSave(Request $request, $id)
    {
        try {
            $recruiter = $request->user();
            
            $candidate = User::where('is_active', true)
                ->whereHas('applications.job', function($q) use ($recruiter) {
                    $q->where('recruiter_id', $recruiter->id);
                })
                ->findOrFail($id);

            $savedCandidate = SavedCandidate::where('recruiter_id', $recruiter->id)
                ->where('user_id', $candidate->id)
                ->first();

            if ($savedCandidate) {
                $savedCandidate->delete();
                $isSaved = false;
                $message = 'Candidate removed from saved list';
            } else {
                SavedCandidate::create([
                    'recruiter_id' => $recruiter->id,
                    'user_id' => $candidate->id,
                ]);
                $isSaved = true;
                $message = 'Candidate saved successfully';
            }

            return response()->json([
                'success' => true,
                'message' => $message,
                'data' => [
                    'candidate_id' => $candidate->id,
                    'is_saved' => $isSaved,
                ]
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Operation failed',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Download candidate resume
     */
    public function downloadResume(Request $request, $id)
    {
        try {
            $recruiter = $request->user();
            
            $candidate = User::where('is_active', true)
                ->whereHas('applications.job', function($q) use ($recruiter) {
                    $q->where('recruiter_id', $recruiter->id);
                })
                ->findOrFail($id);

            if (!$candidate->resume) {
                return response()->json([
                    'success' => false,
                    'message' => 'No resume found'
                ], 404);
            }

            $path = storage_path('app/public/resumes/' . $candidate->resume);
            
            if (!file_exists($path)) {
                return response()->json([
                    'success' => false,
                    'message' => 'Resume file not found'
                ], 404);
            }

            return response()->download($path);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Resume download failed',
                'error' => $e->getMessage()
            ], 500);
        }
    }
}
