# Job Portal App Download Page

This is a responsive, modern download page for the Job Portal Android application. This page allows users to download the Android app directly from your server.

## Features

- Responsive design that works on mobile, tablet, and desktop
- Modern UI with animations and hover effects
- Clear download instructions for users
- App features showcase
- User testimonials
- Privacy policy page
- SEO-friendly meta tags

## How to Access

The download page is available at:
```
https://your-domain.com/download
```

The privacy policy is available at:
```
https://your-domain.com/privacy-policy
```

## Files Structure

- `public/download.html` - The main download page
- `public/privacy-policy.html` - The privacy policy page
- `public/jobportal.apk` - The Android application package file
- `public/assets/images/app-mockup.svg` - The app mockup image shown on the download page

## How to Update the APK File

To update the Android app file that users download:

1. Generate your new APK file from Android Studio
2. Rename it to `jobportal.apk`
3. Replace the existing file at `public/jobportal.apk` with your new one

## Customization

### Changing App Size

If your APK file size changes, update the size information in the download button:

1. Open `public/download.html`
2. Find this line:
   ```html
   <a href="jobportal.apk" class="cta-btn">Download Now (15MB)</a>
   ```
3. Update the size in parentheses to match your actual file size

### Updating App Features

To update the features shown on the download page:

1. Open `public/download.html`
2. Find the `<section class="features">` section
3. Edit the feature cards as needed

### Updating Screenshots or Mockups

To replace the app mockup image:

1. Create your new image
2. Save it as `app-mockup.png` or `app-mockup.svg`
3. Replace the existing file in `public/assets/images/`

## Analytics Integration

The download page includes a basic JavaScript event for tracking downloads. To integrate with your analytics system:

1. Open `public/download.html`
2. Find the script section at the bottom of the file
3. Update the JavaScript code to send events to your analytics platform

Example for Google Analytics:
```javascript
downloadButtons.forEach(button => {
    button.addEventListener('click', function(e) {
        // Google Analytics event
        gtag('event', 'download', {
            'event_category': 'app_download',
            'event_label': 'android_app'
        });
        
        // Show thank you message
        setTimeout(() => {
            alert('Thank you for downloading the Job Portal app! The download should start automatically.');
        }, 1000);
    });
});
```

## SEO Optimization

The download page is optimized for search engines with appropriate meta tags. If you need to update the SEO information:

1. Open `public/download.html`
2. Find the `<head>` section
3. Update the `<title>` and `<meta name="description">` tags

## Contact

If you have any questions or need help with the download page, please contact your development team. 