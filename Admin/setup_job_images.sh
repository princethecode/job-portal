#!/bin/bash

# Setup script for job images feature
# This script sets up the necessary storage configuration for job images

echo "=========================================="
echo "Featured Jobs Image Upload Setup"
echo "=========================================="
echo ""

# Check if we're in the Admin directory
if [ ! -f "artisan" ]; then
    echo "❌ Error: This script must be run from the Admin directory"
    echo "   Usage: cd Admin && bash setup_job_images.sh"
    exit 1
fi

echo "✓ Running from Admin directory"
echo ""

# Run migration
echo "📦 Running database migration..."
php artisan migrate --force
if [ $? -eq 0 ]; then
    echo "✓ Migration completed successfully"
else
    echo "❌ Migration failed"
    exit 1
fi
echo ""

# Create storage link
echo "🔗 Creating storage symbolic link..."
php artisan storage:link
if [ $? -eq 0 ]; then
    echo "✓ Storage link created successfully"
else
    echo "⚠️  Storage link may already exist (this is okay)"
fi
echo ""

# Set permissions
echo "🔐 Setting storage permissions..."
chmod -R 775 storage
chmod -R 775 bootstrap/cache
echo "✓ Permissions set"
echo ""

# Create job_images directory if it doesn't exist
echo "📁 Creating job_images directory..."
mkdir -p storage/app/public/job_images
chmod 775 storage/app/public/job_images
echo "✓ Directory created"
echo ""

# Check if .htaccess exists in public directory
if [ -f "public/.htaccess" ]; then
    echo "✓ .htaccess file exists"
else
    echo "⚠️  Warning: .htaccess file not found in public directory"
fi
echo ""

echo "=========================================="
echo "✅ Setup completed successfully!"
echo "=========================================="
echo ""
echo "Next steps:"
echo "1. Test creating a featured job with an image via the admin panel"
echo "2. Verify the image is accessible at: https://emps.co.in/storage/job_images/[filename]"
echo "3. Test the Android app to ensure images display correctly"
echo ""
echo "Storage structure:"
echo "  storage/app/public/job_images/     - Job images stored here"
echo "  public/storage/job_images/         - Accessible via web (symlink)"
echo ""
