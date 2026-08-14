# AgriDirect Backend

Spring Boot REST API for the AgriDirect farm-to-consumer marketplace.

## Tech Stack

- Java 17 + Spring Boot 3.2
- PostgreSQL (Supabase)
- Firebase Auth + FCM notifications
- Cloudinary (image storage)
- Razorpay (payments)
- Gemini AI (crop advice / disease detection)
- Redis (caching)

## How to Run

### Quick Start - With Custom AI

1. **Install Python 3.10+** from https://www.python.org/downloads/
   - ✅ Check "Add Python to PATH" during installation

2. **Setup & Train AI Models** (first time only):
   ```powershell
   cd local_ai_service
   .\run_setup.bat
   ```
   ⏱️ Takes 10-15 minutes (trains models, starts FastAPI on port 8000)

3. **Start Backend** (in new PowerShell):
   ```powershell
   mvn spring-boot:run
   ```
   The API listens on **port 8001**

4. **Test AI Integration**:
   ```powershell
   curl -X POST http://localhost:8001/api/farmer/ai/chat `
     -H "Content-Type: application/json" `
     -d '{"message":"Hello","language":"English"}'
   ```

### Regular Start - After Setup

Keep both services running:
- **Terminal 1**: `cd local_ai_service && .\start_server.bat` (AI on port 8000)
- **Terminal 2**: `mvn spring-boot:run` (Backend on port 8001)

### Setup Details

See detailed setup guides:
- **SETUP_AND_TEST.md** - Step-by-step guide
- **QUICK_START.txt** - One-page reference
- **AI_TESTING_GUIDE.md** - API testing procedures

## Environment Variables

| Variable | Description |
|---|---|
| `DB_URL` | PostgreSQL JDBC URL (e.g. `jdbc:postgresql://...`) |
| `DB_USERNAME` | Database user |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET` | Secret key for signing JWTs |
| `JWT_EXPIRATION` | Token TTL in ms (default 86400000) |
| `FIREBASE_SERVICE_ACCOUNT_PATH` | Path to Firebase service account JSON |
| `RAZORPAY_KEY_ID` | Razorpay API key |
| `RAZORPAY_KEY_SECRET` | Razorpay API secret |
| `RAZORPAY_WEBHOOK_SECRET` | Razorpay webhook signature secret |
| `CLOUDINARY_CLOUD_NAME` | Cloudinary cloud name |
| `CLOUDINARY_API_KEY` | Cloudinary API key |
| `CLOUDINARY_API_SECRET` | Cloudinary API secret |
| `REDIS_HOST` | Redis host (default `localhost`) |
| `REDIS_PORT` | Redis port (default `6379`) |

> **Note**: GEMINI_API_KEY, XAI_API_KEY, GROQ_API_KEY are **optional**. Backend uses local AI service by default!

---

## 🌾 Custom AI Integration

Your backend now uses **self-hosted AI models** with no third-party API costs:

- ✅ **Chatbot** - TF-IDF + Neural Network (local inference)
- ✅ **Disease Detection** - PyTorch CNN (image analysis)
- ✅ **Crop Advice** - Rule-based recommender
- ✅ **Price Forecast** - ML-based price predictor

### Architecture

```
Backend (Port 8001)
    ↓ HTTP
Local AI Service (Port 8000 - FastAPI)
    ├── Chatbot
    ├── Disease Detection
    ├── Crop Advice
    └── Price Forecast
