<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Database\Eloquent\Factories\HasFactory;

class Job extends Model
{
    use HasFactory;

    protected $fillable = [
        'title',
        'description',
        'company',
        'location',
        'job_type',
        'category',
        'category_id',
        'salary',
        'posting_date',
        'expiry_date',
        'is_active',
        'image',
        'recruiter_id',
        'requirements',
        'benefits',
        'experience_required',
        'skills_required',
        'views_count',
        'approval_status',
        'approved_by',
        'approved_at',
        'decline_reason'
    ];

    protected $casts = [
        'posting_date' => 'date',
        'expiry_date' => 'date',
        'salary' => 'decimal:2',
        'is_active' => 'boolean',
        'approved_at' => 'datetime',
    ];

    /**
     * Boot method to implement cascade delete
     */
    protected static function boot()
    {
        parent::boot();

        // Delete related applications when a job is deleted
        static::deleting(function (Job $job) {
            $job->applications()->delete();
        });
    }

    public function applications(): HasMany
    {
        return $this->hasMany(Application::class);
    }

    /**
     * Get the recruiter who posted this job
     */
    public function recruiter()
    {
        return $this->belongsTo(Recruiter::class);
    }

    /**
     * Get interviews for this job
     */
    public function interviews()
    {
        return $this->hasMany(Interview::class);
    }

    /**
     * Get the category for this job
     */
    public function category()
    {
        return $this->belongsTo(Category::class);
    }

    /**
     * Get the admin who approved/declined this job
     */
    public function approvedBy()
    {
        return $this->belongsTo(User::class, 'approved_by');
    }

    /**
     * Check if job is approved
     */
    public function isApproved()
    {
        return $this->approval_status === 'approved';
    }

    /**
     * Check if job is pending approval
     */
    public function isPending()
    {
        return $this->approval_status === 'pending';
    }

    /**
     * Check if job is declined
     */
    public function isDeclined()
    {
        return $this->approval_status === 'declined';
    }

    /**
     * Scope to get only approved jobs
     */
    public function scopeApproved($query)
    {
        return $query->where('approval_status', 'approved');
    }

    /**
     * Scope to get only pending jobs
     */
    public function scopePending($query)
    {
        return $query->where('approval_status', 'pending');
    }

    /**
     * Scope to get only declined jobs
     */
    public function scopeDeclined($query)
    {
        return $query->where('approval_status', 'declined');
    }
} 