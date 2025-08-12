<?php

namespace App\Exports;

use App\Models\Application;
use App\Models\Job;
use Maatwebsite\Excel\Concerns\FromCollection;
use Maatwebsite\Excel\Concerns\WithHeadings;
use Maatwebsite\Excel\Concerns\WithMapping;
use Maatwebsite\Excel\Concerns\WithTitle;
use Maatwebsite\Excel\Concerns\WithStyles;
use PhpOffice\PhpSpreadsheet\Worksheet\Worksheet;

class ApplicationsExport implements FromCollection, WithHeadings, WithMapping, WithTitle, WithStyles
{
    protected $filters;
    protected $jobTitle;

    public function __construct($filters = [])
    {
        $this->filters = $filters;
        
        // Get job title if job_id is provided
        if (!empty($filters['job_id'])) {
            $job = Job::find($filters['job_id']);
            $this->jobTitle = $job ? $job->title : null;
        }
    }

    public function collection()
    {
        $query = Application::with(['user', 'job']);
        
        if (!empty($this->filters['job_id'])) {
            $query->where('job_id', $this->filters['job_id']);
        }
        
        if (!empty($this->filters['status'])) {
            $query->where('status', $this->filters['status']);
        }
        
        return $query->orderBy('created_at', 'desc')->get();
    }

    public function headings(): array
    {
        $headings = [
            'ID',
            'Applicant Name',
            'Email',
            'Mobile',
            'Current Company',
            'Current Salary',
            'Expected Salary',
            'Joining Period',
            'Department',
            'Job Title',
            'Applied Date',
            'Status',
        ];

        // Add filter information as a note
        if (!empty($this->filters)) {
            $filterInfo = [];
            if (!empty($this->filters['job_id'])) {
                $filterInfo[] = "Job: " . ($this->jobTitle ?? 'Unknown');
            }
            if (!empty($this->filters['status'])) {
                $filterInfo[] = "Status: " . $this->filters['status'];
            }
            $this->filterInfo = implode(", ", $filterInfo);
        }

        return $headings;
    }

    public function map($application): array
    {
        return [
            $application->id,
            $application->user ? $application->user->name : 'Deleted User',
            $application->user ? $application->user->email : 'N/A',
            $application->user ? $application->user->mobile : 'N/A',
            $application->user ? $application->user->current_company : 'N/A',
            $application->user ? $application->user->current_salary : 'N/A',
            $application->user ? $application->user->expected_salary : 'N/A',
            $application->user ? $application->user->joining_period : 'N/A',
            $application->user ? $application->user->department : 'N/A',
            $application->job ? $application->job->title : 'Deleted Job',
            $application->created_at->format('M d, Y'),
            $application->status,
        ];
    }

    public function title(): string
    {
        $title = 'Applications';
        
        if (!empty($this->filters)) {
            $filterParts = [];
            if (!empty($this->filters['job_id'])) {
                $filterParts[] = $this->jobTitle ?? 'Unknown Job';
            }
            if (!empty($this->filters['status'])) {
                $filterParts[] = $this->filters['status'];
            }
            if (!empty($filterParts)) {
                $title .= ' - ' . implode(' - ', $filterParts);
            }
        }
        
        return $title;
    }

    public function styles(Worksheet $sheet)
    {
        return [
            1 => ['font' => ['bold' => true]],
        ];
    }
} 