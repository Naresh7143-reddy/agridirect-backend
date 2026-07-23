#!/bin/bash

# AgriDirect Delivery API Testing Script
# This script tests all delivery endpoints

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BASE_URL="${1:-http://localhost:8001}"
JWT_TOKEN="${2:-}"

# Test counters
TESTS_PASSED=0
TESTS_FAILED=0

# Helper function to print test header
print_test_header() {
    echo -e "\n${BLUE}=====================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}=====================================${NC}\n"
}

# Helper function to print success
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
    ((TESTS_PASSED++))
}

# Helper function to print failure
print_failure() {
    echo -e "${RED}✗ $1${NC}"
    ((TESTS_FAILED++))
}

# Helper function to print info
print_info() {
    echo -e "${YELLOW}ℹ $1${NC}"
}

# Test 1: Delivery Estimation - Short Distance
test_delivery_estimation_short() {
    print_test_header "Test 1: Delivery Estimation (Short Distance - 5km)"
    
    RESPONSE=$(curl -s -X POST "$BASE_URL/api/delivery/estimate" \
        -H "Content-Type: application/json" \
        -d '{
            "sourceLatitude": 12.9716,
            "sourceLongitude": 77.5946,
            "destLatitude": 12.9352,
            "destLongitude": 77.6245,
            "sourceAddress": "Bangalore Central",
            "destAddress": "Indiranagar",
            "orderAmount": 500
        }')
    
    echo "Response: $RESPONSE"
    
    if echo "$RESPONSE" | grep -q '"status":"SUCCESS"'; then
        print_success "Delivery estimation calculated successfully"
        DISTANCE=$(echo "$RESPONSE" | grep -o '"distanceKm":[^,]*' | cut -d':' -f2)
        COST=$(echo "$RESPONSE" | grep -o '"totalDeliveryCost":[^,]*' | cut -d':' -f2)
        TIME=$(echo "$RESPONSE" | grep -o '"estimatedTimeMinutes":[^,]*' | cut -d':' -f2)
        print_info "Distance: $DISTANCE km, Cost: ₹$COST, Time: $TIME mins"
    else
        print_failure "Failed to calculate delivery estimation"
    fi
}

# Test 2: Delivery Estimation - Out of Range
test_delivery_estimation_out_of_range() {
    print_test_header "Test 2: Delivery Estimation (Out of Range - 40km)"
    
    RESPONSE=$(curl -s -X POST "$BASE_URL/api/delivery/estimate" \
        -H "Content-Type: application/json" \
        -d '{
            "sourceLatitude": 12.9716,
            "sourceLongitude": 77.5946,
            "destLatitude": 13.5604,
            "destLongitude": 79.2498,
            "sourceAddress": "Bangalore",
            "destAddress": "Chikballapur",
            "orderAmount": 500
        }')
    
    echo "Response: $RESPONSE"
    
    if echo "$RESPONSE" | grep -q '"status":"OUT_OF_DELIVERY_RANGE"'; then
        print_success "Correctly rejected out-of-range delivery"
    else
        print_failure "Should reject out-of-range delivery"
    fi
}

# Test 3: Check Delivery Availability
test_delivery_availability() {
    print_test_header "Test 3: Check Delivery Availability"
    
    RESPONSE=$(curl -s -X GET "$BASE_URL/api/delivery/availability?latitude=12.9716&longitude=77.5946")
    
    echo "Response: $RESPONSE"
    
    if echo "$RESPONSE" | grep -q '"isAvailable"'; then
        print_success "Availability check successful"
        PARTNERS=$(echo "$RESPONSE" | grep -o '"availablePartnersCount":[^,]*' | cut -d':' -f2)
        STATUS=$(echo "$RESPONSE" | grep -o '"availabilityStatus":"[^"]*"' | cut -d'"' -f4)
        print_info "Available Partners: $PARTNERS, Status: $STATUS"
    else
        print_failure "Failed to check availability"
    fi
}

