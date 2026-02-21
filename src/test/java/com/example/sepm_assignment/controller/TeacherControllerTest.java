package com.example.sepm_assignment.controller;

import com.example.sepm_assignment.config.SecurityConfig;
import com.example.sepm_assignment.model.Teacher;
import com.example.sepm_assignment.service.TeacherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TeacherController.class)
@Import(SecurityConfig.class)
class TeacherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TeacherService teacherService;

    private Teacher teacher;

    @BeforeEach
    void setUp() {
        teacher = new Teacher(1L, "John Doe", "john@school.com", "Computer Science",
                new ArrayList<>(), new ArrayList<>());
    }

    // ─── GET /api/teachers ───────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/teachers – returns 200 with list for USER role")
    void getAllTeachers_returnsOk() throws Exception {
        when(teacherService.findAll()).thenReturn(List.of(teacher));

        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[0].email").value("john@school.com"))
                .andExpect(jsonPath("$[0].department").value("Computer Science"));
    }

    @Test
    @DisplayName("GET /api/teachers – returns 401 when not authenticated")
    void getAllTeachers_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isUnauthorized());
    }

    // ─── GET /api/teachers/{id} ──────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/teachers/{id} – returns 200 when teacher exists")
    void getTeacherById_found() throws Exception {
        when(teacherService.findById(1L)).thenReturn(Optional.of(teacher));

        mockMvc.perform(get("/api/teachers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/teachers/{id} – returns 404 when teacher does not exist")
    void getTeacherById_notFound() throws Exception {
        when(teacherService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/teachers/99"))
                .andExpect(status().isNotFound());
    }

    // ─── POST /api/teachers ──────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/teachers – returns 201 with saved teacher for ADMIN")
    void createTeacher_returnsCreated() throws Exception {
        Teacher input = new Teacher(null, "John Doe", "john@school.com", "Computer Science",
                new ArrayList<>(), new ArrayList<>());
        when(teacherService.save(any(Teacher.class))).thenReturn(teacher);

        mockMvc.perform(post("/api/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/teachers – returns 403 for USER role")
    void createTeacher_forbidden_forUserRole() throws Exception {
        Teacher input = new Teacher(null, "John Doe", "john@school.com", "Computer Science",
                new ArrayList<>(), new ArrayList<>());

        mockMvc.perform(post("/api/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isForbidden());
    }

    // ─── PUT /api/teachers/{id} ──────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/teachers/{id} – returns 200 when update succeeds")
    void updateTeacher_success() throws Exception {
        when(teacherService.update(eq(1L), any(Teacher.class))).thenReturn(teacher);

        mockMvc.perform(put("/api/teachers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teacher)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/teachers/{id} – returns 404 when teacher not found")
    void updateTeacher_notFound() throws Exception {
        when(teacherService.update(eq(99L), any(Teacher.class)))
                .thenThrow(new RuntimeException("Teacher not found with id: 99"));

        mockMvc.perform(put("/api/teachers/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teacher)))
                .andExpect(status().isNotFound());
    }

    // ─── DELETE /api/teachers/{id} ───────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/teachers/{id} – returns 204 on success")
    void deleteTeacher_returnsNoContent() throws Exception {
        doNothing().when(teacherService).delete(1L);

        mockMvc.perform(delete("/api/teachers/1"))
                .andExpect(status().isNoContent());

        verify(teacherService).delete(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/teachers/{id} – returns 403 for USER role")
    void deleteTeacher_forbidden_forUserRole() throws Exception {
        mockMvc.perform(delete("/api/teachers/1"))
                .andExpect(status().isForbidden());
    }
}
