# AgriDirect Delivery API Test Collection

This document contains all the API endpoints for testing the delivery system locally and in production.

## Base URL
- **Local**: `http://localhost:8001`
- **Production**: `https://your-render-app.onrender.com`

---

## 1. DELIVERY ESTIMATION APIs

### 1.1 Estimate Delivery Cost & Time
**Endpoint**: `POST /api/delivery/estimate`
**Auth**: None (Public)
**Description**: Calculate delivery cost and estimated time for an order

**Request**:
```bash
curl -X POST http://localhost:8001/api/delivery/estimate \
  -H "Content-Type: application/json" \
  -d '{
    "sourceLatitude": 12.9716,
    "sourceLongitude": 77.5946,
    "destLatitude": 12.9352,
    "destLongitude": 77.6245,
    "sourceAddress": "Bangalore Central Market",
    "destAddress": "Indiranagar",
    "orderAmount": 500
  }'
```

**Response** (Success):
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

**Response** (Out of Range):
```json
{
  "status_code": 400,
  "message": "Delivery location is outside service area",
  "data": {
    "status": "OUT_OF_DELIVERY_RANGE"
  }
}
```

---

### 1.2 Check Delivery Availability
**Endpoint**: `GET /api/delivery/availability`
**Auth**: None (Public)
**Description**: Check if delivery is available at a location and get wait time

**Request**:
```bash
curl -X GET "http://localhost:8001/api/delivery/availability?latitude=12.9716&longitude=77.5946"
```

**Response**:
```json
{
  "status_code": 200,
  "message": "Delivery availability checked",
  "data": {
    "isAvailable": true,
    "availablePartnersCount": 5,
    "avgRating": 4.5,
    "estimatedWaitMinutes": 10,
    "availabilityStatus": "HIGH"
  }
}
```

---

## 2. DELIVERY TRACKING APIs

### 2.1 Track Delivery (Public)
**Endpoint**: `GET /api/delivery/track/{orderId}`
**Auth**: None (Public)
**Description**: Get real-time delivery tracking for an order

**Request**:
```bash
curl -X GET http://localhost:8001/api/delivery/track/order123
```

**Response**:
```json
{
  "status_code": 200,
  "message": "Delivery tracking retrieved",
  "data": {
    "id": "tracking1",
    "orderId": "order123",
    "deliveryPartnerId": "partner1",
    "status": "IN_TRANSIT",
    "currentLatitude": 12.9716,
    "currentLongitude": 77.5946,
    "currentAddress": "Near Koramangala",
    "distanceRemainingKm": 2.5,
    "estimatedArrivalTime": 1689456000000,
    "lastUpdateTime": 1689455900000,
    "assignedAt": 1689455000000,
    "pickedUpAt": 1689455200000,
    "deliveredAt": null,
    "totalDelaySeconds": 0,
    "notes": "On the way",
    "createdAt": 1689455000000,
    "updatedAt": 1689455900000
  }
}
```

---

### 2.2 Update Delivery Tracking
**Endpoint**: `PUT /api/delivery/track`
**Auth**: Required (DELIVERY_PARTNER role)
**Description**: Update delivery status and location

**Request**:
```bash
curl -X PUT http://localhost:8001/api/delivery/track \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "order123",
    "status": "IN_TRANSIT",
    "currentLatitude": 12.9716,
    "currentLongitude": 77.5946,
    "currentAddress": "Near Koramangala",
    "distanceRemainingKm": 2.5,
    "estimatedArrivalTime": 1689456000000,
    "notes": "On the way"
  }'
```

**Response**:
```json
{
  "status_code": 200,
  "message": "Delivery tracking updated successfully",
  "data": {
    "status": "IN_TRANSIT",
    "updatedAt": 1689455900000
  }
}
```

**Status Values**: `ASSIGNED`, `PICKED_UP`, `IN_TRANSIT`, `NEAR_DELIVERY`, `DELIVERED`, `CANCELLED`

---

## 3. LOCATION MANAGEMENT APIs

### 3.1 Create Location
**Endpoint**: `POST /api/locations/{locationType}`
**Auth**: Required (FARMER, BUYER, or DELIVERY_PARTNER role)
**Description**: Create a new location for the user

**Request**:
```bash
curl -X POST http://localhost:8001/api/locations/FARMER \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "latitude": 12.9716,
    "longitude": 77.5946,
    "address": "123 Farm Road, Bangalore",
    "city": "Bangalore",
    "state": "Karnataka",
    "pincode": "560001",
    "isPrimary": true
  }'
```

**Response**:
```json
{
  "status_code": 201,
  "message": "Location created successfully",
  "data": {
    "id": "location1",
    "userId": "user123",
    "locationType": "FARMER",
    "latitude": 12.9716,
    "longitude": 77.5946,
    "address": "123 Farm Road, Bangalore",
    "city": "Bangalore",
    "state": "Karnataka",
    "pincode": "560001",
    "isPrimary": true,
    "isActive": true,
    "createdAt": 1689455000000,
    "updatedAt": 1689455000000
  }
}
```

**Location Types**: `FARMER`, `BUYER`, `DELIVERY_PARTNER`

---

### 3.2 Get Primary Location
**Endpoint**: `GET /api/locations/primary`
**Auth**: Required (FARMER, BUYER, or DELIVERY_PARTNER role)
**Description**: Get the user's primary location

