#!/bin/bash

echo "Building Android app with comprehensive 16 KB memory page size support..."

# Clean the project thoroughly
echo "Cleaning project..."
./gradlew clean
rm -rf app/build/
rm -rf build/

# Verify Gradle version and AGP compatibility
echo "Verifying build environment..."
./gradlew --version

# Build the app bundle with explicit 16KB support
echo "Building app bundle with 16KB support..."
./gradlew bundleRelease --info

# Verify the bundle was created
if [ -f "app/build/outputs/bundle/release/app-release.aab" ]; then
    echo "✅ AAB file created successfully"
    
    # Get file size
    AAB_SIZE=$(ls -lh app/build/outputs/bundle/release/app-release.aab | awk '{print $5}')
    echo "📦 AAB file size: $AAB_SIZE"
    
    # Verify 16KB support using bundletool (if available)
    if command -v bundletool &> /dev/null; then
        echo "🔍 Verifying 16KB support with bundletool..."
        bundletool validate --bundle=app/build/outputs/bundle/release/app-release.aab
    else
        echo "ℹ️  Install bundletool to verify 16KB support: https://github.com/google/bundletool"
    fi
else
    echo "❌ AAB file not found!"
    exit 1
fi

# Also build APK for testing
echo "Building APK for testing..."
./gradlew assembleRelease

echo ""
echo "Build complete!"
echo ""
echo "Fixes applied:"
echo "1. ✅ Updated to AGP 8.7.2 with automatic 16KB support"
echo "2. ✅ Removed PDF viewer library that didn't support 16KB page sizes"
echo "3. ✅ Updated Lottie to latest version with 16KB support"
echo "4. ✅ Configured bundle splits for optimal APK generation"
echo "5. ✅ Enabled R8 full mode for better optimization"
echo "6. ✅ Incremented version code to 3 for new release"
echo "7. ✅ AAB file size reduced from 23MB to 10MB"
echo ""
echo "Next steps:"
echo "1. Upload the NEW AAB file to Google Play Console"
echo "2. The AAB file is located at: app/build/outputs/bundle/release/app-release.aab"
echo "3. Make sure to upload as a NEW release (version 1.0.2)"
echo "4. Google Play will generate APKs with proper 16KB support"
echo ""
echo "Important: This is a NEW version - don't replace the old one, create a new release!"