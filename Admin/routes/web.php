<?php

use Illuminate\Support\Facades\Route;
use App\Http\Controllers\AdminAuthController;
use App\Http\Controllers\DashboardController;
use App\Http\Controllers\AdminJobController;
use App\Http\Controllers\ApplicationController;
use App\Http\Controllers\UserController;
use App\Http\Controllers\NotificationController;
use App\Http\Controllers\ContactController;
use App\Http\Controllers\FeaturedJobWebController;
use App\Http\Controllers\AppVersionController;

/*
|--------------------------------------------------------------------------
| Web Routes
|--------------------------------------------------------------------------
|
| Here is where you can register web routes for your application. These
| routes are loaded by the RouteServiceProvider and all of them will
| be assigned to the "web" middleware group. Make something great!
|
*/

Route::get('/', function () {
    return view('landing'); // This loads resources/views/landing.blade.php
});


// Public Routes
Route::get('/download', function() {
    return file_get_contents(public_path('download.html'));
})->name('app.download');

Route::get('/privacy-policy', function() {
    return file_get_contents(public_path('privacy-policy.html'));
})->name('app.privacy-policy');

Route::get('/terms-of-service', function() {
    return file_get_contents(public_path('terms-of-service.html'));
})->name('app.terms-of-service');

Route::get('/account-remove', function() {
    return file_get_contents(public_path('account-remove.html'));
})->name('app.account-remove');

// Reset Password Route
Route::get('/reset-password', function() {
    return view('auth.reset-password');
})->name('password.reset.form');

// Handle reset password POST request
Route::post('/reset-password', [App\Http\Controllers\Auth\ResetPasswordController::class, 'reset'])->name('password.update');

// Test-Mail Functionality

Route::get('/test-mail', function () {
    Mail::raw('This is a test email from Laravel.', function ($message) {
        $message->to('g.paresh123@gmail.com')
                ->subject('Test Email');
    });

    return 'Test mail sent!';
});

// Authentication Routes
Route::get('/admin', [AdminAuthController::class, 'showLoginForm'])->name('admin.login');
Route::post('/login', [AdminAuthController::class, 'login'])->name('admin.login.submit');
Route::post('/logout', [AdminAuthController::class, 'logout'])->name('admin.logout');

// Registration Routes
Route::get('/register', [AdminAuthController::class, 'showRegistrationForm'])->name('admin.register');
Route::post('/register', [AdminAuthController::class, 'register'])->name('admin.register.submit');



