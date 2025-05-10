<?php

namespace Tests\Feature;

use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Foundation\Testing\WithFaker;
use Tests\TestCase;
use App\Models\User;
use Laravel\Sanctum\Sanctum;
use Illuminate\Support\Facades\Hash;

class ChangePasswordTest extends TestCase
{
    use RefreshDatabase;

    /** @test */
    public function user_can_change_password_with_correct_credentials()
    {
        // Create a user with a known password
        $user = User::factory()->create([
            'password' => Hash::make('password123'),
        ]);

        // Authenticate the user
        Sanctum::actingAs($user);

        // Attempt to change password
        $response = $this->postJson('/api/change-password', [
            'current_password' => 'password123',
            'new_password' => 'newpassword123',
            'new_password_confirmation' => 'newpassword123',
        ]);

        // Assert response
        $response->assertStatus(200)
                ->assertJson([
                    'success' => true,
                    'message' => 'Password changed successfully'
                ]);

        // Verify the password was actually changed
        $this->assertTrue(Hash::check('newpassword123', $user->fresh()->password));
    }

    /** @test */
    public function user_cannot_change_password_with_incorrect_current_password()
    {
        // Create a user with a known password
        $user = User::factory()->create([
            'password' => Hash::make('password123'),
        ]);

        // Authenticate the user
        Sanctum::actingAs($user);

        // Attempt to change password with wrong current password
        $response = $this->postJson('/api/change-password', [
            'current_password' => 'wrongpassword',
            'new_password' => 'newpassword123',
            'new_password_confirmation' => 'newpassword123',
        ]);

        // Assert response
        $response->assertStatus(401)
                ->assertJson([
                    'success' => false,
                    'message' => 'Current password is incorrect'
                ]);

        // Verify the password was not changed
        $this->assertTrue(Hash::check('password123', $user->fresh()->password));
    }

    /** @test */
    public function user_cannot_change_password_with_non_matching_confirmation()
    {
        // Create a user with a known password
        $user = User::factory()->create([
            'password' => Hash::make('password123'),
        ]);

        // Authenticate the user
        Sanctum::actingAs($user);

        // Attempt to change password with non-matching confirmation
        $response = $this->postJson('/api/change-password', [
            'current_password' => 'password123',
            'new_password' => 'newpassword123',
            'new_password_confirmation' => 'differentpassword',
        ]);

        // Assert response
        $response->assertStatus(422)
                ->assertJsonValidationErrors(['new_password_confirmation']);

        // Verify the password was not changed
        $this->assertTrue(Hash::check('password123', $user->fresh()->password));
    }

    /** @test */
    public function user_cannot_change_password_without_authentication()
    {
        // Attempt to change password without authentication
        $response = $this->postJson('/api/change-password', [
            'current_password' => 'password123',
            'new_password' => 'newpassword123',
            'new_password_confirmation' => 'newpassword123',
        ]);

        // Assert response
        $response->assertStatus(401);
    }
} 