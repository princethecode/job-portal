-- Fix Existing Image Paths in Featured Jobs
-- Run this script if you have existing featured jobs with incorrect image paths

-- ========================================
-- 1. CHECK CURRENT PATHS
-- ========================================

-- View all current paths
SELECT 
    id, 
    job_title,
    company_logo,
    job_image,
    CASE 
        WHEN company_logo LIKE '/storage/%' THEN '✓ Correct'
        WHEN company_logo IS NULL THEN 'NULL'
        ELSE '✗ Needs Fix'
    END as logo_status,
    CASE 
        WHEN job_image LIKE '/storage/%' THEN '✓ Correct'
        WHEN job_image IS NULL THEN 'NULL'
        ELSE '✗ Needs Fix'
    END as image_status
FROM featured_jobs
ORDER BY id;

-- ========================================
-- 2. BACKUP CURRENT DATA (RECOMMENDED)
-- ========================================

-- Create backup table
CREATE TABLE IF NOT EXISTS featured_jobs_backup_20260426 AS 
SELECT * FROM featured_jobs;

-- Verify backup
SELECT COUNT(*) as backup_count FROM featured_jobs_backup_20260426;

-- ========================================
-- 3. FIX COMPANY LOGOS
-- ========================================

-- Fix company logos that don't start with /storage/
UPDATE featured_jobs 
SET company_logo = CONCAT('/storage/', company_logo) 
WHERE company_logo IS NOT NULL 
  AND company_logo != ''
  AND company_logo NOT LIKE '/storage/%'
  AND company_logo NOT LIKE 'http%';

-- Check results
SELECT id, job_title, company_logo 
FROM featured_jobs 
WHERE company_logo IS NOT NULL;

-- ========================================
-- 4. FIX JOB IMAGES
-- ========================================

-- Fix job images that don't start with /storage/
UPDATE featured_jobs 
SET job_image = CONCAT('/storage/', job_image) 
WHERE job_image IS NOT NULL 
  AND job_image != ''
  AND job_image NOT LIKE '/storage/%'
  AND job_image NOT LIKE 'http%';

-- Check results
SELECT id, job_title, job_image 
FROM featured_jobs 
WHERE job_image IS NOT NULL;

-- ========================================
-- 5. CLEAN UP INVALID PATHS
-- ========================================

-- Remove paths that look like temporary PHP upload paths
-- (These are invalid and should be set to NULL)
UPDATE featured_jobs 
SET company_logo = NULL 
WHERE company_logo LIKE '%/tmp/%' 
   OR company_logo LIKE '%php%';

UPDATE featured_jobs 
SET job_image = NULL 
WHERE job_image LIKE '%/tmp/%' 
   OR job_image LIKE '%php%';

-- ========================================
-- 6. VERIFY FIXES
-- ========================================

-- Count records by status
SELECT 
    COUNT(*) as total_jobs,
    SUM(CASE WHEN company_logo IS NOT NULL THEN 1 ELSE 0 END) as jobs_with_logo,
    SUM(CASE WHEN job_image IS NOT NULL THEN 1 ELSE 0 END) as jobs_with_image,
    SUM(CASE WHEN company_logo LIKE '/storage/%' THEN 1 ELSE 0 END) as logos_correct,
    SUM(CASE WHEN job_image LIKE '/storage/%' THEN 1 ELSE 0 END) as images_correct
FROM featured_jobs;

-- Show any remaining issues
SELECT 
    id, 
    job_title,
    company_logo,
    job_image
FROM featured_jobs
WHERE (company_logo IS NOT NULL AND company_logo NOT LIKE '/storage/%' AND company_logo NOT LIKE 'http%')
   OR (job_image IS NOT NULL AND job_image NOT LIKE '/storage/%' AND job_image NOT LIKE 'http%');

-- ========================================
-- 7. ROLLBACK (IF NEEDED)
-- ========================================

-- If something went wrong, restore from backup:
-- DELETE FROM featured_jobs;
-- INSERT INTO featured_jobs SELECT * FROM featured_jobs_backup_20260426;

-- ========================================
-- 8. CLEANUP BACKUP (AFTER VERIFICATION)
-- ========================================

-- Once you've verified everything works, you can drop the backup:
-- DROP TABLE featured_jobs_backup_20260426;

-- ========================================
-- NOTES
-- ========================================

-- This script:
-- 1. Creates a backup of your data
-- 2. Fixes paths that don't start with /storage/
-- 3. Removes invalid temporary paths
-- 4. Provides verification queries
-- 5. Includes rollback instructions

-- After running this script:
-- 1. Test the API: curl https://emps.co.in/api/featured-jobs
-- 2. Test image URLs in browser
-- 3. Test Android app
-- 4. If all works, drop the backup table
