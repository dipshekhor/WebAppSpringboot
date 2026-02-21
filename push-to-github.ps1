# Push to GitHub Script
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "Pushing to GitHub Repository" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

Set-Location "G:\KUET\Projects\intellij\sepm_assignment"

try {
    Write-Host "Step 1: Initializing Git repository..." -ForegroundColor Yellow
    git init
    if ($LASTEXITCODE -ne 0) { throw "Git init failed" }

    Write-Host "`nStep 2: Adding all files..." -ForegroundColor Yellow
    git add .
    if ($LASTEXITCODE -ne 0) { throw "Git add failed" }

    Write-Host "`nStep 3: Creating initial commit..." -ForegroundColor Yellow
    git commit -m "Initial commit: School Management System with Spring Boot, PostgreSQL, and Docker"
    if ($LASTEXITCODE -ne 0) { throw "Git commit failed" }

    Write-Host "`nStep 4: Renaming branch to main..." -ForegroundColor Yellow
    git branch -M main
    if ($LASTEXITCODE -ne 0) { throw "Git branch failed" }

    Write-Host "`nStep 5: Adding remote origin..." -ForegroundColor Yellow
    git remote add origin https://github.com/ripWr3ncH/student_teacher_springboot.git
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Remote already exists, updating..." -ForegroundColor Yellow
        git remote set-url origin https://github.com/ripWr3ncH/student_teacher_springboot.git
    }

    Write-Host "`nStep 6: Pushing to GitHub..." -ForegroundColor Yellow
    Write-Host "This may take a moment..." -ForegroundColor Gray
    git push -u origin main
    if ($LASTEXITCODE -ne 0) { throw "Git push failed" }

    Write-Host "`n============================================" -ForegroundColor Green
    Write-Host "SUCCESS! Project pushed to GitHub!" -ForegroundColor Green
    Write-Host "============================================" -ForegroundColor Green
    Write-Host "`nRepository URL:" -ForegroundColor Cyan
    Write-Host "https://github.com/ripWr3ncH/student_teacher_springboot" -ForegroundColor White

} catch {
    Write-Host "`n============================================" -ForegroundColor Red
    Write-Host "ERROR: $_" -ForegroundColor Red
    Write-Host "============================================" -ForegroundColor Red
    Write-Host "`nTroubleshooting:" -ForegroundColor Yellow
    Write-Host "1. Make sure Git is installed and in PATH" -ForegroundColor Gray
    Write-Host "2. Check your GitHub credentials" -ForegroundColor Gray
    Write-Host "3. Verify the repository exists on GitHub" -ForegroundColor Gray
    Write-Host "4. Try running: git config --global user.email 'your@email.com'" -ForegroundColor Gray
    Write-Host "5. Try running: git config --global user.name 'Your Name'" -ForegroundColor Gray
}

Write-Host "`nPress any key to exit..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
