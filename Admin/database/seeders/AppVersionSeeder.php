<?php

namespace Database\Seeders;

use App\Models\AppVersion;
use Illuminate\Database\Seeder;

class AppVersionSeeder extends Seeder
{
    public function run()
    {
        AppVersion::create([
            'platform' => 'android',
            'version_name' => '1.0.1',
            'version_code' => 2,
            'minimum_version_name' => '1.0.0',
            'minimum_version_code' => 1,
            'force_update' => true,
            'update_message' => 'This update includes important security fixes and new features. Please update to continue using the app.',
            'download_url' => 'https://emps.co.in/jobportal.apk',
            'is_active' => true
        ]);

        AppVersion::create([
            'platform' => 'android',
            'version_name' => '1.0.0',
            'version_code' => 1,
            'force_update' => false,
            'update_message' => 'Initial version of the app.',
            'is_active' => false
        ]);
    }
}