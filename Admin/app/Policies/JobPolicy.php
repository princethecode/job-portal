<?php

namespace App\Policies;

use App\Models\Job;
use App\Models\Recruiter;
use Illuminate\Auth\Access\HandlesAuthorization;

class JobPolicy
{
    use HandlesAuthorization;

    /**
     * Determine whether the recruiter can view the job.
     */
    public function view(Recruiter $recruiter, Job $job)
    {
        return $recruiter->id === $job->recruiter_id;
    }

    /**
     * Determine whether the recruiter can update the job.
     */
    public function update(Recruiter $recruiter, Job $job)
    {
        return $recruiter->id === $job->recruiter_id;
    }

    /**
     * Determine whether the recruiter can delete the job.
     */
    public function delete(Recruiter $recruiter, Job $job)
    {
        return $recruiter->id === $job->recruiter_id;
    }
}