// Protected Routes (require authentication)
Route::middleware(['web', 'admin.auth'])->group(function () {
    // Dashboard
    Route::get('/dashboard', [DashboardController::class, 'index'])->name('admin.dashboard');
   
   // Change Password Routes (require authentication)

    Route::get('/change-password', [AdminAuthController::class, 'showChangePasswordForm'])->name('admin.change-password.form');
    Route::post('/change-password', [AdminAuthController::class, 'changePassword'])->name('admin.change-password.submit');


    // Jobs Management
    Route::prefix('jobs')->group(function () {
        Route::get('/', [AdminJobController::class, 'index'])->name('admin.jobs.index');
        Route::get('/create', [AdminJobController::class, 'create'])->name('admin.jobs.create');
        Route::post('/', [AdminJobController::class, 'store'])->name('admin.jobs.store');
        Route::delete('/bulk-delete', [AdminJobController::class, 'bulkDestroy'])->name('admin.jobs.bulk-destroy');
        
        // Job Approval Routes
        Route::get('/pending-approval', [AdminJobController::class, 'pendingApproval'])->name('admin.jobs.pending-approval');
        Route::post('/{id}/approve', [AdminJobController::class, 'approve'])->name('admin.jobs.approve');
        Route::post('/{id}/decline', [AdminJobController::class, 'decline'])->name('admin.jobs.decline');
        
        // Bulk import routes
        Route::get('/import', [AdminJobController::class, 'import'])->name('admin.jobs.import');
        Route::get('/download-template', [AdminJobController::class, 'downloadTemplate'])->name('admin.jobs.downloadTemplate');
        Route::post('/process-import', [AdminJobController::class, 'processImport'])->name('admin.jobs.processImport');
        
        Route::get('/{id}', [AdminJobController::class, 'show'])->name('admin.jobs.show');
        Route::get('/{id}/edit', [AdminJobController::class, 'edit'])->name('admin.jobs.edit');
        Route::put('/{id}', [AdminJobController::class, 'update'])->name('admin.jobs.update');
        Route::delete('/{id}', [AdminJobController::class, 'destroy'])->name('admin.jobs.destroy');
    });
    
    // Applications Management
    Route::prefix('applications')->group(function () {
        Route::get('/', [ApplicationController::class, 'index'])->name('admin.applications.index');
        Route::get('/export', [ApplicationController::class, 'export'])->name('admin.applications.export');
        Route::get('/{id}', [ApplicationController::class, 'show'])->name('admin.applications.show');
        Route::put('/{id}/status', [ApplicationController::class, 'updateStatus'])->name('admin.applications.update-status');
        Route::get('/{id}/view-resume', [ApplicationController::class, 'viewResume'])->name('admin.applications.view-resume');
        Route::get('/{id}/download-resume', [ApplicationController::class, 'downloadResume'])->name('admin.applications.download-resume');
    });

    // Resume file handling route - with a specific prefix to avoid conflicts
    Route::get('resume/{filename}', function ($filename) {
        $path = storage_path('app/public/resumes/' . $filename);
        
        if (!file_exists($path)) {
            abort(404, 'Resume not found');
        }
        
        return response()->file($path, [
            'Content-Type' => 'application/pdf',
            'Content-Disposition' => 'inline; filename="' . $filename . '"'
        ]);
    })->name('admin.resume.view');
    
    // Profile photo handling route
    Route::get('profile_photos/{filename}', function ($filename) {
        $path = storage_path('app/public/profile_photos/' . $filename);
        
        if (!file_exists($path)) {
            abort(404, 'Profile photo not found');
        }
        
        $mimeType = mime_content_type($path);
        
        return response()->file($path, [
            'Content-Type' => $mimeType,
            'Content-Disposition' => 'inline; filename="' . $filename . '"'
        ]);
    })->name('admin.profile.photo.view');
    
    // Users Management
    Route::prefix('users')->group(function () {
        Route::get('/', [UserController::class, 'index'])->name('admin.users.index');
        Route::delete('/bulk-delete', [UserController::class, 'bulkDestroy'])->name('admin.users.bulk-destroy');
        Route::get('/{id}', [UserController::class, 'show'])->name('admin.users.show');
        Route::put('/{id}/status', [UserController::class, 'updateStatus'])->name('admin.users.update-status');
        Route::delete('/{id}', [UserController::class, 'destroy'])->name('admin.users.destroy');
    });
    
    // Notifications Management
    Route::prefix('notifications')->group(function () {
        Route::get('/', [NotificationController::class, 'index'])->name('admin.notifications.index');
        Route::get('/create', [NotificationController::class, 'create'])->name('admin.notifications.create');
        Route::post('/', [NotificationController::class, 'store'])->name('admin.notifications.store');
        Route::get('/{id}', [NotificationController::class, 'show'])->name('admin.notifications.show');
        Route::get('/{id}/edit', [NotificationController::class, 'edit'])->name('admin.notifications.edit');
        Route::put('/{id}', [NotificationController::class, 'update'])->name('admin.notifications.update');
        Route::delete('/{id}', [NotificationController::class, 'destroy'])->name('admin.notifications.destroy');
    });

    // Recruiter Management
    Route::prefix('recruiters')->group(function () {
        Route::get('/', [App\Http\Controllers\AdminRecruiterController::class, 'index'])->name('admin.recruiters.index');
        Route::get('/export', [App\Http\Controllers\AdminRecruiterController::class, 'export'])->name('admin.recruiters.export');
        Route::get('/{recruiter}', [App\Http\Controllers\AdminRecruiterController::class, 'show'])->name('admin.recruiters.show');
        Route::put('/{recruiter}/status', [App\Http\Controllers\AdminRecruiterController::class, 'updateStatus'])->name('admin.recruiters.update-status');
        Route::put('/{recruiter}/verification', [App\Http\Controllers\AdminRecruiterController::class, 'updateVerification'])->name('admin.recruiters.update-verification');
        Route::delete('/{recruiter}', [App\Http\Controllers\AdminRecruiterController::class, 'destroy'])->name('admin.recruiters.destroy');
        Route::post('/bulk-action', [App\Http\Controllers\AdminRecruiterController::class, 'bulkAction'])->name('admin.recruiters.bulk-action');
    });

    // Contact Management Routes
    Route::get('/contacts', [ContactController::class, 'index'])->name('contacts.index');
    Route::post('/contacts/upload', [ContactController::class, 'upload'])->name('contacts.upload');
    Route::get('/contacts/download', [ContactController::class, 'download'])->name('contacts.download');
    Route::get('/contacts/export', [ContactController::class, 'export'])->name('contacts.export');
    Route::post('/contacts/labels', [ContactController::class, 'storeLabel'])->name('contacts.store-label');
    Route::post('/contacts/{contact}/label', [ContactController::class, 'updateContactLabel'])->name('contacts.update-label');
    Route::post('/contacts/sync-labels', [ContactController::class, 'syncLabels'])->name('contacts.sync-labels');
    Route::get('/contacts/refresh-database', [ContactController::class, 'refreshDatabase'])->name('contacts.refresh-database');
    Route::post('/contacts/bulk-delete', [ContactController::class, 'bulkDelete'])->name('contacts.bulk-delete');

    // Label Management Routes
    Route::get('/labels', [ContactController::class, 'labels'])->name('labels.index');
    Route::post('/labels', [ContactController::class, 'storeLabel'])->name('labels.store');
    Route::put('/labels/{label}', [ContactController::class, 'updateLabel'])->name('labels.update');
    Route::delete('/labels/{label}', [ContactController::class, 'deleteLabel'])->name('labels.destroy');
    
    // App Version Management Routes
    Route::prefix('app-versions')->group(function () {
        Route::get('/', [AppVersionController::class, 'index'])->name('admin.app-versions.index');
        Route::get('/create', [AppVersionController::class, 'create'])->name('admin.app-versions.create');
        Route::post('/', [AppVersionController::class, 'store'])->name('admin.app-versions.store');
        Route::get('/{id}', [AppVersionController::class, 'show'])->name('admin.app-versions.show');
        Route::get('/{id}/edit', [AppVersionController::class, 'edit'])->name('admin.app-versions.edit');
        Route::put('/{id}', [AppVersionController::class, 'update'])->name('admin.app-versions.update');
        Route::delete('/{id}', [AppVersionController::class, 'destroy'])->name('admin.app-versions.destroy');
        Route::post('/{id}/toggle-status', [AppVersionController::class, 'toggleStatus'])->name('admin.app-versions.toggle-status');
        Route::get('/latest/{platform}', [AppVersionController::class, 'getLatestVersion'])->name('admin.app-versions.latest');
    });
});

