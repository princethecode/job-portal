# App Version Update Feature Setup

This document explains how to set up and use the forced app update feature in your Job Portal application.

## Overview

The app update feature allows you to:
- Force users to update to the latest version
- Show optional update prompts
- Set minimum required versions
- Customize update messages
- Manage different versions for Android/iOS

## Backend Setup (Laravel)

### 1. Run Database Migration

```bash
cd Admin
php artisan migrate
```

### 2. Seed Initial Data (Optional)

```bash
php artisan db:seed --class=AppVersionSeeder
```

### 3. Access Admin Panel

Navigate to `/admin/app-versions` in your Laravel admin panel to manage app versions.

## Android Setup

### 1. Update App Version

In `android/app/build.gradle`, update your app version:

```gradle
defaultConfig {
    versionCode 2        // Increment this for each release
    versionName "1.0.1"  // Update version name
}
```

### 2. Check for Updates

The app automatically checks for updates on startup. You can also manually trigger checks:

```kotlin
// In any Activity
checkForAppUpdates()

// Or get the update manager
val updateManager = getAppUpdateManager()
updateManager.checkForUpdates { isForceUpdate ->
    // Handle update result
}
```

## How It Works

### 1. Version Check Flow

1. App sends current version info to `/api/check-version`
2. Server compares with latest version in database
3. Server responds with update requirements
4. App shows update dialog if needed

### 2. API Request Format

```json
{
    "platform": "android",
    "version_code": 1,
    "version_name": "1.0.0"
}
```

### 3. API Response Format

```json
{
    "success": true,
    "update_required": true,
    "force_update": true,
    "is_latest": false,
    "current_version": {
        "name": "1.0.0",
        "code": 1
    },
    "latest_version": {
        "name": "1.0.1",
        "code": 2,
        "download_url": "https://example.com/app.apk",
        "update_message": "Please update to continue using the app."
    }
}
```

## Admin Panel Usage

### 1. Create New Version

1. Go to Admin Panel → App Versions
2. Click "Add New Version"
3. Fill in version details:
   - **Platform**: Android/iOS
   - **Version Name**: User-friendly version (e.g., "1.0.1")
   - **Version Code**: Numeric version for comparison
   - **Minimum Version**: Optional minimum required version
   - **Force Update**: Check to make update mandatory
   - **Update Message**: Custom message for users
   - **Download URL**: Link to APK/App Store

### 2. Version Management

- **Active**: Only active versions are considered for updates
- **Force Update**: Users cannot skip the update
- **Minimum Version**: Users below this version must update

## Update Types

### 1. Force Update
- Users cannot skip the update
- App becomes unusable until updated
- Back button is disabled on update screen

### 2. Optional Update
- Users can skip the update
- App continues to work normally
- Update prompt can be dismissed

### 3. Minimum Version Update
- Users below minimum version must update
- Users above minimum but below latest can skip

## Testing

### 1. Test Force Update

1. Set current app version to 1
2. Create version 2 with `force_update = true`
3. Launch app - should show mandatory update screen

### 2. Test Optional Update

1. Set current app version to 1
2. Create version 2 with `force_update = false`
3. Launch app - should show skippable update dialog

### 3. Test API Manually

```bash
curl -X POST https://your-domain.com/api/check-version \
  -H "Content-Type: application/json" \
  -d '{
    "platform": "android",
    "version_code": 1,
    "version_name": "1.0.0"
  }'
```

## Deployment Checklist

### Before Release

1. ✅ Update `versionCode` and `versionName` in build.gradle
2. ✅ Build and test APK
3. ✅ Upload APK to server
4. ✅ Create new version entry in admin panel
5. ✅ Set correct download URL
6. ✅ Test update flow with previous version

### After Release

1. ✅ Monitor update adoption
2. ✅ Check for any update-related issues
3. ✅ Update minimum version if needed

## Troubleshooting

### Common Issues

1. **Update not showing**: Check if version is active and version codes are correct
2. **Download not working**: Verify download URL is accessible
3. **API errors**: Check server logs and network connectivity

### Debug Tips

1. Check Android logs for update-related messages
2. Test API endpoint manually
3. Verify version codes in database vs app
4. Ensure download URL is publicly accessible

## Security Considerations

1. Use HTTPS for download URLs
2. Implement APK signature verification
3. Consider using Google Play App Signing
4. Monitor for malicious update attempts

## Future Enhancements

1. **Gradual Rollout**: Release updates to percentage of users
2. **A/B Testing**: Test different update messages
3. **Analytics**: Track update adoption rates
4. **In-App Updates**: Use Google Play In-App Updates API
5. **Auto-Download**: Download updates in background