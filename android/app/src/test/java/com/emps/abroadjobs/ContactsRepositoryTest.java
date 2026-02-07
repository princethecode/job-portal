package com.emps.abroadjobs;

import android.content.Context;

import com.emps.abroadjobs.network.ContactsRepository;
import com.emps.abroadjobs.network.RecruiterContactsRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

/**
 * Tests for Contacts Repository classes
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest = Config.NONE)
public class ContactsRepositoryTest {

    private Context context;
    private ContactsRepository contactsRepository;
    private RecruiterContactsRepository recruiterContactsRepository;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
        contactsRepository = new ContactsRepository(context);
        recruiterContactsRepository = new RecruiterContactsRepository(context);
    }

    // ContactsRepository Tests
    @Test
    public void testContactsRepository_initialization() {
        assertNotNull("ContactsRepository should not be null", contactsRepository);
    }

    @Test(expected = NullPointerException.class)
    public void testContactsRepository_withNullContext_throwsException() {
        new ContactsRepository(null);
    }

    @Test
    public void testContactsRepository_multipleInstances() {
        ContactsRepository repo1 = new ContactsRepository(context);
        ContactsRepository repo2 = new ContactsRepository(context);
        
        assertNotNull("First repository should not be null", repo1);
        assertNotNull("Second repository should not be null", repo2);
    }

    // RecruiterContactsRepository Tests
    @Test
    public void testRecruiterContactsRepository_initialization() {
        assertNotNull("RecruiterContactsRepository should not be null", 
            recruiterContactsRepository);
    }

    @Test(expected = NullPointerException.class)
    public void testRecruiterContactsRepository_withNullContext_throwsException() {
        new RecruiterContactsRepository(null);
    }

    @Test
    public void testRecruiterContactsRepository_multipleInstances() {
        RecruiterContactsRepository repo1 = new RecruiterContactsRepository(context);
        RecruiterContactsRepository repo2 = new RecruiterContactsRepository(context);
        
        assertNotNull("First repository should not be null", repo1);
        assertNotNull("Second repository should not be null", repo2);
    }

    // Integration Tests
    @Test
    public void testBothRepositories_canCoexist() {
        assertNotNull("Contacts repository should exist", contactsRepository);
        assertNotNull("Recruiter contacts repository should exist", 
            recruiterContactsRepository);
    }

    @Test
    public void testRepositories_withSameContext() {
        ContactsRepository contacts = new ContactsRepository(context);
        RecruiterContactsRepository recruiterContacts = 
            new RecruiterContactsRepository(context);
        
        assertNotNull("Contacts repository should not be null", contacts);
        assertNotNull("Recruiter contacts repository should not be null", 
            recruiterContacts);
    }
}
