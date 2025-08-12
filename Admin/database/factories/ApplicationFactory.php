<?php

namespace Database\Factories;

use App\Models\Application;
use App\Models\User;
use App\Models\Job;
use Illuminate\Database\Eloquent\Factories\Factory;

/**
 * @extends \Illuminate\Database\Eloquent\Factories\Factory<\App\Models\Application>
 */
class ApplicationFactory extends Factory
{
    protected $model = Application::class;

    /**
     * Define the model's default state.
     *
     * @return array<string, mixed>
     */
    public function definition(): array
    {
        $createdAt = $this->faker->dateTimeBetween('-3 months', 'now');
        return [
            'user_id' => User::factory(),
            'job_id' => Job::factory(),
            'status' => $this->faker->randomElement(['Applied', 'Under Review', 'Shortlisted', 'Rejected']),
            'cover_letter' => $this->faker->paragraphs(3, true),
            'resume_path' => null,
            'posting_date' => $createdAt, // Added missing posting_date field
            'applied_date' => $createdAt, // Added applied_date field for consistency
            'created_at' => $createdAt,
            'updated_at' => function (array $attributes) {
                return $this->faker->dateTimeBetween($attributes['created_at'], 'now');
            },
        ];
    }
} 