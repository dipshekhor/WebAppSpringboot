package com.example.sepm_assignment.controller;

import com.example.sepm_assignment.config.SecurityConfig;
import com.example.sepm_assignment.model.Student;
import com.example.sepm_assignment.model.Teacher;
import com.example.sepm_assignment.service.StudentService;
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

@WebMvcTest(StudentController.class)
@Import(SecurityConfig.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudentService studentService;

    private Teacher teacher;
    private Student student;

    @BeforeEach
    void setUp() {
        teacher = new Teacher(1L, "John Doe", "john@school.com", "Computer Science",
                new ArrayList<>(), new ArrayList<>());
        student = new Student(1L, "Alice Smith", "alice@school.com", "S001", teacher);
    }

    // ─── GET /api/students ───────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/students – returns 200 with list for USER role")
    void getAllStudents_returnsOk() throws Exception {
        when(studentService.findAll()).thenReturn(List.of(student));

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alice Smith"))
                .andExpect(jsonPath("$[0].email").value("alice@school.com"))
                .andExpect(jsonPath("$[0].studentId").value("S001"));
    }

    @Test
    @DisplayName("GET /api/students – returns 401 when not authenticated")
    void getAllStudents_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isUnauthorized());
    }

    // ─── GET /api/students/{id} ──────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/students/{id} – returns 200 when student exists")
    void getStudentById_found() throws Exception {
        when(studentService.findById(1L)).thenReturn(Optional.of(student));

        mockMvc.perform(get("/api/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice Smith"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/students/{id} – returns 404 when student does not exist")
    void getStudentById_notFound() throws Exception {
        when(studentService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/students/99"))
                .andExpect(status().isNotFound());
    }

    // ─── GET /api/students/teacher/{teacherId} ───────────────────────────────

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/students/teacher/{teacherId} – returns list for that teacher")
    void getStudentsByTeacher_returnsList() throws Exception {
        when(studentService.findByTeacherId(1L)).thenReturn(List.of(student));

        mockMvc.perform(get("/api/students/teacher/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentId").value("S001"));
    }

    // ─── POST /api/students/teacher/{teacherId} ──────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/students/teacher/{id} – returns 201 for ADMIN")
    void createStudent_returnsCreated() throws Exception {
        Student input = new Student(null, "Alice Smith", "alice@school.com", "S001", null);
        when(studentService.saveWithTeacher(eq(1L), any(Student.class))).thenReturn(student);

        mockMvc.perform(post("/api/students/teacher/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Alice Smith"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/students/teacher/{id} – returns 400 when teacher not found")
    void createStudent_badRequest_whenTeacherMissing() throws Exception {
        Student input = new Student(null, "Alice Smith", "alice@school.com", "S001", null);
        when(studentService.saveWithTeacher(eq(99L), any(Student.class)))
                .thenThrow(new RuntimeException("Teacher not found with id: 99"));

        mockMvc.perform(post("/api/students/teacher/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/students/teacher/{id} – returns 403 for USER role")
    void createStudent_forbidden_forUserRole() throws Exception {
        Student input = new Student(null, "Alice Smith", "alice@school.com", "S001", null);

        mockMvc.perform(post("/api/students/teacher/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isForbidden());
    }

    // ─── PUT /api/students/{id} ──────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/students/{id} – returns 200 when update succeeds")
    void updateStudent_success() throws Exception {
        when(studentService.update(eq(1L), any(Student.class))).thenReturn(student);

        mockMvc.perform(put("/api/students/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice Smith"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/students/{id} – returns 404 when student not found")
    void updateStudent_notFound() throws Exception {
        when(studentService.update(eq(99L), any(Student.class)))
                .thenThrow(new RuntimeException("Student not found with id: 99"));

        mockMvc.perform(put("/api/students/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(student)))
                .andExpect(status().isNotFound());
    }

    // ─── DELETE /api/students/{id} ───────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/students/{id} – returns 204")
    void deleteStudent_returnsNoContent() throws Exception {
        doNothing().when(studentService).delete(1L);

        mockMvc.perform(delete("/api/students/1"))
                .andExpect(status().isNoContent());

        verify(studentService).delete(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/students/{id} – returns 403 for USER role")
    void deleteStudent_forbidden_forUserRole() throws Exception {
        mockMvc.perform(delete("/api/students/1"))
                .andExpect(status().isForbidden());
    }
}
