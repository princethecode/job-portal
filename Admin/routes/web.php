<?php

use Illuminate\Support\Facades\Route;
use App\Http\Controllers\AdminAuthController;
use App\Http\Controllers\DashboardController;
use App\Http\Controllers\JobController;
use App\Http\Controllers\ApplicationController;
use App\Http\Controllers\UserController;

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

// Authentication Routes
Route::get('/', [AdminAuthController::class, 'showLoginForm'])->name('admin.login');
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
        Route::get('/', [JobController::class, 'index'])->name('admin.jobs.index');
        Route::get('/create', [JobController::class, 'create'])->name('admin.jobs.create');
        Route::post('/', [JobController::class, 'store'])->name('admin.jobs.store');
        Route::get('/{id}', [JobController::class, 'show'])->name('admin.jobs.show');
        Route::get('/{id}/edit', [JobController::class, 'edit'])->name('admin.jobs.edit');
        Route::put('/{id}', [JobController::class, 'update'])->name('admin.jobs.update');
        Route::delete('/{id}', [JobController::class, 'destroy'])->name('admin.jobs.destroy');
    });
    
    // Applications Management
    Route::prefix('applications')->group(function () {
        Route::get('/', [ApplicationController::class, 'index'])->name('admin.applications.index');
        Route::get('/{id}', [ApplicationController::class, 'show'])->name('admin.applications.show');
        Route::put('/{id}/status', [ApplicationController::class, 'updateStatus'])->name('admin.applications.update-status');
    });
    
    // Users Management
    Route::prefix('users')->group(function () {
        Route::get('/', [UserController::class, 'index'])->name('admin.users.index');
        Route::get('/{id}', [UserController::class, 'show'])->name('admin.users.show');
        Route::put('/{id}/status', [UserController::class, 'updateStatus'])->name('admin.users.update-status');
        Route::delete('/{id}', [UserController::class, 'destroy'])->name('admin.users.destroy');
    });
});
