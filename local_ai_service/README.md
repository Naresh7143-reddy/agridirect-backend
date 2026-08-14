# 🌾 Krishi AI - Local AI Service

Your own custom AI models for crop disease detection and farming chatbot (no third-party APIs).

## Architecture

```
AgriDirect Backend (Java Spring Boot)
         ↓
    AiController (port 8080)
         ↓
   GeminiService
         ↓
    Local AI Service (port 8000)
         ↓
   ┌─────────┬──────────────────┐
   ↓         ↓                  ↓
Chatbot   Disease Detection   Crop Advice
(BERT+NN) (PyTorch CNN)       (Rules)
```

## Quick Start

### 1. Install Python (Required)
- Download Python 3.10+ from https://www.python.org/downloads/
- **Important**: Check "Add Python to PATH" during installation
- Verify: Open PowerShell and run `python --version`

### 2. Run Setup (One-time)
```powershell
cd c:\Users\nares\Downloads\backend\local_ai_service
.\run_setup.bat
```

This will:
- ✅ Create Python virtual environment
- ✅ Install dependencies (PyTorch, FastAPI, scikit-learn, etc.)
- ✅ Train disease detection model (CNN) → `models/disease_model.pth`
- ✅ Train chatbot model (TF-IDF + NN) → `models/chatbot_pipeline.pkl`
- ✅ Start FastAPI server on `http://localhost:8000`

### 3. Keep Server Running
The server must stay running for the backend to work.
- Server listens on: `http://localhost:8000`
- Backend calls it at: `http://localhost:8000/api/ai/chat`, `/api/ai/disease`, etc.

### 4. Test the Integration
Once both services are running:
```bash
# Test Chatbot
curl -X POST http://localhost:8000/api/ai/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Hello","language":"English"}'

# Test Disease Detection (requires base64 image)
curl -X POST http://localhost:8000/api/ai/disease \
  -H "Content-Type: application/json" \
  -d '{"base64Image":"<base64_encoded_image>","cropName":"Tomato"}'
```

## What Each Model Does

### 🤖 Chatbot (TF-IDF + Neural Network)
- **Location**: `models/chatbot_pipeline.pkl`
- **Input**: Farmer questions (e.g., "What crop should I grow?")
- **Output**: Relevant farming advice
- **Intents**: greeting, crop_advice, price_forecast, disease_help, fertilizer, thanks

**Trained on**:
- Greeting patterns
- Crop recommendations
- Price forecast questions
- Disease symptom descriptions
- Fertilizer queries

### 🔍 Disease Detection (PyTorch CNN)
- **Location**: `models/disease_model.pth`
- **Input**: Crop leaf image (224x224 pixels)
- **Output**: Disease name + structured treatment plan
- **Classes**: 
  - Tomato diseases (Healthy, Yellow Leaf Curl Virus)
  - Potato diseases (Healthy, Early Blight, Late Blight)
  - Rice diseases (Healthy, Blast, Brown Spot)

**Current**: Trained on synthetic data (100 samples, 5 epochs)
**Next step**: Train on real disease images for production accuracy

### 📊 Crop Advice (Rule-based)
- Rule-based system (no ML needed)
- Considers: Season, location, soil type, water availability
- Returns: Crop recommendations with care tips

### 💰 Price Forecast (ML-based)
- Simulates market trends based on historical patterns
- Generates realistic price forecasts for farmers

## Training Your Own Models

To improve accuracy, train on your real disease dataset:

```powershell
# Edit train_disease.py to load your dataset instead of synthetic data
# Then retrain:
python train_disease.py

# Same for chatbot - add more intents and patterns
python train_chatbot.py
```

### Dataset Format

**For Disease Detection**:
```
dataset/
├── Tomato___Healthy/
│   ├── image1.jpg
│   ├── image2.jpg
│   └── ...
├── Tomato___Early_Blight/
│   ├── image1.jpg
│   └── ...
└── Rice___Blast/
    └── ...
```

**For Chatbot**:
Edit `train_chatbot.py` → add more intents in the `intents` list

## API Endpoints

All endpoints return `{"response": "..."}` JSON format.

### POST /api/ai/chat
Chat with the farming assistant
```json
{
  "message": "What crop should I grow in monsoon?",
  "language": "English",
  "history": [
    {"role": "user", "content": "Hi"},
    {"role": "assistant", "content": "Hello!"}
  ]
}
```

### POST /api/ai/disease
Detect crop disease from image
```json
{
  "base64Image": "<base64_encoded_image>",
  "cropName": "Tomato",
  "mimeType": "image/jpeg"
}
```

Returns structured treatment plan:
```
ISSUE: Tomato Yellow Leaf Curl Virus
SEVERITY: High
CAUSE: Whitefly vector transmission
SYMPTOMS: Yellow curling leaves...
TREATMENT: 1. Remove infected plants...
PREVENTION: Use insect nets...
URGENCY: Within 2-3 days
```

### POST /api/ai/crop-advice
Get crop recommendations
```json
{
  "season": "Monsoon",
  "location": "Maharashtra",
  "soilType": "Loam",
  "waterAvailability": "High"
}
```

### POST /api/ai/price-forecast
Forecast crop prices
```json
{
  "cropName": "Tomato",
  "location": "Bangalore"
}
```

## Troubleshooting

### Python not found
- Install Python 3.10+ and add to PATH
- Restart PowerShell after installation

### Module not found (torch, fastapi, etc.)
- Delete `venv/` folder
- Run `run_setup.bat` again

### Models not training
- Check `venv\Scripts\activate.bat` is called
- Verify pip packages installed: `pip list | grep torch`

### Backend can't connect to AI service
- Ensure service is running: `http://localhost:8000` should be accessible
- Check firewall allows port 8000
- Logs will show: `Error: Failed to connect to local AI service at localhost:8000`

### Port 8000 already in use
- Kill existing process: `netstat -ano | findstr :8000`
- Or change port in `app.py` (requires rebuild)

## Production Deployment

To deploy to Render or AWS:

1. **Train models locally** - generates `.pth` and `.pkl` files
2. **Docker build** - include models in Docker image
3. **Deploy Python service** separately from Java backend
4. **Configure backend** - update `localhost:8000` to remote AI service URL

Example `Dockerfile`:
```dockerfile
FROM python:3.10-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install -r requirements.txt
COPY . .
RUN python train_chatbot.py && python train_disease.py
CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "8000"]
```

## Performance Notes

- **Chatbot**: ~50ms response time (local inference)
- **Disease Detection**: ~100-200ms (CNN forward pass)
- **Memory**: ~2GB for models loaded
- **CPU**: Runs on CPU (GPU optional for faster inference)

## Next Steps

1. ✅ Train models locally with real disease data
2. ✅ Deploy Python service to Render (separate from Java backend)
3. ✅ Update backend to call remote AI service URL
4. ✅ Add web UI for model retraining
5. ✅ Monitor model performance with feedback loop

---

**Made with 🌾 for Indian farmers**