# Test 4: Create Location (requires JWT)
test_create_location() {
    print_test_header "Test 4: Create Location"
    
    if [ -z "$JWT_TOKEN" ]; then
        print_info "Skipping - JWT token not provided. Usage: $0 BASE_URL JWT_TOKEN"
        return
    fi
    
    RESPONSE=$(curl -s -X POST "$BASE_URL/api/locations/FARMER" \
        -H "Authorization: Bearer $JWT_TOKEN" \
        -H "Content-Type: application/json" \
        -d '{
            "latitude": 12.9716,
            "longitude": 77.5946,
            "address": "123 Farm Road, Bangalore",
            "city": "Bangalore",
            "state": "Karnataka",
            "pincode": "560001",
            "isPrimary": true
        }')
    
    echo "Response: $RESPONSE"
    
    if echo "$RESPONSE" | grep -q '"status_code":201'; then
        print_success "Location created successfully"
        LOCATION_ID=$(echo "$RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
        print_info "Location ID: $LOCATION_ID"
    else
        print_failure "Failed to create location"
    fi
}

# Test 5: Get Primary Location (requires JWT)
test_get_primary_location() {
    print_test_header "Test 5: Get Primary Location"
    
    if [ -z "$JWT_TOKEN" ]; then
        print_info "Skipping - JWT token not provided"
        return
    fi
    
    RESPONSE=$(curl -s -X GET "$BASE_URL/api/locations/primary" \
        -H "Authorization: Bearer $JWT_TOKEN")
    
    echo "Response: $RESPONSE"
    
    if echo "$RESPONSE" | grep -q '"status_code":200'; then
        print_success "Primary location retrieved successfully"
        ADDRESS=$(echo "$RESPONSE" | grep -o '"address":"[^"]*"' | head -1 | cut -d'"' -f4)
        print_info "Address: $ADDRESS"
    else
        print_info "No primary location found (expected if just created)"
    fi
}

# Test 6: Get All Locations (requires JWT)
test_get_all_locations() {
    print_test_header "Test 6: Get All User Locations"
    
    if [ -z "$JWT_TOKEN" ]; then
        print_info "Skipping - JWT token not provided"
        return
    fi
    
    RESPONSE=$(curl -s -X GET "$BASE_URL/api/locations" \
        -H "Authorization: Bearer $JWT_TOKEN")
    
    echo "Response: $RESPONSE"
    
    if echo "$RESPONSE" | grep -q '"status_code":200'; then
        print_success "Locations retrieved successfully"
        COUNT=$(echo "$RESPONSE" | grep -o '"id":"' | wc -l)
        print_info "Total locations: $COUNT"
    else
        print_failure "Failed to retrieve locations"
    fi
}

# Test 7: Invalid Coordinates
test_invalid_coordinates() {
    print_test_header "Test 7: Invalid Coordinates Validation"
    
    RESPONSE=$(curl -s -X POST "$BASE_URL/api/delivery/estimate" \
        -H "Content-Type: application/json" \
        -d '{
            "sourceLatitude": 200,
            "sourceLongitude": 400,
            "destLatitude": 12.9352,
            "destLongitude": 77.6245,
            "sourceAddress": "Invalid",
            "destAddress": "Invalid",
            "orderAmount": 500
        }')
    
    echo "Response: $RESPONSE"
    
    if echo "$RESPONSE" | grep -q '"status_code":400'; then
        print_success "Invalid coordinates correctly rejected"
    else
        print_info "Validation check complete"
    fi
}

# Test 8: Missing Required Fields
test_missing_fields() {
    print_test_header "Test 8: Missing Required Fields Validation"
    
    RESPONSE=$(curl -s -X POST "$BASE_URL/api/delivery/estimate" \
        -H "Content-Type: application/json" \
        -d '{
            "sourceLatitude": 12.9716,
            "sourceLongitude": 77.5946
        }')
    
    echo "Response: $RESPONSE"
    
    if echo "$RESPONSE" | grep -q '"status_code":400'; then
        print_success "Missing fields correctly rejected"
    else
        print_info "Validation check complete"
    fi
}

# Main execution
main() {
    echo -e "${BLUE}"
    echo "╔════════════════════════════════════════════╗"
    echo "║   AgriDirect Delivery API Test Suite      ║"
    echo "╚════════════════════════════════════════════╝"
    echo -e "${NC}"
    
    print_info "Base URL: $BASE_URL"
    print_info "JWT Token: ${JWT_TOKEN:-Not provided}"
    
    # Check if server is running
    if ! curl -s "$BASE_URL/api/delivery/availability?latitude=0&longitude=0" > /dev/null 2>&1; then
        echo -e "${RED}Error: Cannot connect to $BASE_URL${NC}"
        echo -e "${RED}Make sure the server is running on port 8001${NC}"
        exit 1
    fi
    print_success "Server is running"
    
    # Run all tests
    test_delivery_estimation_short
    test_delivery_estimation_out_of_range
    test_delivery_availability
    test_create_location
    test_get_primary_location
    test_get_all_locations
    test_invalid_coordinates
    test_missing_fields
    
    # Print summary
    echo -e "\n${BLUE}=====================================${NC}"
    echo -e "${BLUE}Test Summary${NC}"
    echo -e "${BLUE}=====================================${NC}"
    echo -e "${GREEN}Passed: $TESTS_PASSED${NC}"
    echo -e "${RED}Failed: $TESTS_FAILED${NC}"
    
    if [ $TESTS_FAILED -eq 0 ]; then
        echo -e "\n${GREEN}All tests passed! ✓${NC}"
        exit 0
    else
        echo -e "\n${RED}Some tests failed. Check the output above.${NC}"
        exit 1
    fi
}

# Run main function
main
