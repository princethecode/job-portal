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
        'salary',
        'posting_date',
        'expiry_date',
        'is_active',
        'image',
        'share_count'
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
} 