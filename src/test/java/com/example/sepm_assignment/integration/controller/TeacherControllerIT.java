package com.example.sepm_assignment.integration.controller;

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
 * Full-stack integration tests for {@code /api/teachers}.
 * Boots the complete Spring context against an H2 in-memory database
 * (activated via the "test" profile).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TeacherControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @AfterEach
    void cleanUp() {
        studentRepository.deleteAll();
        teacherRepository.deleteAll();
    }

    // ─── GET /api/teachers ───────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/teachers – 200 and returns list of saved teachers")
    void getAllTeachers_returnsOk() throws Exception {
        Teacher t = teacherRepository.save(new Teacher(null, "John Doe", "john_it@school.com",
                "Computer Science", new ArrayList<>(), new ArrayList<>()));

        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(t.getId()))
                .andExpect(jsonPath("$[0].name").value("John Doe"));
    }

    @Test
    @DisplayName("GET /api/teachers – 401 when unauthenticated")
    void getAllTeachers_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isUnauthorized());
    }

    // ─── GET /api/teachers/{id} ──────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/teachers/{id} – 200 for existing teacher")
    void getTeacherById_found() throws Exception {
        Teacher saved = teacherRepository.save(new Teacher(null, "Jane Doe", "jane_it@school.com",
                "Mathematics", new ArrayList<>(), new ArrayList<>()));

        mockMvc.perform(get("/api/teachers/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.department").value("Mathematics"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/teachers/{id} – 404 when teacher does not exist")
    void getTeacherById_notFound() throws Exception {
        mockMvc.perform(get("/api/teachers/99999"))
                .andExpect(status().isNotFound());
    }

    // ─── POST /api/teachers ──────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/teachers – 201 and persists teacher in DB")
    void createTeacher_persistsAndReturnsCreated() throws Exception {
        Teacher input = new Teacher(null, "New Teacher", "newteacher_it@school.com",
                "Physics", new ArrayList<>(), new ArrayList<>());

        String responseBody = mockMvc.perform(post("/api/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Teacher"))
                .andExpect(jsonPath("$.department").value("Physics"))
                .andReturn().getResponse().getContentAsString();

        Teacher created = objectMapper.readValue(responseBody, Teacher.class);
        assertThat(teacherRepository.findById(created.getId())).isPresent();
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/teachers – 403 for USER role")
    void createTeacher_forbidden_forUserRole() throws Exception {
        Teacher input = new Teacher(null, "New Teacher", "newteacher2_it@school.com",
                "Physics", new ArrayList<>(), new ArrayList<>());

        mockMvc.perform(post("/api/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isForbidden());
    }

    // ─── PUT /api/teachers/{id} ──────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/teachers/{id} – 200 and updates teacher in DB")
    void updateTeacher_updatesSuccessfully() throws Exception {
        Teacher saved = teacherRepository.save(new Teacher(null, "Old Name", "oldname_it@school.com",
                "Old Dept", new ArrayList<>(), new ArrayList<>()));

        Teacher updatePayload = new Teacher(null, "Updated Name", "updated_it@school.com",
                "New Dept", new ArrayList<>(), new ArrayList<>());

        mockMvc.perform(put("/api/teachers/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.department").value("New Dept"));

        Teacher updated = teacherRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Updated Name");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/teachers/{id} – 404 when teacher not found")
    void updateTeacher_notFound() throws Exception {
        Teacher updatePayload = new Teacher(null, "X", "x_it@school.com", "X",
                new ArrayList<>(), new ArrayList<>());

        mockMvc.perform(put("/api/teachers/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isNotFound());
    }

    // ─── DELETE /api/teachers/{id} ───────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/teachers/{id} – 204 and removes teacher from DB")
    void deleteTeacher_deletesFromDb() throws Exception {
        Teacher saved = teacherRepository.save(new Teacher(null, "ToDelete", "todelete_it@school.com",
                "Dept", new ArrayList<>(), new ArrayList<>()));

        mockMvc.perform(delete("/api/teachers/" + saved.getId()))
                .andExpect(status().isNoContent());

        assertThat(teacherRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/teachers/{id} – 403 for USER role")
    void deleteTeacher_forbidden_forUserRole() throws Exception {
        Teacher saved = teacherRepository.save(new Teacher(null, "Protected", "protected_it@school.com",
                "Dept", new ArrayList<>(), new ArrayList<>()));

        mockMvc.perform(delete("/api/teachers/" + saved.getId()))
                .andExpect(status().isForbidden());
    }
}
