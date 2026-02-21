# Testing Guide — sepm_assignment

> **Total: 119 tests · 0 failures · BUILD SUCCESS**

This document explains every decision made when building the test suite:
which tool was chosen at each layer, **why** that tool fits that layer,
**what** is tested in each class, and **how** everything was set up
so all tests run without needing a real database or a running server.

---

## Table of Contents
1. [Overall Test Philosophy](#overall-test-philosophy)
2. [Project Structure](#project-structure)
3. [Infrastructure Setup — What Was Changed and Why](#infrastructure-setup--what-was-changed-and-why)
4. [Phase 1 — Unit Tests](#phase-1--unit-tests)
5. [Phase 2 — Integration Tests](#phase-2--integration-tests)
6. [GitHub Actions CI/CD — How It Works and Why](#github-actions-cicd--how-it-works-and-why)
7. [Running Tests](#running-tests)
8. [Results Summary](#results-summary)

---

## Overall Test Philosophy

### Why two phases instead of just one?

Many projects write only one kind of test and miss problems that only appear when
components work together. This project uses **two separate phases** to catch
both kinds of problems:

| Phase | Kind | Spring context started? | Database needed? | Typical speed |
|-------|------|------------------------|-----------------|--------------|
| 1 | Unit tests | No (service) / Web slice only (controller) | No | < 5 seconds total |
| 2 | Integration tests | JPA slice (repository) / Full (controller IT) | H2 in-memory | 10–20 seconds total |

**The rule**: run unit tests first. If they all pass, move on to integration
tests. This way, a bug in the business logic is caught in milliseconds
instead of waiting 15+ seconds for the full application to start.

### What does each phase prove?

- **Unit tests** check one class at a time in complete isolation. Every external
  dependency is replaced with a fake, so if a test fails the only possible cause
  is a bug in that one class — nothing else.
- **Integration tests** check that all the pieces work correctly together:
  the HTTP request reaches the right controller, the database stores the right
  data, and the security rules are enforced end-to-end.

---

## Project Structure

```
src/
├── main/java/com/example/sepm_assignment/
│   ├── config/SecurityConfig.java          ← in-memory users, @PreAuthorize enabled
│   ├── controller/                         ← REST endpoints (StudentController, etc.)
│   ├── service/                            ← business logic (StudentService, etc.)
│   ├── repository/                         ← Spring Data JPA interfaces
│   └── model/                              ← JPA entities (Student, Course, Teacher)
│
└── test/
    ├── resources/
    │   └── application-test.properties     ← H2 datasource, overrides PostgreSQL for tests
    │
    └── java/com/example/sepm_assignment/
        │
        ├── SepmAssignmentApplicationTests.java      ← smoke test (context loads)
        │
        ├── service/                                 ← UNIT: pure Mockito, no Spring
        │   ├── StudentServiceTest.java
        │   ├── CourseServiceTest.java
        │   └── TeacherServiceTest.java
        │
        ├── controller/                              ← UNIT: @WebMvcTest slice
        │   ├── StudentControllerTest.java
        │   ├── CourseControllerTest.java
        │   └── TeacherControllerTest.java
        │
        └── integration/
            ├── repository/                          ← INTEGRATION: @DataJpaTest + H2
            │   ├── StudentRepositoryTest.java
            │   ├── CourseRepositoryTest.java
            │   └── TeacherRepositoryTest.java
            │
            └── controller/                          ← INTEGRATION: @SpringBootTest + H2
                ├── StudentControllerIT.java
                ├── CourseControllerIT.java
                └── TeacherControllerIT.java
```

---

## Infrastructure Setup — What Was Changed and Why

Before any test could run, three changes were made to the project configuration.
Here is exactly what was done and the reason for each decision.

---

### 1. H2 in-memory database added to `pom.xml`

**What was done:**
```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>   <!-- only on the test classpath, never in production -->
</dependency>
```

**Why H2?**

In production, the app uses PostgreSQL running inside Docker. We cannot use
that database for tests because:
- The Docker container would have to be running every time someone runs tests.
- Tests could delete or overwrite real data.
- Tests would be slow and unpredictable.

H2 is a lightweight database that runs entirely inside the Java program itself —
no installation, no Docker, no network connection needed. It starts in
milliseconds and disappears when the tests finish. It is the standard
test database for Spring Boot projects.

`scope=test` makes sure H2 is never bundled into the production build or Docker image.

---

### 2. Test application properties file created

**File:** `src/test/resources/application-test.properties`

**What it contains and why each line exists:**

```properties
# Replace the PostgreSQL URL with an H2 in-memory URL.
# MODE=PostgreSQL tells H2 to accept PostgreSQL-specific SQL syntax
# so the same JPA/Hibernate queries work on both databases.
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL

# H2's own JDBC driver — must match the H2 URL scheme.
spring.datasource.driver-class-name=org.h2.Driver

# Standard H2 credentials for in-memory mode.
spring.datasource.username=sa
spring.datasource.password=

# Tell Hibernate to use H2 dialect (SQL generation is slightly different from PostgreSQLDialect).
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# "create-drop": Hibernate creates all tables at startup and drops them when
# the context closes. Every test run starts with a completely fresh schema.
spring.jpa.hibernate.ddl-auto=create-drop

# The production pom.xml includes spring-boot-docker-compose.
# Without this line, Spring Boot tries to start Docker Compose during tests,
# which fails in CI/CD and wastes time locally.
spring.docker.compose.enabled=false
```

**How it is activated:**

This file is only loaded when the Spring profile `"test"` is active.
Every integration test class has this annotation:
```java
@ActiveProfiles("test")
```
This tells Spring Boot to read `application-test.properties` on top of the
normal `application.properties`, replacing only the settings that are
re-defined (the database URL, driver, and credentials).
Service and controller unit tests never start a Spring context at all,
so this file has no effect on them.

---

### 3. Maven Surefire plugin configured to include `*IT.java` files

**What was done in `pom.xml`:**
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <includes>
            <include>**/*Test.java</include>
            <include>**/*Tests.java</include>
            <include>**/*IT.java</include>   <!-- added line -->
        </includes>
    </configuration>
</plugin>
```

**Why this was needed:**

Maven's test runner only looks for files named `*Test.java` or `*Tests.java`
by default. The integration test classes were named with the `IT` suffix
(e.g., `TeacherControllerIT`) to make it immediately obvious they are
integration tests, not unit tests. Without this extra configuration,
`mvn test` would silently skip all 34 integration tests with no warning.

---

## Phase 1 — Unit Tests

Unit tests test **one class at a time**, replacing every collaborating class
with a fake (mock) so the test only exercises the code inside the class under test.

---

### Why pure Mockito for the Service layer

**Tool used:** `@ExtendWith(MockitoExtension.class)` from JUnit 5 + Mockito

**Where it is used:**
- `StudentServiceTest`, `CourseServiceTest`, `TeacherServiceTest`

**Why this approach is correct for services:**

A service class like `StudentService` only has two jobs: apply the business rules
and call the repository to save or fetch data. The repository is just an interface;
its correctness is tested separately in the repository integration tests.

Using Mockito means:
- No Spring context is started — the test is just a plain Java class.
- No database connection is made.
- Each test finishes in under 5 ms.
- If a test fails, the only possible cause is a bug inside that service method —
  not a database issue, not a Spring wiring issue.

**How it works:**

```java
@ExtendWith(MockitoExtension.class)   // activates Mockito without Spring
class StudentServiceTest {

    @Mock                              // creates a fake StudentRepository
    private StudentRepository studentRepository;

    @Mock                              // creates a fake TeacherRepository
    private TeacherRepository teacherRepository;

    @InjectMocks                       // creates a real StudentService and injects
    private StudentService studentService;  // the mocks above into its constructor

    @Test
    void saveWithTeacher_assignsTeacherAndPersists() {
        // Arrange: tell the fake what to return when called
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(studentRepository.save(student)).thenReturn(student);

        // Act: call the real service method
        Student result = studentService.saveWithTeacher(1L, student);

        // Assert: check the return value
        assertThat(result).isEqualTo(student);

        // Verify: confirm the fake was called with the right argument
        verify(studentRepository).save(student);
    }
}
```

---

### What functions are tested in the Service layer and why

Every public method on every service class is tested because these methods
contain the application's business rules. Skipping any method means a silent
regression could go unnoticed.

#### `TeacherService` — 9 tests in `TeacherServiceTest`

| Test method | Function under test | Why it matters |
|-------------|---------------------|---------------|
| `findAll_returnsAll` | `findAll()` | Verifies the service returns exactly what the repository gives back, no filtering |
| `findById_present` | `findById(id)` | Happy path — teacher exists |
| `findById_missing` | `findById(id)` | Edge case — non-existent ID must return `Optional.empty()`, not throw |
| `findByEmail_present` | `findByEmail(email)` | Happy path — email exists |
| `findByEmail_missing` | `findByEmail(email)` | Edge case — unknown email |
| `save_persistsTeacher` | `save(teacher)` | Verifies the returned object comes from the repository save, not the input reference |
| `update_valid` | `update(id, details)` | Confirms all three mutable fields (name, email, department) are updated |
| `update_missing_throwsException` | `update(id, details)` | Missing ID must throw `RuntimeException` with a descriptive message |
| `delete_delegatesToRepository` | `delete(id)` | Confirms `deleteById` is called on the repository exactly once |

#### `StudentService` — 10 tests in `StudentServiceTest`

| Test method | Function under test | Why it matters |
|-------------|---------------------|---------------|
| `findAll_returnsAll` | `findAll()` | Basic delegation |
| `findById_present` | `findById(id)` | Happy path |
| `findById_missing` | `findById(id)` | Empty Optional edge case |
| `findByTeacherId_returnsStudents` | `findByTeacherId(teacherId)` | Core relationship query |
| `findByTeacherId_empty` | `findByTeacherId(teacherId)` | Teacher with no students |
| `saveWithTeacher_validTeacher` | `saveWithTeacher(teacherId, student)` | Teacher association is set on the student before saving |
| `saveWithTeacher_invalidTeacher_throws` | `saveWithTeacher(teacherId, student)` | Non-existent teacher must throw; `studentRepository.save` must NOT be called |
| `update_valid` | `update(id, studentDetails)` | All three fields updated correctly |
| `update_missing_throws` | `update(id, studentDetails)` | Missing student throws |
| `delete_delegatesToRepository` | `delete(id)` | Delegation check |

#### `CourseService` — 10 tests in `CourseServiceTest`

Mirrors the StudentService tests exactly but for the `Course` entity:
`findAll`, `findById` (found/missing), `findByTeacherId` (found/empty),
`saveWithTeacher` (valid/invalid teacher), `update` (valid/missing), `delete`.

---

### Why `@WebMvcTest` slice for the Controller layer

**Tool used:** `@WebMvcTest` + `@Import(SecurityConfig.class)` + `@MockBean` + `@WithMockUser`

**Where it is used:**
- `StudentControllerTest`, `CourseControllerTest`, `TeacherControllerTest`

**Why not use `@SpringBootTest` for controller unit tests?**

`@SpringBootTest` starts the entire application — the database, JPA, Hibernate,
every service, every repository. For testing a controller alone that is
unnecessary; a controller only handles HTTP logic (reading the request,
building the response, returning the right status code). A full startup
takes 10–15 seconds just to test a 5-line method.

`@WebMvcTest` loads only what the controller needs:
- The HTTP router (`DispatcherServlet`)
- JSON serialization (Jackson)
- The security filter chain
- The one controller class being tested

Everything else — the database, JPA, repositories, services — is not loaded.
The service is replaced with a fake using `@MockBean`:

```java
@WebMvcTest(StudentController.class)
@Import(SecurityConfig.class)      // load the real security rules
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;       // pre-wired HTTP simulation

    @MockBean                      // StudentService is NOT real; it is a Mockito stub
    private StudentService studentService;
```

**Why `@Import(SecurityConfig.class)` is required:**

Without it, `@WebMvcTest` uses a relaxed default security setup that ignores
`@PreAuthorize` rules entirely. The controllers use role-based access control
(`@PreAuthorize("hasRole('ADMIN')")`). Importing `SecurityConfig` activates
the real security rules so that tests actually confirm a `USER` gets `403`
and an `ADMIN` gets `201`.

**Why `@WithMockUser(roles = "ADMIN")` instead of real credentials:**

The real passwords are stored as BCrypt hashes in `SecurityConfig`. Sending
real credentials through HTTP in a test would work but adds unnecessary
complexity. `@WithMockUser` simply tells Spring "treat this test as if a
user with this role is already logged in", skipping the password check step
while still fully testing the role-based rules.

---

### What functions are tested in the Controller layer and why

All three controllers follow the same endpoint pattern. Using `StudentControllerTest`
as the example:

| Test method | Endpoint | HTTP method | What is verified |
|-------------|----------|------------|-----------------|
| `getAllStudents_returnsOk` | `/api/students` | GET | `USER` role → 200, JSON array with correct fields |
| `getAllStudents_unauthenticated_returns401` | `/api/students` | GET | No auth → 401 (Spring Security rejects it) |
| `getStudentById_found` | `/api/students/1` | GET | Student exists → 200, correct JSON body |
| `getStudentById_notFound` | `/api/students/99` | GET | Student missing → 404 |
| `getStudentsByTeacher_returnsList` | `/api/students/teacher/1` | GET | Returns list filtered by teacher |
| `createStudent_returnsCreated` | `/api/students/teacher/1` | POST | `ADMIN` role → 201, saved student in body |
| `createStudent_badRequest_whenTeacherMissing` | `/api/students/teacher/99` | POST | Service throws → controller returns 400 |
| `createStudent_forbidden_forUserRole` | `/api/students/teacher/1` | POST | `USER` role → 403 (write is ADMIN only) |
| `updateStudent_success` | `/api/students/1` | PUT | 200, updated body returned |
| `updateStudent_notFound` | `/api/students/99` | PUT | Service throws → 404 |
| `deleteStudent_returnsNoContent` | `/api/students/1` | DELETE | `ADMIN` → 204, service called once |
| `deleteStudent_forbidden_forUserRole` | `/api/students/1` | DELETE | `USER` → 403 |

`CourseControllerTest` and `TeacherControllerTest` follow the same pattern
for their respective endpoints.

---

## Phase 2 — Integration Tests

Integration tests start a real Spring context (or a JPA slice of it) and
talk to a real database (H2). They verify that all components work correctly
together, which unit tests cannot prove.

---

### Why `@DataJpaTest` + H2 for the Repository layer

**Tool used:** `@DataJpaTest` + `TestEntityManager` + H2

**Where it is used:**
- `StudentRepositoryTest`, `CourseRepositoryTest`, `TeacherRepositoryTest`

**Why not just trust that Spring Data JPA works?**

Spring Data JPA automatically generates query code from method names like
`findByTeacherId`. If the name is misspelled, Spring will crash at startup.
But there is a subtler problem: the method could be named correctly and run
without errors yet still return the wrong data. The only way to be sure a
query is correct is to actually run it against a real database with real rows.

**What `@DataJpaTest` does:**

- Starts only the database-related parts of Spring (JPA, repositories).
- Does **not** start the web layer, security, services, or controllers.
- Automatically swaps PostgreSQL for H2 so no Docker container is needed.
- Wraps each test in a transaction that is **rolled back** when the test ends,
  so data from one test can never interfere with the next one.

**Why `TestEntityManager` for data setup:**

`TestEntityManager` is a special Spring test tool used to insert data directly
into the database before each test runs — without going through the repository
being tested. This is important because if we used the repository to set up
the test data and the repository had a bug, both the setup and the assertion
would fail in a confusing and misleading way.

```java
@DataJpaTest
class TeacherRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;  // inserts seed data

    @Autowired
    private TeacherRepository teacherRepository;  // the class under test

    @BeforeEach
    void setUp() {
        // persist() writes to the H2 database; flush() ensures it is visible
        teacher = entityManager.persistAndFlush(
            new Teacher(null, "John Doe", "john@school.com", "Computer Science", ...)
        );
    }

    @Test
    void findByEmail_match() {
        Optional<Teacher> result = teacherRepository.findByEmail("john@school.com");
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("John Doe");
    }
}
```

---

### What functions are tested in the Repository layer and why

Only **custom query methods** (those declared in the repository interface beyond
standard CRUD) need dedicated tests — Spring Data JPA's built-in `save`,
`findById`, `deleteById` are tested as part of the CRUD scenarios.

#### `TeacherRepository` — 6 tests in `TeacherRepositoryTest`

| Test | Method | Why |
|------|--------|-----|
| `findByEmail_match` | `findByEmail(email)` | Unique email lookup used by security / business logic |
| `findByEmail_noMatch` | `findByEmail(email)` | Must return `Optional.empty()`, not throw |
| `saveAndFindById` | `save` + `findById` | Round-trip persistence |
| `findAll_returnsAll` | `findAll` | Multiple records returned |
| `delete_removesRecord` | `deleteById` | Row actually removed from H2 |
| `update_changesField` | field mutation + `flush` | JPA dirty-checking persists the change |

#### `StudentRepository` — 8 tests in `StudentRepositoryTest`

| Test | Method | Why |
|------|--------|-----|
| `findByTeacherId_returnsStudents` | `findByTeacherId(teacherId)` | Core relationship — must return only students of that teacher |
| `findByTeacherId_empty` | `findByTeacherId(teacherId)` | Teacher with no students must return empty list, not null |
| `findByEmail_match` | `findByEmail(email)` | Unique constraint — must find the right record |
| `findByEmail_noMatch` | `findByEmail(email)` | Missing email |
| `findByStudentId_match` | `findByStudentId(studentId)` | Business key lookup |
| `findByStudentId_noMatch` | `findByStudentId(studentId)` | Missing student ID |
| `saveAndFindById` | `save` + `findById` | Basic CRUD round-trip |
| `delete_removesRecord` | `deleteById` | Row actually removed |

#### `CourseRepository` — 7 tests in `CourseRepositoryTest`

| Test | Method | Why |
|------|--------|-----|
| `findByTeacherId_returnsCourses` | `findByTeacherId(teacherId)` | Relationship query |
| `findByTeacherId_empty` | `findByTeacherId(teacherId)` | No courses edge case |
| `findByCourseCode_match` | `findByCourseCode(code)` | Unique course code lookup |
| `findByCourseCode_noMatch` | `findByCourseCode(code)` | Unknown code |
| `saveAndFindById` | `save` + `findById` | Round-trip |
| `findAll_returnsAll` | `findAll` | Multiple rows |
| `delete_removesRecord` | `deleteById` | Deletion confirmed |

---

### Why `@SpringBootTest` + H2 for the Controller Integration layer

**Tool used:** `@SpringBootTest` + `@AutoConfigureMockMvc` + `@ActiveProfiles("test")` + `@WithMockUser`

**Where it is used:**
- `StudentControllerIT`, `CourseControllerIT`, `TeacherControllerIT`

**Why this is different from `@WebMvcTest`:**

`@WebMvcTest` uses a fake service and never touches the database, so it only
verifies the HTTP layer. `@SpringBootTest` starts the **complete application** —
security, controllers, services, JPA, and H2 all working together, just like
production (but with H2 instead of PostgreSQL). A single test covers the
entire journey:
- The HTTP request is routed to the correct controller method.
- Security rules are enforced by the real filter chain.
- The controller passes the data to the service correctly.
- The service applies the business logic correctly.
- The service calls the repository correctly.
- The repository saves or queries the H2 database correctly.
- The response JSON is built and returned correctly.

This makes it the most thorough test type in the suite — one test verifies
everything at once.

**Why `@AutoConfigureMockMvc`:**

`@SpringBootTest` normally starts a real Tomcat web server on a port.
`@AutoConfigureMockMvc` tells Spring Boot to skip the real server and use
`MockMvc` instead, which simulates HTTP calls inside the JVM process.
This is faster and avoids problems with port numbers already being in use.

**Why `@AfterEach` cleanup:**

Unlike `@DataJpaTest`, `@SpringBootTest` does **not** automatically undo
database changes after each test. Each test must delete its own data
when it finishes, otherwise leftover rows from one test can cause the
next test to fail:

```java
@AfterEach
void cleanUp() {
    studentRepository.deleteAll();
    teacherRepository.deleteAll();
}
```

**How each test is structured:**

```java
@Test
@WithMockUser(roles = "ADMIN")
void createTeacher_persistsAndReturnsCreated() throws Exception {
    Teacher input = new Teacher(null, "New Teacher", "new@school.com", "Physics", ...);

    // 1. Fire a real HTTP POST through the full filter chain
    String responseBody = mockMvc.perform(post("/api/teachers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(input)))
            .andExpect(status().isCreated())         // 2. HTTP response is correct
            .andExpect(jsonPath("$.name").value("New Teacher"))  // 3. JSON body is correct
            .andReturn().getResponse().getContentAsString();

    // 4. Read back from the database directly to prove persistence happened
    Teacher created = objectMapper.readValue(responseBody, Teacher.class);
    assertThat(teacherRepository.findById(created.getId())).isPresent();
}
```

Step 4 — reading directly from the database — is what separates an integration
test from a controller unit test. It proves the data was actually saved to the
database, not just that the controller returned a 201 status code.

---

### What functions are tested in the Controller Integration layer and why

Using `TeacherControllerIT` as the reference (Student and Course follow the same pattern):

| Test | Endpoint | Role | What is verified end-to-end |
|------|----------|------|-----------------------------|
| `getAllTeachers_returnsOk` | GET `/api/teachers` | USER | Returns saved teacher from H2; ID matches |
| `getAllTeachers_unauthenticated` | GET `/api/teachers` | none | 401 from security filter chain |
| `getTeacherById_found` | GET `/api/teachers/{id}` | USER | 200, correct name and department from DB |
| `getTeacherById_notFound` | GET `/api/teachers/{id}` | USER | 404 when row does not exist in H2 |
| `createTeacher_persistsAndReturnsCreated` | POST `/api/teachers` | ADMIN | 201 + DB assertion: `findById` finds the new row |
| `createTeacher_forbidden_forUserRole` | POST `/api/teachers` | USER | 403 from `@PreAuthorize` |
| `updateTeacher_updatesSuccessfully` | PUT `/api/teachers/{id}` | ADMIN | 200 + DB assertion: name actually changed in H2 |
| `updateTeacher_notFound` | PUT `/api/teachers/{id}` | ADMIN | 404 when ID does not exist |
| `deleteTeacher_deletesFromDb` | DELETE `/api/teachers/{id}` | ADMIN | 204 + DB assertion: `findById` returns empty |
| `deleteTeacher_forbidden_forUserRole` | DELETE `/api/teachers/{id}` | USER | 403 |

`StudentControllerIT` and `CourseControllerIT` add two extra tests each:
- `createStudent/Course_badRequest_whenTeacherMissing` — verifies the controller
  returns 400 when the service throws `RuntimeException` because the linked
  teacher does not exist.
- `getStudents/CoursesByTeacher` — verifies the relationship endpoint returns
  only records belonging to that teacher.

---

## GitHub Actions CI/CD — How It Works and Why

**File:** `.github/workflows/ci.yml`

Every time code is pushed to `main`/`master` or a Pull Request is opened,
GitHub automatically runs this pipeline on a free cloud server.
No setup required — GitHub reads the YAML file and executes it.

---

### Why CI/CD matters for this project

Without CI/CD, every developer must remember to run the tests before pushing code.
If one person forgets (or an error only appears on a different machine),
a broken commit reaches the shared branch and can block the whole team.

A CI pipeline makes testing **automatic and unavoidable**:
- No one can accidentally skip them.
- Every commit is tested the exact same way, on a clean server — not just
  "it works on my machine".
- Problems are caught and reported immediately, before they grow.

---

### Pipeline structure — two jobs

```
push / pull_request to main
        │
        ▼
┌─────────────────────┐
│   JOB 1: test       │  ← always runs
│   (ubuntu-latest)   │
└────────┬────────────┘
         │ passes?
         ▼
┌─────────────────────┐
│ JOB 2: docker-build │  ← only runs on push (not on PRs)
│   (ubuntu-latest)   │
└─────────────────────┘
```

Job 2 only starts if Job 1 passes (`needs: test`).
This means a broken test suite never triggers a Docker build.

---

### Job 1 — Build & Test (step by step)

#### Step 1 — `actions/checkout@v4`
```yaml
- name: Checkout source code
  uses: actions/checkout@v4
```
Downloads your repository code onto the runner machine.
Without this, the runner is an empty Linux box with no files.

#### Step 2 — `actions/setup-java@v4`
```yaml
- name: Set up JDK 17
  uses: actions/setup-java@v4
  with:
    java-version: '17'
    distribution: 'temurin'
    cache: 'maven'
```
Installs **JDK 17** (matching `<java.version>17</java.version>` in `pom.xml`)
using the **Eclipse Temurin** distribution — the same one used in the `Dockerfile`.

`cache: 'maven'` stores the `~/.m2` directory between workflow runs.
The first run downloads all Maven dependencies from the internet (~100 MB).
Every subsequent run restores them from cache in seconds.

#### Step 3 — `chmod +x mvnw`
```yaml
- name: Make mvnw executable
  run: chmod +x mvnw
```
On Windows the file permissions are not preserved.
When checked out on Linux the `mvnw` script loses its execute bit.
This one line fixes that; without it the next step fails with "Permission denied".

#### Step 4 — `./mvnw test`
```yaml
- name: Run unit and integration tests
  run: ./mvnw test --batch-mode --no-transfer-progress
```
Runs all **119 tests** — service unit tests, controller unit tests,
repository integration tests, and controller integration tests.

**Why no database is needed in CI:**
The tests use `application-test.properties` activated via `@ActiveProfiles("test")`.
This file points to H2 (in-memory) and sets `spring.docker.compose.enabled=false`.
H2 runs inside the JVM process — no PostgreSQL, no Docker, no network port required.
The CI runner needs nothing extra to run all tests.

`--batch-mode` suppresses interactive prompts (colours, progress bars) that
clutter CI logs.  
`--no-transfer-progress` removes the per-byte download progress lines,
keeping the log readable.

#### Step 5 — Upload test reports
```yaml
- name: Upload test reports
  if: always()
  uses: actions/upload-artifact@v4
  with:
    name: test-reports
    path: target/surefire-reports/
    retention-days: 7
```
Maven Surefire writes an XML + TXT report for every test class to
`target/surefire-reports/`. This step uploads those files as a **downloadable
artifact** in the GitHub Actions UI.

`if: always()` means this step runs **even when tests fail** — which is
exactly when you most need the reports to diagnose what went wrong.
Reports are kept for 7 days then automatically deleted.

---

### Job 2 — Build Docker Image (step by step)

This job only runs on `push` events (not Pull Requests) and only after
Job 1 succeeds.

#### Step 1 — Checkout
Same as Job 1 — downloads the source.

#### Step 2 — `docker/setup-buildx-action@v3`
```yaml
- name: Set up Docker Buildx
  uses: docker/setup-buildx-action@v3
```
Enables Docker BuildKit (the modern build engine).
Required for the layer caching feature used in Step 3.

#### Step 3 — `docker/build-push-action@v5`
```yaml
- name: Build Docker image
  uses: docker/build-push-action@v5
  with:
    context: .
    push: false
    tags: sepm_assignment:latest
    cache-from: type=gha
    cache-to: type=gha,mode=max
```
Builds the Docker image using the project's `Dockerfile`.
`push: false` means the image is built and verified but **not pushed to any registry**.
This proves the image can be built without actually publishing it.

`cache-from/cache-to: type=gha` stores Docker layer cache inside GitHub Actions.
The most expensive layer (`RUN mvn clean package -DskipTests` inside the Dockerfile)
is cached after the first run, making subsequent builds much faster.

---

### How to push to Docker Hub (optional future step)

To push the built image to Docker Hub, add two secrets to your
GitHub repository (`Settings → Secrets → Actions`):
- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN`

Then add a login step and set `push: true`:
```yaml
- name: Log in to Docker Hub
  uses: docker/login-action@v3
  with:
    username: ${{ secrets.DOCKERHUB_USERNAME }}
    password: ${{ secrets.DOCKERHUB_TOKEN }}

- name: Build and push Docker image
  uses: docker/build-push-action@v5
  with:
    context: .
    push: true                          # ← changed from false
    tags: ${{ secrets.DOCKERHUB_USERNAME }}/sepm_assignment:latest
```

---

### Triggering the pipeline

| Event | Job 1 runs? | Job 2 runs? |
|-------|-------------|-------------|
| `git push` to `main`/`master` | ✅ | ✅ (if Job 1 passes) |
| Pull Request opened/updated against `main` | ✅ | ❌ |
| Push to any other branch | ❌ | ❌ |

---

## Running Tests

```bash
# Run the full test suite (unit + integration, all 119 tests)
./mvnw test

# Run only service unit tests (fastest, no Spring context)
./mvnw test -Dtest="StudentServiceTest,CourseServiceTest,TeacherServiceTest"

# Run only controller unit tests (@WebMvcTest slice)
./mvnw test -Dtest="StudentControllerTest,CourseControllerTest,TeacherControllerTest"

# Run only repository integration tests (@DataJpaTest)
./mvnw test -Dtest="StudentRepositoryTest,CourseRepositoryTest,TeacherRepositoryTest"

# Run only controller integration tests (full @SpringBootTest)
./mvnw test -Dtest="StudentControllerIT,CourseControllerIT,TeacherControllerIT"
```

---

## Results Summary

| Test class | Layer | Approach | Tests | Result |
|------------|-------|----------|-------|--------|
| `StudentServiceTest` | Service | Pure Mockito | 10 | ✅ |
| `CourseServiceTest` | Service | Pure Mockito | 10 | ✅ |
| `TeacherServiceTest` | Service | Pure Mockito | 9 | ✅ |
| `StudentControllerTest` | Controller | `@WebMvcTest` + `@MockBean` | 12 | ✅ |
| `CourseControllerTest` | Controller | `@WebMvcTest` + `@MockBean` | 12 | ✅ |
| `TeacherControllerTest` | Controller | `@WebMvcTest` + `@MockBean` | 10 | ✅ |
| `StudentRepositoryTest` | Repository | `@DataJpaTest` + H2 | 8 | ✅ |
| `CourseRepositoryTest` | Repository | `@DataJpaTest` + H2 | 7 | ✅ |
| `TeacherRepositoryTest` | Repository | `@DataJpaTest` + H2 | 6 | ✅ |
| `StudentControllerIT` | API (full stack) | `@SpringBootTest` + H2 | 12 | ✅ |
| `CourseControllerIT` | API (full stack) | `@SpringBootTest` + H2 | 12 | ✅ |
| `TeacherControllerIT` | API (full stack) | `@SpringBootTest` + H2 | 10 | ✅ |
| `SepmAssignmentApplicationTests` | Smoke | `@SpringBootTest` | 1 | ✅ |
| **Total** | | | **119** | **✅** |
