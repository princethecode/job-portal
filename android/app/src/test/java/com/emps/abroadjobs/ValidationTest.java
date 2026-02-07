package com.emps.abroadjobs;

import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Unit tests for input validation
 */
public class ValidationTest {

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9+._%\\-]{1,256}" +
        "@" +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
        "(" +
        "\\." +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
        ")+"
    );

    // Phone validation pattern (10 digits)
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");

    // Password validation (minimum 6 characters)
    private static final int MIN_PASSWORD_LENGTH = 6;

    @Test
    public void testValidEmail() {
        assertTrue("Valid email should pass", isValidEmail("test@example.com"));
        assertTrue("Valid email with subdomain should pass", isValidEmail("user@mail.example.com"));
        assertTrue("Valid email with plus should pass", isValidEmail("user+tag@example.com"));
        assertTrue("Valid email with dots should pass", isValidEmail("first.last@example.com"));
    }

    @Test
    public void testInvalidEmail() {
        assertFalse("Email without @ should fail", isValidEmail("testexample.com"));
        assertFalse("Email without domain should fail", isValidEmail("test@"));
        assertFalse("Email without username should fail", isValidEmail("@example.com"));
        assertFalse("Empty email should fail", isValidEmail(""));
        assertFalse("Null email should fail", isValidEmail(null));
        assertFalse("Email with spaces should fail", isValidEmail("test @example.com"));
        assertFalse("Email without TLD should fail", isValidEmail("test@example"));
    }

    @Test
    public void testValidPhone() {
        assertTrue("Valid 10-digit phone should pass", isValidPhone("1234567890"));
        assertTrue("Valid phone with all same digits should pass", isValidPhone("9999999999"));
    }

    @Test
    public void testInvalidPhone() {
        assertFalse("Phone with less than 10 digits should fail", isValidPhone("123456789"));
        assertFalse("Phone with more than 10 digits should fail", isValidPhone("12345678901"));
        assertFalse("Phone with letters should fail", isValidPhone("12345abcde"));
        assertFalse("Phone with spaces should fail", isValidPhone("123 456 7890"));
        assertFalse("Phone with dashes should fail", isValidPhone("123-456-7890"));
        assertFalse("Empty phone should fail", isValidPhone(""));
        assertFalse("Null phone should fail", isValidPhone(null));
    }

    @Test
    public void testValidPassword() {
        assertTrue("Password with 6 characters should pass", isValidPassword("pass12"));
        assertTrue("Password with more than 6 characters should pass", isValidPassword("password123"));
        assertTrue("Password with special characters should pass", isValidPassword("Pass@123"));
    }

    @Test
    public void testInvalidPassword() {
        assertFalse("Password with less than 6 characters should fail", isValidPassword("pass"));
        assertFalse("Empty password should fail", isValidPassword(""));
        assertFalse("Null password should fail", isValidPassword(null));
        assertFalse("Password with only spaces should fail", isValidPassword("     "));
    }

    @Test
    public void testValidName() {
        assertTrue("Valid name should pass", isValidName("John Doe"));
        assertTrue("Name with single word should pass", isValidName("John"));
        assertTrue("Name with special characters should pass", isValidName("O'Brien"));
    }

    @Test
    public void testInvalidName() {
        assertFalse("Empty name should fail", isValidName(""));
        assertFalse("Null name should fail", isValidName(null));
        assertFalse("Name with only spaces should fail", isValidName("   "));
        assertFalse("Name with numbers should fail", isValidName("John123"));
    }

    @Test
    public void testPasswordMatch() {
        assertTrue("Matching passwords should pass", doPasswordsMatch("password", "password"));
        assertFalse("Non-matching passwords should fail", doPasswordsMatch("password", "different"));
        assertFalse("Null passwords should fail", doPasswordsMatch(null, null));
        assertFalse("One null password should fail", doPasswordsMatch("password", null));
    }

    @Test
    public void testEmptyString() {
        assertTrue("Empty string should be detected", isEmpty(""));
        assertTrue("Null string should be detected as empty", isEmpty(null));
        assertTrue("String with only spaces should be detected as empty", isEmpty("   "));
        assertFalse("Non-empty string should not be detected as empty", isEmpty("test"));
    }

    // Helper methods for validation
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    private boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        return password.trim().length() >= MIN_PASSWORD_LENGTH;
    }

    private boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        // Name should not contain numbers
        return !name.matches(".*\\d.*");
    }

    private boolean doPasswordsMatch(String password1, String password2) {
        if (password1 == null || password2 == null) {
            return false;
        }
        return password1.equals(password2);
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
