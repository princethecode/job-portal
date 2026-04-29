#!/bin/bash

# Storage Setup Checker for Featured Job Images
# Run this script on your production server to diagnose upload issues

echo "=========================================="
echo "Storage Setup Checker"
echo "=========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Get the script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo "Checking from directory: $SCRIPT_DIR"
echo ""

# 1. Check if storage directory exists
echo "1. Checking storage directories..."
if [ -d "$SCRIPT_DIR/storage/app/public" ]; then
    echo -e "${GREEN}✓${NC} storage/app/public exists"
else
    echo -e "${RED}✗${NC} storage/app/public does NOT exist"
    echo "   Run: mkdir -p $SCRIPT_DIR/storage/app/public"
fi

if [ -d "$SCRIPT_DIR/storage/app/public/job_images" ]; then
    echo -e "${GREEN}✓${NC} storage/app/public/job_images exists"
else
    echo -e "${RED}✗${NC} storage/app/public/job_images does NOT exist"
    echo "   Run: mkdir -p $SCRIPT_DIR/storage/app/public/job_images"
fi

if [ -d "$SCRIPT_DIR/storage/app/public/company_logos" ]; then
    echo -e "${GREEN}✓${NC} storage/app/public/company_logos exists"
else
    echo -e "${YELLOW}!${NC} storage/app/public/company_logos does NOT exist"
    echo "   Run: mkdir -p $SCRIPT_DIR/storage/app/public/company_logos"
fi

echo ""

# 2. Check storage link
echo "2. Checking storage symlink..."
if [ -L "$SCRIPT_DIR/public/storage" ]; then
    LINK_TARGET=$(readlink "$SCRIPT_DIR/public/storage")
    echo -e "${GREEN}✓${NC} Symlink exists: public/storage -> $LINK_TARGET"
    
    # Check if link is valid
    if [ -e "$SCRIPT_DIR/public/storage" ]; then
        echo -e "${GREEN}✓${NC} Symlink is valid"
    else
        echo -e "${RED}✗${NC} Symlink is broken"
        echo "   Run: cd $SCRIPT_DIR && php artisan storage:link"
    fi
else
    echo -e "${RED}✗${NC} Symlink does NOT exist"
    echo "   Run: cd $SCRIPT_DIR && php artisan storage:link"
fi

echo ""

# 3. Check permissions
echo "3. Checking permissions..."
if [ -d "$SCRIPT_DIR/storage" ]; then
    STORAGE_PERMS=$(stat -c "%a" "$SCRIPT_DIR/storage" 2>/dev/null || stat -f "%Lp" "$SCRIPT_DIR/storage" 2>/dev/null)
    STORAGE_OWNER=$(stat -c "%U:%G" "$SCRIPT_DIR/storage" 2>/dev/null || stat -f "%Su:%Sg" "$SCRIPT_DIR/storage" 2>/dev/null)
    echo "   storage/ permissions: $STORAGE_PERMS (owner: $STORAGE_OWNER)"
    
    if [ "$STORAGE_PERMS" -ge "775" ]; then
        echo -e "${GREEN}✓${NC} Permissions look good"
    else
        echo -e "${YELLOW}!${NC} Permissions might be too restrictive"
        echo "   Run: chmod -R 775 $SCRIPT_DIR/storage"
    fi
fi

if [ -d "$SCRIPT_DIR/storage/app/public/job_images" ]; then
    JOB_IMAGES_PERMS=$(stat -c "%a" "$SCRIPT_DIR/storage/app/public/job_images" 2>/dev/null || stat -f "%Lp" "$SCRIPT_DIR/storage/app/public/job_images" 2>/dev/null)
    JOB_IMAGES_OWNER=$(stat -c "%U:%G" "$SCRIPT_DIR/storage/app/public/job_images" 2>/dev/null || stat -f "%Su:%Sg" "$SCRIPT_DIR/storage/app/public/job_images" 2>/dev/null)
    echo "   job_images/ permissions: $JOB_IMAGES_PERMS (owner: $JOB_IMAGES_OWNER)"
fi

echo ""

# 4. Check disk space
echo "4. Checking disk space..."
df -h "$SCRIPT_DIR" | tail -1 | awk '{print "   Available: " $4 " (" $5 " used)"}'

USAGE=$(df "$SCRIPT_DIR" | tail -1 | awk '{print $5}' | sed 's/%//')
if [ "$USAGE" -lt 90 ]; then
    echo -e "${GREEN}✓${NC} Disk space is sufficient"
else
    echo -e "${RED}✗${NC} Disk space is low!"
fi

echo ""

