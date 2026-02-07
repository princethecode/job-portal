package com.emps.abroadjobs;

import android.content.Context;

import com.emps.abroadjobs.models.User;
import com.emps.abroadjobs.network.ApiClient;
import com.emps.abroadjobs.utils.SessionManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

/**
 * Integration test for complete application flow
 * Tests the entire user journey from login to logout
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest = Config.NONE)
public class ApplicationFlowTest {

    private Context context;
    private SessionManager sessionManager;
    private ApiClient apiClient;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
        
        // Clear any existing session
        sessionManager = SessionManager.getInstance(context);
        sessionManager.logout();
        
        // Initialize API client
        ApiClient.init(context);
        apiClient = ApiClient.getInstance(context);
    }

    @After
    public void tearDown() {
        // Clean up after each test
        sessionManager.logout();
    }

    @Test
    public void testCompleteUserFlow_LoginToLogout() {
        // Step 1: Verify initial state (not logged in)
        assertFalse("User should not be logged in initially", sessionManager.isLoggedIn());
        assertFalse("Session should not be valid initially", sessionManager.isSessionValid());
        assertNull("Token should be null initially", sessionManager.getToken());

        // Step 2: Simulate login
        int userId = 123;
        String userName = "Test User";
        String userEmail = "test@example.com";
        String authToken = "test_auth_token_12345";
        
        sessionManager.createLoginSession(userId, userName, userEmail, authToken);

        // Step 3: Verify logged in state
        assertTrue("User should be logged in after login", sessionManager.isLoggedIn());
        assertTrue("Session should be valid after login", sessionManager.isSessionValid());
        assertEquals("User ID should match", userId, sessionManager.getUserId());
        assertEquals("User name should match", userName, sessionManager.getName());
        assertEquals("User email should match", userEmail, sessionManager.getEmail());
        assertEquals("Token should match", authToken, sessionManager.getToken());

        // Step 4: Save complete user object
        User user = new User();
        user.setId(String.valueOf(userId));
        user.setFullName(userName);
        user.setEmail(userEmail);
        user.setPhone("1234567890");
        user.setLocation("New York");
        user.setJobTitle("Software Engineer");
        
        sessionManager.saveUser(user);

        // Step 5: Verify user object persistence
        User retrievedUser = sessionManager.getUser();
        assertNotNull("Retrieved user should not be null", retrievedUser);
        assertEquals("User ID should match", String.valueOf(userId), retrievedUser.getId());
        assertEquals("User name should match", userName, retrievedUser.getFullName());
        assertEquals("User email should match", userEmail, retrievedUser.getEmail());
        assertEquals("User phone should match", "1234567890", retrievedUser.getPhone());

        // Step 6: Save FCM token
        String fcmToken = "fcm_token_xyz";
        sessionManager.saveFcmToken(fcmToken);
        assertTrue("Should have FCM token", sessionManager.hasFcmToken());
        assertEquals("FCM token should match", fcmToken, sessionManager.getFcmToken());

        // Step 7: Update user info
        String newName = "Updated User";
        String newEmail = "updated@example.com";
        sessionManager.updateUserInfo(userId, newName, newEmail);
        
        assertEquals("Updated name should match", newName, sessionManager.getName());
        assertEquals("Updated email should match", newEmail, sessionManager.getEmail());

        // Step 8: Update token
        String newToken = "new_auth_token_67890";
        sessionManager.updateToken(newToken);
        assertEquals("Updated token should match", newToken, sessionManager.getToken());

        // Step 9: Logout
        sessionManager.logout();

        // Step 10: Verify logged out state
        assertFalse("User should not be logged in after logout", sessionManager.isLoggedIn());
        assertFalse("Session should not be valid after logout", sessionManager.isSessionValid());
        assertNull("Token should be null after logout", sessionManager.getToken());
        assertNull("User should be null after logout", sessionManager.getUser());
        assertFalse("Should not have FCM token after logout", sessionManager.hasFcmToken());
        assertEquals("User ID should be -1 after logout", -1, sessionManager.getUserId());
    }

    @Test
    public void testApiClientIntegration_WithSession() {
        // Setup: Create a valid session
        sessionManager.createLoginSession(1, "Test", "test@test.com", "valid_token");

        // Verify API client can access session
        assertNotNull("API client should not be null", apiClient);
        assertEquals("API client should have access to token", 
            "valid_token", ApiClient.getAuthToken());

        // Save token through API client
        String newToken = "api_token_123";
        ApiClient.saveAuthToken(newToken);
        assertEquals("Token should be updated through API client", 
            newToken, sessionManager.getToken());

        // Clear token through API client
        ApiClient.clearAuthToken();
        assertNull("Token should be cleared through API client", 
            sessionManager.getToken());
    }

    @Test
    public void testSessionPersistence_AcrossInstances() {
        // Create session with first instance
        sessionManager.createLoginSession(456, "Persistent User", 
            "persistent@test.com", "persistent_token");

        // Get new instance (simulating app restart)
        SessionManager newInstance = SessionManager.getInstance(context);

        // Verify session persists
        assertTrue("Session should persist across instances", 
            newInstance.isLoggedIn());
        assertEquals("User ID should persist", 456, newInstance.getUserId());
        assertEquals("Name should persist", "Persistent User", 
            newInstance.getName());
        assertEquals("Email should persist", "persistent@test.com", 
            newInstance.getEmail());
        assertEquals("Token should persist", "persistent_token", 
            newInstance.getToken());
    }

    @Test
    public void testInvalidSessionScenarios() {
        // Scenario 1: Logged in but no token
        sessionManager.createLoginSession(1, "Test", "test@test.com", "token");
        sessionManager.clearToken();
        assertFalse("Session should be invalid without token", 
            sessionManager.isSessionValid());

        // Scenario 2: Has token but not logged in
        sessionManager.logout();
        sessionManager.updateToken("orphan_token");
        assertFalse("Session should be invalid when not logged in", 
            sessionManager.isSessionValid());

        // Scenario 3: Logged in with token but invalid user ID
        sessionManager.logout();
        sessionManager.updateToken("token");
        context.getSharedPreferences("JobPortalPrefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("IsLoggedIn", true)
            .putInt("user_id", -1)
            .apply();
        assertFalse("Session should be invalid with invalid user ID", 
            sessionManager.isSessionValid());
    }

    @Test
    public void testUserDataIntegrity() {
        // Create user with special characters
        User user = new User();
        user.setId("789");
        user.setFullName("John O'Brien");
        user.setEmail("john+test@example.com");
        user.setPhone("9876543210");
        user.setLocation("New York, NY");
        user.setJobTitle("Senior Software Engineer");
        user.setAboutMe("Experienced developer with 10+ years in the industry.");

        // Save user
        sessionManager.saveUser(user);

        // Retrieve and verify
        User retrieved = sessionManager.getUser();
        assertNotNull("Retrieved user should not be null", retrieved);
        assertEquals("Special characters in name should be preserved", 
            "John O'Brien", retrieved.getFullName());
        assertEquals("Special characters in email should be preserved", 
            "john+test@example.com", retrieved.getEmail());
        assertEquals("Location with comma should be preserved", 
            "New York, NY", retrieved.getLocation());
        assertEquals("Long description should be preserved", 
            "Experienced developer with 10+ years in the industry.", 
            retrieved.getAboutMe());
    }

    @Test
    public void testConcurrentSessionOperations() {
        // Simulate rapid session updates
        for (int i = 0; i < 10; i++) {
            sessionManager.createLoginSession(i, "User" + i, 
                "user" + i + "@test.com", "token" + i);
        }

        // Verify final state
        assertEquals("Final user ID should be 9", 9, sessionManager.getUserId());
        assertEquals("Final name should be User9", "User9", 
            sessionManager.getName());
        assertEquals("Final token should be token9", "token9", 
            sessionManager.getToken());
    }

    @Test
    public void testEmptyAndNullValues() {
        // Test with empty strings
        sessionManager.createLoginSession(1, "", "", "");
        assertTrue("Should be logged in even with empty values", 
            sessionManager.isLoggedIn());
        assertFalse("Session should be invalid with empty token", 
            sessionManager.isSessionValid());

        // Test user with null values
        User user = new User();
        user.setId("1");
        sessionManager.saveUser(user);
        
        User retrieved = sessionManager.getUser();
        assertNotNull("Retrieved user should not be null", retrieved);
        assertEquals("User ID should be preserved", "1", retrieved.getId());
    }
}
