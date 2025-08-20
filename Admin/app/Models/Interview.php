<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Interview extends Model
{
    use HasFactory;

    protected $fillable = [
        'application_id',
        'recruiter_id',
        'user_id',
        'job_id',
        'interview_date',
        'interview_time',
        'interview_type', // online, offline, phone
        'meeting_link',
        'location',
        'status', // scheduled, completed, cancelled, rescheduled
        'notes',
        'feedback',
        'rating',
    ];

    protected $casts = [
        'interview_date' => 'date',
        'interview_time' => 'datetime',
        'rating' => 'integer',
    ];

    /**
     * Get the application for this interview
     */
    public function application()
    {
        return $this->belongsTo(Application::class);
    }

    /**
     * Get the recruiter who scheduled this interview
     */
    public function recruiter()
    {
        return $this->belongsTo(Recruiter::class);
    }

    /**
     * Get the candidate for this interview
     */
    public function user()
    {
        return $this->belongsTo(User::class);
    }

    /**
     * Get the job for this interview
     */
    public function job()
    {
        return $this->belongsTo(Job::class);
    }
}