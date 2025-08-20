<?php

namespace App\Http\Controllers;

use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\DB;

class RecruiterCandidateController extends Controller
{
    public function __construct()
    {
        $this->middleware('auth:recruiter');
    }

    /**
     * Search and browse candidates
     */
    public function index(Request $request)
    {
        $recruiter = Auth::guard('recruiter')->user();
        
        // Get candidates who have applied to jobs posted by this recruiter
        $query = User::where('is_active', true)
            ->whereHas('applications.job', function($q) use ($recruiter) {
                $q->where('recruiter_id', $recruiter->id);
            })
            ->with(['experiences', 'applications.job' => function($q) use ($recruiter) {
                $q->where('recruiter_id', $recruiter->id);
            }]);
        
        // Search by name, email, or skills
        if ($request->filled('search')) {
            $query->where(function($q) use ($request) {
                $q->where('name', 'like', '%' . $request->search . '%')
                  ->orWhere('email', 'like', '%' . $request->search . '%')
                  ->orWhere('skills', 'like', '%' . $request->search . '%')
                  ->orWhere('job_title', 'like', '%' . $request->search . '%');
            });
        }
        
        // Filter by location
        if ($request->filled('location')) {
            $query->where('location', 'like', '%' . $request->location . '%');
        }
        
        // Filter by experience
        if ($request->filled('experience')) {
            $query->where('experience', $request->experience);
        }
        
        // Filter by current company
        if ($request->filled('company')) {
            $query->where('current_company', 'like', '%' . $request->company . '%');
        }
        
        // Filter by salary range
        if ($request->filled('min_salary')) {
            $query->where('expected_salary', '>=', $request->min_salary);
        }
        
        if ($request->filled('max_salary')) {
            $query->where('expected_salary', '<=', $request->max_salary);
        }
        
        $candidates = $query->latest()->paginate(12);
        
        // Get saved candidates for this recruiter
        $savedCandidateIds = $recruiter->savedCandidates()->pluck('user_id')->toArray();
        
        return view('recruiter.candidates.index', compact('candidates', 'savedCandidateIds'));
    }

    /**
     * Show candidate profile
     */
    public function show(User $candidate)
    {
        $candidate->load(['experiences', 'applications.job']);
        
        $recruiter = Auth::guard('recruiter')->user();
        $isSaved = $recruiter->savedCandidates()->where('user_id', $candidate->id)->exists();
        
        // Get applications to recruiter's jobs
        $applicationsToMyJobs = $candidate->applications()
            ->whereHas('job', function($q) use ($recruiter) {
                $q->where('recruiter_id', $recruiter->id);
            })
            ->with('job')
            ->get();
        
        return view('recruiter.candidates.show', compact('candidate', 'isSaved', 'applicationsToMyJobs'));
    }

    /**
     * Save/unsave candidate
     */
    public function toggleSave(Request $request, User $candidate)
    {
        $recruiter = Auth::guard('recruiter')->user();
        
        $exists = $recruiter->savedCandidates()->where('user_id', $candidate->id)->exists();
        
        if ($exists) {
            $recruiter->savedCandidates()->detach($candidate->id);
            $message = 'Candidate removed from saved list.';
        } else {
            $recruiter->savedCandidates()->attach($candidate->id, [
                'notes' => $request->notes,
                'created_at' => now(),
                'updated_at' => now(),
            ]);
            $message = 'Candidate saved successfully.';
        }
        
        return back()->with('success', $message);
    }

    /**
     * Show saved candidates
     */
    public function saved(Request $request)
    {
        $recruiter = Auth::guard('recruiter')->user();
        
        $query = $recruiter->savedCandidates()->with(['experiences']);
        
        // Search within saved candidates
        if ($request->filled('search')) {
            $query->where(function($q) use ($request) {
                $q->where('name', 'like', '%' . $request->search . '%')
                  ->orWhere('email', 'like', '%' . $request->search . '%')
                  ->orWhere('skills', 'like', '%' . $request->search . '%');
            });
        }
        
        $savedCandidates = $query->paginate(12);
        
        return view('recruiter.candidates.saved', compact('savedCandidates'));
    }

    /**
     * Update notes for saved candidate
     */
    public function updateNotes(Request $request, User $candidate)
    {
        $request->validate([
            'notes' => 'nullable|string|max:1000',
        ]);
        
        $recruiter = Auth::guard('recruiter')->user();
        
        $recruiter->savedCandidates()->updateExistingPivot($candidate->id, [
            'notes' => $request->notes,
            'updated_at' => now(),
        ]);
        
        return back()->with('success', 'Notes updated successfully.');
    }

    /**
     * Invite candidate to apply for a job
     */
    public function inviteToJob(Request $request, User $candidate)
    {
        $request->validate([
            'job_id' => 'required|exists:jobs,id',
            'message' => 'nullable|string|max:1000',
        ]);
        
        $recruiter = Auth::guard('recruiter')->user();
        
        // Ensure the job belongs to this recruiter
        $job = $recruiter->jobs()->findOrFail($request->job_id);
        
        // Check if candidate already applied
        $existingApplication = $candidate->applications()
            ->where('job_id', $job->id)
            ->exists();
        
        if ($existingApplication) {
            return back()->with('error', 'Candidate has already applied for this job.');
        }
        
        // Send invitation notification (implement notification system)
        // $this->sendJobInvitation($candidate, $job, $request->message);
        
        return back()->with('success', 'Job invitation sent successfully.');
    }

    /**
     * Download candidate resume
     */
    public function downloadResume(User $candidate)
    {
        if (!$candidate->resume_path || !file_exists(storage_path('app/public/' . $candidate->resume_path))) {
            return back()->with('error', 'Resume file not found.');
        }
        
        return response()->download(storage_path('app/public/' . $candidate->resume_path));
    }
}