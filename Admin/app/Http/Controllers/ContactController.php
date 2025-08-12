<?php

namespace App\Http\Controllers;

use App\Models\Contact;
use App\Models\Label;
use Illuminate\Http\Request;
use League\Csv\Reader;
use League\Csv\Writer;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Auth;
use App\Exports\ContactsExport;
use Maatwebsite\Excel\Facades\Excel;
use Illuminate\Support\Facades\Log;

class ContactController extends Controller
{
    private $countryCodes = [
        '+91' => 'India',
        '+97' => 'UAE',
        '+96' => 'Iraq',
        '+1' => 'USA/Canada',
        '+44' => 'UK',
        '+61' => 'Australia',
        '+86' => 'China',
        '+81' => 'Japan',
        '+49' => 'Germany',
        '+33' => 'France',
        '+971' => 'syria',
        '+973' => 'Bahrain',
        '+965' => 'Kuwait',
        '+968' => 'Oman',
        '+974' => 'Qatar',
        '+966' => 'Saudi Arabia',
        '+971' => 'United Arab Emirates (UAE)',
        '+972' => 'Israel',
        '+970' => 'Palestine',
        '+975' => 'Bhutan',
        '+976' => 'Mongolia',
        '+977' => 'Nepal',
        '+978' => 'Bhutan',
        '+979' => 'Bhutan',
        '+98' => 'Iran',
        '+992' => 'Tajikistan',
        '+993' => 'Turkmenistan',
        '+994' => 'Azerbaijan',
        '+995' => 'Georgia',
        '+996' => 'Kyrgyzstan',
        '+997' => 'Uzbekistan',
        // Add more country codes as needed
    ];

    private function extractPhoneNumber($text)
    {
        // Remove all spaces first
        $text = preg_replace('/\s+/', '', $text);
        
        // Remove any non-digit characters except +
        $text = preg_replace('/[^0-9+]/', '', $text);
        
        // Check if the text contains a phone number pattern
        if (preg_match('/\+?[0-9]{10,15}/', $text, $matches)) {
            return $matches[0];
        }
        
        return null;
    }

    private function processPhoneNumber($phoneNumber)
    {
        // Remove all spaces and any non-digit characters except +
        $phoneNumber = preg_replace('/\s+/', '', $phoneNumber);
        $phoneNumber = preg_replace('/[^0-9+]/', '', $phoneNumber);
        
        // Extract country code if present
        $countryCode = null;
        foreach ($this->countryCodes as $code => $country) {
            if (strpos($phoneNumber, $code) === 0) {
                $countryCode = $code;
                $phoneNumber = substr($phoneNumber, strlen($code));
                break;
            }
        }

        // Remove any remaining non-digit characters
        $phoneNumber = preg_replace('/[^0-9]/', '', $phoneNumber);

        // If number is longer than 10 digits, take last 10 digits
        if (strlen($phoneNumber) > 10) {
            $phoneNumber = substr($phoneNumber, -10);
        }

        // If number is less than 10 digits, pad with zeros
        if (strlen($phoneNumber) < 10) {
            $phoneNumber = str_pad($phoneNumber, 10, '0', STR_PAD_LEFT);
        }

        return [
            'phone_number' => $phoneNumber,
            'country_code' => $countryCode
        ];
    }

    public function index(Request $request)
    {
        $query = Contact::with('labels')->latest();
        
        // Filter by label if label_id is provided
        if ($request->has('label_id')) {
            $query->whereHas('labels', function($q) use ($request) {
                $q->where('labels.id', $request->label_id);
            });
        }

        // Search by name if search term is provided
        if ($request->has('search') && !empty($request->search)) {
            $searchTerm = $request->search;
            $query->where(function($q) use ($searchTerm) {
                $q->where('name', 'LIKE', "%{$searchTerm}%")
                  ->orWhere('phone_number', 'LIKE', "%{$searchTerm}%")
                  ->orWhere('email', 'LIKE', "%{$searchTerm}%");
            });
        }
        
        // Get per page value from request, default to 25
        $perPage = $request->get('per_page', 25);
        
        // Validate per_page value
        $validPerPageValues = [25, 50, 75, 100, 250];
        if (!in_array($perPage, $validPerPageValues)) {
            $perPage = 25;
        }
        
        $contacts = $query->paginate($perPage)->withQueryString();
        $labels = Label::all();
        $selectedLabel = $request->label_id ? Label::find($request->label_id) : null;
        
        return view('contacts.index', compact('contacts', 'labels', 'selectedLabel'));
    }

    public function storeLabel(Request $request)
    {
        $request->validate([
            'name' => 'required|string|max:255|unique:labels',
            'color' => 'required|string|max:7'
        ]);

        Label::create($request->only(['name', 'color']));

        return redirect()->route('labels.index')
            ->with('success', 'Label created successfully.');
    }

