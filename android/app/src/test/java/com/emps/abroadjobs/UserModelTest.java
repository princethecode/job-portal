package com.emps.abroadjobs;

import com.emps.abroadjobs.models.User;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for User model
 */
public class UserModelTest {

    private User user;

    @Before
    public void setUp() {
        user = new User();
    }

    @Test
    public void testSetAndGetId() {
        String id = "123";
        user.setId(id);
        assertEquals("ID should match", id, user.getId());
    }

    @Test
    public void testSetAndGetFullName() {
        String name = "John Doe";
        user.setFullName(name);
        assertEquals("Full name should match", name, user.getFullName());
    }

    @Test
    public void testSetAndGetEmail() {
        String email = "john@example.com";
        user.setEmail(email);
        assertEquals("Email should match", email, user.getEmail());
    }

    @Test
    public void testSetAndGetPhone() {
        String phone = "1234567890";
        user.setPhone(phone);
        assertEquals("Phone should match", phone, user.getPhone());
    }

    @Test
    public void testSetAndGetLocation() {
        String location = "New York";
        user.setLocation(location);
        assertEquals("Location should match", location, user.getLocation());
    }

    @Test
    public void testSetAndGetJobTitle() {
        String jobTitle = "Software Engineer";
        user.setJobTitle(jobTitle);
        assertEquals("Job title should match", jobTitle, user.getJobTitle());
    }

    @Test
    public void testSetAndGetAboutMe() {
        String aboutMe = "Experienced developer";
        user.setAboutMe(aboutMe);
        assertEquals("About me should match", aboutMe, user.getAboutMe());
    }

    @Test
    public void testSetAndGetContact() {
        String contact = "9876543210";
        user.setContact(contact);
        assertEquals("Contact should match", contact, user.getContact());
    }

    @Test
    public void testSetAndGetLastContactSync() {
        String syncDate = "2024-01-01T00:00:00";
        user.setLastContactSync(syncDate);
        assertEquals("Last contact sync should match", syncDate, user.getLastContactSync());
    }

    @Test
    public void testUserWithNullValues() {
        // Assert all fields are null by default
        assertNull("ID should be null", user.getId());
        assertNull("Full name should be null", user.getFullName());
        assertNull("Email should be null", user.getEmail());
        assertNull("Phone should be null", user.getPhone());
        assertNull("Location should be null", user.getLocation());
        assertNull("Job title should be null", user.getJobTitle());
        assertNull("About me should be null", user.getAboutMe());
    }

    @Test
    public void testUserWithEmptyStrings() {
        user.setFullName("");
        user.setEmail("");
        user.setPhone("");
        
        assertEquals("Empty full name should be preserved", "", user.getFullName());
        assertEquals("Empty email should be preserved", "", user.getEmail());
        assertEquals("Empty phone should be preserved", "", user.getPhone());
    }

    @Test
    public void testUserWithSpecialCharacters() {
        String specialName = "John O'Brien";
        String specialEmail = "john+test@example.com";
        
        user.setFullName(specialName);
        user.setEmail(specialEmail);
        
        assertEquals("Special characters in name should be preserved", specialName, user.getFullName());
        assertEquals("Special characters in email should be preserved", specialEmail, user.getEmail());
    }
}
