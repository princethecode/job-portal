package com.example.jobportal.utils;

import com.example.jobportal.models.FeaturedJob;
import com.example.jobportal.models.Job;

/**
 * Utility class for converting between job model types
 */
public class JobConverter {

    /**
     * Convert a FeaturedJob to a regular Job model for use with JobDetailsFragment
     * 
     * @param featuredJob The featured job to convert
     * @return A Job object with data from the featured job
     */
    public static Job convertFeaturedJobToJob(FeaturedJob featuredJob) {
        if (featuredJob == null) {
            return null;
        }
        
        Job job = new Job();
        
        // Set primary attributes
        job.setId(String.valueOf(featuredJob.getId()));
        job.setTitle(featuredJob.getJobTitle());
        job.setCompany(featuredJob.getCompanyName());
        job.setLocation(featuredJob.getLocation());
        job.setSalary(featuredJob.getSalary());
        job.setJobType(featuredJob.getJobType());
        job.setDescription(featuredJob.getDescription());
        job.setPostingDate(featuredJob.getPostedDate());
        
        // Set active status
        job.setActive(featuredJob.isActive());
        
        // Set company logo if available
        if (featuredJob.getCompanyLogo() != null && !featuredJob.getCompanyLogo().isEmpty()) {
            job.setCompanyLogo(featuredJob.getCompanyLogo());
        }
        
        return job;
    }
}
