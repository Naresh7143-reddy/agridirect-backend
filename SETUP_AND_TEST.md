# 🚀 Complete Setup & Testing Instructions

Follow these steps to get your custom AI running and test all endpoints.

---

## Phase 1: Install Python (5 minutes)

### Step 1.1: Download Python
1. Go to https://www.python.org/downloads/
2. Download **Python 3.10** or **3.11** (latest stable)
3. Run the installer

### Step 1.2: Critical - Add to PATH
During installation, **MUST CHECK** this box:
```
✅ Add Python 3.X to PATH
```

If you missed it, reinstall and check the box!

### Step 1.3: Verify Installation
Open **PowerShell** and run:
```powershell
python --version
```

Should show: `Python 3.10.X` or `Python 3.11.X`

If it says "Python not found", your PATH isn't set. Reinstall with the checkbox!

---

## Phase 2: Setup & Train AI Models (10-15 minutes)

### Step 2.1: Navigate to AI Service
Open **PowerShell** and run:
```powershell
cd c:\Users\nares\Downloads\backend\local_ai_service
```

### Step 2.2: Run Setup Script
```powershell
.\run_setup.bat
```

**This will**:
1. ✅ Create Python virtual environment (`venv/`)
2. ✅ Install dependencies (FastAPI, PyTorch, scikit-learn)
3. ✅ Train chatbot model (TF-IDF + Neural Network)
4. ✅ Train disease detection model (PyTorch CNN)
5. ✅ Start FastAPI server on port 8000

**⏱️ WAIT** - This takes 10-15 minutes first time!

### Step 2.3: Verify Server Started
Look for this message in the terminal:
```
INFO:     Started server process [...]
INFO:     Uvicorn running on http://127.0.0.1:8000
```

**🎉 If you see this, AI service is RUNNING!**

---

## Phase 3: Test AI Responses (5 minutes)

### Step 3.1: Open New PowerShell Window
**Keep the AI server running!** Open a NEW PowerShell window.

### Step 3.2: Test 1 - Chatbot

```powershell
curl -X POST http://localhost:8000/api/ai/chat `
  -H "Content-Type: application/json" `
  -d '{"message":"Hello","language":"English"}'
```

**Expected Response**:
```
{"response":"Namaste! I am Krishi AI. How can I help you today with your farming?"}
```

✅ If you see this, **chatbot works!**

---

### Step 3.3: Test 2 - Crop Advice

```powershell
curl -X POST http://localhost:8000/api/ai/crop-advice `
  -H "Content-Type: application/json" `
  -d '{"season":"Monsoon","location":"Maharashtra","soilType":"Loam","waterAvailability":"High"}'
```

**Expected Response**:
```
{"response":"Based on location 'Maharashtra', season 'Monsoon'...\n\nWe recommend growing...\n1. Rice (Paddy)\n2. Maize (Corn)\n3. Cotton"}
```

✅ If you see recommendations, **crop advisor works!**

---

### Step 3.4: Test 3 - Price Forecast

```powershell
curl -X POST http://localhost:8000/api/ai/price-forecast `
  -H "Content-Type: application/json" `
  -d '{"cropName":"Tomato","location":"Bangalore"}'
```

**Expected Response**:
```
{"response":"Price Forecast for Tomato in Bangalore:\nCURRENT PRICE RANGE: ₹... per quintal\nPRICE TREND: ..."}
```

✅ If you see price info, **price forecaster works!**

---

### Step 3.5: Test 4 - Chatbot with Farm Questions

```powershell
curl -X POST http://localhost:8000/api/ai/chat `
  -H "Content-Type: application/json" `
  -d '{"message":"My leaves are yellow","language":"English"}'
```

**Expected Response**:
```
{"response":"Please upload a photo of the affected plant. Our Disease Detection model will analyze it..."}
```

✅ If you see disease detection prompt, **intent recognition works!**

---

## Phase 4: Test Backend Integration (5 minutes)

### Step 4.1: Start Backend in Another PowerShell

**Open THIRD PowerShell window** and run:
```powershell
cd c:\Users\nares\Downloads\backend
mvn spring-boot:run
```

Wait for:
```
Started AgridirectApplication in X seconds
Tomcat started on port 8080
```

🎉 **Backend is running!**

---

### Step 4.2: Test Backend → AI Service Integration

```powershell
curl -X POST http://localhost:8080/api/farmer/ai/chat `
  -H "Content-Type: application/json" `
  -d '{"message":"What crop should I grow?","language":"English"}'
```

**Flow**:
1. Backend receives request at `:8080`
2. Calls local AI service at `:8000`
3. Returns AI response to you

**Expected Response**:
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "reply": "Based on your soil and water availability...",
    "language": "English"
  }
}
```

✅ If you see a response, **full integration works!**

---

## Phase 5: Complete End-to-End Test (10 minutes)

Now test the **complete flow** with all services running:

### Running Services Check
```powershell
# Terminal 1: AI Service (localhost:8000)
# Terminal 2: Backend (localhost:8080)
# Terminal 3: Testing
```

### Test Sequence

**Test 1: Chat**
```powershell
curl -X POST http://localhost:8080/api/farmer/ai/chat `
  -H "Content-Type: application/json" `
  -d '{"message":"Hello farmer!","language":"English"}'
```

