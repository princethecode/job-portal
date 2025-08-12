<?php

namespace App\Http\Controllers\API;

use App\Http\Controllers\Controller;

use App\Models\FeaturedJob;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\Storage;

class FeaturedJobController extends Controller
{
    /**
     * Display a listing of the resource.
     *
     * @return \Illuminate\Http\JsonResponse|\Illuminate\View\View
     */
    public function index(Request $request)
    {
        $jobs = FeaturedJob::where('is_active', true)
            ->orderBy('posted_date', 'desc')
            ->get();
            
        return response()->json([
            'success' => true,
            'data' => $jobs
        ], 200);
    }

    /**
     * Store a newly created resource in storage.
     *
     * @param  \Illuminate\Http\Request  $request
     * @return \Illuminate\Http\JsonResponse|\Illuminate\Http\RedirectResponse
     */
    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'company_logo' => 'nullable|image|mimes:jpeg,png,jpg,gif|max:2048',
            'job_title' => 'required|string|max:255',
            'company_name' => 'required|string|max:255',
            'location' => 'required|string|max:255',
            'salary' => 'required|string|max:255',
            'job_type' => 'required|string|max:255',
            'description' => 'required|string',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation error',
                'errors' => $validator->errors()
            ], 422);
        }

        $data = $request->all();

        // Set the posted_date to current date if not provided
        if (!isset($data['posted_date'])) {
            $data['posted_date'] = now()->format('Y-m-d H:i:s');
        }

        if ($request->hasFile('company_logo')) {
            $logo = $request->file('company_logo');
            $logoPath = $logo->store('company_logos', 'public');
            $data['company_logo'] = Storage::url($logoPath);
        }

        $job = FeaturedJob::create($data);
        
        if ($request->wantsJson()) {
            return response()->json([
                'success' => true,
                'message' => 'Featured job created successfully',
                'data' => $job
            ], 201);
        }
        
        return redirect()->route('featured-jobs.index')
            ->with('success', 'Featured job created successfully.');
    }

    /**
     * Public method to get featured jobs without authentication
     *
     * @param  \Illuminate\Http\Request  $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function publicFeaturedJobs(Request $request)
    {
        // Get active featured jobs, ordered by posted date
        $jobs = FeaturedJob::where('is_active', true)
            ->orderBy('posted_date', 'desc')
            ->get();
            
        // Format each job to ensure proper date formatting
        $formattedJobs = $jobs->map(function($job) {
            // Convert dates to more readable format if needed
            if ($job->posted_date) {
                $job->posted_date = date('Y-m-d H:i:s', strtotime($job->posted_date));
            }
            return $job;
        });
        
        return response()->json([
            'success' => true,
            'message' => 'Featured jobs retrieved successfully',
            'data' => $formattedJobs
        ], 200);
    }

    /**
     * Display the specified resource.
     *
     * @param  int  $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function show($id)
    {
        $featuredJob = FeaturedJob::find($id);
        
        if (!$featuredJob) {
            return response()->json([
                'success' => false,
                'message' => 'Featured job not found',
                'data' => null
            ], 404);
        }
        
        return response()->json([
            'success' => true,
            'data' => $featuredJob
        ], 200);
    }

    /**
     * Update the specified resource in storage.
     *
     * @param  \Illuminate\Http\Request  $request
     * @param  \App\Models\FeaturedJob  $featuredJob
     * @return \Illuminate\Http\JsonResponse|\Illuminate\Http\RedirectResponse
     */
    public function update(Request $request, FeaturedJob $featuredJob)
    {
        $validator = Validator::make($request->all(), [
            'company_logo' => 'nullable|image|mimes:jpeg,png,jpg,gif|max:2048',
            'job_title' => 'sometimes|required|string|max:255',
            'company_name' => 'sometimes|required|string|max:255',
            'location' => 'sometimes|required|string|max:255',
            'salary' => 'sometimes|required|string|max:255',
            'job_type' => 'sometimes|required|string|max:255',
            'description' => 'sometimes|required|string',
            'is_active' => 'sometimes|boolean',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation error',
                'errors' => $validator->errors()
            ], 422);
        }

        $data = $request->all();

        if ($request->hasFile('company_logo')) {
            // Delete old logo if exists
            if ($featuredJob->company_logo) {
                $oldLogoPath = str_replace('/storage/', '', $featuredJob->company_logo);
                Storage::disk('public')->delete($oldLogoPath);
            }

            $logo = $request->file('company_logo');
            $logoPath = $logo->store('company_logos', 'public');
            $data['company_logo'] = Storage::url($logoPath);
        }

        $featuredJob->update($data);
        
        if ($request->wantsJson()) {
            return response()->json([
                'success' => true,
                'message' => 'Featured job updated successfully',
                'data' => $featuredJob
            ], 200);
        }
        
        return redirect()->route('featured-jobs.index')
            ->with('success', 'Featured job updated successfully.');
    }

    /**
     * Remove the specified resource from storage.
     *
     * @param  \App\Models\FeaturedJob  $featuredJob
     * @return \Illuminate\Http\JsonResponse|\Illuminate\Http\RedirectResponse
     */
    public function destroy(FeaturedJob $featuredJob)
    {
        // Delete company logo if exists
        if ($featuredJob->company_logo) {
            $logoPath = str_replace('/storage/', '', $featuredJob->company_logo);
            Storage::disk('public')->delete($logoPath);
        }

        $featuredJob->delete();
        
        if (request()->wantsJson()) {
            return response()->json([
                'success' => true,
                'message' => 'Featured job deleted successfully'
            ], 200);
        }
        
        return redirect()->route('featured-jobs.index')
            ->with('success', 'Featured job deleted successfully.');
    }

    // Web View Methods
    public function create()
    {
        return view('featured-jobs.create');
    }

    public function edit(FeaturedJob $featuredJob)
    {
        return view('featured-jobs.edit', compact('featuredJob'));
    }
}
