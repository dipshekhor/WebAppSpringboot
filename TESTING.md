# Testing Guide — sepm_assignment

> **Total: 119 tests · 0 failures · BUILD SUCCESS**

This document explains every decision made when building the test suite:
which tool was chosen at each layer, **why** that tool fits that layer,
**what** is tested in each class, and **how** the infrastructure was set up
to make all tests run without a real database or running server.

---

## Table of Contents
1. [Overall Test Philosophy](#overall-test-philosophy)
2. [Project Structure](#project-structure)
3. [Infrastructure Setup — What Was Changed and Why](#infrastructure-setup--what-was-changed-and-why)
4. [Phase 1 — Unit Tests](#phase-1--unit-tests)
5. [Phase 2 — Integration Tests](#phase-2--integration-tests)
6. [Running Tests](#running-tests)
7. [Results Summary](#results-summary)

---

## Overall Test Philosophy

### Why two phases instead of just one?

A common mistake is writing only one kind of test.  
This project uses **two deliberately separate phases**:

| Phase | Kind | Spring context started? | Database needed? | Typical speed |
|-------|------|------------------------|-----------------|--------------|
| 1 | Unit tests | No (service) / Web slice only (controller) | No | < 5 seconds total |
| 2 | Integration tests | JPA slice (repository) / Full (controller IT) | H2 in-memory | 10–20 seconds total |

**The rule**: run unit tests first. If they all pass, proceed to integration
tests. This way, a broken service method is caught in milliseconds, not after
waiting 15 seconds for the full Spring context to boot.

### What does each phase prove?

- **Unit tests** prove the business logic (services) and HTTP layer (controllers)
  work correctly _in isolation_. Dependencies are replaced with fakes, so if a
  test fails it can only be the code under test that is wrong.
- **Integration tests** prove that the code actually works together —
  that SQL queries return the right rows, that HTTP requests travel through
  the full security filter chain, and that data really reaches the database.

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

The production application uses PostgreSQL running inside Docker
(configured via `compose.yaml`). You cannot run integration tests against
that database because:
- Tests would require the Docker container to be running.
- Tests could corrupt or dirty real data.
- Tests would be slow and non-deterministic.

H2 is a pure Java database that runs entirely inside the JVM process —
no installation, no Docker, no network — it starts in milliseconds and
is destroyed when the tests finish. It is the standard choice for
Spring Boot integration tests.

`scope=test` ensures H2 is never included in production builds or the
production Docker image.

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
Every integration test class carries the annotation:
```java
@ActiveProfiles("test")
```
This tells Spring to merge `application-test.properties` on top of
`application.properties`, overriding only the keys that are redefined.
Service unit tests and controller unit tests do not start a full Spring
context at all, so this file is irrelevant to them.

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

Maven Surefire (the plugin that runs `mvn test`) only discovers files
named `*Test.java` or `*Tests.java` by default. Integration test classes
were intentionally named with the `IT` suffix (e.g., `TeacherControllerIT`)
to make it visually obvious they are integration tests, not unit tests.
Without this configuration change, running `mvn test` would silently
skip all 34 controller integration tests.

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

A service class such as `StudentService` has exactly two external dependencies:
`StudentRepository` and `TeacherRepository`. The service's job is to apply
business logic — look up an entity, validate it, mutate it, and call the
repository to save it. The repositories themselves are just interfaces;
their correctness is verified separately in the repository integration tests.

Using Mockito means:
- No Spring application context is started. The test is just a plain Java object.
- No database connection is made.
- Each test runs in under 5 ms.
- If a test fails it can only be caused by a mistake in the service method itself,
  never by a database query or Spring wiring issue.

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

`@SpringBootTest` starts the entire application: JPA, database connection pool,
Hibernate, all services, all repositories. For testing a controller in isolation,
that is wasteful — a controller contains only HTTP-level logic (request parsing,
response building, status codes). Starting a full context would take 10–15 seconds
just to test a method that is 5 lines long.

`@WebMvcTest` loads only:
- The `DispatcherServlet` (HTTP routing)
- Jackson (JSON serialization)
- The security filter chain
- The one controller class specified in the annotation

Everything else — JPA, repositories, services — is NOT loaded.
The service is replaced with `@MockBean`:

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

Without it, `@WebMvcTest` uses a default permissive security configuration that
ignores `@PreAuthorize` annotations. The controllers in this project use role-based
access control (`@PreAuthorize("hasRole('ADMIN')")`). Importing `SecurityConfig`
ensures the real filter chain is active so the tests actually verify that
`USER` gets `403` and `ADMIN` gets `201`.

**Why `@WithMockUser(roles = "ADMIN")` instead of real Basic Auth credentials:**

Real credentials (`admin` / `adminpass`) are stored as BCrypt hashes in
`SecurityConfig`. Sending them over HTTP in a test would work but is
unnecessarily complex. `@WithMockUser` injects a pre-authenticated
`SecurityContext` with the specified roles, bypassing the password-check
step while still exercising the role-based authorization logic.

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

Spring Data JPA generates the implementation of repository interfaces
automatically from their method names (e.g., `findByTeacherId`). If a method
name is misspelt or the return type is wrong, Spring Data will throw at startup,
but there is a subtler failure mode: the query might run without error but
return wrong results. The only way to prove a query is correct is to run it
against a real database with real data.

**What `@DataJpaTest` does:**

- Starts only JPA-related beans: `EntityManagerFactory`, `TransactionManager`,
  `DataSource`, all `@Repository` beans.
- Does **not** start web layer, security, services, or controllers.
- Automatically replaces the configured datasource (PostgreSQL) with H2.
- Wraps each test in a transaction that is **rolled back** at the end, so
  data inserted in one test never leaks into the next test.

**Why `TestEntityManager` for data setup:**

`TestEntityManager` is Spring's test-only wrapper around JPA `EntityManager`.
It is used to insert seed data **directly into the database** before each test,
bypassing the repository being tested. This is important: if the repository
under test is also used to set up the data, a bug in the repository could
cause both setup and assertion to fail in a confusing way.

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

`@WebMvcTest` uses a mocked service, so it never touches the database.
`@SpringBootTest` starts the **complete** application context — security,
controllers, services, JPA, and the H2 database all wired together exactly
as they are in production (minus PostgreSQL). This level of test proves that:
- The HTTP request is correctly routed to the right controller method.
- Security rules are enforced by the real filter chain.
- The controller calls the service correctly.
- The service applies business logic correctly.
- The service calls the repository correctly.
- The repository executes the SQL on H2.
- The response JSON is correctly serialized.

All of those steps happen in one test, making it the highest-confidence
test type in the suite.

**Why `@AutoConfigureMockMvc`:**

`@SpringBootTest` by default actually starts a Tomcat server on a random port.
`@AutoConfigureMockMvc` tells Spring Boot to instead wire `MockMvc`, which
simulates the HTTP layer in-process without a real network socket. This is faster
and avoids port conflicts.

**Why `@AfterEach` cleanup:**

Unlike `@DataJpaTest`, `@SpringBootTest` does **not** roll back transactions
automatically (a full `@Transactional` rollback would interfere with tests that
verify background transactions). Each integration test must clean up its own
data after it runs to prevent test-to-test contamination:

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

The database assertion in step 4 is what distinguishes an integration test
from a controller unit test — it proves the data was actually saved, not just
that the controller returned a 201 status.

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
