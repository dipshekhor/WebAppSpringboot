# School Management System

A full-stack web application for managing students, teachers, and courses built with **Spring Boot**, **PostgreSQL**, and **Docker**.

---

## 🚀 Quick Navigation to Code

**Need to see the code? Click on any file below:**

### Backend Code (Java)
| Component | File | Description |
|-----------|------|-------------|
| 🎯 **Main App** | [SepmAssignmentApplication.java](src/main/java/com/example/sepm_assignment/SepmAssignmentApplication.java) | Application entry point |
| 🔐 **Security** | [SecurityConfig.java](src/main/java/com/example/sepm_assignment/config/SecurityConfig.java) | Authentication setup |
| 📡 **Controllers** | [TeacherController](src/main/java/com/example/sepm_assignment/controller/TeacherController.java) • [StudentController](src/main/java/com/example/sepm_assignment/controller/StudentController.java) • [CourseController](src/main/java/com/example/sepm_assignment/controller/CourseController.java) | REST API endpoints |
| 📦 **Models** | [Teacher](src/main/java/com/example/sepm_assignment/model/Teacher.java) • [Student](src/main/java/com/example/sepm_assignment/model/Student.java) • [Course](src/main/java/com/example/sepm_assignment/model/Course.java) | Database entities |
| 🗄️ **Repositories** | [TeacherRepository](src/main/java/com/example/sepm_assignment/repository/TeacherRepository.java) • [StudentRepository](src/main/java/com/example/sepm_assignment/repository/StudentRepository.java) • [CourseRepository](src/main/java/com/example/sepm_assignment/repository/CourseRepository.java) | Data access layer |
| 💼 **Services** | [TeacherService](src/main/java/com/example/sepm_assignment/service/TeacherService.java) • [StudentService](src/main/java/com/example/sepm_assignment/service/StudentService.java) • [CourseService](src/main/java/com/example/sepm_assignment/service/CourseService.java) | Business logic |

### Frontend Code
| File | Description |
|------|-------------|
| [**index.html**](src/main/resources/static/index.html) | Modern UI with login page |
| [**app.js**](src/main/resources/static/app.js) | JavaScript API integration |

### Configuration Files
| File | Purpose |
|------|---------|
| [**application.properties**](src/main/resources/application.properties) | Database & server config |
| [**pom.xml**](pom.xml) | Maven dependencies |
| [**Dockerfile**](Dockerfile) | Container build |
| [**compose.yaml**](compose.yaml) | Docker setup |

### Testing
| File | Purpose |
|------|---------|
| [**API_TEST.md**](API_TEST.md) | API testing with curl |
| [**postman_collection.json**](postman_collection.json) | Postman collection |

---

