<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\API\AuthController;
use App\Http\Controllers\API\JobController;
use App\Http\Controllers\API\ApplicationController;
use App\Http\Controllers\API\NotificationController;
use App\Http\Controllers\API\UserController;
use App\Http\Controllers\API\ContactsController;
use App\Http\Controllers\API\FCMController;
use App\Http\Controllers\API\ExperienceController;
use App\Http\Controllers\API\FeaturedJobController;
use App\Http\Controllers\API\AppVersionController;

/*
|--------------------------------------------------------------------------
| API Routes    
|--------------------------------------------------------------------------
|
| Here is where you can register API routes for your application. These
| routes are loaded by the RouteServiceProvider and all of them will
| be assigned to the "api" middleware group. Make something great!
|
*/

// App Version Routes (Public)
Route::get('/app-versions/latest', [AppVersionController::class, 'getLatestVersion']);
Route::post('/app-versions/check-update', [AppVersionController::class, 'checkUpdate']);

// App Version Management (Admin routes)
Route::middleware('auth:sanctum')->group(function () {
    Route::get('/app-versions', [AppVersionController::class, 'index']);
    Route::post('/app-versions', [AppVersionController::class, 'store']);
    Route::get('/app-versions/{id}', [AppVersionController::class, 'show']);
    Route::put('/app-versions/{id}', [AppVersionController::class, 'update']);
    Route::delete('/app-versions/{id}', [AppVersionController::class, 'destroy']);
    Route::post('/app-versions/{id}/toggle-status', [AppVersionController::class, 'toggleStatus']);
});

// Authentication
Route::post('/login', [AuthController::class, 'login']);
Route::post('/register', [AuthController::class, 'register']);
Route::post('/logout', [AuthController::class, 'logout']);
Route::post('/forgot-password', [AuthController::class, 'forgotPassword']);
Route::post('/reset-password', [AuthController::class, 'resetPassword']);

// Google Authentication
Route::post('/login/google', [AuthController::class, 'loginWithGoogle']);
Route::post('/register/google', [AuthController::class, 'registerWithGoogle']);

// Jobs
Route::get('/jobs', [JobController::class, 'index']);
Route::get('/jobs/{id}', [JobController::class, 'show']);
Route::post('/jobs', [JobController::class, 'store']);
Route::put('/jobs/{id}', [JobController::class, 'update']);
Route::delete('/jobs/{id}', [JobController::class, 'destroy']);
Route::post('/jobs/{id}/share', [JobController::class, 'incrementShareCount']);

// Admin Job Approval Routes (require admin authentication)
Route::middleware('auth:sanctum')->prefix('admin/jobs')->group(function () {
    Route::get('/pending', [App\Http\Controllers\API\AdminJobApprovalController::class, 'pendingJobs']);
    Route::get('/all-with-status', [App\Http\Controllers\API\AdminJobApprovalController::class, 'allJobsWithStatus']);
    Route::post('/{id}/approve', [App\Http\Controllers\API\AdminJobApprovalController::class, 'approveJob']);
    Route::post('/{id}/decline', [App\Http\Controllers\API\AdminJobApprovalController::class, 'declineJob']);
    Route::get('/approval-stats', [App\Http\Controllers\API\AdminJobApprovalController::class, 'getApprovalStats']);
});

// Public Featured Jobs
Route::get('/featured-jobs', [FeaturedJobController::class, 'index']);
Route::get('/featured-jobs/{id}', [FeaturedJobController::class, 'show']);

// Protected routes
Route::middleware('auth:sanctum')->group(function () {
    // Applications
    Route::get('/applications', [ApplicationController::class, 'index']);
    Route::get('/applications/{id}', [ApplicationController::class, 'show']);
    Route::post('/jobs/{id}/apply', [ApplicationController::class, 'apply']);
    Route::post('/jobs/{id}/apply-with-details', [ApplicationController::class, 'applyWithDetails']);
    Route::get('/user/applied-jobs', [ApplicationController::class, 'userAppliedJobs']);

    // User profile
    Route::get('/user', [UserController::class, 'show']);
    Route::post('/user/update-profile', [UserController::class, 'updateProfile']);
    
    // Experiences
    Route::get('/experiences', [ExperienceController::class, 'index']);
    Route::post('/experiences', [ExperienceController::class, 'store']);
    Route::get('/experiences/{id}', [ExperienceController::class, 'show']);
    Route::put('/experiences/{id}', [ExperienceController::class, 'update']);
    Route::delete('/experiences/{id}', [ExperienceController::class, 'destroy']);
    

    // Protected Featured Jobs Routes (for admin actions)
    Route::post('/featured-jobs', [FeaturedJobController::class, 'store']);
    Route::put('/featured-jobs/{id}', [FeaturedJobController::class, 'update']);
    Route::delete('/featured-jobs/{id}', [FeaturedJobController::class, 'destroy']);


    // User profile
    Route::post('/profile', [UserController::class, 'updateProfile']);
    Route::post('/profile/photo', [UserController::class, 'uploadProfilePhoto']);
    Route::post('/profile/resume', [UserController::class, 'uploadResume']);
    Route::post('/change-password', [UserController::class, 'changePassword']);
    
    // Contacts
    Route::post('/contacts/upload', [ContactsController::class, 'upload']);
    Route::post('/users/update-contact', [UserController::class, 'updateContact'])->middleware('auth:sanctum');
    
    // Notifications
    Route::get('/notifications', [NotificationController::class, 'index']);
    Route::post('/notifications/{id}/read', [NotificationController::class, 'markAsRead']);

    // FCM Token Registration
    Route::post('/users/register-fcm-token', [FCMController::class, 'registerToken']);
});