**Test 2: Crop Advice**
```powershell
curl -X POST http://localhost:8080/api/farmer/ai/advice `
  -H "Content-Type: application/json" `
  -d '{"season":"Monsoon","location":"Karnataka","soilType":"Loam","waterAvailability":"High"}'
```

**Test 3: Price Forecast**
```powershell
curl -X POST http://localhost:8080/api/farmer/ai/price-forecast `
  -H "Content-Type: application/json" `
  -d '{"cropName":"Rice","location":"Bangalore"}'
```

---

## Troubleshooting During Setup

### Issue 1: "Python not found"
```
Python was not found
```
**Solution**:
- Install Python 3.10+ from https://www.python.org/downloads/
- **IMPORTANT**: Check ✅ "Add Python to PATH"
- Restart PowerShell
- Verify: `python --version`

---

### Issue 2: "pip not found"
```
pip: command not found
```
**Solution**:
- Python installed but not in PATH
- Restart PowerShell after installation
- Or add manually: `C:\Python310\Scripts` to PATH

---

### Issue 3: "ModuleNotFoundError"
```
ModuleNotFoundError: No module named 'fastapi'
```
**Solution**:
- Virtual environment not activated properly
- Delete `venv/` folder completely
- Run `run_setup.bat` again

---

### Issue 4: "Port 8000 already in use"
```
Address already in use
```
**Solution**:
```powershell
netstat -ano | findstr :8000
taskkill /PID <PID> /F
```
Then restart AI service.

---

### Issue 5: Server started but no response
```
curl: Connection refused
```
**Solution**:
- Check AI service is actually running
- Look for "Uvicorn running on http://127.0.0.1:8000" in Terminal 1
- Verify port 8000 is not blocked by firewall

---

### Issue 6: "Connection refused" from Backend
```
Failed to connect to local AI service at localhost:8000
```
**Solution**:
- AI service not running (check Terminal 1)
- Firewall blocking port 8000
- Both services need to be running simultaneously

---

## Quick Reference: What Each Terminal Should Show

### Terminal 1: AI Service (Local FastAPI)
```
INFO:     Started server process [12345]
INFO:     Uvicorn running on http://127.0.0.1:8000
INFO:     Application startup complete
```

### Terminal 2: Backend (Java Spring Boot)
```
Started AgridirectApplication in X.XXX seconds (process running for Y.YYY)
Tomcat started on port 8080 (http) with context path ''
o.s.b.w.e.t.TomcatWebServer : Tomcat started on port 8080
```

### Terminal 3: Testing (PowerShell - Your commands)
```
curl outputs and test results
```

---

## Success Criteria

✅ **All tests pass** if you see:

1. ✅ AI service responds to `/api/ai/chat` on port 8000
2. ✅ AI service responds to `/api/ai/crop-advice` on port 8000  
3. ✅ AI service responds to `/api/ai/price-forecast` on port 8000
4. ✅ Backend responds to `/api/farmer/ai/chat` on port 8080
5. ✅ Backend responses contain AI data (calls local service)

---

## Performance Expectations

| Test | Response Time | Notes |
|------|---------------|-------|
| Chat | 50-100ms | Depends on network/CPU |
| Crop Advice | <50ms | Cache enabled |
| Price Forecast | <100ms | Random generation |
| Disease Detection | 100-200ms | Model inference |

---

## Next: Keep Services Running

Once everything works:

1. **Keep AI service running** in Terminal 1
2. **Keep Backend running** in Terminal 2
3. **Use Terminal 3 for testing** or run mobile/web app

---

## Quick Command Reference

### Start AI Service
```powershell
cd c:\Users\nares\Downloads\backend\local_ai_service
.\run_setup.bat
```

### Start Backend (new terminal)
```powershell
cd c:\Users\nares\Downloads\backend
mvn spring-boot:run
```

### Run Tests (new terminal)
```powershell
# Test 1
curl -X POST http://localhost:8000/api/ai/chat -H "Content-Type: application/json" -d '{"message":"Hello","language":"English"}'

# Test 2
curl -X POST http://localhost:8080/api/farmer/ai/chat -H "Content-Type: application/json" -d '{"message":"Hello","language":"English"}'
```

---

## Documentation Files

After setup, refer to:

1. **QUICK_START.txt** - One-page reference
2. **AI_TESTING_GUIDE.md** - Detailed test procedures
3. **CUSTOM_AI_SETUP.md** - Advanced setup & deployment
4. **AI_IMPLEMENTATION_SUMMARY.md** - Architecture overview

---

## Questions?

If something doesn't work:

1. Check the **Troubleshooting** section above
2. Look at AI service logs in Terminal 1
3. Look at Backend logs in Terminal 2
4. Try the tests in **AI_TESTING_GUIDE.md**

---

**Ready? Start with: `cd c:\Users\nares\Downloads\backend\local_ai_service && .\run_setup.bat`**

Let me know when you complete each phase! 🌾
