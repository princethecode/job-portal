<?php

namespace App\Exports;

use App\Models\Contact;
use Maatwebsite\Excel\Concerns\FromQuery;
use Maatwebsite\Excel\Concerns\Exportable;
use Maatwebsite\Excel\Concerns\WithHeadings;
use Maatwebsite\Excel\Concerns\WithMapping;
use Maatwebsite\Excel\Concerns\WithStyles;
use Maatwebsite\Excel\Concerns\ShouldAutoSize;
use PhpOffice\PhpSpreadsheet\Worksheet\Worksheet;
use Illuminate\Support\Facades\Log;

class ContactsExport implements FromQuery, WithHeadings, WithMapping, WithStyles, ShouldAutoSize
{
    use Exportable;

    protected $query;
    protected $labelId;
    protected $searchTerm;

    public function __construct($query = null, $labelId = null, $searchTerm = null)
    {
        $this->query = $query;
        $this->labelId = $labelId;
        $this->searchTerm = $searchTerm;
        
        Log::info('ContactsExport initialized', [
            'labelId' => $labelId,
            'searchTerm' => $searchTerm
        ]);
    }

    public function query()
    {
        try {
            $query = $this->query ?? Contact::query()->with('labels');

            if ($this->labelId) {
                $query->whereHas('labels', function($q) {
                    $q->where('labels.id', $this->labelId);
                });
            }

            if ($this->searchTerm) {
                $query->where(function($q) {
                    $q->where('name', 'LIKE', "%{$this->searchTerm}%")
                      ->orWhere('phone_number', 'LIKE', "%{$this->searchTerm}%")
                      ->orWhere('email', 'LIKE', "%{$this->searchTerm}%");
                });
            }

            Log::info('Export query built', [
                'sql' => $query->toSql(),
                'bindings' => $query->getBindings()
            ]);

            return $query;
        } catch (\Exception $e) {
            Log::error('Error in ContactsExport query', [
                'error' => $e->getMessage(),
                'trace' => $e->getTraceAsString()
            ]);
            throw $e;
        }
    }

    public function headings(): array
    {
        return [
            'ID',
            'Name',
            'Phone Number',
            'Country Code',
            'Email',
            'Labels',
            'Import Tag',
            'Created At',
            'Updated At'
        ];
    }

    public function map($contact): array
    {
        try {
            return [
                $contact->id,
                $contact->name,
                $contact->phone_number,
                $contact->country_code,
                $contact->email,
                $contact->labels->pluck('name')->implode(', '),
                $contact->import_tag,
                $contact->created_at ? $contact->created_at->format('Y-m-d H:i:s') : '',
                $contact->updated_at ? $contact->updated_at->format('Y-m-d H:i:s') : ''
            ];
        } catch (\Exception $e) {
            Log::error('Error mapping contact', [
                'contact_id' => $contact->id ?? 'unknown',
                'error' => $e->getMessage()
            ]);
            return [];
        }
    }

    public function styles(Worksheet $sheet)
    {
        return [
            1 => ['font' => ['bold' => true]],
            'A1:I1' => [
                'fill' => [
                    'fillType' => \PhpOffice\PhpSpreadsheet\Style\Fill::FILL_SOLID,
                    'startColor' => ['rgb' => 'E2EFDA']
                ]
            ]
        ];
    }
}