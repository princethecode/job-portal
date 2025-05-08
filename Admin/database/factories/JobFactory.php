<?php

namespace Database\Factories;

use App\Models\Job;
use Illuminate\Database\Eloquent\Factories\Factory;

/**
 * @extends \Illuminate\Database\Eloquent\Factories\Factory<\App\Models\Job>
 */
class JobFactory extends Factory
{
    protected $model = Job::class;

    /**
     * Define the model's default state.
     *
     * @return array<string, mixed>
     */
    public function definition(): array
    {
        $jobTypes = ['Full-time', 'Part-time', 'Contract'];
        $categories = ['Technology', 'Marketing', 'Sales', 'Design', 'Engineering', 'Customer Service'];
        
        return [
            'title' => $this->faker->jobTitle,
            'description' => $this->faker->paragraphs(3, true),
            'company' => $this->faker->company,
            'location' => $this->faker->city,
            'job_type' => $this->faker->randomElement($jobTypes),
            'category' => $this->faker->randomElement($categories),
            'salary' => $this->faker->numberBetween(30000, 150000),
            'posting_date' => $this->faker->dateTimeBetween('-2 months', 'now'),
            'expiry_date' => $this->faker->dateTimeBetween('now', '+2 months'),
            'is_active' => $this->faker->boolean(80), // 80% chance of being active
            'created_at' => function (array $attributes) {
                return $attributes['posting_date'];
            },
            'updated_at' => function (array $attributes) {
                return $this->faker->dateTimeBetween($attributes['posting_date'], 'now');
            },
        ];
    }
} 