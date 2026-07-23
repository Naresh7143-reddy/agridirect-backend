# AgriDirect Backend - Render Deployment Guide

Complete step-by-step guide to deploy the AgriDirect backend on Render with environment variables.

---

## Prerequisites

1. **GitHub Account** - Code is pushed to GitHub
2. **Render Account** - Sign up at https://render.com
3. **PostgreSQL Database** - Supabase or any PostgreSQL provider
4. **API Keys** - Have all required API keys ready

---

## Step 1: Prepare Required API Keys

Before deploying, gather all required environment variables:

### Database
- **DB_URL** - PostgreSQL JDBC URL
  - Format: `jdbc:postgresql://host:port/database`
  - Example: `jdbc:postgresql://db.supabase.co:5432/postgres`
- **DB_USERNAME** - Database user
- **DB_PASSWORD** - Database password

### Authentication
- **JWT_SECRET** - Generate a strong secret (min 256 bits)
  - Use: `openssl rand -base64 32`
- **JWT_EXPIRATION** - Token TTL in milliseconds (default: 86400000 = 24 hours)

### Firebase
- **FIREBASE_SERVICE_ACCOUNT_PATH** - Upload the JSON file (see below)

### Payment
- **RAZORPAY_KEY_ID** - From Razorpay dashboard
- **RAZORPAY_KEY_SECRET** - From Razorpay dashboard
- **RAZORPAY_WEBHOOK_SECRET** - From Razorpay dashboard

### Cloud Storage
- **CLOUDINARY_CLOUD_NAME** - From Cloudinary dashboard
- **CLOUDINARY_API_KEY** - From Cloudinary dashboard
- **CLOUDINARY_API_SECRET** - From Cloudinary dashboard

### AI Services
- **GEMINI_API_KEY** - Google Gemini API key
- **XAI_API_KEY** - XAI API key (optional)
- **GROQ_API_KEY** - Groq API key (optional)

### Maps (NEW)
- **GOOGLE_MAPS_API_KEY** - Google Maps API key
  - Enable: Distance Matrix API, Directions API, Maps JavaScript API

### Delivery Configuration (NEW)
- **DELIVERY_BASE_COST** - Base delivery charge (default: 50)
- **DELIVERY_PER_KM_COST** - Per km charge (default: 8)
- **DELIVERY_PER_MINUTE_COST** - Per minute charge (default: 1)
- **MIN_DELIVERY_RADIUS** - Minimum delivery radius in km (default: 0.5)
- **MAX_DELIVERY_RADIUS** - Maximum delivery radius in km (default: 25)
- **DELIVERY_PARTNER_SEARCH_RADIUS** - Radius to search partners in km (default: 5)

### Server
- **PORT** - Server port (default: 8001, Render will override)

---

## Step 2: Handle Firebase Service Account

Firebase requires a JSON file. On Render, you have two options:

### Option A: Use Render's File System (Recommended)
1. In Render dashboard, navigate to your service
2. Go to **Environment** tab
3. Create a new environment variable:
   - Name: `FIREBASE_SERVICE_ACCOUNT_PATH`
   - Value: `/etc/secrets/firebase-service-account.json`
4. In the same page, find **Secret Files** section
5. Click **Add Secret File**
6. Filename: `firebase-service-account.json`
7. Paste your Firebase JSON content

### Option B: Base64 Encode in Environment Variable
1. Encode your Firebase JSON:
   ```bash
   cat firebase-service-account.json | base64
   ```
2. Create environment variable:
   - Name: `FIREBASE_SERVICE_ACCOUNT_BASE64`
   - Value: `<base64-encoded-json>`
3. Update application to decode it (requires code change)

---

## Step 3: Deploy on Render

### 3.1 Connect GitHub Repository

1. Go to https://render.com/dashboard
2. Click **+ New** → **Web Service**
3. Click **Connect your GitHub repository**
4. Search for `agridirect-backend` repository
5. Click **Connect**

### 3.2 Configure Service

Fill in the following settings:

| Field | Value |
|-------|-------|
| **Name** | `agridirect-backend` (or your choice) |
| **Environment** | `Docker` or `Native` (Spring Boot detected automatically) |
| **Build Command** | `mvn clean package -DskipTests` |
| **Start Command** | `java -jar target/backend-0.0.1-SNAPSHOT.jar` |
| **Plan** | Free tier for testing, Paid for production |