    public function updateContactLabel(Request $request, Contact $contact)
    {
        $request->validate([
            'label_ids' => 'required|array',
            'label_ids.*' => 'exists:labels,id'
        ]);

        // If action is remove and we have a single label_id, remove just that label
        if ($request->has('action') && $request->action === 'remove' && count($request->label_ids) === 1) {
            $contact->labels()->detach($request->label_ids[0]);
            return redirect()->route('contacts.index')
                ->with('success', 'Label removed successfully.');
        }

        // Otherwise, sync all labels (this will remove all existing labels and add the new ones)
        $contact->labels()->sync($request->label_ids);

        return redirect()->route('contacts.index')
            ->with('success', 'Contact labels updated successfully.');
    }

    public function syncLabels()
    {
        try {
            DB::beginTransaction();
            
            $labels = Label::all();
            $updatedCount = 0;
            
            foreach ($labels as $label) {
                // Find contacts where name contains the label name (case insensitive)
                $contacts = Contact::whereRaw('LOWER(name) LIKE ?', ['%' . strtolower($label->name) . '%'])
                    ->whereDoesntHave('labels', function($query) use ($label) {
                        $query->where('labels.id', $label->id);
                    })
                    ->get();
                
                // Add the label to the contacts
                foreach ($contacts as $contact) {
                    $contact->addLabel($label->id);
                    $updatedCount++;
                }
            }
            
            DB::commit();
            
            return redirect()->route('contacts.index')
                ->with('success', "Successfully synced labels. Updated {$updatedCount} contacts.");
                
        } catch (\Exception $e) {
            DB::rollBack();
            return redirect()->route('contacts.index')
                ->with('error', 'Error syncing labels: ' . $e->getMessage());
        }
    }

    public function upload(Request $request)
    {
        $request->validate([
            'csv_file' => 'required|file|mimes:csv,txt'
        ]);

        try {
            $csv = Reader::createFromPath($request->file('csv_file')->getPathname());
            $csv->setHeaderOffset(0);

            $records = $csv->getRecords();
            $processedContacts = [];
            $uniquePhones = [];

            // Generate import tag
            $importTag = date('Y-m-d') . '_' . 
                         str_replace(' ', '_', Auth::user()->name) . '_' .  // Replace spaces with underscores in username
                         pathinfo($request->file('csv_file')->getClientOriginalName(), PATHINFO_FILENAME);  // Get filename without extension

            DB::beginTransaction();

            foreach ($records as $record) {
                // Extract phone number from both PhoneNumber and Name fields
                $phoneNumber = $record['PhoneNumber'] ?? '';
                $name = $record['Name'] ?? '';
                $email = $record['Email'] ?? '';

                // Check if name field contains a phone number
                $phoneInName = $this->extractPhoneNumber($name);
                if ($phoneInName) {
                    $phoneNumber = $phoneInName;
                    // Remove the phone number from the name
                    $name = preg_replace('/\+?[0-9]{10,15}/', '', $name);
                    $name = trim($name);
                }

                // Skip if no phone number found
                if (empty($phoneNumber)) {
                    continue;
                }

                // Process phone number
                $processedPhone = $this->processPhoneNumber($phoneNumber);

                // Skip if phone number already exists
                if (in_array($processedPhone['phone_number'], $uniquePhones)) {
                    continue;
                }

                $uniquePhones[] = $processedPhone['phone_number'];

                // Create contact with import tag
                $contact = Contact::create([
                    'name' => $name,
                    'phone_number' => $processedPhone['phone_number'],
                    'country_code' => $processedPhone['country_code'],
                    'email' => $email,
                    'import_tag' => $importTag
                ]);

                $processedContacts[] = $contact;
            }

            DB::commit();

            // After successful upload, trigger label sync
            $this->syncLabels();

            return redirect()->route('contacts.index')
                ->with('success', 'Contacts uploaded and processed successfully.');

        } catch (\Exception $e) {
            DB::rollBack();
            return redirect()->route('contacts.index')
                ->with('error', 'Error processing CSV file: ' . $e->getMessage());
        }
    }

    public function download()
    {
        $contacts = Contact::with('labels')->get();
        
        $csv = Writer::createFromString('');
        $csv->insertOne(['ID', 'Name', 'Phone Number', 'Country Code', 'Email', 'Labels', 'Import Tag']);

        foreach ($contacts as $contact) {
            $csv->insertOne([
                $contact->id,
                $contact->name,
                $contact->phone_number,
                $contact->country_code,
                $contact->email,
                $contact->labels->pluck('name')->implode(', '),
                $contact->import_tag
            ]);
        }

        $headers = [
            'Content-Type' => 'text/csv',
            'Content-Disposition' => 'attachment; filename="processed_contacts.csv"',
        ];

        return response($csv->toString(), 200, $headers);
    }

