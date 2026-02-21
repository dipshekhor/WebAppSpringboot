package com.example.sepm_assignment.service;

import com.example.sepm_assignment.model.Teacher;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @InjectMocks
    private TeacherService teacherService;

    private Teacher teacher;

    @BeforeEach
    void setUp() {
        teacher = new Teacher(1L, "John Doe", "john@school.com", "Computer Science",
                new ArrayList<>(), new ArrayList<>());
    }

    // ─── findAll ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAll – returns every teacher from the repository")
    void findAll_returnsAll() {
        when(teacherRepository.findAll()).thenReturn(List.of(teacher));

        List<Teacher> result = teacherService.findAll();

        assertThat(result).hasSize(1).containsExactly(teacher);
        verify(teacherRepository).findAll();
    }

    // ─── findById ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findById – returns Optional with teacher when ID exists")
    void findById_present() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));

        assertThat(teacherService.findById(1L)).isPresent().contains(teacher);
    }

    @Test
    @DisplayName("findById – returns empty Optional when ID does not exist")
    void findById_missing() {
        when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(teacherService.findById(99L)).isEmpty();
    }

    // ─── findByEmail ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("findByEmail – delegates to repository and returns result")
    void findByEmail_present() {
        when(teacherRepository.findByEmail("john@school.com")).thenReturn(Optional.of(teacher));

        assertThat(teacherService.findByEmail("john@school.com"))
                .isPresent().contains(teacher);
    }

    @Test
    @DisplayName("findByEmail – returns empty when email not found")
    void findByEmail_missing() {
        when(teacherRepository.findByEmail("unknown@school.com")).thenReturn(Optional.empty());

        assertThat(teacherService.findByEmail("unknown@school.com")).isEmpty();
    }

    // ─── save ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("save – persists and returns the saved teacher")
    void save_persistsTeacher() {
        when(teacherRepository.save(teacher)).thenReturn(teacher);

        Teacher result = teacherService.save(teacher);

        assertThat(result).isEqualTo(teacher);
        verify(teacherRepository).save(teacher);
    }

    // ─── update ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update – modifies all fields and saves")
    void update_valid() {
        Teacher details = new Teacher(null, "Jane Doe", "jane@school.com", "Mathematics",
                new ArrayList<>(), new ArrayList<>());

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(teacherRepository.save(teacher)).thenReturn(teacher);

        Teacher result = teacherService.update(1L, details);

        assertThat(result.getName()).isEqualTo("Jane Doe");
        assertThat(result.getEmail()).isEqualTo("jane@school.com");
        assertThat(result.getDepartment()).isEqualTo("Mathematics");
        verify(teacherRepository).save(teacher);
    }

    @Test
    @DisplayName("update – throws RuntimeException when teacher not found")
    void update_missing_throwsException() {
        when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teacherService.update(99L, teacher))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Teacher not found with id: 99");
    }

    // ─── delete ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete – calls deleteById on repository")
    void delete_delegatesToRepository() {
        teacherService.delete(1L);

        verify(teacherRepository).deleteById(1L);
    }
}
