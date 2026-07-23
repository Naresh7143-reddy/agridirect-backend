# Render Environment Variables Quick Setup

**Copy and paste each section into Render dashboard.**

---

## 🔒 Security & Auth

```
JWT_SECRET=your-super-secret-key-minimum-256-bits
JWT_EXPIRATION=86400000
```

**Generate JWT_SECRET:**
```bash
openssl rand -base64 32
```

---

## 📦 Database

```
DB_URL=jdbc:postgresql://db.supabase.co:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=your_password
```

**For Supabase:**
1. Go to Project Settings
2. Copy Connection String
3. Format: `jdbc:postgresql://host:port/database`

---

## 🔥 Firebase

```
FIREBASE_SERVICE_ACCOUNT_PATH=/etc/secrets/firebase-service-account.json
```

**Upload Firebase JSON:**
1. Render Dashboard → Environment
2. Find "Secret Files"
3. Add file: `firebase-service-account.json`
4. Paste your Firebase service account JSON

---

## 💰 Razorpay Payment

```
RAZORPAY_KEY_ID=rzp_live_xxxxx
RAZORPAY_KEY_SECRET=xxxxx
RAZORPAY_WEBHOOK_SECRET=xxxxx
```

**Get from:** Razorpay Dashboard → Settings → API Keys

---

## 🖼️ Cloudinary Image Storage

```
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=xxxxx
CLOUDINARY_API_SECRET=xxxxx
```

**Get from:** Cloudinary Dashboard → Settings → API Keys

---

## 🤖 AI Services

```
GEMINI_API_KEY=xxxxx
XAI_API_KEY=xxxxx
GROQ_API_KEY=xxxxx
```

**Optional but recommended.** Get from respective API providers.

---

## 🗺️ Google Maps (NEW)

```
GOOGLE_MAPS_API_KEY=AIzaSyDO2-nd2r08Iqzb9RAE62TF_Xtzgk5oqKM
```

**Setup:**
1. Google Cloud Console
2. Enable: Distance Matrix API, Directions API
3. Create API Key
4. Restrict to HTTP referrers

---

## 🚚 Delivery Configuration (NEW)

```
DELIVERY_BASE_COST=50
DELIVERY_PER_KM_COST=8
DELIVERY_PER_MINUTE_COST=1
MIN_DELIVERY_RADIUS=0.5
MAX_DELIVERY_RADIUS=25
DELIVERY_PARTNER_SEARCH_RADIUS=5
```

**Customizable based on your business:**
- BASE_COST: Fixed charge per delivery
- PER_KM_COST: Charge per kilometer
- PER_MINUTE_COST: Charge per minute
- RADIUS: Service area in kilometers

---

## 🌐 Server

```
PORT=10000
```

**Note:** Render auto-assigns port. This value is informational.

---

## ✅ Complete Setup in Render

### Step 1: Go to Render Dashboard
https://render.com/dashboard

### Step 2: Create New Web Service
- Click **+ New**
- Select **Web Service**
- Connect GitHub repo

### Step 3: Configure Build
| Field | Value |
|-------|-------|
| Name | agridirect-backend |
| Build Command | `mvn clean package -DskipTests` |
| Start Command | `java -jar target/backend-0.0.1-SNAPSHOT.jar` |

### Step 4: Add Environment Variables
1. Click **Environment** tab
2. Add each variable from the sections above
3. Keep sensitive values secure

### Step 5: Upload Firebase File (if needed)
1. Scroll to **Secret Files**
2. Click **Add Secret File**
3. Filename: `firebase-service-account.json`
4. Paste Firebase JSON content

### Step 6: Deploy
- Click **Create Web Service**
- Monitor build in **Logs** tab
- Wait for "Live" status

---

## 🧪 Test After Deployment

### Get your URL
- Copy from Render dashboard (looks like: `https://agridirect-backend-xxxx.onrender.com`)

### Test Delivery Estimation
```bash
curl -X POST https://YOUR_URL/api/delivery/estimate \
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

**Expected Response:**
```json
{
  "status_code": 200,
  "message": "Delivery estimate calculated successfully",
  "data": {
    "distanceKm": 5.4,
    "estimatedTimeMinutes": 20,
    "totalDeliveryCost": 113.2,
    "status": "SUCCESS"
  }
}
```

### Test Availability
```bash
curl https://YOUR_URL/api/delivery/availability?latitude=12.9716&longitude=77.5946
```

---

## 🔍 Quick Validation

- [ ] All database variables set
- [ ] Firebase service account uploaded
- [ ] Google Maps API key added
- [ ] Razorpay keys configured
- [ ] Cloudinary credentials added
- [ ] JWT secret is strong (32+ characters)
- [ ] Delivery configuration values are reasonable
- [ ] Service deploys successfully
- [ ] Base endpoint responds (status 200)
- [ ] Delivery estimation works

---

## ⚠️ Common Mistakes

1. **Wrong DB_URL format**
   - ❌ `postgresql://host/db`
   - ✅ `jdbc:postgresql://host:5432/db`

2. **Weak JWT_SECRET**
   - ❌ `secret123`
   - ✅ Use 32+ random characters

3. **Missing Firebase file**
   - ❌ Just the key in variable
   - ✅ Upload actual JSON file in Secret Files

4. **Wrong API key**
   - ❌ Copy-pasting from docs
   - ✅ Get actual key from your account

5. **Port conflicts**
   - ❌ Setting PORT=8001 (Render uses different port)
   - ✅ Leave PORT or use assigned port

---

## 🔗 Copy Full Configuration

**Copy this entire block and paste into Render:**

```
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000
DB_URL=jdbc:postgresql://host:port/db
DB_USERNAME=user
DB_PASSWORD=pass
FIREBASE_SERVICE_ACCOUNT_PATH=/etc/secrets/firebase-service-account.json
RAZORPAY_KEY_ID=key
RAZORPAY_KEY_SECRET=secret
RAZORPAY_WEBHOOK_SECRET=webhook
CLOUDINARY_CLOUD_NAME=name
CLOUDINARY_API_KEY=key
CLOUDINARY_API_SECRET=secret
GEMINI_API_KEY=key
XAI_API_KEY=key
GROQ_API_KEY=key
GOOGLE_MAPS_API_KEY=AIzaSyDO2-nd2r08Iqzb9RAE62TF_Xtzgk5oqKM
DELIVERY_BASE_COST=50
DELIVERY_PER_KM_COST=8
DELIVERY_PER_MINUTE_COST=1
MIN_DELIVERY_RADIUS=0.5
MAX_DELIVERY_RADIUS=25
DELIVERY_PARTNER_SEARCH_RADIUS=5
PORT=10000
```

---

## 📞 Support

If deployment fails:
1. Check Render logs (red errors)
2. Verify all environment variables
3. Test DB connection string locally
4. Ensure GitHub repository is connected
5. Check Maven build locally: `mvn clean package`

---

**Happy Deploying! 🚀**
