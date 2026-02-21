package com.example.sepm_assignment.integration.repository;

import com.example.sepm_assignment.model.Student;
import com.example.sepm_assignment.model.Teacher;
import com.example.sepm_assignment.repository.StudentRepository;
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
 * Integration tests for {@link StudentRepository}.
 * Uses an H2 in-memory database spun up by {@code @DataJpaTest}.
 */
@DataJpaTest
class StudentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StudentRepository studentRepository;

    private Teacher teacher;
    private Student student;

    @BeforeEach
    void setUp() {
        teacher = new Teacher(null, "John Doe", "john@school.com", "Computer Science",
                new ArrayList<>(), new ArrayList<>());
        teacher = entityManager.persistAndFlush(teacher);

        student = new Student(null, "Alice Smith", "alice@school.com", "S001", teacher);
        student = entityManager.persistAndFlush(student);
    }

    // ─── findByTeacherId ─────────────────────────────────────────────────────

    @Test
    @DisplayName("findByTeacherId – returns students belonging to that teacher")
    void findByTeacherId_returnsStudents() {
        List<Student> result = studentRepository.findByTeacherId(teacher.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Alice Smith");
        assertThat(result.get(0).getStudentId()).isEqualTo("S001");
    }

    @Test
    @DisplayName("findByTeacherId – returns empty list when teacher has no students")
    void findByTeacherId_empty() {
        Teacher otherTeacher = new Teacher(null, "Jane Doe", "jane@school.com", "Mathematics",
                new ArrayList<>(), new ArrayList<>());
        otherTeacher = entityManager.persistAndFlush(otherTeacher);

        List<Student> result = studentRepository.findByTeacherId(otherTeacher.getId());

        assertThat(result).isEmpty();
    }

    // ─── findByEmail ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("findByEmail – returns student when email matches")
    void findByEmail_match() {
        Optional<Student> result = studentRepository.findByEmail("alice@school.com");

        assertThat(result).isPresent();
        assertThat(result.get().getStudentId()).isEqualTo("S001");
    }

    @Test
    @DisplayName("findByEmail – returns empty Optional when email not found")
    void findByEmail_noMatch() {
        Optional<Student> result = studentRepository.findByEmail("nobody@school.com");

        assertThat(result).isEmpty();
    }

    // ─── findByStudentId ─────────────────────────────────────────────────────

    @Test
    @DisplayName("findByStudentId – returns student when studentId matches")
    void findByStudentId_match() {
        Optional<Student> result = studentRepository.findByStudentId("S001");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Alice Smith");
    }

    @Test
    @DisplayName("findByStudentId – returns empty Optional when studentId not found")
    void findByStudentId_noMatch() {
        Optional<Student> result = studentRepository.findByStudentId("UNKNOWN");

        assertThat(result).isEmpty();
    }

    // ─── CRUD ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("save and findById – persists student and retrieves it")
    void saveAndFindById() {
        Student newStudent = new Student(null, "Bob Jones", "bob@school.com", "S002", teacher);
        Student saved = studentRepository.save(newStudent);

        Optional<Student> found = studentRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Bob Jones");
    }

    @Test
    @DisplayName("delete – removes student from database")
    void delete_removesRecord() {
        Long id = student.getId();
        studentRepository.deleteById(id);
        entityManager.flush();

        assertThat(studentRepository.findById(id)).isEmpty();
    }
}
