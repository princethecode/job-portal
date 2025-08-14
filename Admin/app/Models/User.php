<?php

namespace App\Models;

// use Illuminate\Contracts\Auth\MustVerifyEmail;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;
use Laravel\Sanctum\HasApiTokens;

class User extends Authenticatable
{
    use HasApiTokens, HasFactory, Notifiable;

    /**
     * The attributes that are mass assignable.
     *
     * @var array<int, string>
     */
    protected $fillable = [
        'name',
        'email',
        'password',
        'mobile',
        'google_id',
        'provider',
        'resume_path',
        'profile_photo',
        'location',
        'job_title',
        'about_me',
        'skills',
        'experience',
        'is_active',
        'is_admin',
        'current_company',
        'department',
        'current_salary',
        'expected_salary',
        'joining_period',
        'contact',
        'last_contact_sync',
        'fcm_token',
    ];

    /**
     * The attributes that should be hidden for serialization.
     *
     * @var array<int, string>
     */
    protected $hidden = [
        'password',
        'remember_token',
        'google_id', // Hide sensitive Google ID
    ];

    /**
     * The attributes that should be cast.
     *
     * @var array<string, string>
     */
    protected $casts = [
        'email_verified_at' => 'datetime',
        'password' => 'hashed',
        'is_active' => 'boolean',
        'is_admin' => 'boolean',
        'current_salary' => 'decimal:2',
        'expected_salary' => 'decimal:2',
    ];

    /**
     * Boot method to implement cascade delete
     */
    protected static function boot()
    {
        parent::boot();

        // Delete related applications when a user is deleted
        static::deleting(function (User $user) {
            $user->applications()->delete();
        });
    }

    /**
     * Get the applications for the user.
     */
    public function applications()
    {
        return $this->hasMany(Application::class);
    }
    
    /**
     * Get the experiences for the user.
     */
    public function experiences()
    {
        return $this->hasMany(Experience::class);
    }

    /**
     * Check if user is a Google user
     */
    public function isGoogleUser()
    {
        return $this->provider === 'google';
    }

    /**
     * Check if user has password (non-Google user)
     */
    public function hasPassword()
    {
        return !is_null($this->password);
    }
}
