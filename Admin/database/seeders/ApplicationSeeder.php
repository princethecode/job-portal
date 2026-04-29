<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;
use App\Models\Application;
use App\Models\User;
use App\Models\Job;
use Carbon\Carbon;

class ApplicationSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        $statuses = ['Applied', 'Under Review', 'Shortlisted', 'Rejected'];
        $users = User::where('is_admin', 0)->get();
        $jobs = Job::all();

        // Generate applications for the last 30 days
        for ($i = 0; $i < 200; $i++) {
            $createdAt = Carbon::now()->subDays(rand(0, 30));
            $status = $statuses[array_rand($statuses)];
            
            Application::create([
                'user_id' => $users->random()->id,
                'job_id' => $jobs->random()->id,
                'status' => $status,
                'created_at' => $createdAt,
                'updated_at' => $createdAt->copy()->addDays(rand(0, 5)),
            ]);
        }
    }
} 