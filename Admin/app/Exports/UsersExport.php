<?php

namespace App\Exports;

use App\Models\User;
use Maatwebsite\Excel\Concerns\FromCollection;
use Maatwebsite\Excel\Concerns\WithHeadings;
use Maatwebsite\Excel\Concerns\WithMapping;
use Maatwebsite\Excel\Concerns\WithTitle;
use Maatwebsite\Excel\Concerns\WithStyles;
use Maatwebsite\Excel\Concerns\WithColumnWidths;
use PhpOffice\PhpSpreadsheet\Worksheet\Worksheet;

class UsersExport implements FromCollection, WithHeadings, WithMapping, WithTitle, WithStyles, WithColumnWidths
{
    protected $filters;

    public function __construct($filters = [])
    {
        $this->filters = $filters;
    }

    public function collection()
    {
        $query = User::query();
        
        // Apply filters if provided
        if (!empty($this->filters['status'])) {
            if ($this->filters['status'] === 'active') {
                $query->where('is_active', true);
            } elseif ($this->filters['status'] === 'inactive') {
                $query->where('is_active', false);
            }
        }
        
        if (!empty($this->filters['search'])) {
            $search = $this->filters['search'];
            $query->where(function($q) use ($search) {
                $q->where('name', 'like', "%{$search}%")
                  ->orWhere('email', 'like', "%{$search}%")
                  ->orWhere('mobile', 'like', "%{$search}%")
                  ->orWhere('current_company', 'like', "%{$search}%");
            });
        }
        
        return $query->orderBy('created_at', 'desc')->get();
    }

    public function headings(): array
    {
        return [
            'ID',
            'Name',
            'Email',
            'Mobile',
            'Current Company',
            'Department',
            'Current Salary',
            'Expected Salary',
            'Total Experience',
            'Joining Period',
            'Skills',
            'Education',
            'Location',
            'Status',
            'Registered On',
            'Last Updated',
        ];
    }

    public function map($user): array
    {
        return [
            $user->id,
            $user->name,
            $user->email,
            $user->mobile ?? 'N/A',
            $user->current_company ?? 'N/A',
            $user->department ?? 'N/A',
            $user->current_salary ? '₹' . number_format($user->current_salary, 2) : 'N/A',
            $user->expected_salary ? '₹' . number_format($user->expected_salary, 2) : 'N/A',
            $user->total_experience ?? 'N/A',
            $user->joining_period ?? 'N/A',
            $user->skills ?? 'N/A',
            $user->education ?? 'N/A',
            $user->location ?? 'N/A',
            $user->is_active ? 'Active' : 'Inactive',
            $user->created_at->format('M d, Y H:i:s'),
            $user->updated_at->format('M d, Y H:i:s'),
        ];
    }

    public function title(): string
    {
        $title = 'Users';
        
        if (!empty($this->filters['status'])) {
            $title .= ' - ' . ucfirst($this->filters['status']);
        }
        
        if (!empty($this->filters['search'])) {
            $title .= ' - Search: ' . $this->filters['search'];
        }
        
        return $title;
    }

    public function styles(Worksheet $sheet)
    {
        return [
            // Style the first row (headings)
            1 => [
                'font' => [
                    'bold' => true,
                    'size' => 12,
                    'color' => ['rgb' => 'FFFFFF']
                ],
                'fill' => [
                    'fillType' => \PhpOffice\PhpSpreadsheet\Style\Fill::FILL_SOLID,
                    'startColor' => ['rgb' => '4e73df']
                ],
                'alignment' => [
                    'horizontal' => \PhpOffice\PhpSpreadsheet\Style\Alignment::HORIZONTAL_CENTER,
                    'vertical' => \PhpOffice\PhpSpreadsheet\Style\Alignment::VERTICAL_CENTER,
                ],
            ],
        ];
    }

    public function columnWidths(): array
    {
        return [
            'A' => 8,   // ID
            'B' => 25,  // Name
            'C' => 30,  // Email
            'D' => 15,  // Mobile
            'E' => 25,  // Current Company
            'F' => 20,  // Department
            'G' => 18,  // Current Salary
            'H' => 18,  // Expected Salary
            'I' => 18,  // Total Experience
            'J' => 18,  // Joining Period
            'K' => 30,  // Skills
            'L' => 25,  // Education
            'M' => 20,  // Location
            'N' => 12,  // Status
            'O' => 20,  // Registered On
            'P' => 20,  // Last Updated
        ];
    }
}
