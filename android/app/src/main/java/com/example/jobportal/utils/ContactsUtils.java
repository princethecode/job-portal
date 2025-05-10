package com.example.jobportal.utils;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Utility class for handling contacts-related operations
 */
public class ContactsUtils {
    private static final String TAG = "ContactsUtils";

    /**
     * Contact data class to hold contact information
     */
    public static class Contact {
        private String id;
        private String displayName;
        private List<String> phoneNumbers;
        private List<String> emails;

        public Contact(String id, String displayName, List<String> phoneNumbers, List<String> emails) {
            this.id = id;
            this.displayName = displayName;
            this.phoneNumbers = phoneNumbers;
            this.emails = emails;
        }

        public String getId() {
            return id;
        }

        public String getDisplayName() {
            return displayName;
        }

        public List<String> getPhoneNumbers() {
            return phoneNumbers;
        }

        public List<String> getEmails() {
            return emails;
        }
    }

    /**
     * Reads all contacts from the device
     * @param context Application context
     * @return List of contacts
     */
    public static List<Contact> readContacts(Context context) {
        List<Contact> contacts = new ArrayList<>();
        try {
            Cursor cursor = context.getContentResolver().query(
                    ContactsContract.Contacts.CONTENT_URI,
                    null,
                    null,
                    null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            );

            if (cursor != null) {
                try {
                    while (cursor.moveToNext()) {
                        int idColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                        int nameColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                        int hasPhoneColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
                        
                        if (idColumnIndex < 0 || nameColumnIndex < 0 || hasPhoneColumnIndex < 0) {
                            continue;
                        }
                        
                        String id = cursor.getString(idColumnIndex);
                        String name = cursor.getString(nameColumnIndex);
                        if (name == null) name = "Unknown";
                        
                        int hasPhoneNumber = Integer.parseInt(cursor.getString(hasPhoneColumnIndex));

                        List<String> phoneNumbers = new ArrayList<>();
                        if (hasPhoneNumber > 0) {
                            Cursor phoneCursor = context.getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                    new String[]{id},
                                    null
                            );

                            if (phoneCursor != null) {
                                try {
                                    while (phoneCursor.moveToNext()) {
                                        int phoneColumnIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                                        if (phoneColumnIndex >= 0) {
                                            String phoneNumber = phoneCursor.getString(phoneColumnIndex);
                                            phoneNumbers.add(phoneNumber);
                                        }
                                    }
                                } finally {
                                    phoneCursor.close();
                                }
                            }
                        }

                        List<String> emails = new ArrayList<>();
                        Cursor emailCursor = context.getContentResolver().query(
                                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                                new String[]{id},
                                null
                        );

                        if (emailCursor != null) {
                            try {
                                while (emailCursor.moveToNext()) {
                                    int emailColumnIndex = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);
                                    if (emailColumnIndex >= 0) {
                                        String email = emailCursor.getString(emailColumnIndex);
                                        if (email != null && !email.isEmpty()) {
                                            emails.add(email);
                                        }
                                    }
                                }
                            } finally {
                                emailCursor.close();
                            }
                        }

                        contacts.add(new Contact(id, name, phoneNumbers, emails));
                    }
                } finally {
                    cursor.close();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading contacts", e);
        }
        return contacts;
    }

    /**
     * Creates a CSV file with contacts data
     * @param context Application context
     * @param contacts List of contacts to include in the CSV
     * @return File object pointing to the created CSV file, or null if creation failed
     */
    public static File createContactsCsvFile(Context context, List<Contact> contacts) {
        try {
            String fileName = "contacts_" + UUID.randomUUID().toString() + ".csv";
            File file = new File(context.getCacheDir(), fileName);
            
            FileWriter writer = null;
            try {
                writer = new FileWriter(file);
                
                // Write CSV header
                writer.append("ID,Name,PhoneNumber,Email\n");
                
                // Write each contact
                for (Contact contact : contacts) {
                    // For contacts with multiple phones/emails, create a row for each combination
                    List<String> phoneNumbers = contact.getPhoneNumbers();
                    List<String> emails = contact.getEmails();
                    
                    if (phoneNumbers.isEmpty()) {
                        phoneNumbers = new ArrayList<>();
                        phoneNumbers.add("");
                    }
                    
                    if (emails.isEmpty()) {
                        emails = new ArrayList<>();
                        emails.add("");
                    }
                    
                    for (String phone : phoneNumbers) {
                        for (String email : emails) {
                            writer.append(contact.getId()).append(",");
                            writer.append("\"").append(escapeCsvField(contact.getDisplayName())).append("\",");
                            writer.append("\"").append(escapeCsvField(phone)).append("\",");
                            writer.append("\"").append(escapeCsvField(email)).append("\"\n");
                        }
                    }
                }
                
                return file;
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating contacts CSV file", e);
            return null;
        }
    }
    
    /**
     * Escapes special characters in CSV fields
     * @param field Field to escape
     * @return Escaped field
     */
    private static String escapeCsvField(String field) {
        return field.replace("\"", "\"\"");
    }
} 