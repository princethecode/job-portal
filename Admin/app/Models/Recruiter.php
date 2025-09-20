<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;
use Laravel\Sanctum\HasApiTokens;

class Recruiter extends Authenticatable
{
    use HasApiTokens, HasFactory, Notifiable;

    protected $fillable = [
        'name',
        'email',
        'password',
        'mobile',
        'company_name',
        'company_logo',
        'company_website',
        'company_description',
        'company_size',
        'industry',
        'location',
        'designation',
        'company_license',
        'license_uploaded_at',
        'is_active',
        'is_verified',
        'fcm_token',
    ];

    protected $hidden = [
        'password',
        'remember_token',
    ];

    protected $casts = [
        'email_verified_at' => 'datetime',
        'password' => 'hashed',
        'license_uploaded_at' => 'datetime',
        'is_active' => 'boolean',
        'is_verified' => 'boolean',
    ];

    /**
     * Boot method to implement cascade delete
     */
    protected static function boot()
    {
        parent::boot();

        static::deleting(function (Recruiter $recruiter) {
            $recruiter->jobs()->delete();
            $recruiter->interviews()->delete();
        });
    }

    /**
     * Get the jobs posted by this recruiter
     */
    public function jobs()
    {
        return $this->hasMany(Job::class);
    }

    /**
     * Get applications for jobs posted by this recruiter
     */
    public function applications()
    {
        return $this->hasManyThrough(Application::class, Job::class);
    }

    /**
     * Get interviews scheduled by this recruiter
     */
    public function interviews()
    {
        return $this->hasMany(Interview::class);
    }

    /**
     * Get saved candidates by this recruiter
     */
    public function savedCandidates()
    {
        return $this->belongsToMany(User::class, 'saved_candidates', 'recruiter_id', 'user_id')
                    ->withTimestamps();
    }

    /**
     * Get the full URL for the company license
     */
    public function getCompanyLicenseUrlAttribute()
    {
        if ($this->company_license) {
            return asset('storage/' . $this->company_license);
        }
        return null;
    }

    /**
     * Check if recruiter has uploaded a company license
     */
    public function hasCompanyLicense()
    {
        return !empty($this->company_license);
    }
}