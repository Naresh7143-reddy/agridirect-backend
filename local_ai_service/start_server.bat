@echo off
echo ===================================================
echo Starting Krishi AI Service Server (Port 8000)
echo ===================================================

cd /d "%~dp0"

:: Check Python
py --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Python was not found. Please install Python 3.10+ and add to PATH.
    echo Download from: https://www.python.org/downloads/
    pause
    exit /b 1
)

:: Check Virtual Environment exists
if not exist "venv" (
    echo ERROR: Virtual environment not found. Run run_setup.bat first.
    pause
    exit /b 1
)

:: Check Models exist
if not exist "models\disease_model.pth" (
    echo ERROR: Disease model not found. Run run_setup.bat first.
    pause
    exit /b 1
)

if not exist "models\chatbot_pipeline.pkl" (
    echo ERROR: Chatbot model not found. Run run_setup.bat first.
    pause
    exit /b 1
)

:: Activate Virtual Environment
echo Activating virtual environment...
call venv\Scripts\activate.bat

:: Start Server
echo ===================================================
echo Server Starting on http://localhost:8000
echo ===================================================
echo.
echo Test the server:
echo   curl -X POST http://localhost:8000/api/ai/chat ^
echo     -H "Content-Type: application/json" ^
echo     -d "{\"message\":\"hello\",\"language\":\"English\"}"
echo.
echo Press CTRL+C to stop
echo ===================================================
echo.

py -m uvicorn app:app --reload --port 8000 --host 127.0.0.1

pause
