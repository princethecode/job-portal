-- Manual Database Update for Job Images Feature
-- Use this if you prefer to update the database manually instead of running migrations

-- Add job_image column to featured_jobs table
ALTER TABLE `featured_jobs` 
ADD COLUMN `job_image` VARCHAR(255) NULL AFTER `description`;

-- Verify the column was added
DESCRIBE `featured_jobs`;

-- Optional: Add an index for faster queries (if needed in future)
-- CREATE INDEX idx_job_image ON featured_jobs(job_image);

-- Check existing featured jobs
SELECT id, job_title, company_name, job_image FROM featured_jobs;

-- Example: Update an existing featured job with an image path (for testing)
-- UPDATE featured_jobs 
-- SET job_image = '/storage/job_images/sample.jpg' 
-- WHERE id = 1;

-- Rollback (if needed)
-- ALTER TABLE `featured_jobs` DROP COLUMN `job_image`;
