#!/bin/bash

# Production Readiness Verification Script
# Runs all checks to ensure the application is production-ready

echo "=========================================="
echo "Production Readiness Verification"
echo "=========================================="
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

CHECKS_PASSED=0
CHECKS_FAILED=0

print_check() {
    echo -e "${BLUE}[CHECK]${NC} $1"
}

print_pass() {
    echo -e "${GREEN}✓ PASS${NC} $1"
    ((CHECKS_PASSED++))
}

print_fail() {
    echo -e "${RED}✗ FAIL${NC} $1"
    ((CHECKS_FAILED++))
}

print_info() {
    echo -e "${YELLOW}ℹ INFO${NC} $1"
}

# Check 1: Gradle build
print_check "Running Gradle build..."
./gradlew clean build -x lint > /dev/null 2>&1
if [ $? -eq 0 ]; then
    print_pass "Gradle build successful"
else
    print_fail "Gradle build failed"
fi

# Check 2: Unit tests
print_check "Running unit tests..."
./gradlew test --no-daemon > /dev/null 2>&1
if [ $? -eq 0 ]; then
    # Count tests
    TOTAL_TESTS=$(grep -h "tests=" app/build/test-results/testDebugUnitTest/*.xml 2>/dev/null | awk -F'tests="' '{print $2}' | awk -F'"' '{sum+=$1} END {print sum}')
    if [ ! -z "$TOTAL_TESTS" ]; then
        print_pass "All $TOTAL_TESTS unit tests passed"
    else
        print_pass "Unit tests passed"
    fi
else
    print_fail "Unit tests failed"
fi

# Check 3: Code compilation
print_check "Checking code compilation..."
./gradlew compileDebugJavaWithJavac compileReleaseJavaWithJavac > /dev/null 2>&1
if [ $? -eq 0 ]; then
    print_pass "Code compiles successfully"
else
    print_fail "Code compilation failed"
fi

# Check 4: Check for critical files
print_check "Verifying critical files exist..."
CRITICAL_FILES=(
    "app/src/main/AndroidManifest.xml"
    "app/src/main/java/com/emps/abroadjobs/MainActivity.java"
    "app/src/main/java/com/emps/abroadjobs/utils/SessionManager.java"
    "app/src/main/java/com/emps/abroadjobs/network/ApiClient.java"
    "app/build.gradle"
)

ALL_FILES_EXIST=true
for file in "${CRITICAL_FILES[@]}"; do
    if [ ! -f "$file" ]; then
        ALL_FILES_EXIST=false
        print_info "Missing: $file"
    fi
done

if [ "$ALL_FILES_EXIST" = true ]; then
    print_pass "All critical files exist"
else
    print_fail "Some critical files are missing"
fi

# Check 5: Test files exist
print_check "Verifying test files exist..."
TEST_FILES=(
    "app/src/test/java/com/emps/abroadjobs/SessionManagerTest.java"
    "app/src/test/java/com/emps/abroadjobs/ApiClientTest.java"
    "app/src/test/java/com/emps/abroadjobs/UserModelTest.java"
    "app/src/test/java/com/emps/abroadjobs/ValidationTest.java"
    "app/src/test/java/com/emps/abroadjobs/ApplicationFlowTest.java"
)

ALL_TESTS_EXIST=true
for file in "${TEST_FILES[@]}"; do
    if [ ! -f "$file" ]; then
        ALL_TESTS_EXIST=false
        print_info "Missing: $file"
    fi
done

if [ "$ALL_TESTS_EXIST" = true ]; then
    print_pass "All test files exist"
else
    print_fail "Some test files are missing"
fi

# Check 6: Dependencies resolved
print_check "Checking dependency resolution..."
./gradlew dependencies > /dev/null 2>&1
if [ $? -eq 0 ]; then
    print_pass "All dependencies resolved"
else
    print_fail "Dependency resolution failed"
fi

# Check 7: APK can be built
print_check "Building debug APK..."
./gradlew assembleDebug > /dev/null 2>&1
if [ $? -eq 0 ]; then
    if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
        APK_SIZE=$(du -h app/build/outputs/apk/debug/app-debug.apk | cut -f1)
        print_pass "Debug APK built successfully (Size: $APK_SIZE)"
    else
        print_pass "Debug APK built successfully"
    fi
else
    print_fail "Debug APK build failed"
fi

# Summary
echo ""
echo "=========================================="
echo "Verification Summary"
echo "=========================================="
echo -e "Checks Passed: ${GREEN}$CHECKS_PASSED${NC}"
echo -e "Checks Failed: ${RED}$CHECKS_FAILED${NC}"
echo ""

if [ $CHECKS_FAILED -eq 0 ]; then
    echo -e "${GREEN}✓ APPLICATION IS PRODUCTION READY${NC}"
    echo ""
    print_info "All checks passed successfully!"
    print_info "The application is ready for deployment."
    exit 0
else
    echo -e "${RED}✗ APPLICATION IS NOT PRODUCTION READY${NC}"
    echo ""
    print_info "Please fix the failed checks before deployment."
    exit 1
fi