    public function labels()
    {
        $labels = Label::withCount('contacts')->get();
        return view('contacts.labels', compact('labels'));
    }

    public function updateLabel(Request $request, Label $label)
    {
        $request->validate([
            'name' => 'required|string|max:255',
            'color' => 'required|string|max:7'
        ]);

        $label->update($request->only(['name', 'color']));

        return redirect()->route('labels.index')
            ->with('success', 'Label updated successfully.');
    }

    public function deleteLabel(Label $label)
    {
        // Check if label has any contacts
        if ($label->contacts_count > 0) {
            return redirect()->route('labels.index')
                ->with('error', 'Cannot delete label that has contacts assigned. Please remove contacts from this label first.');
        }

        $label->delete();

        return redirect()->route('labels.index')
            ->with('success', 'Label deleted successfully.');
    }

    public function export(Request $request)
    {
        try {
            $labelId = $request->label_id;
            $searchTerm = $request->search;
            
            $filename = 'contacts_export_' . date('Y-m-d_His');
            if ($labelId) {
                $label = Label::find($labelId);
                if ($label) {
                    $filename .= '_' . str_replace(' ', '_', strtolower($label->name));
                }
            }
            if ($searchTerm) {
                $filename .= '_search_' . str_replace(' ', '_', strtolower($searchTerm));
            }
            $filename .= '.xlsx';

            Log::info('Starting contacts export', [
                'labelId' => $labelId,
                'searchTerm' => $searchTerm,
                'filename' => $filename
            ]);

            return Excel::download(new ContactsExport(null, $labelId, $searchTerm), $filename);
        } catch (\Exception $e) {
            Log::error('Error exporting contacts', [
                'error' => $e->getMessage(),
                'trace' => $e->getTraceAsString()
            ]);
            
            return redirect()->route('contacts.index')
                ->with('error', 'Error exporting contacts: ' . $e->getMessage());
        }
    }

    public function refreshDatabase()
    {
        try {
            DB::beginTransaction();

            // Get all contacts ordered by created_at to keep the oldest record
            $contacts = Contact::orderBy('created_at')->get();
            $uniquePhones = [];
            $duplicates = [];

            // First pass: identify duplicates and track name lengths
            foreach ($contacts as $contact) {
                $phoneNumber = $contact->phone_number;
                if (isset($uniquePhones[$phoneNumber])) {
                    // Compare name lengths with existing contact
                    $existingContact = Contact::find($uniquePhones[$phoneNumber]);
                    if (strlen(trim($contact->name)) > strlen(trim($existingContact->name))) {
                        // Current contact has longer name, keep this one instead
                        $duplicates[] = $uniquePhones[$phoneNumber];
                        $uniquePhones[$phoneNumber] = $contact->id;
                    } else {
                        // Existing contact has longer or equal name length, keep that one
                        $duplicates[] = $contact->id;
                    }
                } else {
                    $uniquePhones[$phoneNumber] = $contact->id;
                }
            }

            // Second pass: delete duplicates
            if (!empty($duplicates)) {
                // Delete the duplicate contacts
                Contact::whereIn('id', $duplicates)->delete();
                
                DB::commit();
                
                return redirect()->route('contacts.index')
                    ->with('success', count($duplicates) . ' duplicate contacts have been removed. Kept contacts with longer names for each phone number.');
            }

            DB::commit();
            return redirect()->route('contacts.index')
                ->with('info', 'No duplicate contacts found.');

        } catch (\Exception $e) {
            DB::rollBack();
            Log::error('Error refreshing contacts database', [
                'error' => $e->getMessage(),
                'trace' => $e->getTraceAsString()
            ]);
            
            return redirect()->route('contacts.index')
                ->with('error', 'Error refreshing database: ' . $e->getMessage());
        }
    }

    public function bulkDelete(Request $request)
    {
        try {
            $request->validate([
                'contact_ids' => 'required|array',
                'contact_ids.*' => 'exists:contacts,id'
            ]);

            DB::beginTransaction();

            // Delete the selected contacts
            Contact::whereIn('id', $request->contact_ids)->delete();

            DB::commit();

            return redirect()->route('contacts.index')
                ->with('success', count($request->contact_ids) . ' contacts have been deleted successfully.');

        } catch (\Exception $e) {
            DB::rollBack();
            Log::error('Error deleting contacts', [
                'error' => $e->getMessage(),
                'trace' => $e->getTraceAsString()
            ]);
            
            return redirect()->route('contacts.index')
                ->with('error', 'Error deleting contacts: ' . $e->getMessage());
        }
    }
} 