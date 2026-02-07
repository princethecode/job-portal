package com.emps.abroadjobs;

import android.content.Context;
import android.content.SharedPreferences;

import com.emps.abroadjobs.models.User;
import com.emps.abroadjobs.utils.SessionManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SessionManager
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest = Config.NONE)
public class SessionManagerTest {

    private SessionManager sessionManager;
    private Context context;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = RuntimeEnvironment.getApplication();
        
        // Clear any existing preferences
        SharedPreferences prefs = context.getSharedPreferences("JobPortalPrefs", Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
        
        // Force new instance by clearing the singleton
        try {
            java.lang.reflect.Field instance = SessionManager.class.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(null, null);
        } catch (Exception e) {
            // Ignore reflection errors
        }
        
        sessionManager = SessionManager.getInstance(context);
    }

    @Test
    public void testGetInstance_withValidContext_returnsInstance() {
        assertNotNull("SessionManager instance should not be null", sessionManager);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetInstance_withNullContext_throwsException() {
        SessionManager.getInstance(null);
    }

    @Test
    public void testCreateLoginSession_savesUserData() {
        // Arrange
        int userId = 123;
        String name = "Test User";
        String email = "test@example.com";
        String token = "test_token_123";

        // Act
        sessionManager.createLoginSession(userId, name, email, token);

        // Assert
        assertTrue("User should be logged in", sessionManager.isLoggedIn());
        assertEquals("User ID should match", userId, sessionManager.getUserId());
        assertEquals("Name should match", name, sessionManager.getName());
        assertEquals("Email should match", email, sessionManager.getEmail());
        assertEquals("Token should match", token, sessionManager.getToken());
    }

    @Test
    public void testSaveUser_withValidUser_savesSuccessfully() {
        // Arrange
        User user = new User();
        user.setId("456");
        user.setFullName("John Doe");
        user.setEmail("john@example.com");

        // Act
        sessionManager.saveUser(user);

        // Assert
        User retrievedUser = sessionManager.getUser();
        assertNotNull("Retrieved user should not be null", retrievedUser);
        assertEquals("User ID should match", "456", retrievedUser.getId());
        assertEquals("User name should match", "John Doe", retrievedUser.getFullName());
        assertEquals("User email should match", "john@example.com", retrievedUser.getEmail());
    }

    @Test
    public void testSaveUser_withNullUser_doesNotCrash() {
        // Act & Assert - should not throw exception
        sessionManager.saveUser(null);
    }

    @Test
    public void testIsLoggedIn_whenNotLoggedIn_returnsFalse() {
        // Assert
        assertFalse("User should not be logged in initially", sessionManager.isLoggedIn());
    }

    @Test
    public void testIsLoggedIn_afterLogin_returnsTrue() {
        // Arrange & Act
        sessionManager.createLoginSession(1, "Test", "test@test.com", "token");

        // Assert
        assertTrue("User should be logged in", sessionManager.isLoggedIn());
    }

    @Test
    public void testGetToken_whenNoToken_returnsNull() {
        // Assert
        assertNull("Token should be null when not set", sessionManager.getToken());
    }

    @Test
    public void testHasToken_whenNoToken_returnsFalse() {
        // Assert
        assertFalse("Should return false when no token", sessionManager.hasToken());
    }

    @Test
    public void testHasToken_withEmptyToken_returnsFalse() {
        // Arrange
        sessionManager.updateToken("");

        // Assert
        assertFalse("Should return false for empty token", sessionManager.hasToken());
    }

    @Test
    public void testHasToken_withValidToken_returnsTrue() {
        // Arrange
        sessionManager.updateToken("valid_token");

        // Assert
        assertTrue("Should return true for valid token", sessionManager.hasToken());
    }

    @Test
    public void testUpdateToken_updatesTokenSuccessfully() {
        // Arrange
        String newToken = "new_token_456";

        // Act
        sessionManager.updateToken(newToken);

        // Assert
        assertEquals("Token should be updated", newToken, sessionManager.getToken());
    }

    @Test
    public void testClearToken_removesToken() {
        // Arrange
        sessionManager.updateToken("token_to_clear");

        // Act
        sessionManager.clearToken();

        // Assert
        assertNull("Token should be null after clearing", sessionManager.getToken());
        assertFalse("hasToken should return false", sessionManager.hasToken());
    }

    @Test
    public void testUpdateUserInfo_updatesSuccessfully() {
        // Arrange
        int newUserId = 789;
        String newName = "Updated Name";
        String newEmail = "updated@example.com";

        // Act
        sessionManager.updateUserInfo(newUserId, newName, newEmail);

        // Assert
        assertEquals("User ID should be updated", newUserId, sessionManager.getUserId());
        assertEquals("Name should be updated", newName, sessionManager.getName());
        assertEquals("Email should be updated", newEmail, sessionManager.getEmail());
    }

    @Test
    public void testClearUserInfo_removesUserData() {
        // Arrange
        sessionManager.createLoginSession(1, "Test", "test@test.com", "token");
        User user = new User();
        user.setId("1");
        sessionManager.saveUser(user);

        // Act
        sessionManager.clearUserInfo();

        // Assert
        assertEquals("User ID should be -1", -1, sessionManager.getUserId());
        assertNull("Name should be null", sessionManager.getName());
        assertNull("Email should be null", sessionManager.getEmail());
        assertNull("User object should be null", sessionManager.getUser());
    }

    @Test
    public void testSaveFcmToken_savesSuccessfully() {
        // Arrange
        String fcmToken = "fcm_token_123";

        // Act
        sessionManager.saveFcmToken(fcmToken);

        // Assert
        assertEquals("FCM token should match", fcmToken, sessionManager.getFcmToken());
        assertTrue("Should have FCM token", sessionManager.hasFcmToken());
    }

    @Test
    public void testSaveFcmToken_withNullToken_doesNotSave() {
        // Act
        sessionManager.saveFcmToken(null);

        // Assert
        assertNull("FCM token should be null", sessionManager.getFcmToken());
        assertFalse("Should not have FCM token", sessionManager.hasFcmToken());
    }

    @Test
    public void testClearFcmToken_removesToken() {
        // Arrange
        sessionManager.saveFcmToken("fcm_token");

        // Act
        sessionManager.clearFcmToken();

        // Assert
        assertNull("FCM token should be null", sessionManager.getFcmToken());
        assertFalse("Should not have FCM token", sessionManager.hasFcmToken());
    }

    @Test
    public void testLogout_clearsAllData() {
        // Arrange
        sessionManager.createLoginSession(1, "Test", "test@test.com", "token");
        sessionManager.saveFcmToken("fcm_token");

        // Act
        sessionManager.logout();

        // Assert
        assertFalse("User should not be logged in", sessionManager.isLoggedIn());
        assertFalse("Should not have token", sessionManager.hasToken());
        assertEquals("User ID should be -1", -1, sessionManager.getUserId());
        assertNull("Name should be null", sessionManager.getName());
        assertNull("Email should be null", sessionManager.getEmail());
        assertNull("FCM token should be null", sessionManager.getFcmToken());
    }

    @Test
    public void testIsSessionValid_withCompleteSession_returnsTrue() {
        // Arrange
        sessionManager.createLoginSession(1, "Test", "test@test.com", "valid_token");

        // Assert
        assertTrue("Session should be valid", sessionManager.isSessionValid());
    }

    @Test
    public void testIsSessionValid_withoutLogin_returnsFalse() {
        // Assert
        assertFalse("Session should not be valid", sessionManager.isSessionValid());
    }

    @Test
    public void testIsSessionValid_withoutToken_returnsFalse() {
        // Arrange
        sessionManager.createLoginSession(1, "Test", "test@test.com", "token");
        sessionManager.clearToken();

        // Assert
        assertFalse("Session should not be valid without token", sessionManager.isSessionValid());
    }

    @Test
    public void testIsSessionValid_withInvalidUserId_returnsFalse() {
        // Arrange
        SharedPreferences prefs = context.getSharedPreferences("JobPortalPrefs", Context.MODE_PRIVATE);
        prefs.edit()
            .putBoolean("IsLoggedIn", true)
            .putString("token", "valid_token")
            .putInt("user_id", -1)
            .apply();

        // Assert
        assertFalse("Session should not be valid with invalid user ID", sessionManager.isSessionValid());
    }

    @Test
    public void testGetUser_withNoSavedUser_returnsNull() {
        // Assert
        assertNull("User should be null when not saved", sessionManager.getUser());
    }

    @Test
    public void testGetUser_withBasicFields_constructsUser() {
        // Arrange
        sessionManager.createLoginSession(123, "Test User", "test@test.com", "token");

        // Act
        User user = sessionManager.getUser();

        // Assert
        assertNotNull("User should not be null", user);
        assertEquals("User ID should match", "123", user.getId());
        assertEquals("User name should match", "Test User", user.getFullName());
        assertEquals("User email should match", "test@test.com", user.getEmail());
    }
}
