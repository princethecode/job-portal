package com.emps.abroadjobs;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Integration test suite that runs all unit tests
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    SessionManagerTest.class,
    ApiClientTest.class,
    UserModelTest.class,
    NetworkUtilsTest.class,
    ValidationTest.class
})
public class IntegrationTestSuite {
    // This class remains empty, it's used only as a holder for the above annotations
}
