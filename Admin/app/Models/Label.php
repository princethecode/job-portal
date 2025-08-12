<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsToMany;

class Label extends Model
{
    protected $fillable = ['name', 'color'];

    protected $withCount = ['contacts'];

    public function contacts(): BelongsToMany
    {
        return $this->belongsToMany(Contact::class)
            ->withTimestamps();
    }
} 