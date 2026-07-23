# AgriDirect Backend - Deployment Verification Guide

Complete verification checklist after deploying to Render.

---

## Pre-Deployment Checklist

### GitHub
- [x] Code pushed to main branch
- [x] 35 files committed
- [x] All new delivery system files included
- [x] Tests and documentation included
- [x] .env.example created

### Local Testing
- [ ] Maven build successful: `mvn clean package -DskipTests`
- [ ] No compilation errors
- [ ] Tests pass locally: `mvn test`
- [ ] Application starts on port 8001: `mvn spring-boot:run`

### Environment Variables
- [ ] All 28 environment variables identified
- [ ] Sensitive values stored securely
- [ ] Database connection tested
- [ ] API keys validated

---

## Post-Deployment Verification

### Step 1: Check Service Status

**In Render Dashboard:**
1. Navigate to your service
2. Check **Status** indicator
   - ✅ Should show: **Live**
   - ❌ Should NOT show: **Build Failed** or **Error**

**Check Build Logs:**
```
Logs Tab → Look for:
- "Spring Boot application started"
- "Listening on port 10000" (or assigned port)
- No errors or warnings
```

### Step 2: Test Basic Connectivity

```bash
# Replace with your Render URL
export BASE_URL="https://agridirect-backend-xxxx.onrender.com"

# Test if server is responsive
curl -I $BASE_URL/api/delivery/availability?latitude=0&longitude=0
```

**Expected Response:**
```
HTTP/1.1 200 OK
```

### Step 3: Test Delivery Estimation (Core Feature)

```bash
curl -X POST $BASE_URL/api/delivery/estimate \
  -H "Content-Type: application/json" \
  -d '{
    "sourceLatitude": 12.9716,
    "sourceLongitude": 77.5946,
    "destLatitude": 12.9352,
    "destLongitude": 77.6245,
    "sourceAddress": "Bangalore Central",
    "destAddress": "Indiranagar",
    "orderAmount": 500
  }'
```

**Expected Response:**
```json
{
  "status_code": 200,
  "message": "Delivery estimate calculated successfully",
  "data": {
    "distanceKm": 5.4,
    "estimatedTimeMinutes": 20,
    "baseCost": 50.0,
    "distanceCost": 43.2,
    "timeCost": 20.0,
    "totalDeliveryCost": 113.2,
    "platformFee": 5.66,
    "grandTotal": 118.86,
    "estimatedDeliveryTime": "20 mins",
    "estimatedDeliveryRange": "15-25 mins",
    "status": "SUCCESS"
  }
}
```

✅ **Criteria for Success:**
- HTTP Status: 200
- `status` field: "SUCCESS"
- `distanceKm` is positive number
- `estimatedTimeMinutes` is positive number
- `totalDeliveryCost` > 0

### Step 4: Test Delivery Availability

```bash
curl -X GET "$BASE_URL/api/delivery/availability?latitude=12.9716&longitude=77.5946"
```

**Expected Response:**
```json
{
  "status_code": 200,
  "message": "Delivery availability checked",
  "data": {
    "isAvailable": false,
    "availablePartnersCount": 0,
    "avgRating": 0.0,
    "estimatedWaitMinutes": 30,
    "availabilityStatus": "LOW"
  }
}
```

✅ **Criteria for Success:**
- HTTP Status: 200
- Response contains `isAvailable` field
- Response contains `availablePartnersCount`
- Response contains `availabilityStatus`

### Step 5: Test Out-of-Range Validation

```bash
curl -X POST $BASE_URL/api/delivery/estimate \
  -H "Content-Type: application/json" \
  -d '{
    "sourceLatitude": 12.9716,
    "sourceLongitude": 77.5946,
    "destLatitude": 13.5604,
    "destLongitude": 79.2498,
    "sourceAddress": "Bangalore",
    "destAddress": "Chikballapur (40km away)",
    "orderAmount": 500
  }'
```

**Expected Response:**
```json
{
  "status_code": 400,
  "message": "Delivery location is outside service area",
  "data": {
    "status": "OUT_OF_DELIVERY_RANGE"
  }
}
```

✅ **Criteria for Success:**
- HTTP Status: 400
- `status` field: "OUT_OF_DELIVERY_RANGE"
- Correctly rejects distant locations

### Step 6: Test Invalid Input Handling

```bash
curl -X POST $BASE_URL/api/delivery/estimate \
  -H "Content-Type: application/json" \
  -d '{
    "sourceLatitude": 200,
    "sourceLongitude": 400,
    "destLatitude": 12.9352,
    "destLongitude": 77.6245,
    "sourceAddress": "Invalid",
    "destAddress": "Invalid",
    "orderAmount": 500
  }'
```

**Expected Response:**
```json
{
  "status_code": 400,
  "message": "Invalid input..."
}
```

✅ **Criteria for Success:**
- HTTP Status: 400
- Validation errors are reported
- Server doesn't crash

