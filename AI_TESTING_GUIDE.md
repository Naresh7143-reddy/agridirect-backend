# 🧪 AI Testing Guide - AgriDirect

This guide walks you through testing the custom AI models (chatbot, disease detection, etc.).

## Prerequisites

### 1. Install Python
- Download Python 3.10+ from https://www.python.org/downloads/
- **IMPORTANT**: Check ✅ "Add Python to PATH"
- Restart PowerShell after installation
- Verify: `python --version`

### 2. Setup Models (First Time Only)

```powershell
cd c:\Users\nares\Downloads\backend\local_ai_service
.\run_setup.bat
```

This will:
- Create Python virtual environment
- Install dependencies
- **Train both models** (5-10 minutes)
- Start FastAPI server

⏱️ **Wait for "Server Starting on http://localhost:8000" message**

---

## Testing the AI Service

### Test 1: Chatbot - Basic Greeting

```powershell
curl -X POST http://localhost:8000/api/ai/chat `
  -H "Content-Type: application/json" `
  -d '{"message":"Hello","language":"English"}'
```

**Expected Response**:
```json
{
  "response": "Namaste! I am Krishi AI. How can I help you today with your farming?"
}
```

---

### Test 2: Chatbot - Crop Advice Question

```powershell
curl -X POST http://localhost:8000/api/ai/chat `
  -H "Content-Type: application/json" `
  -d '{"message":"Which crop should I grow in monsoon?","language":"English"}'
```

**Expected Response**:
```json
{
  "response": "Based on your soil and water availability, you can grow Rice or Maize this season..."
}
```

---

### Test 3: Chatbot - Disease Help

```powershell
curl -X POST http://localhost:8000/api/ai/chat `
  -H "Content-Type: application/json" `
  -d '{"message":"My leaves are yellow and curling","language":"English"}'
```

**Expected Response**:
```json
{
  "response": "Please upload a photo of the affected plant. Our Disease Detection model will analyze it and suggest treatments!"
}
```

---

### Test 4: Crop Advice

```powershell
curl -X POST http://localhost:8000/api/ai/crop-advice `
  -H "Content-Type: application/json" `
  -d '{
    "season": "Monsoon",
    "location": "Maharashtra",
    "soilType": "Loam",
    "waterAvailability": "High"
  }'
```

**Expected Response**:
```json
{
  "response": "Based on location 'Maharashtra', season 'Monsoon', soil 'Loam', and water 'High':\n\nWe recommend growing the following crops:\n1. Rice (Paddy) - High yield potential...\n2. Maize (Corn) - High yield potential...\n3. Cotton - High yield potential...\n\nCare Tips: Ensure proper soil preparation..."
}
```

---

### Test 5: Price Forecast

```powershell
curl -X POST http://localhost:8000/api/ai/price-forecast `
  -H "Content-Type: application/json" `
  -d '{
    "cropName": "Tomato",
    "location": "Bangalore"
  }'
```

**Expected Response**:
```json
{
  "response": "Price Forecast for Tomato in Bangalore:\nCURRENT PRICE RANGE: ₹2500 - ₹2800 per quintal\nPRICE TREND: Rising\nNEXT 30 DAYS FORECAST: ₹2950 per quintal\nBEST TIME TO SELL: Within 2-3 weeks for optimal profits.\nFACTORS: Local market supply and weather patterns affecting transport."
}
```

---

### Test 6: Disease Detection (Image Analysis)

First, create a simple test image or download one. Then encode it to base64:

**PowerShell - Encode Image to Base64**:
```powershell
$imagePath = "C:\path\to\crop_image.jpg"
$imageBytes = [System.IO.File]::ReadAllBytes($imagePath)
$base64 = [Convert]::ToBase64String($imageBytes)

$body = @{
    base64Image = $base64
    cropName = "Tomato"
    mimeType = "image/jpeg"
} | ConvertTo-Json

curl -X POST http://localhost:8000/api/ai/disease `
  -H "Content-Type: application/json" `
  -d $body
```

**Expected Response** (Healthy Plant):
```json
{
  "response": "ISSUE: Healthy Tomato\nSEVERITY: None\nCAUSE: Optimal growing conditions\nSYMPTOMS: Plant shows strong vigor, leaves are green and healthy.\nTREATMENT: Continue current watering and weeding practices.\nPREVENTION: Follow standard preventive schedules and maintain soil nutrition.\nURGENCY: Monitor closely"
}
```

**Expected Response** (Disease Detected):
```json
{
  "response": "ISSUE: Tomato Yellow Leaf Curl Virus in Tomato\nSEVERITY: Moderate\nCAUSE: Fungal pathogen or pest causing stress to the Tomato plant.\nSYMPTOMS: Spotting, curling, or discoloration visible on leaf surfaces.\nTREATMENT: 1. Prune and destroy infected leaves.\n2. Apply appropriate local organic fungicide/neem oil.\n3. Avoid overhead watering.\nPREVENTION: Practice crop rotation, ensure clean seeds, and keep fields clear of weeds.\nURGENCY: Within a week"
}
```

---

## Testing via Backend API

Once the local AI service is running, you can also test through the **Java backend**:

### Start Backend
```powershell
cd c:\Users\nares\Downloads\backend
mvn spring-boot:run
```

Backend runs on: `http://localhost:8080`

### Test: Chat via Backend

```powershell
curl -X POST http://localhost:8080/api/farmer/ai/chat `
  -H "Content-Type: application/json" `
  -d '{"message":"Hello","language":"English"}'
```

