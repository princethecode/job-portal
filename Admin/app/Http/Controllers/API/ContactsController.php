<?php

namespace App\Http\Controllers\API;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Storage;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Str;
use Carbon\Carbon;
use App\Models\User;

class ContactsController extends Controller
{
    /**
     * Upload and store contacts from CSV file
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function upload(Request $request)
    {
        // Log the beginning of the upload process
        \Log::info('ContactsController: Upload method called');
        
        // Log request information for debugging
        \Log::info('Request details:', [
            'has_file' => $request->hasFile('contacts'),
            'content_type' => $request->header('Content-Type'),
            'user_authenticated' => $request->user() ? 'yes' : 'no'
        ]);
        
        // Validate file
        $validator = \Validator::make($request->all(), [
            'contacts' => 'required|file|mimes:csv,txt|max:10240', // Max 10MB
        ]);
        
        if ($validator->fails()) {
            \Log::error('ContactsController: Validation failed', $validator->errors()->toArray());
            return response()->json([
                'success' => false,
                'message' => 'Validation error',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            // Check if user is authenticated
            if (!$request->user()) {
                \Log::error('ContactsController: User not authenticated');
                return response()->json([
                    'success' => false,
                    'message' => 'User not authenticated'
                ], 401);
            }
            
            // Get the authenticated user
            $user = $request->user();
            \Log::info('ContactsController: User authenticated', ['user_id' => $user->id]);

            // Get the file from the request
            $file = $request->file('contacts');
            
            // Log file information
            \Log::info('ContactsController: File received', [
                'original_name' => $file->getClientOriginalName(),
                'size' => $file->getSize(),
                'mime_type' => $file->getMimeType()
            ]);
            
            // Generate a unique filename with timestamp and user ID
            $userId = $user->id;
            $userName = Str::slug($user->name); // Sanitize name for filesystem
            $timestamp = Carbon::now()->format('YmdHis');
            $filename = "contacts_{$timestamp}.csv";
            
            // Create directory structure: contacts/user_{id}_{name}/
            $directory = "contacts/user_{$userId}_{$userName}";
            
            // Make sure the directory exists
            if (!Storage::disk('local')->exists($directory)) {
                Storage::disk('local')->makeDirectory($directory);
                \Log::info('ContactsController: Directory created', ['directory' => $directory]);
            }
            
            // Store the file in the user-specific directory
            $path = Storage::disk('local')->putFileAs(
                $directory, 
                $file, 
                $filename
            );
            
            \Log::info('ContactsController: File saved successfully', ['path' => $path]);
            
            // Optional: Process the CSV file to store contacts in database
            // $this->processContactsFile(Storage::disk('local')->path($path), $user);
            
            return response()->json([
                'success' => true,
                'message' => 'Contacts uploaded successfully',
                'file_path' => $path,
                'user_id' => $userId,
                'user_name' => $user->name
            ]);
        } catch (\Exception $e) {
            \Log::error('ContactsController: Exception during upload', [
                'error' => $e->getMessage(),
                'trace' => $e->getTraceAsString()
            ]);
            
            return response()->json([
                'success' => false,
                'message' => 'Failed to upload contacts: ' . $e->getMessage()
            ], 500);
        }
    }
    
    /**
     * Optional: Process the contacts CSV file to store data in database
     * 
     * @param string $filePath Path to the CSV file
     * @param User $user The user who uploaded the contacts
     * @return void
     */
    private function processContactsFile($filePath, $user)
    {
        // Open the CSV file
        $file = fopen($filePath, 'r');
        
        // Skip header row
        fgetcsv($file);
        
        // Process each row
        while (($row = fgetcsv($file)) !== false) {
            if (count($row) >= 4) {
                list($id, $name, $phone, $email) = $row;
                
                // Store in database logic here
                // Example:
                // Contact::create([
                //     'user_id' => $user->id,
                //     'contact_id' => $id,
                //     'name' => $name,
                //     'phone' => $phone,
                //     'email' => $email,
                // ]);
            }
        }
        
        // Close the file
        fclose($file);
    }
} 