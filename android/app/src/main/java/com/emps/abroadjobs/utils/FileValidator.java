package com.emps.abroadjobs.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import java.io.File;

/**
 * Utility class for validating file uploads
 */
public class FileValidator {
    
    // Maximum file size: 10MB in bytes
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    
    // Supported document MIME types
    public static final String[] SUPPORTED_DOCUMENT_TYPES = {
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain",
            "application/rtf",
            "application/vnd.oasis.opendocument.text",
            "application/vnd.oasis.opendocument.spreadsheet",
            "application/vnd.oasis.opendocument.presentation"
    };
    
    // Supported image MIME types
    public static final String[] SUPPORTED_IMAGE_TYPES = {
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/bmp",
            "image/webp",
            "image/svg+xml",
            "image/tiff",
            "image/x-icon",
            "image/heic",
            "image/heif"
    };
    
    /**
     * Validate file size from URI
     * @param context Application context
     * @param uri File URI
     * @return true if file size is within limit, false otherwise
     */
    public static boolean isFileSizeValid(Context context, Uri uri) {
        long fileSize = getFileSize(context, uri);
        return fileSize > 0 && fileSize <= MAX_FILE_SIZE;
    }
    
    /**
     * Validate file size from File object
     * @param file File object
     * @return true if file size is within limit, false otherwise
     */
    public static boolean isFileSizeValid(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        long fileSize = file.length();
        return fileSize > 0 && fileSize <= MAX_FILE_SIZE;
    }
    
    /**
     * Get file size from URI
     * @param context Application context
     * @param uri File URI
     * @return File size in bytes, or -1 if unable to determine
     */
    public static long getFileSize(Context context, Uri uri) {
        long fileSize = -1;
        
        try {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex != -1) {
                    fileSize = cursor.getLong(sizeIndex);
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return fileSize;
    }
    
    /**
     * Format file size to human-readable string
     * @param size File size in bytes
     * @return Formatted string (e.g., "2.5 MB")
     */
    public static String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        
        final String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        
        return String.format("%.2f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }
    
    /**
     * Get maximum file size as formatted string
     * @return Formatted max file size (e.g., "10.00 MB")
     */
    public static String getMaxFileSizeFormatted() {
        return formatFileSize(MAX_FILE_SIZE);
    }
    
    /**
     * Check if MIME type is a supported document type
     * @param mimeType MIME type to check
     * @return true if supported, false otherwise
     */
    public static boolean isSupportedDocumentType(String mimeType) {
        if (mimeType == null) return false;
        
        for (String type : SUPPORTED_DOCUMENT_TYPES) {
            if (type.equalsIgnoreCase(mimeType)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if MIME type is a supported image type
     * @param mimeType MIME type to check
     * @return true if supported, false otherwise
     */
    public static boolean isSupportedImageType(String mimeType) {
        if (mimeType == null) return false;
        
        for (String type : SUPPORTED_IMAGE_TYPES) {
            if (type.equalsIgnoreCase(mimeType)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if MIME type is supported (document or image)
     * @param mimeType MIME type to check
     * @return true if supported, false otherwise
     */
    public static boolean isSupportedFileType(String mimeType) {
        return isSupportedDocumentType(mimeType) || isSupportedImageType(mimeType);
    }
    
    /**
     * Get MIME type from URI
     * @param context Application context
     * @param uri File URI
     * @return MIME type string, or null if unable to determine
     */
    public static String getMimeType(Context context, Uri uri) {
        String mimeType = context.getContentResolver().getType(uri);
        
        if (mimeType == null) {
            // Try to get from file extension
            String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            if (extension != null) {
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            }
        }
        
        return mimeType;
    }
    
    /**
     * Validate file completely (size and type)
     * @param context Application context
     * @param uri File URI
     * @return ValidationResult object with validation details
     */
    public static ValidationResult validateFile(Context context, Uri uri) {
        ValidationResult result = new ValidationResult();
        
        // Check file size
        long fileSize = getFileSize(context, uri);
        result.fileSize = fileSize;
        
        if (fileSize <= 0) {
            result.isValid = false;
            result.errorMessage = "Unable to determine file size";
            return result;
        }
        
        if (fileSize > MAX_FILE_SIZE) {
            result.isValid = false;
            result.errorMessage = "File size exceeds maximum limit of " + getMaxFileSizeFormatted();
            return result;
        }
        
        // Check file type
        String mimeType = getMimeType(context, uri);
        result.mimeType = mimeType;
        
        if (!isSupportedFileType(mimeType)) {
            result.isValid = false;
            result.errorMessage = "Unsupported file type";
            return result;
        }
        
        result.isValid = true;
        return result;
    }
    
    /**
     * Result class for file validation
     */
    public static class ValidationResult {
        public boolean isValid = false;
        public String errorMessage = "";
        public long fileSize = 0;
        public String mimeType = "";
        
        public String getFormattedFileSize() {
            return formatFileSize(fileSize);
        }
    }
}
