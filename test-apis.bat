@echo off
REM AgriDirect Delivery API Testing Script (Windows)
REM This script tests all delivery endpoints

setlocal enabledelayedexpansion

REM Configuration
set BASE_URL=%1
if "%BASE_URL%"=="" set BASE_URL=http://localhost:8001

set JWT_TOKEN=%2

REM Test counters
set TESTS_PASSED=0
set TESTS_FAILED=0

echo.
echo ============================================
echo    AgriDirect Delivery API Test Suite
echo ============================================
echo.
echo Base URL: %BASE_URL%
if "%JWT_TOKEN%"=="" (
    echo JWT Token: Not provided
) else (
    echo JWT Token: Provided
)
echo.

REM Test 1: Delivery Estimation - Short Distance
echo.
echo ============ Test 1: Delivery Estimation (5km) ============
curl -X POST "%BASE_URL%/api/delivery/estimate" ^
  -H "Content-Type: application/json" ^
  -d "{\"sourceLatitude\": 12.9716, \"sourceLongitude\": 77.5946, \"destLatitude\": 12.9352, \"destLongitude\": 77.6245, \"sourceAddress\": \"Bangalore Central\", \"destAddress\": \"Indiranagar\", \"orderAmount\": 500}"
echo.

REM Test 2: Delivery Availability
echo.
echo ============ Test 2: Check Delivery Availability ============
curl -X GET "%BASE_URL%/api/delivery/availability?latitude=12.9716&longitude=77.5946"
echo.

REM Test 3: Out of Range
echo.
echo ============ Test 3: Out of Range (40km) ============
curl -X POST "%BASE_URL%/api/delivery/estimate" ^
  -H "Content-Type: application/json" ^
  -d "{\"sourceLatitude\": 12.9716, \"sourceLongitude\": 77.5946, \"destLatitude\": 13.5604, \"destLongitude\": 79.2498, \"sourceAddress\": \"Bangalore\", \"destAddress\": \"Chikballapur\", \"orderAmount\": 500}"
echo.

REM Test 4: Invalid Coordinates
echo.
echo ============ Test 4: Invalid Coordinates ============
curl -X POST "%BASE_URL%/api/delivery/estimate" ^
  -H "Content-Type: application/json" ^
  -d "{\"sourceLatitude\": 200, \"sourceLongitude\": 400, \"destLatitude\": 12.9352, \"destLongitude\": 77.6245, \"sourceAddress\": \"Invalid\", \"destAddress\": \"Invalid\", \"orderAmount\": 500}"
echo.

REM Test 5: Missing Fields
echo.
echo ============ Test 5: Missing Required Fields ============
curl -X POST "%BASE_URL%/api/delivery/estimate" ^
  -H "Content-Type: application/json" ^
  -d "{\"sourceLatitude\": 12.9716, \"sourceLongitude\": 77.5946}"
echo.

if not "%JWT_TOKEN%"=="" (
    echo.
    echo ============ Test 6: Create Location ============
    curl -X POST "%BASE_URL%/api/locations/FARMER" ^
      -H "Authorization: Bearer %JWT_TOKEN%" ^
      -H "Content-Type: application/json" ^
      -d "{\"latitude\": 12.9716, \"longitude\": 77.5946, \"address\": \"123 Farm Road, Bangalore\", \"city\": \"Bangalore\", \"state\": \"Karnataka\", \"pincode\": \"560001\", \"isPrimary\": true}"
    echo.

    echo.
    echo ============ Test 7: Get All Locations ============
    curl -X GET "%BASE_URL%/api/locations" ^
      -H "Authorization: Bearer %JWT_TOKEN%"
    echo.
)

echo.
echo ============================================
echo Tests Complete!
echo ============================================
echo.
echo For more detailed testing, see API_TESTS.md
echo.

endlocal
