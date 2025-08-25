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
            // Standard job categories as requested
            [
                'name' => 'Delivery',
                'slug' => 'delivery',
                'description' => 'Delivery and courier services',
                'icon' => 'fas fa-truck',
                'sort_order' => 1,
            ],
            [
                'name' => 'Housekeeping',
                'slug' => 'housekeeping',
                'description' => 'Cleaning and housekeeping services',
                'icon' => 'fas fa-broom',
                'sort_order' => 2,
            ],
            [
                'name' => 'Welder',
                'slug' => 'welder',
                'description' => 'Welding and metal fabrication jobs',
                'icon' => 'fas fa-fire',
                'sort_order' => 3,
            ],
            [
                'name' => 'Labor/Helper',
                'slug' => 'labor-helper',
                'description' => 'General labor and helper positions',
                'icon' => 'fas fa-hand-rock',
                'sort_order' => 4,
            ],
            [
                'name' => 'Carpenter',
                'slug' => 'carpenter',
                'description' => 'Carpentry and woodworking jobs',
                'icon' => 'fas fa-hammer',
                'sort_order' => 5,
            ],
            [
                'name' => 'Driver',
                'slug' => 'driver',
                'description' => 'Driving and transportation jobs',
                'icon' => 'fas fa-car',
                'sort_order' => 6,
            ],
            [
                'name' => 'Mason',
                'slug' => 'mason',
                'description' => 'Masonry and stonework jobs',
                'icon' => 'fas fa-building',
                'sort_order' => 7,
            ],
            [
                'name' => 'Electrician',
                'slug' => 'electrician',
                'description' => 'Electrical work and maintenance',
                'icon' => 'fas fa-bolt',
                'sort_order' => 8,
            ],
            [
                'name' => 'Designer',
                'slug' => 'designer',
                'description' => 'Design and creative positions',
                'icon' => 'fas fa-palette',
                'sort_order' => 9,
            ],
            [
                'name' => 'Scaffolding',
                'slug' => 'scaffolding',
                'description' => 'Scaffolding installation and maintenance',
                'icon' => 'fas fa-project-diagram',
                'sort_order' => 10,
            ],
            [
                'name' => 'Technician',
                'slug' => 'technician',
                'description' => 'Technical support and maintenance jobs',
                'icon' => 'fas fa-tools',
                'sort_order' => 11,
            ],
            [
                'name' => 'Warehouse',
                'slug' => 'warehouse',
                'description' => 'Warehouse and storage operations',
                'icon' => 'fas fa-warehouse',
                'sort_order' => 12,
            ],
            [
                'name' => 'Wind blade',
                'slug' => 'wind-blade',
                'description' => 'Wind turbine blade manufacturing and maintenance',
                'icon' => 'fas fa-wind',
                'sort_order' => 13,
            ],
        ];

        foreach ($categories as $category) {
            \App\Models\Category::updateOrCreate(
                ['slug' => $category['slug']], // Match by slug
                $category // Update or create with these values
            );
        }
    }
}
