package com.emps.abroadjobs.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.emps.abroadjobs.models.Job;
import com.emps.abroadjobs.models.FeaturedJob;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Utility class for sharing jobs on WhatsApp and other platforms
 */
public class ShareUtils {
    private static final String TAG = "ShareUtils";
    private static final int MAX_DESCRIPTION_LENGTH = 100;

    /**
     * Share a job on WhatsApp with image and short description
     *
     * @param context The context
     * @param job     The job to share
     */
    public static void shareJobOnWhatsApp(Context context, Job job) {
        Log.d(TAG, "📱 Starting WhatsApp share for job: " + job.getTitle() + " (ID: " + job.getId() + ")");

        String imageUrl = getJobImageUrl(job);
        
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Share with image
            shareWithImage(context, job, imageUrl);
        } else {
            // Share text only if no image available
            shareTextOnly(context, job);
        }
    }

    /**
     * Share a featured job on WhatsApp with image and short description
     *
     * @param context The context
     * @param job     The featured job to share
     */
    public static void shareJobOnWhatsApp(Context context, FeaturedJob job) {
        Log.d(TAG, "📱 Starting WhatsApp share for featured job: " + job.getJobTitle() + " (ID: " + job.getId() + ")");

        String imageUrl = getFeaturedJobImageUrl(job);
        
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Share with image
            shareWithImage(context, job, imageUrl);
        } else {
            // Share text only if no image available
            shareTextOnly(context, job);
        }
    }

    /**
     * Share job with image
     */
    private static void shareWithImage(Context context, Job job, String imageUrl) {
        // Load image using Glide and share
        Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                        try {
                            // Save bitmap to cache directory
                            File cachePath = new File(context.getCacheDir(), "images");
                            cachePath.mkdirs();
                            File imageFile = new File(cachePath, "job_share_" + job.getId() + ".jpg");
                            
                            FileOutputStream stream = new FileOutputStream(imageFile);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                            stream.close();

                            // Get URI for the file
                            Uri imageUri = FileProvider.getUriForFile(
                                    context,
                                    context.getPackageName() + ".fileprovider",
                                    imageFile
                            );

                            // Create share text
                            String shareText = createShareText(job);

                            // Create share intent
                            Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                            whatsappIntent.setType("image/*");
                            whatsappIntent.setPackage("com.whatsapp");
                            whatsappIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                            whatsappIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                            whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                            try {
                                context.startActivity(whatsappIntent);
                                Log.d(TAG, "✅ WhatsApp intent with image started successfully");
                            } catch (android.content.ActivityNotFoundException ex) {
                                Log.d(TAG, "⚠️ WhatsApp not found, trying general share");
                                // WhatsApp not installed, try with general share
                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.setType("image/*");
                                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Job Opportunity: " + job.getTitle());
                                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                
                                context.startActivity(Intent.createChooser(shareIntent, "Share Job"));
                                Log.d(TAG, "✅ General share intent with image started successfully");
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "❌ Error saving image: " + e.getMessage(), e);
                            // Fallback to text only
                            shareTextOnly(context, job);
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Do nothing
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        Log.e(TAG, "❌ Failed to load image, falling back to text only");
                        // Fallback to text only
                        shareTextOnly(context, job);
                    }
                });
    }

    /**
     * Share featured job with image
     */
    private static void shareWithImage(Context context, FeaturedJob job, String imageUrl) {
        // Load image using Glide and share
        Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                        try {
                            // Save bitmap to cache directory
                            File cachePath = new File(context.getCacheDir(), "images");
                            cachePath.mkdirs();
                            File imageFile = new File(cachePath, "job_share_" + job.getId() + ".jpg");
                            
                            FileOutputStream stream = new FileOutputStream(imageFile);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                            stream.close();

                            // Get URI for the file
                            Uri imageUri = FileProvider.getUriForFile(
                                    context,
                                    context.getPackageName() + ".fileprovider",
                                    imageFile
                            );

                            // Create share text
                            String shareText = createShareText(job);

                            // Create share intent
                            Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                            whatsappIntent.setType("image/*");
                            whatsappIntent.setPackage("com.whatsapp");
                            whatsappIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                            whatsappIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                            whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                            try {
                                context.startActivity(whatsappIntent);
                                Log.d(TAG, "✅ WhatsApp intent with image started successfully");
                            } catch (android.content.ActivityNotFoundException ex) {
                                Log.d(TAG, "⚠️ WhatsApp not found, trying general share");
                                // WhatsApp not installed, try with general share
                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.setType("image/*");
                                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Job Opportunity: " + job.getJobTitle());
                                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                
                                context.startActivity(Intent.createChooser(shareIntent, "Share Job"));
                                Log.d(TAG, "✅ General share intent with image started successfully");
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "❌ Error saving image: " + e.getMessage(), e);
                            // Fallback to text only
                            shareTextOnly(context, job);
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Do nothing
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        Log.e(TAG, "❌ Failed to load image, falling back to text only");
                        // Fallback to text only
                        shareTextOnly(context, job);
                    }
                });
    }

    /**
     * Share text only (fallback when no image available)
     */
    private static void shareTextOnly(Context context, Job job) {
        try {
            String shareText = createShareText(job);

            Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
            whatsappIntent.setType("text/plain");
            whatsappIntent.setPackage("com.whatsapp");
            whatsappIntent.putExtra(Intent.EXTRA_TEXT, shareText);

            try {
                context.startActivity(whatsappIntent);
                Log.d(TAG, "✅ WhatsApp text intent started successfully");
            } catch (android.content.ActivityNotFoundException ex) {
                Log.d(TAG, "⚠️ WhatsApp not found, trying general share");
                // WhatsApp not installed, try with general share
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Job Opportunity: " + job.getTitle());

                context.startActivity(Intent.createChooser(shareIntent, "Share Job"));
                Log.d(TAG, "✅ General text share intent started successfully");
            }
        } catch (Exception e) {
            Toast.makeText(context, "Unable to share job", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "❌ Error sharing job: " + e.getMessage(), e);
        }
    }

    /**
     * Share text only for featured job (fallback when no image available)
     */
    private static void shareTextOnly(Context context, FeaturedJob job) {
        try {
            String shareText = createShareText(job);

            Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
            whatsappIntent.setType("text/plain");
            whatsappIntent.setPackage("com.whatsapp");
            whatsappIntent.putExtra(Intent.EXTRA_TEXT, shareText);

            try {
                context.startActivity(whatsappIntent);
                Log.d(TAG, "✅ WhatsApp text intent started successfully");
            } catch (android.content.ActivityNotFoundException ex) {
                Log.d(TAG, "⚠️ WhatsApp not found, trying general share");
                // WhatsApp not installed, try with general share
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Job Opportunity: " + job.getJobTitle());

                context.startActivity(Intent.createChooser(shareIntent, "Share Job"));
                Log.d(TAG, "✅ General text share intent started successfully");
            }
        } catch (Exception e) {
            Toast.makeText(context, "Unable to share job", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "❌ Error sharing job: " + e.getMessage(), e);
        }
    }

    /**
     * Create share text with short description
     */
    private static String createShareText(Job job) {
        String shortDescription = shortenDescription(job.getDescription());
        
        return "🔥 *Job Opportunity* 🔥\n\n" +
                "📋 *Position:* " + job.getTitle() + "\n" +
                "🏢 *Company:* " + job.getCompany() + "\n" +
                "📍 *Location:* " + job.getLocation() + "\n" +
                "💰 *Salary:* " + job.getSalary() + "\n\n" +
                "📝 " + shortDescription + "\n\n" +
                "Apply now through our Job Portal app! 📱\n" +
                "📲 Download: https://emps.co.in/";
    }

    /**
     * Create share text for featured job with short description
     */
    private static String createShareText(FeaturedJob job) {
        String shortDescription = shortenDescription(job.getDescription());
        
        return "🔥 *Featured Job Opportunity* 🔥\n\n" +
                "📋 *Position:* " + job.getJobTitle() + "\n" +
                "🏢 *Company:* " + job.getCompanyName() + "\n" +
                "📍 *Location:* " + job.getLocation() + "\n" +
                "💰 *Salary:* " + job.getSalary() + "\n\n" +
                "📝 " + shortDescription + "\n\n" +
                "Apply now through our Job Portal app! 📱\n" +
                "📲 Download: https://emps.co.in/";
    }

    /**
     * Shorten description to a maximum length
     */
    private static String shortenDescription(String description) {
        if (description == null || description.isEmpty()) {
            return "Great opportunity! Apply now.";
        }

        // Remove HTML tags if any
        String cleanDescription = description.replaceAll("<[^>]*>", "");
        
        // Trim whitespace
        cleanDescription = cleanDescription.trim();

        if (cleanDescription.length() <= MAX_DESCRIPTION_LENGTH) {
            return cleanDescription;
        }

        // Find the last space before MAX_DESCRIPTION_LENGTH
        int lastSpace = cleanDescription.lastIndexOf(' ', MAX_DESCRIPTION_LENGTH);
        if (lastSpace > 0) {
            return cleanDescription.substring(0, lastSpace) + "...";
        }

        return cleanDescription.substring(0, MAX_DESCRIPTION_LENGTH) + "...";
    }

    /**
     * Get job image URL
     */
    private static String getJobImageUrl(Job job) {
        if (job.getImage() != null && !job.getImage().isEmpty()) {
            // If image path starts with http, return as is
            if (job.getImage().startsWith("http")) {
                return job.getImage();
            }
            // Otherwise, prepend base URL
            return "https://emps.co.in/" + job.getImage();
        }
        return null;
    }

    /**
     * Get featured job image URL
     */
    private static String getFeaturedJobImageUrl(FeaturedJob job) {
        if (job.getJobImage() != null && !job.getJobImage().isEmpty()) {
            // If image path starts with http, return as is
            if (job.getJobImage().startsWith("http")) {
                return job.getJobImage();
            }
            // Otherwise, prepend base URL
            return "https://emps.co.in/" + job.getJobImage();
        }
        return null;
    }
}
