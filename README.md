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

1. Copy `.env.example` to `.env` and fill in all values (see Environment Variables below).
2. Start the server:
   ```
   mvn spring-boot:run
   ```
   The API listens on **port 8001**.

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
| `GEMINI_API_KEY` | Google Gemini API key |
| `CLOUDINARY_CLOUD_NAME` | Cloudinary cloud name |
| `CLOUDINARY_API_KEY` | Cloudinary API key |
| `CLOUDINARY_API_SECRET` | Cloudinary API secret |
| `REDIS_HOST` | Redis host (default `localhost`) |
| `REDIS_PORT` | Redis port (default `6379`) |

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
