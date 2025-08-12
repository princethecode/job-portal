<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsToMany;

class Contact extends Model
{
    use HasFactory;

    protected $fillable = [
        'name',
        'phone_number',
        'country_code',
        'email',
        'import_tag'
    ];

    public function labels(): BelongsToMany
    {
        return $this->belongsToMany(Label::class)
            ->withTimestamps();
    }

    // Helper method to check if contact has a specific label
    public function hasLabel($labelId): bool
    {
        return $this->labels()->where('label_id', $labelId)->exists();
    }

    // Helper method to add a label
    public function addLabel($labelId): void
    {
        if (!$this->hasLabel($labelId)) {
            $this->labels()->attach($labelId);
        }
    }

    // Helper method to remove a label
    public function removeLabel($labelId): void
    {
        $this->labels()->detach($labelId);
    }
} 