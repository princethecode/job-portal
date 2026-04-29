#!/bin/bash

# Android Application Test Runner Script
# This script runs all tests and generates reports

echo "=========================================="
echo "Android Application Test Suite"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ $1${NC}"
}

# Clean previous build artifacts
print_info "Cleaning previous build artifacts..."
./gradlew clean > /dev/null 2>&1
print_success "Clean completed"

echo ""
print_info "Running Unit Tests..."
echo "=========================================="

# Run unit tests
./gradlew test --no-daemon

if [ $? -eq 0 ]; then
    print_success "All unit tests passed!"
else
    print_error "Some unit tests failed!"
    echo ""
    print_info "Check the test report at:"
    echo "  android/app/build/reports/tests/testDebugUnitTest/index.html"
    exit 1
fi

echo ""
echo "=========================================="
print_info "Test Summary"
echo "=========================================="

# Count test results
if [ -f "app/build/test-results/testDebugUnitTest/TEST-*.xml" ]; then
    TOTAL_TESTS=$(grep -r "tests=" app/build/test-results/testDebugUnitTest/*.xml | head -1 | sed 's/.*tests="\([0-9]*\)".*/\1/')
    FAILED_TESTS=$(grep -r "failures=" app/build/test-results/testDebugUnitTest/*.xml | head -1 | sed 's/.*failures="\([0-9]*\)".*/\1/')
    SKIPPED_TESTS=$(grep -r "skipped=" app/build/test-results/testDebugUnitTest/*.xml | head -1 | sed 's/.*skipped="\([0-9]*\)".*/\1/')
    
    echo "Total Tests: $TOTAL_TESTS"
    echo "Passed: $((TOTAL_TESTS - FAILED_TESTS - SKIPPED_TESTS))"
    echo "Failed: $FAILED_TESTS"
    echo "Skipped: $SKIPPED_TESTS"
fi

echo ""
print_success "Test execution completed successfully!"
echo ""
print_info "Test Reports:"
echo "  HTML Report: android/app/build/reports/tests/testDebugUnitTest/index.html"
echo "  XML Results: android/app/build/test-results/testDebugUnitTest/"
echo ""

# Open test report in browser (macOS)
if [[ "$OSTYPE" == "darwin"* ]]; then
    print_info "Opening test report in browser..."
    open app/build/reports/tests/testDebugUnitTest/index.html
fi

exit 0
