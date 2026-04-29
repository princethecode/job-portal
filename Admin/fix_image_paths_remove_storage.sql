-- Fix Image Paths - Remove /storage/ Prefix
-- This script removes the /storage/ prefix from existing image paths
-- Run this after deploying the updated controllers

-- ========================================
-- 1. BACKUP CURRENT DATA (RECOMMENDED)
-- ========================================

CREATE TABLE IF NOT EXISTS featured_jobs_backup_storage_fix AS 
SELECT * FROM featured_jobs;

-- Verify backup
SELECT COUNT(*) as backup_count FROM featured_jobs_backup_storage_fix;

-- ========================================
-- 2. CHECK CURRENT PATHS
-- ========================================

SELECT 
    id, 
    job_title,
    company_logo,
    job_image,
    CASE 
        WHEN company_logo LIKE '/storage/%' THEN '✗ Has /storage/ prefix'
        WHEN company_logo IS NULL THEN 'NULL'
        ELSE '✓ Correct format'
    END as logo_status,
    CASE 
        WHEN job_image LIKE '/storage/%' THEN '✗ Has /storage/ prefix'
        WHEN job_image IS NULL THEN 'NULL'
        ELSE '✓ Correct format'
    END as image_status
FROM featured_jobs
ORDER BY id;

-- ========================================
-- 3. FIX COMPANY LOGOS - Remove /storage/ prefix
-- ========================================

UPDATE featured_jobs 
SET company_logo = SUBSTRING(company_logo, 10)  -- Remove '/storage/' (9 chars + 1)
WHERE company_logo LIKE '/storage/%';

-- Check results
SELECT id, job_title, company_logo 
FROM featured_jobs 
WHERE company_logo IS NOT NULL;

-- ========================================
-- 4. FIX JOB IMAGES - Remove /storage/ prefix
-- ========================================

UPDATE featured_jobs 
SET job_image = SUBSTRING(job_image, 10)  -- Remove '/storage/' (9 chars + 1)
WHERE job_image LIKE '/storage/%';

-- Check results
SELECT id, job_title, job_image 
FROM featured_jobs 
WHERE job_image IS NOT NULL;

-- ========================================
-- 5. CLEAN UP INVALID PATHS
-- ========================================

-- Remove paths that look like temporary PHP upload paths
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
    SUM(CASE WHEN company_logo NOT LIKE '/storage/%' AND company_logo IS NOT NULL THEN 1 ELSE 0 END) as logos_correct,
    SUM(CASE WHEN job_image NOT LIKE '/storage/%' AND job_image IS NOT NULL THEN 1 ELSE 0 END) as images_correct
FROM featured_jobs;

-- Show final paths (should be like: company_logos/filename.jpg or job_images/filename.jpg)
SELECT 
    id, 
    job_title,
    company_logo,
    job_image
FROM featured_jobs
WHERE company_logo IS NOT NULL OR job_image IS NOT NULL
ORDER BY id;

-- ========================================
-- 7. ROLLBACK (IF NEEDED)
-- ========================================

-- If something went wrong, restore from backup:
-- DELETE FROM featured_jobs;
-- INSERT INTO featured_jobs SELECT * FROM featured_jobs_backup_storage_fix;

-- ========================================
-- 8. CLEANUP BACKUP (AFTER VERIFICATION)
-- ========================================

-- Once you've verified everything works, you can drop the backup:
-- DROP TABLE featured_jobs_backup_storage_fix;

-- ========================================
-- EXPECTED RESULTS
-- ========================================

-- Before fix:
-- company_logo: /storage/company_logos/abc123.jpg
-- job_image: /storage/job_images/xyz789.jpg

-- After fix:
-- company_logo: company_logos/abc123.jpg
-- job_image: job_images/xyz789.jpg

-- These will be accessed via:
-- https://emps.co.in/storage/company_logos/abc123.jpg (symlink handles /storage/)
-- https://emps.co.in/storage/job_images/xyz789.jpg (symlink handles /storage/)