### Step 7: Check Google Maps Integration

**In Render Logs:**
```
Look for:
- "GeoApiContext initialized successfully" OR
- No Google Maps API errors

Error examples to watch for:
- "java.lang.IllegalArgumentException: api key is required"
- "com.google.maps.errors.ApiException"
```

**If Error Occurs:**
1. Verify `GOOGLE_MAPS_API_KEY` is set in Render
2. Check API key is correct
3. Ensure Distance Matrix API is enabled in Google Cloud

---

## Database Verification

### Test Database Connection

**Check Logs for:**
```
Hibernate: creating tables...
Spring Data Connection: Success
```

**Or via Direct Connection:**
```bash
# If you have psql installed
psql -h your-db-host -U postgres -d agridirect -c "SELECT COUNT(*) FROM users;"
```

### Verify Tables Created

```sql
-- Connect to your database and run:
\dt  -- List all tables

-- Should see:
- users
- locations
- delivery_partners
- delivery_tracking
- payments
- orders
- products
- categories
- farmer_profiles
- buyer_profiles
- delivery_profiles
```

---

## Environment Variables Verification

### Check All Variables Are Loaded

**In Render Logs, look for:**
```
Application context initialized
Database URL: jdbc:postgresql://...
JWT configured: true
Maps API Key: configured
Delivery configuration loaded:
  - Base Cost: 50
  - Max Radius: 25km
  - Per KM Cost: 8
```

**Missing Variable Indicators:**
```
❌ "Could not resolve placeholder 'VAR_NAME'"
❌ "java.lang.IllegalArgumentException"
❌ "NullPointerException"
```

### Manually Verify in Render

1. Go to **Environment** tab
2. Verify these are set:
   - `GOOGLE_MAPS_API_KEY` ✅
   - `DB_URL` ✅
   - `JWT_SECRET` ✅
   - `DELIVERY_BASE_COST` ✅
   - All others from checklist ✅

---

## Performance Testing

### Response Time Test

```bash
# Measure response time
time curl -X POST $BASE_URL/api/delivery/estimate \
  -H "Content-Type: application/json" \
  -d '{"sourceLatitude": 12.9716, ...}'
```

**Expected Times:**
- First request: 2-5 seconds (cold start)
- Subsequent requests: 500-1000ms

### Concurrent Requests Test

```bash
# Test 10 concurrent requests
for i in {1..10}; do
  curl -X GET "$BASE_URL/api/delivery/availability?latitude=12.9716&longitude=77.5946" &
done
wait

echo "All requests completed"
```

**Expected Result:**
- All 10 requests succeed
- No errors in logs
- Response times consistent

---

## Error Handling Verification

### Test Error Scenarios

**1. Missing Required Field**
```bash
curl -X POST $BASE_URL/api/delivery/estimate \
  -H "Content-Type: application/json" \
  -d '{"sourceLatitude": 12.9716}'
```
Expected: 400 Bad Request ✅

**2. Invalid JSON**
```bash
curl -X POST $BASE_URL/api/delivery/estimate \
  -H "Content-Type: application/json" \
  -d '{invalid json}'
```
Expected: 400 Bad Request ✅

**3. Non-existent Endpoint**
```bash
curl -X GET $BASE_URL/api/delivery/nonexistent
```
Expected: 404 Not Found ✅

**4. Wrong HTTP Method**
```bash
curl -X GET $BASE_URL/api/delivery/estimate
```
Expected: 405 Method Not Allowed ✅

---

## Security Verification

### Check HTTPS

```bash
# Should be HTTPS
curl -I $BASE_URL/api/delivery/estimate

# Expected:
# HTTP/1.1 200 OK
# Server: (Render/nginx)
```

✅ Render provides automatic HTTPS

### Check Headers

```bash
curl -I $BASE_URL/api/delivery/availability?latitude=0&longitude=0

# Look for:
# X-Frame-Options: SAMEORIGIN
# X-Content-Type-Options: nosniff
# Strict-Transport-Security: (if configured)
```

### Test JWT Authentication (if enabled)

```bash
# Without token (should fail if protected)
curl -X GET $BASE_URL/api/locations \
  -H "Authorization: Bearer invalid_token"

# Expected: 401 Unauthorized
```

---

## Monitoring & Logs

### Access Real-time Logs

**In Render Dashboard:**
1. Select your service
2. Click **Logs** tab
3. Monitor for errors

**Common Log Entries:**
```
✅ Good:
- "Started AgridirectApplication in X seconds"
- "POST /api/delivery/estimate"
- "HTTP response: 200"

❌ Bad:
- "Exception occurred"
- "Connection refused"
- "OutOfMemoryError"
- "StackOverflowError"
```

### Enable Debug Logging (Optional)

Add to environment if needed:
```
LOGGING_LEVEL_COM_AGRIDIRECT=DEBUG
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK=DEBUG
```

