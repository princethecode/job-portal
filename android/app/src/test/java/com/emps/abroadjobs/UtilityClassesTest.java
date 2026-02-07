package com.emps.abroadjobs;

import android.content.Context;

import com.emps.abroadjobs.utils.AppUpdateManager;
import com.emps.abroadjobs.utils.NetworkMonitor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

/**
 * Tests for utility classes
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest = Config.NONE)
public class UtilityClassesTest {

    private Context context;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
    }

    // AppUpdateManager Tests
    @Test
    public void testAppUpdateManager_initialization() {
        AppUpdateManager manager = new AppUpdateManager(context);
        assertNotNull("AppUpdateManager should not be null", manager);
    }

    // NetworkMonitor Tests
    @Test
    public void testNetworkMonitor_initialization() {
        NetworkMonitor monitor = new NetworkMonitor(context);
        assertNotNull("NetworkMonitor should not be null", monitor);
    }

    // Integration test for multiple utilities
    @Test
    public void testUtilities_integration() {
        // Test that utilities can work together
        NetworkMonitor monitor = new NetworkMonitor(context);
        AppUpdateManager updateManager = new AppUpdateManager(context);
        
        assertNotNull("Monitor should be initialized", monitor);
        assertNotNull("Update manager should be initialized", updateManager);
    }
}
