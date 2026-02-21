Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  School Management System - Quick Check" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "[1/5] Checking Docker..." -ForegroundColor Yellow
$dockerInfo = docker info 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "  OK - Docker is running" -ForegroundColor Green
} else {
    Write-Host "  ERROR - Docker is not running!" -ForegroundColor Red
    exit 1
}
Write-Host ""
Write-Host "[2/5] Checking containers..." -ForegroundColor Yellow
$containers = docker ps --format "{{.Names}}" 2>&1
if ($containers -like "*sepm_assignment-app-1*") {
    Write-Host "  OK - Application container is running" -ForegroundColor Green
} else {
    Write-Host "  ERROR - Application container is NOT running" -ForegroundColor Red
}
if ($containers -like "*sepm_assignment-postgres-1*") {
    Write-Host "  OK - Database container is running" -ForegroundColor Green
} else {
    Write-Host "  ERROR - Database container is NOT running" -ForegroundColor Red
}
Write-Host ""
Write-Host "[3/5] Checking if port 8081 is responding..." -ForegroundColor Yellow
$response = Invoke-WebRequest -Uri "http://localhost:8081" -Method GET -TimeoutSec 5 -UseBasicParsing -ErrorAction SilentlyContinue
if ($response) {
    Write-Host "  OK - Application is responding on port 8081!" -ForegroundColor Green
    $urlWorks = $true
} else {
    Write-Host "  ERROR - Port 8081 is not responding" -ForegroundColor Red
    $urlWorks = $false
}
Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "           RESULTS" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
if ($urlWorks) {
    Write-Host "SUCCESS! Your application is ready!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Open this URL in your browser:" -ForegroundColor White
    Write-Host "  http://localhost:8081" -ForegroundColor Cyan
    Write-Host ""
} else {
    Write-Host "Application is not ready yet" -ForegroundColor Yellow
    Write-Host "Run: docker logs sepm_assignment-app-1" -ForegroundColor Cyan
}
Write-Host ""
