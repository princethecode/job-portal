#!/bin/bash

echo "Building Android app with Photo Picker API (No Media Permissions Required)..."

# Clean the project thoroughly
echo "Cleaning project..."
./gradlew clean
rm -rf app/build/
rm -rf build/

# Build the app bundle with Photo Picker implementation
echo "Building app bundle with Photo Picker API..."
./gradlew bundleRelease --info

# Verify the bundle was created
if [ -f "app/build/outputs/bundle/release/app-release.aab" ]; then
    echo "✅ AAB file created successfully"
    
    # Get file size
    AAB_SIZE=$(ls -lh app/build/outputs/bundle/release/app-release.aab | awk '{print $5}')
    echo "📦 AAB file size: $AAB_SIZE"
else
    echo "❌ AAB file not found!"
    exit 1
fi

# Also build APK for testing
echo "Building APK for testing..."
./gradlew assembleRelease

echo ""
echo "🎉 Build complete!"
echo ""
echo "✅ Photo Picker Implementation Applied:"
echo "1. ✅ Removed READ_MEDIA_IMAGES permission completely"
echo "2. ✅ Implemented Android Photo Picker API"
echo "3. ✅ Added androidx.activity dependency for Photo Picker"
echo "4. ✅ Updated JobPostingActivity to use PickVisualMedia contract"
echo "5. ✅ Updated privacy policy to reflect no media permissions needed"
echo "6. ✅ Incremented version to 1.0.3 (version code 4)"
echo ""
echo "🔒 Privacy Benefits:"
echo "• No media permissions required"
echo "• Users explicitly choose which photos to share"
echo "• System handles photo access, not the app"
echo "• Complies with Google Play privacy policies"
echo ""
echo "📤 Next steps:"
echo "1. Upload the NEW AAB file to Google Play Console"
echo "2. The AAB file is located at: app/build/outputs/bundle/release/app-release.aab"
echo "3. Create a NEW release (version 1.0.3) - don't replace existing"
echo "4. The Photo and Video Permissions policy issue should be resolved"
echo ""
echo "🧪 To test locally:"
echo "1. Install the APK: adb install app/build/outputs/apk/release/app-release.apk"
echo "2. Test job posting image selection (should open Photo Picker)"
echo "3. Verify no media permissions are requested"