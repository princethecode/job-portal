<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class AppVersion extends Model
{
    use HasFactory;

    protected $fillable = [
        'platform',
        'version_name',
        'version_code',
        'minimum_version_name',
        'minimum_version_code',
        'force_update',
        'update_message',
        'download_url',
        'is_active'
    ];

    protected $casts = [
        'force_update' => 'boolean',
        'is_active' => 'boolean',
        'version_code' => 'integer',
        'minimum_version_code' => 'integer'
    ];

    public static function getLatestVersion($platform = 'android')
    {
        return self::where('platform', $platform)
                   ->where('is_active', true)
                   ->orderBy('version_code', 'desc')
                   ->first();
    }

    public function isUpdateRequired($currentVersionCode)
    {
        if ($this->force_update) {
            return $currentVersionCode < $this->version_code;
        }
        
        if ($this->minimum_version_code) {
            return $currentVersionCode < $this->minimum_version_code;
        }
        
        return false;
    }
}