## 📋 Table of Contents
1. [Project Overview](#project-overview)
2. [Architecture](#architecture)
3. [Technologies Used](#technologies-used)
4. [Project Structure](#project-structure)
5. [Database Design](#database-design)
6. [REST API Endpoints](#rest-api-endpoints)
7. [Security](#security)
8. [How to Run](#how-to-run)
9. [Docker Configuration](#docker-configuration)
10. [Frontend](#frontend)
---

## 📖 Project Overview

This School Management System is a web-based application that allows administrators to manage:

- **👨‍🏫 Teachers** - Add, view, update, and delete teacher records
- **👨‍🎓 Students** - Manage student information with teacher assignments
- **📚 Courses** - Create and manage courses taught by teachers

The application follows the **MVC (Model-View-Controller)** architecture pattern and implements **RESTful API** principles.

---
## 🏗️ Architecture

### MVC Pattern Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                     🌐 CLIENT (Browser)                          │
│              index.html + app.js + style.css                     │
└──────────────────────────┬──────────────────────────────────────┘
                           │ HTTP Requests (REST API)
                           ↓
┌─────────────────────────────────────────────────────────────────┐
│                   📡 CONTROLLER LAYER                            │
│   StudentController | TeacherController | CourseController      │
├─────────────────────────────────────────────────────────────────┤
│  ✓ Handles HTTP requests (GET, POST, PUT, DELETE)               │
│  ✓ Maps URLs to methods (@GetMapping, @PostMapping)             │
│  ✓ Returns JSON responses (@RestController)                     │
│  ✓ Validates input data (@Valid, @RequestBody)                  │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ↓
┌─────────────────────────────────────────────────────────────────┐
│                     💼 SERVICE LAYER                             │
│       StudentService | TeacherService | CourseService           │
├─────────────────────────────────────────────────────────────────┤
│  ✓ Contains business logic                                      │
│  ✓ Handles data validation and processing                       │
│  ✓ Manages transactions (@Transactional)                        │
│  ✓ Implements error handling                                    │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ↓
┌─────────────────────────────────────────────────────────────────┐
│                   🗄️  REPOSITORY LAYER                          │
│   StudentRepository | TeacherRepository | CourseRepository      │
├─────────────────────────────────────────────────────────────────┤
│  ✓ JPA/Hibernate ORM (@Repository)                              │
│  ✓ Database CRUD operations (extends JpaRepository)             │
│  ✓ Custom query methods (findById, findAll, save, delete)       │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ↓
┌─────────────────────────────────────────────────────────────────┐
│                   🐘 DATABASE (PostgreSQL)                       │
│              students | teachers | courses tables               │
└─────────────────────────────────────────────────────────────────┘
```

### 📊 Layered Architecture Components

<table>
<tr>
<th>Layer</th>
<th>Component</th>
<th>Responsibility</th>
<th>Annotations</th>
</tr>
<tr>
<td><b>🎨 Presentation</b></td>
<td>index.html, app.js</td>
<td>User Interface & User Experience</td>
<td>HTML5, CSS3, JavaScript ES6</td>
</tr>
<tr>
<td><b>📡 Controller</b></td>
<td>*Controller.java</td>
<td>Handle HTTP requests, route to services</td>
<td>@RestController, @RequestMapping</td>
</tr>
<tr>
<td><b>💼 Service</b></td>
<td>*Service.java</td>
<td>Business logic implementation</td>
<td>@Service, @Transactional</td>
</tr>
<tr>
<td><b>🗄️ Repository</b></td>
<td>*Repository.java</td>
<td>Database access using Spring Data JPA</td>
<td>@Repository, JpaRepository</td>
</tr>
<tr>
<td><b>📦 Model</b></td>
<td>*.java entities</td>
<td>Entity classes representing database tables</td>
<td>@Entity, @Table, @Id</td>
</tr>
</table>

### 🔄 Request Flow Example

```
User clicks "Add Student" button
         ↓
JavaScript sends POST request to /api/students
         ↓
StudentController.createStudent() receives request
         ↓
StudentService.saveStudent() validates and processes data
         ↓
StudentRepository.save() persists to database
         ↓
PostgreSQL stores the record
         ↓
Response flows back through layers
         ↓
Frontend displays success message
```

---
## 💻 Technologies Used

### 🔧 Backend Technologies
<table>
<tr>
<th>Technology</th>
<th>Version</th>
<th>Purpose</th>
<th>Documentation</th>
</tr>
<tr>
<td>☕ Java</td>
<td>17</td>
<td>Programming language</td>
<td><a href="https://www.oracle.com/java/">Oracle Java</a></td>
</tr>
<tr>
<td>🍃 Spring Boot</td>
<td>3.2.5</td>
<td>Application framework</td>
<td><a href="https://spring.io/projects/spring-boot">Spring Boot Docs</a></td>
</tr>
<tr>
<td>📊 Spring Data JPA</td>
<td>3.2.5</td>
<td>Database ORM</td>
<td><a href="https://spring.io/projects/spring-data-jpa">Spring Data JPA</a></td>
</tr>
<tr>
<td>🔐 Spring Security</td>
<td>6.2.4</td>
<td>Authentication & Authorization</td>
<td><a href="https://spring.io/projects/spring-security">Spring Security</a></td>
</tr>
<tr>
<td>🐘 PostgreSQL</td>
<td>16</td>
<td>Relational Database</td>
<td><a href="https://www.postgresql.org/">PostgreSQL</a></td>
</tr>
<tr>
<td>📦 Maven</td>
<td>3.9.6</td>
<td>Build tool & Dependency management</td>
<td><a href="https://maven.apache.org/">Apache Maven</a></td>
</tr>
</table>

### 🎨 Frontend Technologies
<table>
<tr>
<th>Technology</th>
<th>Purpose</th>
</tr>
<tr>
<td>🌐 HTML5</td>
<td>Page structure and semantic markup</td>
</tr>
<tr>
<td>🎨 CSS3</td>
<td>Styling, animations, and responsive layout</td>
</tr>
<tr>
<td>⚡ JavaScript (ES6+)</td>
<td>Dynamic functionality and API communication</td>
</tr>
<tr>
<td>🔗 Fetch API</td>
<td>REST API communication (AJAX)</td>
</tr>
</table>

### 🐳 DevOps & Tools
<table>
<tr>
<th>Technology</th>
<th>Version</th>
<th>Purpose</th>
</tr>
<tr>
<td>🐳 Docker</td>
<td>Latest</td>
<td>Containerization platform</td>
</tr>
<tr>
<td>🔧 Docker Compose</td>
<td>Latest</td>
<td>Multi-container orchestration</td>
</tr>
<tr>
<td>📮 Postman</td>
<td>Latest</td>
<td>API testing and documentation</td>
</tr>
<tr>
<td>📝 Git</td>
<td>Latest</td>
<td>Version control system</td>
</tr>
</table>

---
## 📁 Project Structure

### 🗂️ Complete Directory Tree

```
sepm_assignment/
│
├── 📄 README.md                          # Project documentation
├── 📄 API_TEST.md                        # API testing guide
├── 📄 postman_collection.json            # Postman API collection
├── 📄 pom.xml                            # Maven dependencies
├── 🐳 Dockerfile                         # Docker image configuration
├── 🐳 compose.yaml                       # Docker Compose setup
│
├── 📂 src/
│   ├── 📂 main/
│   │   ├── 📂 java/com/example/sepm_assignment/
│   │   │   │
│   │   │   ├── 🎯 SepmAssignmentApplication.java    # ⚡ Application Entry Point
│   │   │   │                                         # - Contains main() method
│   │   │   │                                         # - @SpringBootApplication annotation
│   │   │   │                                         # - Starts embedded Tomcat server
│   │   │   │
│   │   │   ├── 📂 config/                            # ⚙️ Configuration Classes
│   │   │   │   └── 🔐 SecurityConfig.java            # - Spring Security setup
│   │   │   │                                         # - HTTP Basic Authentication
│   │   │   │                                         # - User credentials (admin/user)
│   │   │   │                                         # - CORS configuration
│   │   │   │
│   │   │   ├── 📂 controller/                        # 📡 REST API Controllers
│   │   │   │   ├── 👨‍🏫 TeacherController.java        # Teacher CRUD endpoints
│   │   │   │   │                                     # - GET /api/teachers
│   │   │   │   │                                     # - POST /api/teachers
│   │   │   │   │                                     # - PUT /api/teachers/{id}
│   │   │   │   │                                     # - DELETE /api/teachers/{id}
│   │   │   │   │
│   │   │   │   ├── 👨‍🎓 StudentController.java        # Student CRUD endpoints
│   │   │   │   │                                     # - GET /api/students
│   │   │   │   │                                     # - POST /api/students
│   │   │   │   │                                     # - PUT /api/students/{id}
│   │   │   │   │                                     # - DELETE /api/students/{id}
│   │   │   │   │
│   │   │   │   └── 📚 CourseController.java          # Course CRUD endpoints
│   │   │   │                                         # - GET /api/courses
│   │   │   │                                         # - POST /api/courses
│   │   │   │                                         # - PUT /api/courses/{id}
│   │   │   │                                         # - DELETE /api/courses/{id}
│   │   │   │
│   │   │   ├── 📂 model/                             # 📦 Entity Classes (Database Models)
│   │   │   │   ├── 👨‍🏫 Teacher.java                  # Teacher entity
│   │   │   │   │                                     # - @Entity, @Table("teachers")
│   │   │   │   │                                     # - Fields: id, name, email, department
│   │   │   │   │                                     # - @OneToMany with Students
│   │   │   │   │                                     # - @OneToMany with Courses
│   │   │   │   │
│   │   │   │   ├── 👨‍🎓 Student.java                  # Student entity
│   │   │   │   │                                     # - @Entity, @Table("students")
│   │   │   │   │                                     # - Fields: id, name, email, studentId
│   │   │   │   │                                     # - @ManyToOne with Teacher
│   │   │   │   │
│   │   │   │   └── 📚 Course.java                    # Course entity
│   │   │   │                                         # - @Entity, @Table("courses")
│   │   │   │                                         # - Fields: id, title, code, credits
│   │   │   │                                         # - @ManyToOne with Teacher
│   │   │   │
│   │   │   ├── 📂 repository/                        # 🗄️ Data Access Layer (JPA Repositories)
│   │   │   │   ├── 👨‍🏫 TeacherRepository.java        # Teacher data operations
│   │   │   │   │                                     # - extends JpaRepository
│   │   │   │   │                                     # - Auto-generated CRUD methods
│   │   │   │   │
│   │   │   │   ├── 👨‍🎓 StudentRepository.java        # Student data operations
│   │   │   │   │                                     # - extends JpaRepository
│   │   │   │   │                                     # - Custom query methods
│   │   │   │   │
│   │   │   │   └── 📚 CourseRepository.java          # Course data operations
│   │   │   │                                         # - extends JpaRepository
│   │   │   │                                         # - findById, findAll, save, delete
│   │   │   │
│   │   │   └── 📂 service/                           # 💼 Business Logic Layer
│   │   │       ├── 👨‍🏫 TeacherService.java           # Teacher business logic
│   │   │       │                                     # - saveTeacher(), getAllTeachers()
│   │   │       │                                     # - getTeacherById(), deleteTeacher()
│   │   │       │                                     # - Data validation
│   │   │       │
│   │   │       ├── 👨‍🎓 StudentService.java           # Student business logic
│   │   │       │                                     # - saveStudent(), getAllStudents()
│   │   │       │                                     # - getStudentById(), deleteStudent()
│   │   │       │                                     # - Teacher assignment validation
│   │   │       │
│   │   │       └── 📚 CourseService.java             # Course business logic
│   │   │                                             # - saveCourse(), getAllCourses()
│   │   │                                             # - getCourseById(), deleteCourse()
│   │   │                                             # - Course code uniqueness check
│   │   │
│   │   └── 📂 resources/
│   │       ├── ⚙️ application.properties             # 🔧 Application Configuration
│   │       │                                         # - Database connection (PostgreSQL)
│   │       │                                         # - Server port (8081)
│   │       │                                         # - JPA/Hibernate settings
│   │       │                                         # - Logging configuration
│   │       │
│   │       └── 📂 static/                            # 🌐 Frontend Files (Served by Spring Boot)
│   │           ├── 🌐 index.html                     # Main HTML page
│   │           │                                     # - Login form
│   │           │                                     # - Dashboard layout
│   │           │                                     # - Tab-based navigation
│   │           │
│   │           └── ⚡ app.js                          # JavaScript logic
│   │                                                 # - API calls using Fetch
│   │                                                 # - DOM manipulation
│   │                                                 # - Form handling
│   │                                                 # - Authentication
│   │
│   └── 📂 test/                                      # 🧪 Test Files (JUnit & Integration Tests)
│
└── 📂 target/                                        # 🎯 Compiled Files (Auto-generated by Maven)
    └── sepm_assignment-0.0.1-SNAPSHOT.jar           # Executable JAR file
```

### 🎯 Key Files Explanation

<table>
<tr>
<th>File Type</th>
<th>Files</th>
<th>Purpose</th>
</tr>
<tr>
<td><b>🎯 Entry Point</b></td>
<td><code>SepmAssignmentApplication.java</code></td>
<td>Main method to start Spring Boot application</td>
</tr>
<tr>
<td><b>📡 Controllers</b></td>
<td><code>*Controller.java</code></td>
<td>Handle HTTP requests and return JSON responses</td>
</tr>
<tr>
<td><b>💼 Services</b></td>
<td><code>*Service.java</code></td>
<td>Business logic, validation, and data processing</td>
</tr>
<tr>
<td><b>🗄️ Repositories</b></td>
<td><code>*Repository.java</code></td>
<td>Database operations using Spring Data JPA</td>
</tr>
<tr>
<td><b>📦 Models</b></td>
<td><code>*.java entities</code></td>
<td>Entity classes mapped to database tables</td>
</tr>
<tr>
<td><b>⚙️ Configuration</b></td>
<td><code>application.properties</code></td>
<td>Database connection, server port, JPA settings</td>
</tr>
<tr>
<td><b>🌐 Frontend</b></td>
<td><code>index.html, app.js</code></td>
<td>User interface and JavaScript logic</td>
</tr>
<tr>
<td><b>🐳 Docker</b></td>
<td><code>Dockerfile, compose.yaml</code></td>
<td>Container configuration and orchestration</td>
</tr>
<tr>
<td><b>📦 Dependencies</b></td>
<td><code>pom.xml</code></td>
<td>Maven dependencies and build configuration</td>
</tr>
</table>

---
## 🗄️ Database Design

### Entity Relationship Diagram (ERD)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                               │
│  ┌──────────────────────┐         ┌──────────────────────┐                  │
│  │    👨‍🏫 TEACHER        │         │    👨‍🎓 STUDENT        │                  │
│  ├──────────────────────┤         ├──────────────────────┤                  │
│  │ 🔑 id (PK)           │◄────┐   │ 🔑 id (PK)           │                  │
│  │ 📝 name              │     │   │ 📝 name              │                  │
│  │ 📧 email             │     │   │ 📧 email             │                  │
│  │ 🏢 department        │     │   │ 🎓 student_id        │                  │
│  └──────────────────────┘     │   │ 🔗 teacher_id (FK)   │                  │
│           │                   │   └──────────┬───────────┘                  │
│           │                   │              │                              │
│           │                   └──────────────┘                              │
│           │                   ONE-TO-MANY                                   │
│           │                   (1 Teacher → Many Students)                   │
│           │                                                                 │
│           │                                                                 │
│           │  ONE-TO-MANY                                                    │
│           │  (1 Teacher → Many Courses)                                     │
│           │                                                                 │
│           └────────────┐                                                    │
│                        │                                                    │
│                        ↓                                                    │
│           ┌──────────────────────┐                                         │
│           │    📚 COURSE         │                                         │
│           ├──────────────────────┤                                         │
│           │ 🔑 id (PK)           │                                         │
│           │ 📝 title             │                                         │
│           │ 🔢 code              │                                         │
│           │ ⭐ credits           │                                         │
│           │ 🔗 teacher_id (FK)   │                                         │
│           └──────────────────────┘                                         │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 🔗 Relationships

<table>
<tr>
<th>Relationship</th>
<th>Type</th>
<th>Description</th>
</tr>
<tr>
<td><b>Teacher → Students</b></td>
<td>One-to-Many</td>
<td>
• One teacher can supervise multiple students<br>
• Each student has one teacher (advisor/supervisor)<br>
• FK: <code>teacher_id</code> in students table<br>
• <code>@OneToMany</code> in Teacher entity<br>
• <code>@ManyToOne</code> in Student entity
</td>
</tr>
<tr>
<td><b>Teacher → Courses</b></td>
<td>One-to-Many</td>
<td>
• One teacher can teach multiple courses<br>
• Each course is taught by one teacher<br>
• FK: <code>teacher_id</code> in courses table<br>
• <code>@OneToMany</code> in Teacher entity<br>
• <code>@ManyToOne</code> in Course entity
</td>
</tr>
</table>

### 📊 Database Tables

<table>
<tr>
<th>Table: teachers</th>
<th>Type</th>
<th>Constraints</th>
</tr>
<tr>
<td>id</td>
<td>BIGINT</td>
<td>PRIMARY KEY, AUTO_INCREMENT</td>
</tr>
<tr>
<td>name</td>
<td>VARCHAR(100)</td>
<td>NOT NULL</td>
</tr>
<tr>
<td>email</td>
<td>VARCHAR(100)</td>
<td>NOT NULL, UNIQUE</td>
</tr>
<tr>
<td>department</td>
<td>VARCHAR(100)</td>
<td>NOT NULL</td>
</tr>
</table>

<table>
<tr>
<th>Table: students</th>
<th>Type</th>
<th>Constraints</th>
</tr>
<tr>
<td>id</td>
<td>BIGINT</td>
<td>PRIMARY KEY, AUTO_INCREMENT</td>
</tr>
<tr>
<td>name</td>
<td>VARCHAR(100)</td>
<td>NOT NULL</td>
</tr>
<tr>
<td>email</td>
<td>VARCHAR(100)</td>
<td>NOT NULL, UNIQUE</td>
</tr>
<tr>
<td>student_id</td>
<td>VARCHAR(50)</td>
<td>NOT NULL, UNIQUE</td>
</tr>
<tr>
<td>teacher_id</td>
<td>BIGINT</td>
<td>FOREIGN KEY → teachers(id)</td>
</tr>
</table>

<table>
<tr>
<th>Table: courses</th>
<th>Type</th>
<th>Constraints</th>
</tr>
<tr>
<td>id</td>
<td>BIGINT</td>
<td>PRIMARY KEY, AUTO_INCREMENT</td>
</tr>
<tr>
<td>title</td>
<td>VARCHAR(100)</td>
<td>NOT NULL</td>
</tr>
<tr>
<td>code</td>
<td>VARCHAR(20)</td>
<td>NOT NULL, UNIQUE</td>
</tr>
<tr>
<td>credits</td>
<td>INTEGER</td>
<td>NOT NULL</td>
</tr>
<tr>
<td>teacher_id</td>
<td>BIGINT</td>
<td>FOREIGN KEY → teachers(id)</td>
</tr>
</table>

---
## 📡 REST API Endpoints

### Base URL: `http://localhost:8081/api`

### 👨‍🏫 Teachers API
| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| GET | `/teachers` | Get all teachers | - | `200 OK` + JSON array |
| GET | `/teachers/{id}` | Get teacher by ID | - | `200 OK` + JSON object |
| POST | `/teachers` | Create new teacher | JSON | `201 Created` + JSON |
| PUT | `/teachers/{id}` | Update teacher | JSON | `200 OK` + JSON |
| DELETE | `/teachers/{id}` | Delete teacher | - | `204 No Content` |

### 👨‍🎓 Students API
| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| GET | `/students` | Get all students | - | `200 OK` + JSON array |
| GET | `/students/{id}` | Get student by ID | - | `200 OK` + JSON object |
| POST | `/students` | Create new student | JSON | `201 Created` + JSON |
| PUT | `/students/{id}` | Update student | JSON | `200 OK` + JSON |
| DELETE | `/students/{id}` | Delete student | - | `204 No Content` |

### 📚 Courses API
| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| GET | `/courses` | Get all courses | - | `200 OK` + JSON array |
| GET | `/courses/{id}` | Get course by ID | - | `200 OK` + JSON object |
| POST | `/courses` | Create new course | JSON | `201 Created` + JSON |
| PUT | `/courses/{id}` | Update course | JSON | `200 OK` + JSON |
| DELETE | `/courses/{id}` | Delete course | - | `204 No Content` |

---
## 🔐 Security

### Authentication
The application uses **HTTP Basic Authentication** with **Spring Security**.

### Users Configuration
| Username | Password | Role | Permissions |
|----------|----------|------|-------------|
| admin | adminpass | ADMIN, USER | Full CRUD access |
| user | userpass | USER | Read-only access |

### Protected Endpoints
- All `/api/**` endpoints require authentication
- Static resources (HTML, CSS, JS) are publicly accessible

---
## 🚀 How to Run

### Prerequisites
- Docker Desktop installed and running
- Git (optional)

### Quick Start
1. **Start the application using Docker Compose**
   ```bash
   docker compose up -d --build
   ```

2. **Wait for containers to start** (about 30-60 seconds)

3. **Open in browser**
   ```
   http://localhost:8081
   ```

4. **Login with credentials**
   - Admin: admin / adminpass
   - User: user / userpass

### Check Application Status
```powershell
.\check-app.ps1
```
Or
```bash
.\check-app.bat
```

### Stop the Application
```bash
docker compose down
```

### View Logs
```bash
docker logs sepm_assignment-app-1
```

---
## 🐳 Docker Configuration

### Dockerfile Explanation
The Dockerfile uses multi-stage build:
1. **Build Stage**: Uses Maven to compile and package the application
2. **Run Stage**: Uses lightweight JRE image to run the JAR file

### Docker Compose
The `compose.yaml` defines two services:
1. **app**: Spring Boot application (port 8081)
2. **postgres**: PostgreSQL database (port 5432)

### Container Communication
- The app container connects to postgres container using internal Docker network
- PostgreSQL data is persisted using Docker volume (`pgdata`)

---
## 🌐 Frontend

### Single Page Application (SPA)
The frontend is a Single Page Application built with vanilla JavaScript.

### Features
- ✅ Responsive Design - Works on desktop and mobile
- ✅ Tab-based Navigation - Teachers, Students, Courses
- ✅ Dynamic Data Loading - Fetches data from REST API
- ✅ Form Validation - Client-side input validation
- ✅ Real-time Feedback - Success/error messages

### Authentication Flow
1. User enters username and password
2. Frontend sends credentials to API with Basic Auth header
3. If valid - Show main content
4. If invalid - Show error message

---

## ✅ Summary

This project demonstrates:
- ✅ MVC Architecture - Clear separation of concerns
- ✅ REST API Principles - Proper HTTP methods and status codes
- ✅ PostgreSQL Database - Relational data storage
- ✅ One-to-Many Relationships - Teacher to Students, Teacher to Courses
- ✅ Spring Security - Authentication and authorization
- ✅ Docker Containerization - Easy deployment
- ✅ Responsive Frontend - User-friendly interface

---

## 👨‍💻 Author
DEWAN SALMAN  RAHMAN ZISAN
