<?php

namespace App\Policies;

use App\Models\Application;
use App\Models\Recruiter;
use Illuminate\Auth\Access\HandlesAuthorization;

class ApplicationPolicy
{
    use HandlesAuthorization;

    /**
     * Determine whether the recruiter can view the application.
     */
    public function view(Recruiter $recruiter, Application $application)
    {
        return $recruiter->id === $application->job->recruiter_id;
    }

    /**
     * Determine whether the recruiter can update the application.
     */
    public function update(Recruiter $recruiter, Application $application)
    {
        return $recruiter->id === $application->job->recruiter_id;
    }
}