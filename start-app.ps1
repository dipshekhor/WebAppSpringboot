Write-Host "=== Stopping existing containers ===" -ForegroundColor Cyan
docker compose down -v

Write-Host "`n=== Building and starting application ===" -ForegroundColor Cyan
docker compose up --build -d

Write-Host "`n=== Waiting for application to start ===" -ForegroundColor Cyan
Start-Sleep -Seconds 45

Write-Host "`n=== Container Status ===" -ForegroundColor Cyan
docker compose ps

Write-Host "`n=== Application Logs ===" -ForegroundColor Cyan
docker logs sepm_assignment-app-1 --tail 30

Write-Host "`n=== Testing Application ===" -ForegroundColor Cyan
Write-Host "Application should be running at: http://localhost:8081" -ForegroundColor Green
Write-Host "Testing if port 8081 is responding..." -ForegroundColor Yellow

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081" -Method GET -TimeoutSec 5 -UseBasicParsing
    Write-Host "✓ Application is responding on port 8081!" -ForegroundColor Green
    Write-Host "Status Code: $($response.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "✗ Application is not responding yet. Please check the logs above." -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== Instructions ===" -ForegroundColor Cyan
Write-Host "1. Open your browser and go to: http://localhost:8081"
Write-Host "2. You should see the custom login page (no browser popup!)"
Write-Host "3. Login credentials:"
Write-Host "   - Admin: username=admin, password=adminpass"
Write-Host "   - User: username=user, password=userpass"
Write-Host "`n4. To view logs: docker logs sepm_assignment-app-1 -f"
Write-Host "5. To stop: docker compose down"
