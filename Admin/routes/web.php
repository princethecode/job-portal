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
    
    // Jobs Management
    Route::prefix('jobs')->group(function () {
        Route::get('/', [AdminJobController::class, 'index'])->name('admin.jobs.index');
        Route::get('/create', [AdminJobController::class, 'create'])->name('admin.jobs.create');
        Route::post('/', [AdminJobController::class, 'store'])->name('admin.jobs.store');
        Route::delete('/bulk-delete', [AdminJobController::class, 'bulkDestroy'])->name('admin.jobs.bulk-destroy');
        
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

