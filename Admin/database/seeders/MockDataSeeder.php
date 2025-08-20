<?php

namespace Database\Seeders;

use Illuminate\Database\Console\Seeds\WithoutModelEvents;
use Illuminate\Database\Seeder;

class MockDataSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        // Create a test recruiter
        $recruiter = \App\Models\Recruiter::firstOrCreate(
            ['email' => 'recruiter@test.com'],
            [
                'name' => 'John Recruiter',
                'password' => \Illuminate\Support\Facades\Hash::make('password'),
                'mobile' => '+1234567890',
                'company_name' => 'TechCorp Solutions',
                'company_website' => 'https://techcorp.com',
                'company_description' => 'Leading technology solutions provider',
                'company_size' => '51-200',
                'industry' => 'Technology',
                'location' => 'San Francisco, CA',
                'designation' => 'Senior HR Manager',
                'is_active' => true,
                'is_verified' => true,
            ]
        );

        // Create mock candidates/users
        $candidates = [
            [
                'name' => 'Alice Johnson',
                'email' => 'alice@example.com',
                'mobile' => '+1234567891',
                'location' => 'New York, NY',
                'job_title' => 'Senior Software Engineer',
                'current_company' => 'Google',
                'experience' => '5+ years',
                'skills' => 'JavaScript, React, Node.js, Python, AWS',
                'about_me' => 'Passionate full-stack developer with 6 years of experience.',
                'expected_salary' => 120000,
                'current_salary' => 110000,
            ],
            [
                'name' => 'Bob Smith',
                'email' => 'bob@example.com',
                'mobile' => '+1234567892',
                'location' => 'Austin, TX',
                'job_title' => 'Frontend Developer',
                'current_company' => 'Microsoft',
                'experience' => '3-5 years',
                'skills' => 'React, Vue.js, TypeScript, CSS, HTML',
                'about_me' => 'Creative frontend developer focused on user experience.',
                'expected_salary' => 95000,
                'current_salary' => 85000,
            ],
            [
                'name' => 'Carol Davis',
                'email' => 'carol@example.com',
                'mobile' => '+1234567893',
                'location' => 'Seattle, WA',
                'job_title' => 'DevOps Engineer',
                'current_company' => 'Amazon',
                'experience' => '3-5 years',
                'skills' => 'Docker, Kubernetes, AWS, Jenkins, Terraform',
                'about_me' => 'DevOps specialist with expertise in cloud infrastructure.',
                'expected_salary' => 115000,
                'current_salary' => 105000,
            ],
            [
                'name' => 'David Wilson',
                'email' => 'david@example.com',
                'mobile' => '+1234567894',
                'location' => 'Boston, MA',
                'job_title' => 'Data Scientist',
                'current_company' => 'Netflix',
                'experience' => '1-3 years',
                'skills' => 'Python, Machine Learning, SQL, TensorFlow, Pandas',
                'about_me' => 'Data scientist passionate about extracting insights.',
                'expected_salary' => 100000,
                'current_salary' => 90000,
            ],
            [
                'name' => 'Emma Brown',
                'email' => 'emma@example.com',
                'mobile' => '+1234567895',
                'location' => 'Los Angeles, CA',
                'job_title' => 'UX Designer',
                'current_company' => 'Adobe',
                'experience' => '3-5 years',
                'skills' => 'Figma, Sketch, Adobe XD, User Research, Prototyping',
                'about_me' => 'UX designer focused on creating intuitive experiences.',
                'expected_salary' => 90000,
                'current_salary' => 80000,
            ]
        ];

        $users = [];
        foreach ($candidates as $candidateData) {
            $users[] = \App\Models\User::firstOrCreate(
                ['email' => $candidateData['email']],
                array_merge($candidateData, [
                    'password' => \Illuminate\Support\Facades\Hash::make('password'),
                    'is_active' => true,
                ])
            );
        }

        // Create mock jobs
        $jobsData = [
            [
                'title' => 'Senior Full Stack Developer',
                'description' => 'We are looking for an experienced full-stack developer to join our team.',
                'location' => 'San Francisco, CA (Remote)',
                'job_type' => 'Full-time',
                'category' => 'Technology',
                'salary' => 130000,
                'requirements' => 'Bachelor degree in Computer Science. 5+ years experience with JavaScript, React, Node.js.',
                'benefits' => 'Health insurance, 401k matching, flexible PTO, remote work options.',
                'experience_required' => '5+ years',
                'skills_required' => json_encode(['JavaScript', 'React', 'Node.js', 'AWS', 'PostgreSQL']),
                'expiry_date' => \Carbon\Carbon::now()->addDays(30),
            ],
            [
                'title' => 'Frontend React Developer',
                'description' => 'Join our frontend team to build beautiful user interfaces.',
                'location' => 'New York, NY',
                'job_type' => 'Full-time',
                'category' => 'Technology',
                'salary' => 100000,
                'requirements' => '3+ years of React experience. Strong knowledge of JavaScript, HTML, CSS.',
                'benefits' => 'Competitive salary, health benefits, stock options, learning stipend.',
                'experience_required' => '3-5 years',
                'skills_required' => json_encode(['React', 'JavaScript', 'TypeScript', 'CSS', 'Redux']),
                'expiry_date' => \Carbon\Carbon::now()->addDays(25),
            ],
            [
                'title' => 'DevOps Engineer',
                'description' => 'We need a DevOps engineer to help us scale our infrastructure.',
                'location' => 'Austin, TX',
                'job_type' => 'Full-time',
                'category' => 'Technology',
                'salary' => 120000,
                'requirements' => 'Experience with Docker, Kubernetes, CI/CD pipelines. Knowledge of AWS or Azure.',
                'benefits' => 'Excellent benefits package, remote work flexibility, conference attendance.',
                'experience_required' => '3-5 years',
                'skills_required' => json_encode(['Docker', 'Kubernetes', 'AWS', 'Jenkins', 'Python']),
                'expiry_date' => \Carbon\Carbon::now()->addDays(20),
            ]
        ];

        $jobs = [];
        foreach ($jobsData as $jobData) {
            $jobs[] = \App\Models\Job::firstOrCreate(
                ['title' => $jobData['title'], 'recruiter_id' => $recruiter->id],
                array_merge($jobData, [
                    'recruiter_id' => $recruiter->id,
                    'company' => $recruiter->company_name,
                    'posting_date' => \Carbon\Carbon::now()->subDays(rand(1, 10)),
                    'is_active' => true,
                    'views_count' => rand(50, 200),
                ])
            );
        }

        // Create applications
        $statuses = ['Applied', 'Under Review', 'Shortlisted', 'Rejected'];
        
        foreach ($jobs as $job) {
            // Each job gets 2-3 applications
            $numApplications = rand(2, 3);
            $selectedUsers = collect($users)->random($numApplications);
            
            foreach ($selectedUsers as $user) {
                $application = \App\Models\Application::firstOrCreate(
                    ['user_id' => $user->id, 'job_id' => $job->id],
                    [
                        'status' => $statuses[array_rand($statuses)],
                        'cover_letter' => "I am very interested in the {$job->title} position. My experience makes me a great fit.",
                        'resume_path' => 'resumes/sample_resume_' . $user->id . '.pdf',
                        'applied_date' => \Carbon\Carbon::now()->subDays(rand(1, 15)),
                        'notes' => rand(0, 1) ? 'Strong candidate with relevant experience.' : null,
                    ]
                );

                // Create interviews for some shortlisted applications
                if ($application->status === 'Shortlisted' && rand(0, 1)) {
                    \App\Models\Interview::firstOrCreate(
                        ['application_id' => $application->id],
                        [
                            'recruiter_id' => $recruiter->id,
                            'user_id' => $user->id,
                            'job_id' => $job->id,
                            'interview_date' => \Carbon\Carbon::now()->addDays(rand(1, 14)),
                            'interview_time' => \Carbon\Carbon::now()->addDays(rand(1, 14))->setTime(rand(9, 17), [0, 30][rand(0, 1)]),
                            'interview_type' => ['online', 'offline', 'phone'][rand(0, 2)],
                            'meeting_link' => 'https://zoom.us/j/123456789',
                            'location' => $recruiter->company_name . ' Office',
                            'status' => ['scheduled', 'completed'][rand(0, 1)],
                            'notes' => 'Technical interview focusing on problem-solving skills.',
                            'feedback' => rand(0, 1) ? 'Candidate showed strong technical skills.' : null,
                            'rating' => rand(0, 1) ? rand(3, 5) : null,
                        ]
                    );
                }
            }
        }

        // Save some candidates for the recruiter
        $recruiter->savedCandidates()->sync([
            $users[0]->id => ['notes' => 'Excellent React skills, potential team lead.', 'created_at' => now(), 'updated_at' => now()],
            $users[2]->id => ['notes' => 'Strong DevOps background, good for infrastructure.', 'created_at' => now(), 'updated_at' => now()],
        ]);

        $this->command->info('Mock data created successfully!');
        $this->command->info('Recruiter login: recruiter@test.com / password');
        $this->command->info('Created:');
        $this->command->info('- 1 Recruiter');
        $this->command->info('- 5 Candidates');
        $this->command->info('- 3 Jobs');
        $this->command->info('- Multiple Applications');
        $this->command->info('- Multiple Interviews');
    }
}
