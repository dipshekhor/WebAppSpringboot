package com.example.sepm_assignment.integration.controller;

import com.example.sepm_assignment.model.Student;
import com.example.sepm_assignment.model.Teacher;
import com.example.sepm_assignment.repository.StudentRepository;
import com.example.sepm_assignment.repository.TeacherRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full-stack integration tests for {@code /api/students}.
 * Boots the complete Spring context against an H2 in-memory database
 * (activated via the "test" profile).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StudentControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @AfterEach
    void cleanUp() {
        studentRepository.deleteAll();
        teacherRepository.deleteAll();
    }

    // helpers
    private Teacher persistTeacher(String name, String email) {
        return teacherRepository.save(new Teacher(null, name, email, "CS",
                new ArrayList<>(), new ArrayList<>()));
    }

    // ─── GET /api/students ───────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/students – 200 with all students")
    void getAllStudents_returnsOk() throws Exception {
        Teacher t = persistTeacher("Teacher A", "teacherA_it@school.com");
        studentRepository.save(new Student(null, "Alice", "alice_it1@school.com", "S-I-001", t));

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alice"));
    }

    @Test
    @DisplayName("GET /api/students – 401 when unauthenticated")
    void getAllStudents_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isUnauthorized());
    }

    // ─── GET /api/students/{id} ──────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/students/{id} – 200 for existing student")
    void getStudentById_found() throws Exception {
        Teacher t = persistTeacher("Teacher B", "teacherB_it@school.com");
        Student saved = studentRepository.save(new Student(null, "Bob", "bob_it@school.com", "S-I-002", t));

        mockMvc.perform(get("/api/students/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bob"))
                .andExpect(jsonPath("$.studentId").value("S-I-002"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/students/{id} – 404 when student does not exist")
    void getStudentById_notFound() throws Exception {
        mockMvc.perform(get("/api/students/99999"))
                .andExpect(status().isNotFound());
    }

    // ─── GET /api/students/teacher/{teacherId} ───────────────────────────────

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/students/teacher/{teacherId} – returns students for teacher")
    void getStudentsByTeacher() throws Exception {
        Teacher t = persistTeacher("Teacher C", "teacherC_it@school.com");
        studentRepository.save(new Student(null, "Carol", "carol_it@school.com", "S-I-003", t));

        mockMvc.perform(get("/api/students/teacher/" + t.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Carol"));
    }

    // ─── POST /api/students/teacher/{teacherId} ──────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/students/teacher/{id} – 201 and persists student")
    void createStudent_persistsAndReturnsCreated() throws Exception {
        Teacher t = persistTeacher("Teacher D", "teacherD_it@school.com");
        Student input = new Student(null, "Dave", "dave_it@school.com", "S-I-004", null);

        String responseBody = mockMvc.perform(post("/api/students/teacher/" + t.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Dave"))
                .andReturn().getResponse().getContentAsString();

        Student created = objectMapper.readValue(responseBody, Student.class);
        assertThat(studentRepository.findById(created.getId())).isPresent();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/students/teacher/{id} – 400 when teacher not found")
    void createStudent_badRequest_whenTeacherMissing() throws Exception {
        Student input = new Student(null, "Eve", "eve_it@school.com", "S-I-005", null);

        mockMvc.perform(post("/api/students/teacher/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/students/teacher/{id} – 403 for USER role")
    void createStudent_forbidden_forUserRole() throws Exception {
        Teacher t = persistTeacher("Teacher E", "teacherE_it@school.com");
        Student input = new Student(null, "Frank", "frank_it@school.com", "S-I-006", null);

        mockMvc.perform(post("/api/students/teacher/" + t.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isForbidden());
    }

    // ─── PUT /api/students/{id} ──────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/students/{id} – 200 and updates student in DB")
    void updateStudent_updatesSuccessfully() throws Exception {
        Teacher t = persistTeacher("Teacher F", "teacherF_it@school.com");
        Student saved = studentRepository.save(new Student(null, "Grace", "grace_it@school.com", "S-I-007", t));

        Student updatePayload = new Student(null, "Grace Updated", "grace.updated_it@school.com", "S-I-007U", null);

        mockMvc.perform(put("/api/students/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Grace Updated"));

        Student updated = studentRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Grace Updated");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/students/{id} – 404 when student not found")
    void updateStudent_notFound() throws Exception {
        Student updatePayload = new Student(null, "Nobody", "nobody_it@school.com", "S-X", null);

        mockMvc.perform(put("/api/students/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isNotFound());
    }

    // ─── DELETE /api/students/{id} ───────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/students/{id} – 204 and removes from DB")
    void deleteStudent_deletesFromDb() throws Exception {
        Teacher t = persistTeacher("Teacher G", "teacherG_it@school.com");
        Student saved = studentRepository.save(new Student(null, "Holly", "holly_it@school.com", "S-I-008", t));

        mockMvc.perform(delete("/api/students/" + saved.getId()))
                .andExpect(status().isNoContent());

        assertThat(studentRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/students/{id} – 403 for USER role")
    void deleteStudent_forbidden_forUserRole() throws Exception {
        Teacher t = persistTeacher("Teacher H", "teacherH_it@school.com");
        Student saved = studentRepository.save(new Student(null, "Ian", "ian_it@school.com", "S-I-009", t));

        mockMvc.perform(delete("/api/students/" + saved.getId()))
                .andExpect(status().isForbidden());
    }
}
