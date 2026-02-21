package com.example.sepm_assignment.service;

import com.example.sepm_assignment.model.Course;
import com.example.sepm_assignment.model.Teacher;
import com.example.sepm_assignment.repository.CourseRepository;
import com.example.sepm_assignment.repository.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @InjectMocks
    private CourseService courseService;

    private Teacher teacher;
    private Course course;

    @BeforeEach
    void setUp() {
        teacher = new Teacher(1L, "John Doe", "john@school.com", "Computer Science",
                new ArrayList<>(), new ArrayList<>());
        course = new Course(1L, "Java Basics", "CS101", 3, teacher);
    }

    // ─── findAll ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAll – returns all courses from repository")
    void findAll_returnsAll() {
        when(courseRepository.findAll()).thenReturn(List.of(course));

        List<Course> result = courseService.findAll();

        assertThat(result).hasSize(1).containsExactly(course);
        verify(courseRepository).findAll();
    }

    // ─── findById ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findById – returns Optional with course when ID exists")
    void findById_present() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        assertThat(courseService.findById(1L)).isPresent().contains(course);
    }

    @Test
    @DisplayName("findById – returns empty Optional when ID does not exist")
    void findById_missing() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(courseService.findById(99L)).isEmpty();
    }

    // ─── findByTeacherId ─────────────────────────────────────────────────────

    @Test
    @DisplayName("findByTeacherId – returns courses for given teacher")
    void findByTeacherId_returnsCourses() {
        when(courseRepository.findByTeacherId(1L)).thenReturn(List.of(course));

        assertThat(courseService.findByTeacherId(1L)).hasSize(1).containsExactly(course);
    }

    @Test
    @DisplayName("findByTeacherId – returns empty list when teacher has no courses")
    void findByTeacherId_empty() {
        when(courseRepository.findByTeacherId(99L)).thenReturn(List.of());

        assertThat(courseService.findByTeacherId(99L)).isEmpty();
    }

    // ─── saveWithTeacher ─────────────────────────────────────────────────────

    @Test
    @DisplayName("saveWithTeacher – assigns teacher and persists course")
    void saveWithTeacher_valid() {
        Course input = new Course(null, "Java Basics", "CS101", 3, null);

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(courseRepository.save(input)).thenReturn(course);

        Course result = courseService.saveWithTeacher(1L, input);

        assertThat(result).isEqualTo(course);
        assertThat(input.getTeacher()).isEqualTo(teacher);
        verify(courseRepository).save(input);
    }

    @Test
    @DisplayName("saveWithTeacher – throws when teacher does not exist")
    void saveWithTeacher_invalidTeacher_throws() {
        when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.saveWithTeacher(99L, course))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Teacher not found with id: 99");

        verify(courseRepository, never()).save(any());
    }

    // ─── update ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update – modifies title, courseCode, credits and saves")
    void update_valid() {
        Course details = new Course(null, "Advanced Java", "CS201", 4, null);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.save(course)).thenReturn(course);

        Course result = courseService.update(1L, details);

        assertThat(result.getTitle()).isEqualTo("Advanced Java");
        assertThat(result.getCourseCode()).isEqualTo("CS201");
        assertThat(result.getCredits()).isEqualTo(4);
        verify(courseRepository).save(course);
    }

    @Test
    @DisplayName("update – throws when course not found")
    void update_missing_throws() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.update(99L, course))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Course not found with id: 99");
    }

    // ─── delete ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete – delegates to repository deleteById")
    void delete_delegatesToRepository() {
        courseService.delete(1L);

        verify(courseRepository).deleteById(1L);
    }
}
