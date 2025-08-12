<?php

namespace App\Http\Controllers;

use App\Models\FeaturedJob;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\Storage;

class FeaturedJobWebController extends Controller
{
    public function index()
    {
        $jobs = FeaturedJob::orderBy('posted_date', 'desc')->paginate(10);
        return view('featured-jobs.index', compact('jobs'));
    }

    public function create()
    {
        return view('featured-jobs.create');
    }

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
            return redirect()->back()->withErrors($validator)->withInput();
        }

        $data = $request->all();
        $data['is_active'] = $request->has('is_active');

        if ($request->hasFile('company_logo')) {
            $logo = $request->file('company_logo');
            $logoPath = $logo->store('company_logos', 'public');
            $data['company_logo'] = $logoPath;
        }

        FeaturedJob::create($data);
        return redirect()->route('featured-jobs.index')->with('success', 'Featured job created successfully.');
    }

    public function edit(FeaturedJob $featuredJob)
    {
        return view('featured-jobs.edit', compact('featuredJob'));
    }

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
            return redirect()->back()->withErrors($validator)->withInput();
        }

        $data = $request->all();
        $data['is_active'] = $request->has('is_active');

        if ($request->hasFile('company_logo')) {
            // Delete old logo if exists
            if ($featuredJob->company_logo) {
                $oldLogoPath = $featuredJob->company_logo;
                Storage::disk('public')->delete($oldLogoPath);
            }
            $logo = $request->file('company_logo');
            $logoPath = $logo->store('company_logos', 'public');
            $data['company_logo'] = $logoPath;
        }

        $featuredJob->update($data);
        return redirect()->route('featured-jobs.index')->with('success', 'Featured job updated successfully.');
    }

    public function destroy(FeaturedJob $featuredJob)
    {
        if ($featuredJob->company_logo) {
            $logoPath = $featuredJob->company_logo;
            Storage::disk('public')->delete($logoPath);
        }
        $featuredJob->delete();
        return redirect()->route('featured-jobs.index')->with('success', 'Featured job deleted successfully.');
    }
}
