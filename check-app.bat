@echo off
echo ============================================
echo   School Management System - Status Check
echo ============================================
echo.

echo [Checking Docker containers...]
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo.

echo [Checking if port 8081 is responding...]
curl -s http://localhost:8081 >nul 2>&1
if %errorlevel% == 0 (
    echo SUCCESS: Application is responding on port 8081!
    echo.
    echo Open your browser and go to:
    echo   http://localhost:8081
    echo.
    echo Login credentials:
    echo   Admin: admin / adminpass
    echo   User:  user / userpass
) else (
    echo ERROR: Port 8081 is not responding
    echo.
    echo Starting Docker containers...
    docker compose up -d
    echo.
    echo Wait 30-60 seconds, then run this script again.
)

echo.
echo ============================================
pause
