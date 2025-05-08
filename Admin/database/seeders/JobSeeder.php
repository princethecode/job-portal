<?php

namespace Database\Seeders;

use App\Models\Job;
use Illuminate\Database\Seeder;
use Carbon\Carbon;

class JobSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        $jobTypes = ['Full-time', 'Part-time', 'Contract', 'Freelance', 'Internship'];
        $categories = ['IT', 'Marketing', 'Sales', 'Finance', 'HR', 'Engineering', 'Design', 'Customer Service'];
        $companies = ['TechCorp', 'MarketGuru', 'SalesForce', 'FinTech', 'HR Solutions', 'EngiTech', 'DesignHub', 'ServicePro'];
        $locations = ['New York', 'San Francisco', 'London', 'Berlin', 'Tokyo', 'Sydney', 'Toronto', 'Singapore'];

        for ($i = 0; $i < 50; $i++) {
            $postingDate = Carbon::now()->subDays(rand(0, 60));
            $expiryDate = $postingDate->copy()->addDays(rand(30, 90));

            Job::create([
                'title' => $this->generateJobTitle(),
                'description' => $this->generateJobDescription(),
                'company' => $companies[array_rand($companies)],
                'location' => $locations[array_rand($locations)],
                'job_type' => $jobTypes[array_rand($jobTypes)],
                'category' => $categories[array_rand($categories)],
                'salary' => rand(30000, 150000),
                'posting_date' => $postingDate,
                'expiry_date' => $expiryDate,
                'is_active' => rand(0, 1),
            ]);
        }
    }

    private function generateJobTitle()
    {
        $titles = [
            'Senior Software Engineer',
            'Marketing Manager',
            'Sales Representative',
            'Financial Analyst',
            'HR Specialist',
            'Product Designer',
            'Customer Support Agent',
            'Data Scientist',
            'Project Manager',
            'Content Writer'
        ];
        return $titles[array_rand($titles)];
    }

    private function generateJobDescription()
    {
        return "We are looking for a talented professional to join our team. The ideal candidate will have experience in the field and a passion for excellence. This position offers great opportunities for growth and development.";
    }
} 