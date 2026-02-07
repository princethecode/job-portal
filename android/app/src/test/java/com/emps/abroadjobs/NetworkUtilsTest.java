package com.emps.abroadjobs;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.emps.abroadjobs.utils.NetworkMonitor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowNetworkInfo;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for Network utilities
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest = Config.NONE)
public class NetworkUtilsTest {

    private Context context;
    private NetworkMonitor networkMonitor;

    @Mock
    private ConnectivityManager connectivityManager;

    @Mock
    private NetworkInfo networkInfo;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = RuntimeEnvironment.getApplication();
        networkMonitor = new NetworkMonitor(context);
    }

    @Test
    public void testNetworkMonitor_initialization() {
        assertNotNull("NetworkMonitor should not be null", networkMonitor);
    }

    @Test
    public void testNetworkMonitor_withContext() {
        NetworkMonitor monitor = new NetworkMonitor(context);
        assertNotNull("NetworkMonitor should be created with context", monitor);
    }

    @Test(expected = NullPointerException.class)
    public void testNetworkMonitor_withNullContext_throwsException() {
        new NetworkMonitor(null);
    }
}