// Default user route (sanctum)
Route::middleware('auth:sanctum')->get('/user', function (Request $request) {
    return $request->user();
});

// Profile photo access route
Route::get('profile_photos/{filename}', function ($filename) {
    $path = storage_path('app/public/profile_photos/' . $filename);
    if (!file_exists($path)) {
        return response()->json(['error' => 'Image not found'], 404);
    }
    
    $mimeType = mime_content_type($path);
    return response()->file($path, [
        'Content-Type' => $mimeType,
        'Content-Disposition' => 'inline; filename="' . $filename . '"'
    ]);
});

// Resume file access route
Route::get('resumes/{filename}', function ($filename) {
    $path = storage_path('app/public/resumes/' . $filename);
    if (!file_exists($path)) {
        return response()->json(['error' => 'Resume not found'], 404);
    }
    
    $mimeType = mime_content_type($path);
    return response()->file($path, [
        'Content-Type' => $mimeType,
        'Content-Disposition' => 'attachment; filename="' . $filename . '"'
    ]);
});

// ========================================
// RECRUITER API ROUTES
// ========================================

// Recruiter Authentication (Public)
Route::post('/recruiter/login', [App\Http\Controllers\API\RecruiterAuthController::class, 'login']);
Route::post('/recruiter/register', [App\Http\Controllers\API\RecruiterAuthController::class, 'register']);

// Protected Recruiter Routes
Route::middleware('recruiter.sanctum')->group(function () {
    // Recruiter Profile
    Route::get('/recruiter/profile', [App\Http\Controllers\API\RecruiterController::class, 'profile']);
    Route::post('/recruiter/profile', [App\Http\Controllers\API\RecruiterController::class, 'updateProfile']);
    Route::post('/recruiter/logout', [App\Http\Controllers\API\RecruiterAuthController::class, 'logout']);
    
    // Dashboard
    Route::get('/recruiter/dashboard', [App\Http\Controllers\API\RecruiterDashboardController::class, 'index']);
    
    // Jobs Management
    Route::prefix('recruiter/jobs')->group(function () {
        Route::get('/', [App\Http\Controllers\API\RecruiterJobController::class, 'index']);
        Route::post('/', [App\Http\Controllers\API\RecruiterJobController::class, 'store']);
        Route::get('/{id}', [App\Http\Controllers\API\RecruiterJobController::class, 'show']);
        Route::put('/{id}', [App\Http\Controllers\API\RecruiterJobController::class, 'update']);
        Route::delete('/{id}', [App\Http\Controllers\API\RecruiterJobController::class, 'destroy']);
        Route::patch('/{id}/toggle-status', [App\Http\Controllers\API\RecruiterJobController::class, 'toggleStatus']);
    });
    
    // Applications Management
    Route::prefix('recruiter/applications')->group(function () {
        Route::get('/', [App\Http\Controllers\API\RecruiterApplicationController::class, 'index']);
        Route::get('/{id}', [App\Http\Controllers\API\RecruiterApplicationController::class, 'show']);
        Route::patch('/{id}/status', [App\Http\Controllers\API\RecruiterApplicationController::class, 'updateStatus']);
        Route::post('/{id}/schedule-interview', [App\Http\Controllers\API\RecruiterApplicationController::class, 'scheduleInterview']);
        Route::get('/{id}/download-resume', [App\Http\Controllers\API\RecruiterApplicationController::class, 'downloadResume']);
    });
    
    // Candidates Management
    Route::prefix('recruiter/candidates')->group(function () {
        Route::get('/', [App\Http\Controllers\API\RecruiterCandidateController::class, 'index']);
        Route::get('/{id}', [App\Http\Controllers\API\RecruiterCandidateController::class, 'show']);
        Route::patch('/{id}/toggle-save', [App\Http\Controllers\API\RecruiterCandidateController::class, 'toggleSave']);
        Route::get('/{id}/download-resume', [App\Http\Controllers\API\RecruiterCandidateController::class, 'downloadResume']);
    });
    
    // Interviews Management
    Route::prefix('recruiter/interviews')->group(function () {
        Route::get('/', [App\Http\Controllers\API\RecruiterInterviewController::class, 'index']);
        Route::get('/{id}', [App\Http\Controllers\API\RecruiterInterviewController::class, 'show']);
        Route::put('/{id}', [App\Http\Controllers\API\RecruiterInterviewController::class, 'update']);
        Route::patch('/{id}/status', [App\Http\Controllers\API\RecruiterInterviewController::class, 'updateStatus']);
        Route::post('/{id}/cancel', [App\Http\Controllers\API\RecruiterInterviewController::class, 'cancel']);
    });
    
    // Analytics
    Route::get('/recruiter/analytics', [App\Http\Controllers\API\RecruiterAnalyticsController::class, 'index']);
});
