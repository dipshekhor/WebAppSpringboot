package com.example.sepm_assignment.integration.repository;

import com.example.sepm_assignment.model.Teacher;
import com.example.sepm_assignment.repository.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for {@link TeacherRepository}.
 * Uses an H2 in-memory database spun up by {@code @DataJpaTest}.
 */
@DataJpaTest
class TeacherRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TeacherRepository teacherRepository;

    private Teacher teacher;

    @BeforeEach
    void setUp() {
        teacher = new Teacher(null, "John Doe", "john@school.com", "Computer Science",
                new ArrayList<>(), new ArrayList<>());
        teacher = entityManager.persistAndFlush(teacher);
    }

    // ─── findByEmail ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("findByEmail – returns teacher when email matches")
    void findByEmail_match() {
        Optional<Teacher> result = teacherRepository.findByEmail("john@school.com");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("John Doe");
        assertThat(result.get().getDepartment()).isEqualTo("Computer Science");
    }

    @Test
    @DisplayName("findByEmail – returns empty Optional when email not found")
    void findByEmail_noMatch() {
        Optional<Teacher> result = teacherRepository.findByEmail("unknown@school.com");

        assertThat(result).isEmpty();
    }

    // ─── standard CRUD ───────────────────────────────────────────────────────

    @Test
    @DisplayName("save and findById – persists teacher and retrieves it")
    void saveAndFindById() {
        Teacher newTeacher = new Teacher(null, "Jane Doe", "jane@school.com", "Mathematics",
                new ArrayList<>(), new ArrayList<>());
        Teacher saved = teacherRepository.save(newTeacher);

        Optional<Teacher> found = teacherRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Jane Doe");
        assertThat(found.get().getEmail()).isEqualTo("jane@school.com");
    }

    @Test
    @DisplayName("findAll – returns all persisted teachers")
    void findAll_returnsAll() {
        Teacher second = new Teacher(null, "Jane Doe", "jane@school.com", "Mathematics",
                new ArrayList<>(), new ArrayList<>());
        entityManager.persistAndFlush(second);

        List<Teacher> all = teacherRepository.findAll();

        assertThat(all).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("delete – removes teacher from database")
    void delete_removesRecord() {
        Long id = teacher.getId();
        teacherRepository.deleteById(id);
        entityManager.flush();

        assertThat(teacherRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("update – changes in entity are reflected after flush")
    void update_changesField() {
        teacher.setDepartment("Physics");
        entityManager.persistAndFlush(teacher);

        Optional<Teacher> updated = teacherRepository.findById(teacher.getId());

        assertThat(updated).isPresent();
        assertThat(updated.get().getDepartment()).isEqualTo("Physics");
    }
}
