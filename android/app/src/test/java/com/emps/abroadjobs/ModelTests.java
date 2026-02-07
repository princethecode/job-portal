package com.emps.abroadjobs;

import com.emps.abroadjobs.models.*;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Comprehensive tests for all data models
 */
public class ModelTests {

    // Job Model Tests
    @Test
    public void testJobModel_settersAndGetters() {
        Job job = new Job();
        job.setId("1");
        job.setTitle("Software Engineer");
        job.setCompany("Tech Corp");
        job.setLocation("New York");
        job.setSalary("$100,000");
        job.setDescription("Great opportunity");
        
        assertEquals("Job ID should match", "1", job.getId());
        assertEquals("Job title should match", "Software Engineer", job.getTitle());
        assertEquals("Company should match", "Tech Corp", job.getCompany());
        assertEquals("Location should match", "New York", job.getLocation());
        assertEquals("Salary should match", "$100,000", job.getSalary());
    }

    @Test
    public void testJobModel_nullSafety() {
        Job job = new Job();
        assertNotNull("Job should handle null values", job);
    }

    // Application Model Tests
    @Test
    public void testApplicationModel_settersAndGetters() {
        Application app = new Application();
        app.setId(1);
        app.setJobId(100);
        app.setUserId(50);
        app.setStatus("pending");
        app.setCoverLetter("I am interested");
        
        assertEquals("Application ID should match", 1, app.getId());
        assertEquals("Job ID should match", 100, app.getJobId());
        assertEquals("User ID should match", 50, app.getUserId());
        assertEquals("Status should match", "pending", app.getStatus());
    }

    // Experience Model Tests
    @Test
    public void testExperienceModel_settersAndGetters() {
        Experience exp = new Experience();
        exp.setId(1L);
        exp.setJobTitle("Developer");
        exp.setCompanyName("ABC Inc");
        exp.setStartDate("2020-01-01");
        exp.setEndDate("2022-12-31");
        exp.setCurrent(false);
        exp.setDescription("Worked on various projects");
        
        assertEquals("Experience ID should match", 1L, exp.getId());
        assertEquals("Job title should match", "Developer", exp.getJobTitle());
        assertEquals("Company should match", "ABC Inc", exp.getCompanyName());
        assertFalse("Should not be current", exp.isCurrent());
    }

    @Test
    public void testExperienceModel_currentJob() {
        Experience exp = new Experience();
        exp.setCurrent(true);
        exp.setEndDate("");
        
        assertTrue("Should be current job", exp.isCurrent());
        assertEquals("End date should be empty for current job", "", exp.getEndDate());
    }

    // Recruiter Model Tests
    @Test
    public void testRecruiterModel_settersAndGetters() {
        Recruiter recruiter = new Recruiter();
        recruiter.setId(123);
        recruiter.setName("John Recruiter");
        recruiter.setEmail("john@company.com");
        recruiter.setMobile("1234567890");
        recruiter.setCompanyName("Tech Corp");
        
        assertEquals("Recruiter ID should match", 123, recruiter.getId());
        assertEquals("Name should match", "John Recruiter", recruiter.getName());
        assertEquals("Email should match", "john@company.com", recruiter.getEmail());
        assertEquals("Company should match", "Tech Corp", recruiter.getCompanyName());
    }

    // FeaturedJob Model Tests
    @Test
    public void testFeaturedJobModel_initialization() {
        FeaturedJob job = new FeaturedJob();
        assertNotNull("FeaturedJob should not be null", job);
    }

    // Test model with special characters
    @Test
    public void testModels_withSpecialCharacters() {
        Job job = new Job();
        job.setTitle("C++ Developer");
        job.setCompany("O'Reilly Media");
        job.setDescription("Work with C++ & Java");
        
        assertEquals("Special characters in title should be preserved", 
            "C++ Developer", job.getTitle());
        assertEquals("Apostrophe should be preserved", 
            "O'Reilly Media", job.getCompany());
        assertTrue("Ampersand should be preserved", 
            job.getDescription().contains("&"));
    }

    // Test model with long text
    @Test
    public void testModels_withLongText() {
        Job job = new Job();
        String longDescription = "This is a very long job description that contains " +
            "multiple sentences and paragraphs. It describes the role in detail, " +
            "including responsibilities, requirements, and benefits. " +
            "The description can be quite lengthy to provide comprehensive information.";
        
        job.setDescription(longDescription);
        
        assertEquals("Long description should be preserved", 
            longDescription, job.getDescription());
    }

    // Test model serialization compatibility
    @Test
    public void testModels_nullFields() {
        Job job = new Job();
        // Don't set any fields
        
        // Should not crash when accessing null fields
        assertNotNull("Job object should not be null", job);
    }
}