---

## Full Test Suite Summary

| Test | Endpoint | Expected | Status |
|------|----------|----------|--------|
| Server Health | GET /api/delivery/availability | 200 | ⏳ |
| Estimation | POST /api/delivery/estimate | 200 + data | ⏳ |
| Out of Range | POST /api/delivery/estimate (40km) | 400 + error | ⏳ |
| Availability | GET /api/delivery/availability | 200 + status | ⏳ |
| Invalid Input | POST /api/delivery/estimate (invalid) | 400 | ⏳ |
| Missing Fields | POST /api/delivery/estimate (partial) | 400 | ⏳ |
| Response Time | Any endpoint | < 2s cold | ⏳ |
| Concurrent Req | 10 parallel requests | All succeed | ⏳ |

---

## Troubleshooting If Tests Fail

### Issue: Server Returns 502 Bad Gateway

**Possible Causes:**
1. Application crashed during startup
2. Database connection failed
3. Out of memory

**Solutions:**
```
1. Check logs for errors
2. Verify all environment variables
3. Restart service: Render Dashboard → Restart
4. Check database is accessible
5. Increase free tier or upgrade plan
```

### Issue: Google Maps API Errors

**Possible Causes:**
1. Invalid API key
2. API not enabled in Google Cloud
3. Quota exceeded
4. Wrong key format

**Solutions:**
```
1. Verify GOOGLE_MAPS_API_KEY in Render
2. Enable APIs in Google Cloud Console:
   - Distance Matrix API
   - Directions API
3. Check quota usage
4. Regenerate key if needed
```

### Issue: Database Connection Errors

**Possible Causes:**
1. Wrong DB_URL format
2. Credentials incorrect
3. Network/firewall issue
4. Database not running

**Solutions:**
```
1. Verify format: jdbc:postgresql://host:port/db
2. Test credentials locally
3. Check Supabase/Database firewall
4. Verify database is running
```

### Issue: Slow Response Times

**Possible Causes:**
1. Cold start (first deployment)
2. Database queries slow
3. External API calls slow (Google Maps)
4. Free tier resource limits

**Solutions:**
```
1. Wait for warm up (5-10 min)
2. Check database performance
3. Monitor Google Maps quota
4. Consider upgrade for production
```

---

## Success Criteria

Your deployment is **SUCCESSFUL** if:

✅ Service status shows **Live**
✅ Delivery estimation endpoint returns **200** with valid data
✅ Availability check endpoint returns **200**
✅ Invalid inputs correctly return **400**
✅ Logs show **no errors**
✅ Response times are **< 2 seconds**
✅ Database tables are **created**
✅ Environment variables are **all set**
✅ Google Maps API **working**
✅ Concurrent requests **succeed**

---

## Next Steps After Verification

1. **Monitor in Production**
   - Set up error tracking (Sentry)
   - Monitor performance (New Relic)
   - Check logs daily

2. **Connect Frontend**
   - Update API base URL in mobile/web app
   - Test end-to-end flows
   - Verify JWT authentication

3. **Database Backups**
   - Enable automatic backups in Supabase
   - Test restore procedures
   - Document backup schedule

4. **Performance Optimization**
   - Analyze slow queries
   - Add caching where appropriate
   - Optimize API response times

5. **Security Hardening**
   - Enable rate limiting
   - Add API key validation
   - Implement CORS properly

6. **Scale When Needed**
   - Monitor resource usage
   - Upgrade plan if necessary
   - Set up load balancing

---

## Contact & Support

**If deployment fails:**
1. Check Render logs
2. Review this guide
3. Check RENDER_DEPLOYMENT_GUIDE.md
4. Verify GitHub repository
5. Test locally first

**Useful Links:**
- [Render Documentation](https://render.com/docs)
- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [PostgreSQL Connection Guide](https://www.postgresql.org/docs/current/libpq-connect-dbname.html)

---

## Verification Checklist

Print this and check off as you verify:

```
Deployment Status
[ ] Service shows "Live"
[ ] Build logs show success
[ ] No startup errors

API Tests
[ ] Delivery estimation works
[ ] Availability check works
[ ] Error handling works
[ ] Invalid inputs rejected

Database
[ ] Connection successful
[ ] Tables created
[ ] No connection errors

Environment
[ ] All 28 variables set
[ ] Sensitive values secure
[ ] Google Maps API key valid

Performance
[ ] Response time < 2 seconds
[ ] Concurrent requests work
[ ] No timeouts

Security
[ ] HTTPS enabled
[ ] JWT configured
[ ] CORS set properly

Monitoring
[ ] Logs accessible
[ ] Errors are logged
[ ] Performance monitored
```

---

**Deployment Date:** _____________
**Verified By:** _____________
**Environment:** Production / Staging
**Status:** ✅ Complete / ⚠️ In Progress / ❌ Failed

---

*Last Updated: July 2026*