// Serve app screenshots
Route::get('/app-screenshot/{number}', function($number) {
    $path = public_path('assets/images/app-screenshot-' . $number . '.png');
    
    if (!file_exists($path)) {
        return response()->json(['error' => 'Screenshot not found'], 404);
    }
    
    return response()->file($path);
})->name('app.screenshot');

// Add APK download route
Route::get('/jobportal.apk', function() {
    $path = public_path('jobportal.apk');
    
    if (!file_exists($path)) {
        return response()->json(['error' => 'APK file not found'], 404);
    }
    
    return response()->download($path, 'jobportal.apk', [
        'Content-Type' => 'application/vnd.android.package-archive',
        'Content-Disposition' => 'attachment; filename="jobportal.apk"'
    ]);
})->name('app.download.apk');

// Test notification route
Route::get('/test-notification', function() {
    $notificationService = app(App\Services\FirebaseNotificationService::class);
    
    // Get a user with FCM token
    $user = \App\Models\User::whereNotNull('fcm_token')->first();
    
    if (!$user) {
        return 'No user found with FCM token';
    }
    
    $result = $notificationService->sendNotification(
        $user->fcm_token,
        'Test Notification',
        'This is a test notification from the server',
        ['type' => 'test']
    );
    
    return $result ? 'Notification sent successfully' : 'Failed to send notification';
})->name('test.notification');

// Featured Jobs Web Routes
Route::get('/featured-jobs', [FeaturedJobWebController::class, 'index'])->name('featured-jobs.index');
Route::get('/featured-jobs/create', [FeaturedJobWebController::class, 'create'])->name('featured-jobs.create');
Route::post('/featured-jobs', [FeaturedJobWebController::class, 'store'])->name('featured-jobs.store');
Route::get('/featured-jobs/{featuredJob}/edit', [FeaturedJobWebController::class, 'edit'])->name('featured-jobs.edit');
Route::put('/featured-jobs/{featuredJob}', [FeaturedJobWebController::class, 'update'])->name('featured-jobs.update');
Route::delete('/featured-jobs/{featuredJob}', [FeaturedJobWebController::class, 'destroy'])->name('featured-jobs.destroy');

// New route for company logos
Route::get('/company_logos/{filename}', function ($filename) {
    $path = storage_path('app/public/company_logos/' . $filename);
    if (!file_exists($path)) {
        abort(404);
    }
    return response()->file($path);
});