**Request**:
```bash
curl -X GET http://localhost:8001/api/locations/primary \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response**:
```json
{
  "status_code": 200,
  "message": "Primary location retrieved",
  "data": {
    "id": "location1",
    "userId": "user123",
    "locationType": "FARMER",
    "latitude": 12.9716,
    "longitude": 77.5946,
    "address": "123 Farm Road, Bangalore",
    "isPrimary": true,
    "isActive": true,
    "createdAt": 1689455000000
  }
}
```

---

### 3.3 Get All User Locations
**Endpoint**: `GET /api/locations`
**Auth**: Required (FARMER, BUYER, or DELIVERY_PARTNER role)
**Description**: Get all locations for the user

**Request**:
```bash
curl -X GET http://localhost:8001/api/locations \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response**:
```json
{
  "status_code": 200,
  "message": "User locations retrieved",
  "data": [
    {
      "id": "location1",
      "userId": "user123",
      "locationType": "FARMER",
      "latitude": 12.9716,
      "longitude": 77.5946,
      "address": "Farm 1",
      "isPrimary": true
    },
    {
      "id": "location2",
      "userId": "user123",
      "locationType": "FARMER",
      "latitude": 12.9352,
      "longitude": 77.6245,
      "address": "Farm 2",
      "isPrimary": false
    }
  ]
}
```

---

## 4. DELIVERY PARTNER APIs

### 4.1 Update Partner Location
**Endpoint**: `PUT /api/delivery/partner/{partnerId}/location`
**Auth**: Required (DELIVERY_PARTNER role)
**Description**: Update delivery partner's current location

**Request**:
```bash
curl -X PUT http://localhost:8001/api/delivery/partner/partner1/location \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '?latitude=12.9716&longitude=77.5946'
```

**Response**:
```json
{
  "status_code": 200,
  "message": "Partner location updated successfully",
  "data": "OK"
}
```

---

### 4.2 Update Partner Availability
**Endpoint**: `PUT /api/delivery/partner/{partnerId}/availability`
**Auth**: Required (DELIVERY_PARTNER role)
**Description**: Toggle delivery partner availability

**Request**:
```bash
curl -X PUT "http://localhost:8001/api/delivery/partner/partner1/availability?isAvailable=true&latitude=12.9716&longitude=77.5946" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response**:
```json
{
  "status_code": 200,
  "message": "Partner availability updated successfully",
  "data": "OK"
}
```

---

## 5. ADMIN APIs

### 5.1 Match Order with Partner
**Endpoint**: `POST /api/delivery/match-partner`
**Auth**: Required (ADMIN role)
**Description**: Manually match an order with the best delivery partner

**Request**:
```bash
curl -X POST "http://localhost:8001/api/delivery/match-partner?orderId=order123&pickupLat=12.9716&pickupLng=77.5946&deliveryLat=12.9352&deliveryLng=77.6245" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response**:
```json
{
  "status_code": 200,
  "message": "Delivery partner matched successfully",
  "data": {
    "id": "partner1",
    "name": "Rajesh Kumar",
    "phone": "9876543210",
    "vehicleType": "BIKE",
    "currentLatitude": 12.9716,
    "currentLongitude": 77.5946,
    "isAvailable": true,
    "totalDeliveries": 150,
    "avgRating": 4.7,
    "verificationStatus": "VERIFIED"
  }
}
```

---

### 5.2 Get Available Partners Count
**Endpoint**: `GET /api/delivery/partners/available/count`
**Auth**: Required (ADMIN role)
**Description**: Get count of currently available delivery partners

**Request**:
```bash
curl -X GET http://localhost:8001/api/delivery/partners/available/count \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response**:
```json
{
  "status_code": 200,
  "message": "Available partners count retrieved",
  "data": 12
}
```

---

## Testing with Postman

### Import Collection
1. Save the requests above as a Postman collection (JSON format)
2. Import in Postman: **File → Import → Paste JSON**
3. Set variables:
   - `base_url`: http://localhost:8001
   - `jwt_token`: Your JWT token from login

### Quick Test Flow
1. **Login** first to get JWT token
2. **Create Location** (POST /api/locations/FARMER)
3. **Estimate Delivery** (POST /api/delivery/estimate)
4. **Check Availability** (GET /api/delivery/availability)
5. **Update Tracking** (PUT /api/delivery/track)
6. **Track Order** (GET /api/delivery/track/{orderId})

---

## Error Codes

| Code | Message | Meaning |
|------|---------|---------|
| 200 | OK | Success |
| 201 | Created | Resource created |
| 400 | Bad Request | Invalid input/out of range |
| 401 | Unauthorized | Missing/invalid JWT |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 500 | Internal Server Error | Server error |

---

## Important Notes

1. **All POST/PUT requests** require `Content-Type: application/json`
2. **Authentication** uses JWT Bearer tokens from login endpoint
3. **Coordinates** use standard lat/lng format (WGS84)
4. **Timestamps** are in milliseconds (Unix epoch)
5. **Distances** are in kilometers
6. **Costs** are in INR (Indian Rupees)

---

## Sample Integration

```java
// Java Example - Estimate Delivery
RestTemplate restTemplate = new RestTemplate();
DeliveryEstimateRequestDTO request = new DeliveryEstimateRequestDTO(
    12.9716, 77.5946,
    12.9352, 77.6245,
    "Pickup Location",
    "Delivery Location",
    500.0
);

DeliveryEstimateResponseDTO response = restTemplate.postForObject(
    "http://localhost:8001/api/delivery/estimate",
    request,
    DeliveryEstimateResponseDTO.class
);

System.out.println("Delivery Cost: " + response.getTotalDeliveryCost());
System.out.println("Estimated Time: " + response.getEstimatedDeliveryRange());
```

---

## Next Steps

1. Test all endpoints locally
2. Verify JWT authentication works
3. Test with actual coordinates in your service area
4. Deploy to Render
5. Test production endpoints
