package com.example.sepm_assignment.service;

import com.example.sepm_assignment.model.Student;
import com.example.sepm_assignment.model.Teacher;
import com.example.sepm_assignment.repository.StudentRepository;
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
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @InjectMocks
    private StudentService studentService;

    private Teacher teacher;
    private Student student;

    @BeforeEach
    void setUp() {
        teacher = new Teacher(1L, "John Doe", "john@school.com", "Computer Science",
                new ArrayList<>(), new ArrayList<>());
        student = new Student(1L, "Alice Smith", "alice@school.com", "S001", teacher);
    }

    // ─── findAll ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAll – returns all students from repository")
    void findAll_returnsAll() {
        when(studentRepository.findAll()).thenReturn(List.of(student));

        List<Student> result = studentService.findAll();

        assertThat(result).hasSize(1).containsExactly(student);
        verify(studentRepository).findAll();
    }

    // ─── findById ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findById – returns Optional with student when ID exists")
    void findById_present() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        assertThat(studentService.findById(1L)).isPresent().contains(student);
    }

    @Test
    @DisplayName("findById – returns empty Optional when ID does not exist")
    void findById_missing() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(studentService.findById(99L)).isEmpty();
    }

    // ─── findByTeacherId ─────────────────────────────────────────────────────

    @Test
    @DisplayName("findByTeacherId – returns students for given teacher")
    void findByTeacherId_returnsStudents() {
        when(studentRepository.findByTeacherId(1L)).thenReturn(List.of(student));

        assertThat(studentService.findByTeacherId(1L)).hasSize(1).containsExactly(student);
    }

    @Test
    @DisplayName("findByTeacherId – returns empty list when teacher has no students")
    void findByTeacherId_empty() {
        when(studentRepository.findByTeacherId(99L)).thenReturn(List.of());

        assertThat(studentService.findByTeacherId(99L)).isEmpty();
    }

    // ─── saveWithTeacher ─────────────────────────────────────────────────────

    @Test
    @DisplayName("saveWithTeacher – assigns teacher and persists student")
    void saveWithTeacher_validTeacher() {
        Student input = new Student(null, "Alice Smith", "alice@school.com", "S001", null);

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(studentRepository.save(input)).thenReturn(student);

        Student result = studentService.saveWithTeacher(1L, input);

        assertThat(result).isEqualTo(student);
        assertThat(input.getTeacher()).isEqualTo(teacher);
        verify(studentRepository).save(input);
    }

    @Test
    @DisplayName("saveWithTeacher – throws when teacher does not exist")
    void saveWithTeacher_invalidTeacher_throws() {
        when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.saveWithTeacher(99L, student))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Teacher not found with id: 99");

        verify(studentRepository, never()).save(any());
    }

    // ─── update ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update – modifies name, email, studentId and saves")
    void update_valid() {
        Student details = new Student(null, "Bob Jones", "bob@school.com", "S002", null);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.save(student)).thenReturn(student);

        Student result = studentService.update(1L, details);

        assertThat(result.getName()).isEqualTo("Bob Jones");
        assertThat(result.getEmail()).isEqualTo("bob@school.com");
        assertThat(result.getStudentId()).isEqualTo("S002");
        verify(studentRepository).save(student);
    }

    @Test
    @DisplayName("update – throws when student not found")
    void update_missing_throws() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.update(99L, student))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Student not found with id: 99");
    }

    // ─── delete ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete – delegates to repository deleteById")
    void delete_delegatesToRepository() {
        studentService.delete(1L);

        verify(studentRepository).deleteById(1L);
    }
}
