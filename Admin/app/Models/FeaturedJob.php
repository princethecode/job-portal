<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class FeaturedJob extends Model
{
    use HasFactory;

    protected $fillable = [
        'company_logo',
        'job_title',
        'company_name',
        'location',
        'salary',
        'job_type',
        'description',
        'posted_date',
        'is_active'
    ];

    protected $casts = [
        'posted_date' => 'datetime',
        'is_active' => 'boolean'
    ];
}
