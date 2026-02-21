package com.example.sepm_assignment.integration.controller;

import com.example.sepm_assignment.model.Course;
import com.example.sepm_assignment.model.Teacher;
import com.example.sepm_assignment.repository.CourseRepository;
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
 * Full-stack integration tests for {@code /api/courses}.
 * Boots the complete Spring context against an H2 in-memory database
 * (activated via the "test" profile).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CourseControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @AfterEach
    void cleanUp() {
        courseRepository.deleteAll();
        studentRepository.deleteAll();
        teacherRepository.deleteAll();
    }

    // helpers
    private Teacher persistTeacher(String name, String email) {
        return teacherRepository.save(new Teacher(null, name, email, "CS",
                new ArrayList<>(), new ArrayList<>()));
    }

    // ─── GET /api/courses ────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/courses – 200 with all courses")
    void getAllCourses_returnsOk() throws Exception {
        Teacher t = persistTeacher("Teacher A", "teacherA_c_it@school.com");
        courseRepository.save(new Course(null, "Java Basics", "C-I-001", 3, t));

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Java Basics"));
    }

    @Test
    @DisplayName("GET /api/courses – 401 when unauthenticated")
    void getAllCourses_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isUnauthorized());
    }

    // ─── GET /api/courses/{id} ───────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/courses/{id} – 200 for existing course")
    void getCourseById_found() throws Exception {
        Teacher t = persistTeacher("Teacher B", "teacherB_c_it@school.com");
        Course saved = courseRepository.save(new Course(null, "Algorithms", "C-I-002", 4, t));

        mockMvc.perform(get("/api/courses/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Algorithms"))
                .andExpect(jsonPath("$.credits").value(4));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/courses/{id} – 404 when course does not exist")
    void getCourseById_notFound() throws Exception {
        mockMvc.perform(get("/api/courses/99999"))
                .andExpect(status().isNotFound());
    }

    // ─── GET /api/courses/teacher/{teacherId} ────────────────────────────────

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/courses/teacher/{teacherId} – returns courses for teacher")
    void getCoursesByTeacher() throws Exception {
        Teacher t = persistTeacher("Teacher C", "teacherC_c_it@school.com");
        courseRepository.save(new Course(null, "OS", "C-I-003", 3, t));

        mockMvc.perform(get("/api/courses/teacher/" + t.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courseCode").value("C-I-003"));
    }

    // ─── POST /api/courses/teacher/{teacherId} ───────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/courses/teacher/{id} – 201 and persists course")
    void createCourse_persistsAndReturnsCreated() throws Exception {
        Teacher t = persistTeacher("Teacher D", "teacherD_c_it@school.com");
        Course input = new Course(null, "Databases", "C-I-004", 3, null);

        String responseBody = mockMvc.perform(post("/api/courses/teacher/" + t.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Databases"))
                .andReturn().getResponse().getContentAsString();

        Course created = objectMapper.readValue(responseBody, Course.class);
        assertThat(courseRepository.findById(created.getId())).isPresent();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/courses/teacher/{id} – 400 when teacher not found")
    void createCourse_badRequest_whenTeacherMissing() throws Exception {
        Course input = new Course(null, "Networks", "C-I-005", 3, null);

        mockMvc.perform(post("/api/courses/teacher/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/courses/teacher/{id} – 403 for USER role")
    void createCourse_forbidden_forUserRole() throws Exception {
        Teacher t = persistTeacher("Teacher E", "teacherE_c_it@school.com");
        Course input = new Course(null, "Security", "C-I-006", 3, null);

        mockMvc.perform(post("/api/courses/teacher/" + t.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isForbidden());
    }

    // ─── PUT /api/courses/{id} ───────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/courses/{id} – 200 and updates course in DB")
    void updateCourse_updatesSuccessfully() throws Exception {
        Teacher t = persistTeacher("Teacher F", "teacherF_c_it@school.com");
        Course saved = courseRepository.save(new Course(null, "Old Title", "C-I-007", 2, t));

        Course updatePayload = new Course(null, "New Title", "C-I-007U", 5, null);

        mockMvc.perform(put("/api/courses/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.credits").value(5));

        Course updated = courseRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("New Title");
        assertThat(updated.getCredits()).isEqualTo(5);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/courses/{id} – 404 when course not found")
    void updateCourse_notFound() throws Exception {
        Course updatePayload = new Course(null, "X", "X", 1, null);

        mockMvc.perform(put("/api/courses/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isNotFound());
    }

    // ─── DELETE /api/courses/{id} ────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/courses/{id} – 204 and removes from DB")
    void deleteCourse_deletesFromDb() throws Exception {
        Teacher t = persistTeacher("Teacher G", "teacherG_c_it@school.com");
        Course saved = courseRepository.save(new Course(null, "ToDelete", "C-I-008", 1, t));

        mockMvc.perform(delete("/api/courses/" + saved.getId()))
                .andExpect(status().isNoContent());

        assertThat(courseRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/courses/{id} – 403 for USER role")
    void deleteCourse_forbidden_forUserRole() throws Exception {
        Teacher t = persistTeacher("Teacher H", "teacherH_c_it@school.com");
        Course saved = courseRepository.save(new Course(null, "Protected", "C-I-009", 2, t));

        mockMvc.perform(delete("/api/courses/" + saved.getId()))
                .andExpect(status().isForbidden());
    }
}