# 5. Check PHP upload settings
echo "5. Checking PHP upload settings..."
if command -v php &> /dev/null; then
    UPLOAD_MAX=$(php -r "echo ini_get('upload_max_filesize');")
    POST_MAX=$(php -r "echo ini_get('post_max_size');")
    MAX_EXEC=$(php -r "echo ini_get('max_execution_time');")
    
    echo "   upload_max_filesize: $UPLOAD_MAX"
    echo "   post_max_size: $POST_MAX"
    echo "   max_execution_time: $MAX_EXEC seconds"
    
    # Convert to bytes for comparison (simplified)
    if [[ "$UPLOAD_MAX" == *"M"* ]]; then
        UPLOAD_MB=${UPLOAD_MAX%M}
        if [ "$UPLOAD_MB" -ge 5 ]; then
            echo -e "${GREEN}✓${NC} Upload size is sufficient for job images (5MB)"
        else
            echo -e "${YELLOW}!${NC} Upload size might be too small for job images"
        fi
    fi
else
    echo -e "${YELLOW}!${NC} PHP command not found in PATH"
fi

echo ""

# 6. Check Laravel logs
echo "6. Checking Laravel logs..."
if [ -f "$SCRIPT_DIR/storage/logs/laravel.log" ]; then
    LOG_SIZE=$(du -h "$SCRIPT_DIR/storage/logs/laravel.log" | cut -f1)
    echo -e "${GREEN}✓${NC} Laravel log exists (size: $LOG_SIZE)"
    echo "   Location: $SCRIPT_DIR/storage/logs/laravel.log"
    
    # Check for recent errors
    echo ""
    echo "   Recent errors (last 5):"
    grep -i "error\|exception\|failed" "$SCRIPT_DIR/storage/logs/laravel.log" | tail -5 | sed 's/^/   /'
else
    echo -e "${YELLOW}!${NC} Laravel log not found"
fi

echo ""

# 7. Check web server user
echo "7. Checking web server..."
if command -v ps &> /dev/null; then
    # Try to detect web server process
    if ps aux | grep -v grep | grep -q "apache2\|httpd"; then
        WEB_USER=$(ps aux | grep -v grep | grep "apache2\|httpd" | head -1 | awk '{print $1}')
        echo "   Web server: Apache (user: $WEB_USER)"
    elif ps aux | grep -v grep | grep -q "nginx"; then
        WEB_USER=$(ps aux | grep -v grep | grep "nginx" | head -1 | awk '{print $1}')
        echo "   Web server: Nginx (user: $WEB_USER)"
    else
        echo "   Web server: Unknown"
        WEB_USER="unknown"
    fi
    
    if [ "$WEB_USER" != "unknown" ]; then
        echo "   Storage should be owned by: $WEB_USER"
        echo "   Run: chown -R $WEB_USER:$WEB_USER $SCRIPT_DIR/storage"
    fi
fi

echo ""

# 8. Test file creation
echo "8. Testing file write..."
TEST_FILE="$SCRIPT_DIR/storage/app/public/job_images/.test_write"
if touch "$TEST_FILE" 2>/dev/null; then
    echo -e "${GREEN}✓${NC} Can write to job_images directory"
    rm "$TEST_FILE"
else
    echo -e "${RED}✗${NC} Cannot write to job_images directory"
    echo "   This is the problem! Fix permissions."
fi

echo ""

# 9. Summary
echo "=========================================="
echo "Summary & Recommendations"
echo "=========================================="
echo ""

# Count issues
ISSUES=0

if [ ! -d "$SCRIPT_DIR/storage/app/public/job_images" ]; then
    echo -e "${RED}[ACTION REQUIRED]${NC} Create job_images directory:"
    echo "   mkdir -p $SCRIPT_DIR/storage/app/public/job_images"
    ISSUES=$((ISSUES+1))
fi

if [ ! -L "$SCRIPT_DIR/public/storage" ] || [ ! -e "$SCRIPT_DIR/public/storage" ]; then
    echo -e "${RED}[ACTION REQUIRED]${NC} Create storage symlink:"
    echo "   cd $SCRIPT_DIR && php artisan storage:link"
    ISSUES=$((ISSUES+1))
fi

if [ ! -w "$SCRIPT_DIR/storage/app/public/job_images" ]; then
    echo -e "${RED}[ACTION REQUIRED]${NC} Fix permissions:"
    echo "   chmod -R 775 $SCRIPT_DIR/storage"
    echo "   chown -R www-data:www-data $SCRIPT_DIR/storage"
    ISSUES=$((ISSUES+1))
fi

if [ "$ISSUES" -eq 0 ]; then
    echo -e "${GREEN}✓ All checks passed!${NC}"
    echo ""
    echo "If uploads still fail:"
    echo "1. Check Laravel logs: tail -f $SCRIPT_DIR/storage/logs/laravel.log"
    echo "2. Try uploading via admin panel"
    echo "3. Check for SELinux/AppArmor restrictions"
else
    echo ""
    echo -e "${RED}Found $ISSUES issue(s) that need attention${NC}"
fi

echo ""
echo "=========================================="