### 3.3 Add Environment Variables

1. In the dashboard, go to **Environment** tab
2. Add each variable from the list below (or copy-paste the entire list)

---

## Step 4: Environment Variables Configuration

### Complete Environment Variables List

Copy and paste into Render's environment variables section:

```
# Database Configuration
DB_URL=jdbc:postgresql://your-db-host:5432/agridirect
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password

# JWT Configuration
JWT_SECRET=your-super-secret-key-at-least-256-bits
JWT_EXPIRATION=86400000

# Firebase Configuration
FIREBASE_SERVICE_ACCOUNT_PATH=/etc/secrets/firebase-service-account.json

# Razorpay Configuration
RAZORPAY_KEY_ID=rzp_live_xxxxx
RAZORPAY_KEY_SECRET=your_razorpay_secret
RAZORPAY_WEBHOOK_SECRET=your_webhook_secret

# Cloudinary Configuration
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

# AI APIs
GEMINI_API_KEY=your_gemini_api_key
XAI_API_KEY=your_xai_api_key
GROQ_API_KEY=your_groq_api_key

# Google Maps (NEW)
GOOGLE_MAPS_API_KEY=AIzaSyDO2-nd2r08Iqzb9RAE62TF_Xtzgk5oqKM

# Delivery Configuration (NEW)
DELIVERY_BASE_COST=50
DELIVERY_PER_KM_COST=8
DELIVERY_PER_MINUTE_COST=1
MIN_DELIVERY_RADIUS=0.5
MAX_DELIVERY_RADIUS=25
DELIVERY_PARTNER_SEARCH_RADIUS=5

# Server
PORT=10000
```

### How to Add Variables in Render:

**Method 1: Individual Variables**
1. Click **Add Environment Variable** for each one
2. Enter Name and Value
3. Click **Add**

**Method 2: Bulk Import**
1. Save the above list as `env-vars.txt`
2. Some services support bulk import - check Render UI

---

## Step 5: Database Migrations

### If Using Supabase:

1. Go to Supabase dashboard
2. Open SQL Editor
3. Create initial tables (Hibernate will handle most):

```sql
-- Users table (if not auto-created)
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    phone VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(255),
    role VARCHAR(50),
    is_active BOOLEAN DEFAULT true,
    created_at BIGINT,
    updated_at BIGINT
);

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "PostGIS";
```

4. Let Hibernate create other tables automatically (ddl-auto: update)

---

## Step 6: Deploy

1. Click **Deploy Service** button
2. Monitor build progress in the **Logs** tab
3. Wait for **"Your service is live at https://agridirect-backend-xxxx.onrender.com"**

### Expected Deployment Time: 5-10 minutes

---

## Step 7: Verify Deployment

### 7.1 Check Service Status

1. Go to your service dashboard
2. Check **Status** shows **Live**
3. Copy your service URL

### 7.2 Test Basic Endpoint

```bash
# Test delivery estimation
curl -X POST https://agridirect-backend-xxxx.onrender.com/api/delivery/estimate \
  -H "Content-Type: application/json" \
  -d '{
    "sourceLatitude": 12.9716,
    "sourceLongitude": 77.5946,
    "destLatitude": 12.9352,
    "destLongitude": 77.6245,
    "sourceAddress": "Bangalore",
    "destAddress": "Indiranagar",
    "orderAmount": 500
  }'
```

Expected Response:
```json
{
  "status_code": 200,
  "message": "Delivery estimate calculated successfully",
  "data": {
    "distanceKm": 5.4,
    "estimatedTimeMinutes": 20,
    "totalDeliveryCost": 113.2,
    "grandTotal": 118.86,
    "status": "SUCCESS"
  }
}
```

### 7.3 Check Availability

```bash
curl -X GET "https://agridirect-backend-xxxx.onrender.com/api/delivery/availability?latitude=12.9716&longitude=77.5946"
```

---

## Step 8: Production Checklist

- [ ] Database is connected and migrations are complete
- [ ] All API keys are added and valid
- [ ] Firebase service account file is uploaded
- [ ] JWT secret is strong and secure
- [ ] HTTPS is enabled (automatic on Render)
- [ ] Logs show no errors
- [ ] Test endpoints respond correctly
- [ ] Delivery estimation works
- [ ] Availability checking works
- [ ] Frontend can connect to backend

