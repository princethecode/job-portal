<?php

namespace App\Http\Controllers;

use App\Models\Job;
use App\Models\Application;
use Illuminate\Http\Request;
use Exception;
use Illuminate\Support\Facades\Validator;
use League\Csv\Reader;
use League\Csv\Writer;

class AdminJobController extends Controller
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
            'image' => 'nullable|image|mimes:jpeg,png,jpg,gif,svg|max:2048',
        ]);

        try {
            $data = $request->all();
            if ($request->hasFile('image')) {
                $imagePath = $request->file('image')->store('job_images', 'public');
                $data['image'] = $imagePath;
            }
            $job = Job::create($data);

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

    /**
     * Remove multiple jobs at once
     *
     * @param Request $request
     * @return \Illuminate\Http\RedirectResponse
     */
    public function bulkDestroy(Request $request)
    {
        $request->validate([
            'job_ids' => 'required|array',
            'job_ids.*' => 'integer|exists:jobs,id'
        ]);

        try {
            $count = Job::whereIn('id', $request->job_ids)->delete();

            return redirect()->route('admin.jobs.index')
                ->with('success', "{$count} jobs deleted successfully");
        } catch (Exception $e) {
            return redirect()->route('admin.jobs.index')
                ->with('error', 'Failed to delete jobs. ' . $e->getMessage());
        }
    }

    /**
     * Show the form for importing jobs via CSV
     *
     * @return \Illuminate\View\View
     */
    public function import()
    {
        return view('jobs.import');
    }

    /**
     * Download a CSV template for job import
     *
     * @return \Symfony\Component\HttpFoundation\BinaryFileResponse
     */
    public function downloadTemplate()
    {
        $headers = [
            'title', 'description', 'company', 'location', 'salary', 'job_type',
            'category', 'posting_date', 'expiry_date', 'is_active'
        ];

        $csvExporter = Writer::createFromString('');
        $csvExporter->insertOne($headers);

        // Add a sample row
        $sampleData = [
            'Software Engineer',
            'We are looking for an experienced software engineer to join our team.',
            'Tech Company',
            'New York, NY',
            '$100,000 - $120,000',
            'Full-time',
            'IT',
            date('Y-m-d'),
            date('Y-m-d', strtotime('+30 days')),
            '1'
        ];
        $csvExporter->insertOne($sampleData);

        $filename = 'job_import_template_' . date('Y-m-d') . '.csv';
        
        return response((string) $csvExporter)
            ->header('Content-Type', 'text/csv')
            ->header('Content-Disposition', "attachment; filename=\"$filename\"");
    }

    /**
     * Process the CSV import
     *
     * @param Request $request
     * @return \Illuminate\Http\RedirectResponse
     */
    public function processImport(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'csv_file' => 'required|file|mimes:csv,txt|max:2048'
        ]);

        if ($validator->fails()) {
            return redirect()->route('admin.jobs.import')
                ->withErrors($validator)
                ->with('error', 'Invalid file format. Please upload a CSV file.');
        }

        try {
            $path = $request->file('csv_file')->getRealPath();
            $csv = Reader::createFromPath($path, 'r');
            $csv->setHeaderOffset(0);

            $records = $csv->getRecords();
            $successCount = 0;
            $errors = [];
            $requiredFields = ['title', 'company', 'location', 'job_type', 'category'];

            foreach ($records as $offset => $record) {
                // Check for required fields
                $missingFields = [];
                foreach ($requiredFields as $field) {
                    if (empty($record[$field])) {
                        $missingFields[] = $field;
                    }
                }

                if (!empty($missingFields)) {
                    $errors[] = "Row " . ($offset + 2) . ": Missing required fields: " . implode(', ', $missingFields);
                    continue;
                }

                // Validate job_type
                if (!in_array($record['job_type'], ['Full-time', 'Part-time', 'Contract'])) {
                    $errors[] = "Row " . ($offset + 2) . ": Invalid job_type. Must be 'Full-time', 'Part-time', or 'Contract'.";
                    continue;
                }

                // Set default values if not provided
                $record['posting_date'] = !empty($record['posting_date']) ? $record['posting_date'] : date('Y-m-d');
                $record['expiry_date'] = !empty($record['expiry_date']) ? $record['expiry_date'] : date('Y-m-d', strtotime('+30 days'));
                $record['is_active'] = isset($record['is_active']) ? (bool)$record['is_active'] : true;
                $record['description'] = !empty($record['description']) ? $record['description'] : 'No description provided.';

                // Create the job
                Job::create([
                    'title' => $record['title'],
                    'description' => $record['description'],
                    'company' => $record['company'],
                    'location' => $record['location'],
                    'salary' => $record['salary'] ?? null,
                    'job_type' => $record['job_type'],
                    'category' => $record['category'],
                    'posting_date' => $record['posting_date'],
                    'expiry_date' => $record['expiry_date'],
                    'is_active' => $record['is_active']
                ]);

                $successCount++;
            }

            $message = $successCount . ' jobs imported successfully.';
            
            if (!empty($errors)) {
                return redirect()->route('admin.jobs.import')
                    ->with('success', $message)
                    ->with('error', 'Some rows had errors: ' . implode('; ', $errors));
            }

            return redirect()->route('admin.jobs.import')
                ->with('success', $message);
                
        } catch (Exception $e) {
            return redirect()->route('admin.jobs.import')
                ->with('error', 'Failed to import jobs: ' . $e->getMessage());
        }
    }
}