The request flows:
1. Backend receives at `/api/farmer/ai/chat`
2. Calls `GeminiService.chat()`
3. GeminiService calls `http://localhost:8000/api/ai/chat`
4. Returns response to user

---

## Testing Disease Detection (Complete Flow)

### 1. Upload Image to Backend

```powershell
curl -X POST http://localhost:8080/api/farmer/ai/disease `
  -F "image=@C:\path\to\crop_image.jpg" `
  -F "cropName=Tomato"
```

### 2. Backend Returns Structured Response

```json
{
  "success": true,
  "data": {
    "diseaseName": "Tomato Yellow Leaf Curl Virus",
    "severity": "High",
    "symptoms": ["Leaf curling", "Yellowing", "Stunted growth"],
    "treatment": [
      {
        "step": 1,
        "description": "Remove infected plants immediately"
      },
      {
        "step": 2,
        "description": "Spray neem oil or approved fungicide"
      }
    ]
  }
}
```

---

## Performance Benchmarks

| Test | Expected Time | Memory |
|------|---------------|--------|
| Chatbot Query | 50-100ms | ~50MB |
| Crop Advice | <50ms | ~30MB |
| Price Forecast | <100ms | ~40MB |
| Disease Detection | 100-200ms | ~500MB |
| **Total Startup** | 10-30s | ~2GB |

---

## Troubleshooting Tests

### Problem: "Connection refused"
```
Error: Failed to connect to http://localhost:8000
```
**Solution**:
- Run `run_setup.bat` if not already running
- Verify: `curl http://localhost:8000/docs` should show Swagger UI
- Check firewall allows port 8000

### Problem: "Python not found"
```
Python was not found
```
**Solution**:
- Install Python 3.10+
- Add to PATH (check during installation)
- Restart PowerShell

### Problem: "Module not found"
```
ModuleNotFoundError: No module named 'fastapi'
```
**Solution**:
- Delete `venv/` folder
- Run `run_setup.bat` again

### Problem: "Models not found"
```
Error loading Crop Disease model: [Errno 2] No such file
```
**Solution**:
- Delete `models/` folder (if exists)
- Run `run_setup.bat` to retrain

### Problem: "Port 8000 already in use"
```
Address already in use
```
**Solution**:
```powershell
netstat -ano | findstr :8000
taskkill /PID <PID> /F
```

---

## Advanced Testing

### Test with Chat History

```powershell
curl -X POST http://localhost:8000/api/ai/chat `
  -H "Content-Type: application/json" `
  -d '{
    "message": "What about rice?",
    "language": "English",
    "history": [
      {"role": "user", "content": "Tell me about monsoon crops"},
      {"role": "assistant", "content": "Good crops for monsoon include..."}
    ]
  }'
```

### Test Different Languages

```powershell
curl -X POST http://localhost:8000/api/ai/chat `
  -H "Content-Type: application/json" `
  -d '{"message":"नमस्ते","language":"Hindi"}'
```

### Test Multiple Seasons

```powershell
# Winter (Rabi)
curl -X POST http://localhost:8000/api/ai/crop-advice `
  -H "Content-Type: application/json" `
  -d '{"season":"Winter","location":"Punjab","soilType":"Sandy","waterAvailability":"Medium"}'

# Summer (Zaid)
curl -X POST http://localhost:8000/api/ai/crop-advice `
  -H "Content-Type: application/json" `
  -d '{"season":"Summer","location":"Haryana","soilType":"Loam","waterAvailability":"Low"}'
```

---

## API Response Format

All endpoints return a consistent format:

```json
{
  "response": "<string with complete answer>"
}
```

### Disease Detection Returns

```
ISSUE: <disease name>
SEVERITY: <Mild|Moderate|High|Critical>
CAUSE: <what causes it>
SYMPTOMS: <visible symptoms>
TREATMENT: <numbered steps>
PREVENTION: <preventive measures>
URGENCY: <timeframe to act>
```

---

## Monitoring

### Check Server Health
```powershell
curl http://localhost:8000/docs
```
Opens: `http://localhost:8000/docs` (Swagger UI)

### View Logs
The FastAPI server logs in real-time in the terminal window.

**Look for**:
- ✅ "Uvicorn running on" = Server started
- ✅ "Successfully loaded" = Models loaded
- ❌ "Error loading" = Models failed

---

## Automated Test Batch File

Create `test_ai.bat`:

```batch
@echo off
echo Testing AgriDirect Custom AI...
echo.

echo Test 1: Chat
curl -X POST http://localhost:8000/api/ai/chat -H "Content-Type: application/json" -d "{\"message\":\"Hello\",\"language\":\"English\"}"
echo.
echo.

echo Test 2: Crop Advice
curl -X POST http://localhost:8000/api/ai/crop-advice -H "Content-Type: application/json" -d "{\"season\":\"Monsoon\",\"location\":\"Maharashtra\",\"soilType\":\"Loam\",\"waterAvailability\":\"High\"}"
echo.
echo.

echo Test 3: Price Forecast
curl -X POST http://localhost:8000/api/ai/price-forecast -H "Content-Type: application/json" -d "{\"cropName\":\"Tomato\",\"location\":\"Bangalore\"}"
echo.

pause
```

Save as `c:\Users\nares\Downloads\backend\local_ai_service\test_ai.bat`
Run: `.\test_ai.bat`

---

## Next Steps

1. ✅ Install Python
2. ✅ Run `run_setup.bat`
3. ✅ Wait for models to train
4. ✅ Run tests from above
5. ✅ Monitor responses
6. ✅ Collect real disease data
7. ✅ Retrain with real data

---

**Your custom AI is now testable! Let me know the results! 🌾**
