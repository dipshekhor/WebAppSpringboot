package com.example.sepm_assignment.controller;

import com.example.sepm_assignment.config.SecurityConfig;
import com.example.sepm_assignment.model.Course;
import com.example.sepm_assignment.model.Teacher;
import com.example.sepm_assignment.service.CourseService;
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

@WebMvcTest(CourseController.class)
@Import(SecurityConfig.class)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourseService courseService;

    private Teacher teacher;
    private Course course;

    @BeforeEach
    void setUp() {
        teacher = new Teacher(1L, "John Doe", "john@school.com", "Computer Science",
                new ArrayList<>(), new ArrayList<>());
        course = new Course(1L, "Java Basics", "CS101", 3, teacher);
    }

    // ─── GET /api/courses ────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/courses – returns 200 with list for USER role")
    void getAllCourses_returnsOk() throws Exception {
        when(courseService.findAll()).thenReturn(List.of(course));

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Java Basics"))
                .andExpect(jsonPath("$[0].courseCode").value("CS101"))
                .andExpect(jsonPath("$[0].credits").value(3));
    }

    @Test
    @DisplayName("GET /api/courses – returns 401 when not authenticated")
    void getAllCourses_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isUnauthorized());
    }

    // ─── GET /api/courses/{id} ───────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/courses/{id} – returns 200 when course exists")
    void getCourseById_found() throws Exception {
        when(courseService.findById(1L)).thenReturn(Optional.of(course));

        mockMvc.perform(get("/api/courses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Java Basics"))
                .andExpect(jsonPath("$.credits").value(3));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/courses/{id} – returns 404 when course does not exist")
    void getCourseById_notFound() throws Exception {
        when(courseService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/courses/99"))
                .andExpect(status().isNotFound());
    }

    // ─── GET /api/courses/teacher/{teacherId} ────────────────────────────────

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/courses/teacher/{teacherId} – returns courses list")
    void getCoursesByTeacher_returnsList() throws Exception {
        when(courseService.findByTeacherId(1L)).thenReturn(List.of(course));

        mockMvc.perform(get("/api/courses/teacher/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courseCode").value("CS101"));
    }

    // ─── POST /api/courses/teacher/{teacherId} ───────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/courses/teacher/{id} – returns 201 for ADMIN")
    void createCourse_returnsCreated() throws Exception {
        Course input = new Course(null, "Java Basics", "CS101", 3, null);
        when(courseService.saveWithTeacher(eq(1L), any(Course.class))).thenReturn(course);

        mockMvc.perform(post("/api/courses/teacher/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseCode").value("CS101"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/courses/teacher/{id} – returns 400 when teacher not found")
    void createCourse_badRequest_whenTeacherMissing() throws Exception {
        Course input = new Course(null, "Java Basics", "CS101", 3, null);
        when(courseService.saveWithTeacher(eq(99L), any(Course.class)))
                .thenThrow(new RuntimeException("Teacher not found with id: 99"));

        mockMvc.perform(post("/api/courses/teacher/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/courses/teacher/{id} – returns 403 for USER role")
    void createCourse_forbidden_forUserRole() throws Exception {
        Course input = new Course(null, "Java Basics", "CS101", 3, null);

        mockMvc.perform(post("/api/courses/teacher/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isForbidden());
    }

    // ─── PUT /api/courses/{id} ───────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/courses/{id} – returns 200 when update succeeds")
    void updateCourse_success() throws Exception {
        when(courseService.update(eq(1L), any(Course.class))).thenReturn(course);

        mockMvc.perform(put("/api/courses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(course)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Java Basics"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/courses/{id} – returns 404 when course not found")
    void updateCourse_notFound() throws Exception {
        when(courseService.update(eq(99L), any(Course.class)))
                .thenThrow(new RuntimeException("Course not found with id: 99"));

        mockMvc.perform(put("/api/courses/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(course)))
                .andExpect(status().isNotFound());
    }

    // ─── DELETE /api/courses/{id} ────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/courses/{id} – returns 204")
    void deleteCourse_returnsNoContent() throws Exception {
        doNothing().when(courseService).delete(1L);

        mockMvc.perform(delete("/api/courses/1"))
                .andExpect(status().isNoContent());

        verify(courseService).delete(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/courses/{id} – returns 403 for USER role")
    void deleteCourse_forbidden_forUserRole() throws Exception {
        mockMvc.perform(delete("/api/courses/1"))
                .andExpect(status().isForbidden());
    }
}