// Recruiter Routes
Route::prefix('recruiter')->name('recruiter.')->group(function () {
    // Authentication Routes
    Route::get('/login', [App\Http\Controllers\RecruiterAuthController::class, 'showLoginForm'])->name('login');
    Route::post('/login', [App\Http\Controllers\RecruiterAuthController::class, 'login'])->name('login.submit');
    Route::get('/register', [App\Http\Controllers\RecruiterAuthController::class, 'showRegistrationForm'])->name('register');
    Route::post('/register', [App\Http\Controllers\RecruiterAuthController::class, 'register'])->name('register.submit');
    Route::post('/logout', [App\Http\Controllers\RecruiterAuthController::class, 'logout'])->name('logout');
    
    // Protected Recruiter Routes
    Route::middleware('recruiter.auth')->group(function () {
        // Dashboard
        Route::get('/dashboard', [App\Http\Controllers\RecruiterDashboardController::class, 'index'])->name('dashboard');
        
        // Job Management
        Route::resource('jobs', App\Http\Controllers\RecruiterJobController::class);
        Route::post('/jobs/{job}/toggle-status', [App\Http\Controllers\RecruiterJobController::class, 'toggleStatus'])->name('jobs.toggle-status');
        
        // Application Management
        Route::prefix('applications')->name('applications.')->group(function () {
            Route::get('/', [App\Http\Controllers\RecruiterApplicationController::class, 'index'])->name('index');
            Route::get('/{application}', [App\Http\Controllers\RecruiterApplicationController::class, 'show'])->name('show');
            Route::put('/{application}/status', [App\Http\Controllers\RecruiterApplicationController::class, 'updateStatus'])->name('update-status');
            Route::get('/{application}/download-resume', [App\Http\Controllers\RecruiterApplicationController::class, 'downloadResume'])->name('download-resume');
            Route::post('/{application}/schedule-interview', [App\Http\Controllers\RecruiterApplicationController::class, 'scheduleInterview'])->name('schedule-interview');
            Route::post('/bulk-update-status', [App\Http\Controllers\RecruiterApplicationController::class, 'bulkUpdateStatus'])->name('bulk-update-status');
        });
        
        // Interview Management
        Route::prefix('interviews')->name('interviews.')->group(function () {
            Route::get('/', [App\Http\Controllers\RecruiterInterviewController::class, 'index'])->name('index');
            Route::get('/calendar', [App\Http\Controllers\RecruiterInterviewController::class, 'calendar'])->name('calendar');
            Route::get('/calendar-simple', function() {
                $recruiter = Auth::guard('recruiter')->user();
                $interviews = $recruiter->interviews()
                    ->with(['user', 'job'])
                    ->whereMonth('interview_date', now()->month)
                    ->whereYear('interview_date', now()->year)
                    ->orderBy('interview_date')
                    ->get();
                return view('recruiter.interviews.calendar-simple', compact('interviews'));
            })->name('calendar-simple');
            Route::get('/{interview}', [App\Http\Controllers\RecruiterInterviewController::class, 'show'])->name('show');
            Route::put('/{interview}', [App\Http\Controllers\RecruiterInterviewController::class, 'update'])->name('update');
            Route::put('/{interview}/status', [App\Http\Controllers\RecruiterInterviewController::class, 'updateStatus'])->name('update-status');
            Route::post('/{interview}/cancel', [App\Http\Controllers\RecruiterInterviewController::class, 'cancel'])->name('cancel');
        });
        
        // Candidate Management
        Route::prefix('candidates')->name('candidates.')->group(function () {
            Route::get('/', [App\Http\Controllers\RecruiterCandidateController::class, 'index'])->name('index');
            Route::get('/saved', [App\Http\Controllers\RecruiterCandidateController::class, 'saved'])->name('saved');
            Route::get('/{candidate}', [App\Http\Controllers\RecruiterCandidateController::class, 'show'])->name('show');
            Route::post('/{candidate}/toggle-save', [App\Http\Controllers\RecruiterCandidateController::class, 'toggleSave'])->name('toggle-save');
            Route::put('/{candidate}/notes', [App\Http\Controllers\RecruiterCandidateController::class, 'updateNotes'])->name('update-notes');
            Route::post('/{candidate}/invite-to-job', [App\Http\Controllers\RecruiterCandidateController::class, 'inviteToJob'])->name('invite-to-job');
            Route::get('/{candidate}/download-resume', [App\Http\Controllers\RecruiterCandidateController::class, 'downloadResume'])->name('download-resume');
        });
        
        // Analytics Routes
        Route::prefix('analytics')->name('analytics.')->group(function () {
            Route::get('/', [App\Http\Controllers\RecruiterAnalyticsController::class, 'index'])->name('index');
            Route::get('/export', [App\Http\Controllers\RecruiterAnalyticsController::class, 'export'])->name('export');
        });
        
        // API Routes for AJAX calls
        Route::prefix('api')->name('api.')->group(function () {
            Route::get('/jobs/active', [App\Http\Controllers\API\RecruiterJobAPIController::class, 'getActiveJobs'])->name('jobs.active');
            Route::get('/jobs/stats', [App\Http\Controllers\API\RecruiterJobAPIController::class, 'getJobStats'])->name('jobs.stats');
        });
    });
});