---

## Troubleshooting

### Issue: Build Fails

**Symptom**: Red error in build log
**Solution**:
1. Check build command: `mvn clean package -DskipTests`
2. Verify Java version: Should be 17
3. Check pom.xml for dependency issues

```bash
# Test locally first
mvn clean package -DskipTests
```

### Issue: "Connection refused" Error

**Symptom**: `java.sql.SQLException: Connection refused`
**Solution**:
1. Verify DB_URL is correct (format: `jdbc:postgresql://host:port/db`)
2. Check DB_USERNAME and DB_PASSWORD
3. Ensure database is accessible from Render's servers
4. For Supabase: Check firewall/SSL rules

### Issue: Google Maps API Error

**Symptom**: `com.google.maps.errors.ApiException`
**Solution**:
1. Verify GOOGLE_MAPS_API_KEY is correct
2. Enable required APIs in Google Cloud Console:
   - Distance Matrix API
   - Directions API
   - Maps JavaScript API
3. Check API quotas and billing

### Issue: Application Won't Start

**Symptom**: Logs show `Application failed to start`
**Solution**:
1. Check all required environment variables are set
2. Verify DB connection
3. Check log for specific error message
4. Look for missing dependencies in pom.xml

### Issue: Environment Variables Not Recognized

**Symptom**: `java.lang.IllegalArgumentException: Could not resolve placeholder`
**Solution**:
1. Check variable names are exactly as expected
2. Redeploy after adding variables
3. Use Render's provided environment variables
4. Check application.yml for correct syntax: `${VARIABLE_NAME}`

---

## Performance Optimization

### For Production:

1. **Database Connection Pool**
   ```properties
   spring.datasource.hikari.maximum-pool-size=20
   spring.datasource.hikari.minimum-idle=5
   ```

2. **Enable Caching**
   - Redis is configured but disabled in autoconfiguration
   - Enable for production

3. **API Rate Limiting**
   - Already implemented in RateLimitFilter
   - Adjust limits in configuration

4. **Monitoring**
   - Enable application logging
   - Set up error tracking (Sentry, etc.)

---

## Auto-Deployment Setup

To automatically redeploy when you push to GitHub:

1. Render automatically watches your branch
2. Every push to `main` triggers a new deployment
3. To disable: Go to **Settings** → Turn off auto-deploy

---

## Useful Commands

### SSH into Render Service
```bash
render connect agridirect-backend
```

### View Real-time Logs
```bash
# Directly in Render dashboard
# Or use Render CLI
render tail
```

### Restart Service
1. Go to dashboard
2. Click **Restart** button
3. Or redeploy from GitHub

---

## Environment Variables Summary

**Total Variables: 28**

| Category | Count | Examples |
|----------|-------|----------|
| Database | 3 | DB_URL, DB_USERNAME, DB_PASSWORD |
| JWT | 2 | JWT_SECRET, JWT_EXPIRATION |
| Firebase | 1 | FIREBASE_SERVICE_ACCOUNT_PATH |
| Razorpay | 3 | RAZORPAY_KEY_ID, etc. |
| Cloudinary | 3 | CLOUDINARY_CLOUD_NAME, etc. |
| AI APIs | 3 | GEMINI_API_KEY, etc. |
| Google Maps | 1 | GOOGLE_MAPS_API_KEY |
| Delivery | 6 | BASE_COST, PER_KM_COST, etc. |
| Server | 1 | PORT |

---

## Next Steps

1. ✅ Deploy to Render
2. ✅ Configure all environment variables
3. ✅ Test all endpoints
4. ✅ Set up monitoring
5. ✅ Configure custom domain (optional)
6. ✅ Set up CI/CD pipeline
7. ✅ Enable backups for database

---

## Support

For issues:
1. Check Render logs
2. Review application logs
3. Verify environment variables
4. Test locally first
5. Check GitHub issues

---

## Additional Resources

- [Render Docs](https://render.com/docs)
- [Spring Boot Deployment](https://spring.io/guides/gs/deploying_to_aws/)
- [PostgreSQL Connection Strings](https://www.postgresql.org/docs/current/libpq-connect-dbname.html)

---

Last Updated: July 2026
