<?php

namespace Database\Seeders;

use Illuminate\Database\Console\Seeds\WithoutModelEvents;
use Illuminate\Database\Seeder;

class CategorySeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        $categories = [
            [
                'name' => 'Technology',
                'slug' => 'technology',
                'description' => 'Software development, IT, and tech-related jobs',
                'icon' => 'fas fa-laptop-code',
                'sort_order' => 1,
            ],
            [
                'name' => 'Marketing',
                'slug' => 'marketing',
                'description' => 'Digital marketing, advertising, and promotion roles',
                'icon' => 'fas fa-bullhorn',
                'sort_order' => 2,
            ],
            [
                'name' => 'Sales',
                'slug' => 'sales',
                'description' => 'Sales representatives, account managers, and business development',
                'icon' => 'fas fa-chart-line',
                'sort_order' => 3,
            ],
            [
                'name' => 'Design',
                'slug' => 'design',
                'description' => 'UI/UX design, graphic design, and creative roles',
                'icon' => 'fas fa-palette',
                'sort_order' => 4,
            ],
            [
                'name' => 'Finance',
                'slug' => 'finance',
                'description' => 'Accounting, financial analysis, and banking jobs',
                'icon' => 'fas fa-dollar-sign',
                'sort_order' => 5,
            ],
            [
                'name' => 'Healthcare',
                'slug' => 'healthcare',
                'description' => 'Medical, nursing, and healthcare administration',
                'icon' => 'fas fa-heartbeat',
                'sort_order' => 6,
            ],
            [
                'name' => 'Education',
                'slug' => 'education',
                'description' => 'Teaching, training, and educational roles',
                'icon' => 'fas fa-graduation-cap',
                'sort_order' => 7,
            ],
            [
                'name' => 'Human Resources',
                'slug' => 'human-resources',
                'description' => 'HR management, recruitment, and people operations',
                'icon' => 'fas fa-users',
                'sort_order' => 8,
            ],
            [
                'name' => 'Customer Service',
                'slug' => 'customer-service',
                'description' => 'Customer support, service, and relations',
                'icon' => 'fas fa-headset',
                'sort_order' => 9,
            ],
            [
                'name' => 'Operations',
                'slug' => 'operations',
                'description' => 'Operations management, logistics, and supply chain',
                'icon' => 'fas fa-cogs',
                'sort_order' => 10,
            ],
        ];

        foreach ($categories as $category) {
            \App\Models\Category::create($category);
        }
    }
}
