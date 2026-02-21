# API Testing Guide
This document provides curl commands and examples to test all REST API endpoints.
## Prerequisites
- Application running at http://localhost:8081
- curl installed (or use Postman/Insomnia)
- Credentials: admin/adminpass or user/userpass
## Authentication
All API endpoints require Basic Authentication.
- Admin credentials: admin:adminpass
- User credentials: user:userpass
---
## TEACHERS API
### 1. Get All Teachers
`ash
curl -X GET http://localhost:8081/api/teachers -u admin:adminpass
`
**Expected Response:**
`json
[
  {
    "id": 1,
    "name": "Dr. John Smith",
    "email": "john.smith@school.edu",
    "department": "Computer Science"
  }
]
`
### 2. Get Teacher by ID
`ash
curl -X GET http://localhost:8081/api/teachers/1 -u admin:adminpass
`
### 3. Create New Teacher
`ash
curl -X POST http://localhost:8081/api/teachers \
  -u admin:adminpass \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Dr. Jane Doe",
    "email": "jane.doe@school.edu",
    "department": "Mathematics"
  }'
`
**Expected Response:** 201 Created
`json
{
  "id": 2,
  "name": "Dr. Jane Doe",
  "email": "jane.doe@school.edu",
  "department": "Mathematics"
}
`
### 4. Update Teacher
`ash
curl -X PUT http://localhost:8081/api/teachers/1 \
  -u admin:adminpass \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Dr. John Smith Updated",
    "email": "john.smith@school.edu",
    "department": "Computer Science"
  }'
`
### 5. Delete Teacher
`ash
curl -X DELETE http://localhost:8081/api/teachers/1 -u admin:adminpass
`
**Expected Response:** 204 No Content
---
## STUDENTS API
### 1. Get All Students
`ash
curl -X GET http://localhost:8081/api/students -u admin:adminpass
`
**Expected Response:**
`json
[
  {
    "id": 1,
    "name": "Alice Johnson",
    "email": "alice@student.edu",
    "studentId": "STU001",
    "teacher": {
      "id": 1,
      "name": "Dr. John Smith"
    }
  }
]
`
### 2. Get Student by ID
`ash
curl -X GET http://localhost:8081/api/students/1 -u admin:adminpass
`
### 3. Create New Student
`ash
curl -X POST http://localhost:8081/api/students \
  -u admin:adminpass \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Bob Wilson",
    "email": "bob.wilson@student.edu",
    "studentId": "STU002",
    "teacher": {
      "id": 1
    }
  }'
`
**Expected Response:** 201 Created
`json
{
  "id": 2,
  "name": "Bob Wilson",
  "email": "bob.wilson@student.edu",
  "studentId": "STU002",
  "teacher": {
    "id": 1,
    "name": "Dr. John Smith"
  }
}
`
### 4. Update Student
`ash
curl -X PUT http://localhost:8081/api/students/1 \
  -u admin:adminpass \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Johnson Updated",
    "email": "alice.updated@student.edu",
    "studentId": "STU001",
    "teacher": {
      "id": 1
    }
  }'
`
### 5. Delete Student
`ash
curl -X DELETE http://localhost:8081/api/students/1 -u admin:adminpass
`
---
## COURSES API
### 1. Get All Courses
`ash
curl -X GET http://localhost:8081/api/courses -u admin:adminpass
`
**Expected Response:**
`json
[
  {
    "id": 1,
    "title": "Introduction to Programming",
    "code": "CS101",
    "credits": 3,
    "teacher": {
      "id": 1,
      "name": "Dr. John Smith"
    }
  }
]
`
### 2. Get Course by ID
`ash
curl -X GET http://localhost:8081/api/courses/1 -u admin:adminpass
`
### 3. Create New Course
`ash
curl -X POST http://localhost:8081/api/courses \
  -u admin:adminpass \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Data Structures",
    "code": "CS201",
    "credits": 4,
    "teacher": {
      "id": 1
    }
  }'
`
**Expected Response:** 201 Created
`json
{
  "id": 2,
  "title": "Data Structures",
  "code": "CS201",
  "credits": 4,
  "teacher": {
    "id": 1,
    "name": "Dr. John Smith"
  }
}
`
### 4. Update Course
`ash
curl -X PUT http://localhost:8081/api/courses/1 \
  -u admin:adminpass \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Introduction to Programming Updated",
    "code": "CS101",
    "credits": 3,
    "teacher": {
      "id": 1
    }
  }'
`
### 5. Delete Course
`ash
curl -X DELETE http://localhost:8081/api/courses/1 -u admin:adminpass
`
---
## POWERSHELL COMMANDS (Windows)
If you're using PowerShell, use Invoke-RestMethod:
### Get All Teachers
`powershell
$cred = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("admin:adminpass"))
Invoke-RestMethod -Uri "http://localhost:8081/api/teachers" -Headers @{Authorization="Basic $cred"}
`
### Create Teacher
`powershell
$cred = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("admin:adminpass"))
$body = @{
    name = "Dr. Test Teacher"
    email = "test@school.edu"
    department = "Testing"
} | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8081/api/teachers" 
    -Method POST 
    -Headers @{Authorization="Basic $cred"; "Content-Type"="application/json"} 
    -Body $body
`
---
## ERROR RESPONSES
### 401 Unauthorized
`json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required"
}
`
**Solution:** Check your credentials
### 404 Not Found
`json
{
  "status": 404,
  "error": "Not Found",
  "message": "Teacher not found with id: 999"
}
`
**Solution:** Use a valid ID
### 400 Bad Request
`json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed"
}
`
**Solution:** Check your request body format
---
## TESTING WORKFLOW
### Complete Test Sequence
1. **Create a Teacher**
`ash
curl -X POST http://localhost:8081/api/teachers \
  -u admin:adminpass \
  -H "Content-Type: application/json" \
  -d '{"name":"Prof. Test","email":"prof@test.edu","department":"Testing"}'
`
2. **Create a Student (assign to Teacher ID 1)**
`ash
curl -X POST http://localhost:8081/api/students \
  -u admin:adminpass \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Student","email":"student@test.edu","studentId":"TEST001","teacher":{"id":1}}'
`
3. **Create a Course (assign to Teacher ID 1)**
`ash
curl -X POST http://localhost:8081/api/courses \
  -u admin:adminpass \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Course","code":"TEST101","credits":3,"teacher":{"id":1}}'
`
4. **Verify all data**
`ash
curl -X GET http://localhost:8081/api/teachers -u admin:adminpass
curl -X GET http://localhost:8081/api/students -u admin:adminpass
curl -X GET http://localhost:8081/api/courses -u admin:adminpass
`
---
## POSTMAN COLLECTION
If using Postman:
1. Create a new collection named "School Management API"
2. Set up Authorization:
   - Type: Basic Auth
   - Username: admin
   - Password: adminpass
3. Create requests for each endpoint
4. Set Content-Type header to application/json for POST/PUT requests
---
## HTTP STATUS CODES
| Code | Meaning | When |
|------|---------|------|
| 200 | OK | GET, PUT success |
| 201 | Created | POST success |
| 204 | No Content | DELETE success |
| 400 | Bad Request | Invalid input |
| 401 | Unauthorized | Missing/wrong credentials |
| 404 | Not Found | Resource doesn't exist |
| 500 | Server Error | Server-side issue |
---
## Notes
- All timestamps are in UTC
- IDs are auto-generated by the database
- Deleting a teacher will fail if students/courses are assigned to them
- Email fields should be unique