```

### Files

- `local_ai_service/app.py` - FastAPI server
- `local_ai_service/train_chatbot.py` - Chatbot training
- `local_ai_service/train_disease.py` - Disease model training
- `local_ai_service/run_setup.bat` - First-time setup
- `local_ai_service/start_server.bat` - Quick start (after setup)

### Documentation

- **SETUP_AND_TEST.md** - Complete setup steps
- **AI_IMPLEMENTATION_SUMMARY.md** - Architecture details
- **AI_TESTING_GUIDE.md** - API testing examples

## Database Tables (10)

`users`, `farmer_profiles`, `buyer_profiles`, `delivery_profiles`,
`categories`, `products`, `product_images`, `orders`, `order_items`, `payments`

---

## API Endpoints

### Auth — `/api/auth`
| Method | Path | Description |
|---|---|---|
| POST | `/register` | Register user (idToken, name, role, farmName, location) |
| POST | `/login` | Login with Firebase idToken |
| GET | `/me` | Get current user (JWT required) |

### Farmer — `/api/farmer` (JWT + FARMER role)
| Method | Path | Description |
|---|---|---|
| GET | `/profile` | Get farmer profile |
| PUT | `/profile` | Update farmer profile |
| GET | `/dashboard` | Farmer dashboard stats |
| GET | `/earnings` | Earnings summary |
| POST | `/profile/photo` | Upload profile photo (multipart) |

### Products — `/api/farmer/products` (FARMER) + `/api/products` (public)
| Method | Path | Description |
|---|---|---|
| GET | `/api/products` | List all available products |
| GET | `/api/products/{id}` | Get product by ID |
| GET | `/api/products/category/{categoryId}` | Products by category |
| GET | `/api/products/search?q=` | Search products |
| GET | `/api/farmer/products` | My listings |
| POST | `/api/farmer/products` | Create product |
| PUT | `/api/farmer/products/{id}` | Update product |
| DELETE | `/api/farmer/products/{id}` | Delete product |
| POST | `/api/farmer/products/upload-image` | Upload product image (multipart) |

### Buyer — `/api/buyer` (JWT + BUYER role)
| Method | Path | Description |
|---|---|---|
| GET | `/profile` | Get buyer profile |
| PUT | `/profile` | Update buyer profile |

### Orders — `/api/buyer/orders` + `/api/farmer/orders`
| Method | Path | Description |
|---|---|---|
| POST | `/api/buyer/orders` | Place order |
| GET | `/api/buyer/orders` | Buyer's orders |
| GET | `/api/buyer/orders/{id}` | Order detail |
| PUT | `/api/buyer/orders/{id}/cancel` | Cancel order |
| GET | `/api/farmer/orders` | Farmer's incoming orders |
| PUT | `/api/farmer/orders/{id}/accept` | Accept order |
| PUT | `/api/farmer/orders/{id}/pack` | Mark order as packed |

### Payment — `/api/payment` (JWT)
| Method | Path | Description |
|---|---|---|
| POST | `/create-order` | Create Razorpay order |
| POST | `/verify` | Verify payment signature |
| GET | `/order/{orderId}` | Get payment for order |
| POST | `/webhook` | Razorpay webhook (public) |

### Delivery — `/api/delivery` (JWT + DELIVERY role)
| Method | Path | Description |
|---|---|---|
| GET | `/profile` | Get delivery profile |
| PUT | `/availability` | Toggle availability |
| GET | `/orders` | Assigned orders |
| PUT | `/orders/{id}/status` | Update order status (PICKED_UP / DELIVERED) |

### AI — `/api/farmer/ai` (FARMER role)
| Method | Path | Description |
|---|---|---|
| POST | `/disease` | Detect crop disease (multipart image) |
| POST | `/advice` | Get crop advice |
| POST | `/price-forecast` | Get price forecast |
| POST | `/chat` | Chat with AI assistant |

### Categories — `/api/categories`
| Method | Path | Description |
|---|---|---|
| GET | `/` | List active categories (public) |
| POST | `/` | Create category (ADMIN) |
| PUT | `/{id}/toggle` | Enable/disable category (ADMIN) |

### Admin — `/api/admin` (JWT + ADMIN role)
| Method | Path | Description |
|---|---|---|
| GET | `/users` | List all users |
| GET | `/users/role/{role}` | Users by role |
| PUT | `/users/{id}/block` | Block user |
| PUT | `/users/{id}/unblock` | Unblock user |
| GET | `/farmers/pending` | Unverified farmers |
| PUT | `/farmers/{farmerId}/verify` | Verify farmer |
| GET | `/orders` | All orders |
| GET | `/analytics` | Platform analytics |

---

## Supabase Setup

### Create first ADMIN user
```sql
INSERT INTO users (id, phone, name, role, is_active, created_at)
VALUES (gen_random_uuid(), '+919999999999', 'Admin', 'ADMIN', true, NOW());
```

### Seed default categories
```sql
INSERT INTO categories (id, name, image_url, is_active) VALUES
(gen_random_uuid(), 'Vegetables', '', true),
(gen_random_uuid(), 'Fruits',     '', true),
(gen_random_uuid(), 'Grains',     '', true),
(gen_random_uuid(), 'Dairy',      '', true),
(gen_random_uuid(), 'Herbs',      '', true),
(gen_random_uuid(), 'Pulses',     '', true);
```
