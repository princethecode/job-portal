<?php

namespace App\Policies;

use App\Models\Job;
use App\Models\User;
use App\Models\Recruiter;
use Illuminate\Auth\Access\HandlesAuthorization;

class JobPolicy
{
    use HandlesAuthorization;

    /**
     * Determine whether the user can view the job.
     */
    public function view($user, Job $job)
    {
        // If user is a Recruiter, check ownership
        if ($user instanceof Recruiter) {
            return $user->id === $job->recruiter_id;
        }
        
        // If user is an Admin (User with is_admin=true), allow viewing
        if ($user instanceof User && $user->is_admin) {
            return true;
        }
        
        return false;
    }

    /**
     * Determine whether the user can update the job.
     */
    public function update($user, Job $job)
    {
        // If user is a Recruiter, check ownership
        if ($user instanceof Recruiter) {
            return $user->id === $job->recruiter_id;
        }
        
        // If user is an Admin (User with is_admin=true), allow updating
        if ($user instanceof User && $user->is_admin) {
            return true;
        }
        
        return false;
    }

    /**
     * Determine whether the user can delete the job.
     */
    public function delete($user, Job $job)
    {
        // If user is a Recruiter, check ownership
        if ($user instanceof Recruiter) {
            return $user->id === $job->recruiter_id;
        }
        
        // If user is an Admin (User with is_admin=true), allow deleting any job
        if ($user instanceof User && $user->is_admin) {
            return true;
        }
        
        return false;
    }
}