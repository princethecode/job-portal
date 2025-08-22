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
        'share_count',
        'recruiter_id',
        'requirements',
        'benefits',
        'experience_required',
        'skills_required',
        'views_count',
        'experience_level',
        'company_name',
        'company_website',
        'company_description'
    ];

    protected $casts = [
        'posting_date' => 'date',
        'expiry_date' => 'date',
        'salary' => 'decimal:2',
        'is_active' => 'boolean',
        'share_count' => 'integer',
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
} 