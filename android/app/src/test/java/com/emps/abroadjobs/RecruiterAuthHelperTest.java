package com.emps.abroadjobs;

import android.content.Context;
import android.content.SharedPreferences;

import com.emps.abroadjobs.auth.RecruiterAuthHelper;
import com.emps.abroadjobs.models.Recruiter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

/**
 * Unit tests for RecruiterAuthHelper
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest = Config.NONE)
public class RecruiterAuthHelperTest {

    private RecruiterAuthHelper authHelper;
    private Context context;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
        
        // Clear preferences
        SharedPreferences prefs = context.getSharedPreferences("RecruiterPrefs", Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
        
        // Reset singleton
        try {
            java.lang.reflect.Field instance = RecruiterAuthHelper.class.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(null, null);
        } catch (Exception e) {
            // Ignore
        }
        
        authHelper = RecruiterAuthHelper.getInstance(context);
    }

    @Test
    public void testGetInstance_returnsNonNull() {
        assertNotNull("RecruiterAuthHelper instance should not be null", authHelper);
    }

    @Test
    public void testSaveRecruiterToken_savesSuccessfully() {
        String token = "recruiter_token_123";
        authHelper.saveRecruiterToken(token);
        assertEquals("Token should be saved", token, authHelper.getRecruiterToken());
    }

    @Test
    public void testIsRecruiterLoggedIn_initiallyFalse() {
        assertFalse("Recruiter should not be logged in initially", 
            authHelper.isLoggedIn());
    }

    @Test
    public void testSaveRecruiter_savesSuccessfully() {
        Recruiter recruiter = new Recruiter();
        recruiter.setId(123);
        recruiter.setName("Test Recruiter");
        recruiter.setEmail("recruiter@test.com");
        
        authHelper.saveRecruiterData(recruiter);
        
        Recruiter retrieved = authHelper.getRecruiterData();
        assertNotNull("Retrieved recruiter should not be null", retrieved);
        assertEquals("Recruiter ID should match", 123, retrieved.getId());
    }

    @Test
    public void testLogout_clearsAllData() {
        authHelper.saveRecruiterToken("token");
        Recruiter recruiter = new Recruiter();
        recruiter.setId(1);
        authHelper.saveRecruiterData(recruiter);
        
        authHelper.logout();
        
        assertNull("Token should be null after logout", authHelper.getRecruiterToken());
        assertFalse("Should not be logged in after logout", 
            authHelper.isLoggedIn());
    }

    @Test
    public void testHasValidToken_withValidToken() {
        authHelper.saveRecruiterToken("valid_token_12345");
        assertTrue("Should have valid token", authHelper.hasValidToken());
    }

    @Test
    public void testHasValidToken_withShortToken() {
        authHelper.saveRecruiterToken("short");
        assertFalse("Short token should be invalid", authHelper.hasValidToken());
    }

    @Test
    public void testSetLoggedIn_setsStatus() {
        authHelper.setLoggedIn(true);
        assertTrue("Should be logged in", authHelper.isLoggedIn());
        
        authHelper.setLoggedIn(false);
        assertFalse("Should not be logged in", authHelper.isLoggedIn());
    }
}
