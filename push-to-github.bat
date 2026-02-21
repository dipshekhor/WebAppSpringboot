@echo off
echo ============================================
echo Pushing to GitHub Repository
echo ============================================
echo.

cd /d "G:\KUET\Projects\intellij\sepm_assignment"

echo Step 1: Initializing Git repository...
git init

echo.
echo Step 2: Adding all files...
git add .

echo.
echo Step 3: Creating initial commit...
git commit -m "Initial commit: School Management System with Spring Boot, PostgreSQL, and Docker"

echo.
echo Step 4: Renaming branch to main...
git branch -M main

echo.
echo Step 5: Adding remote origin...
git remote add origin https://github.com/ripWr3ncH/student_teacher_springboot.git

echo.
echo Step 6: Pushing to GitHub...
git push -u origin main

echo.
echo ============================================
echo Done! Check output above for any errors.
echo ============================================
pause
