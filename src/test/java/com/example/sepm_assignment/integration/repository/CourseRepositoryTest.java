package com.example.sepm_assignment.integration.repository;

import com.example.sepm_assignment.model.Course;
import com.example.sepm_assignment.model.Teacher;
import com.example.sepm_assignment.repository.CourseRepository;
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
 * Integration tests for {@link CourseRepository}.
 * Uses an H2 in-memory database spun up by {@code @DataJpaTest}.
 */
@DataJpaTest
class CourseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CourseRepository courseRepository;

    private Teacher teacher;
    private Course course;

    @BeforeEach
    void setUp() {
        teacher = new Teacher(null, "John Doe", "john@school.com", "Computer Science",
                new ArrayList<>(), new ArrayList<>());
        teacher = entityManager.persistAndFlush(teacher);

        course = new Course(null, "Java Basics", "CS101", 3, teacher);
        course = entityManager.persistAndFlush(course);
    }

    // ─── findByTeacherId ─────────────────────────────────────────────────────

    @Test
    @DisplayName("findByTeacherId – returns courses belonging to that teacher")
    void findByTeacherId_returnsCourses() {
        List<Course> result = courseRepository.findByTeacherId(teacher.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Java Basics");
        assertThat(result.get(0).getCourseCode()).isEqualTo("CS101");
    }

    @Test
    @DisplayName("findByTeacherId – returns empty list when teacher has no courses")
    void findByTeacherId_empty() {
        Teacher otherTeacher = new Teacher(null, "Jane Doe", "jane@school.com", "Mathematics",
                new ArrayList<>(), new ArrayList<>());
        otherTeacher = entityManager.persistAndFlush(otherTeacher);

        List<Course> result = courseRepository.findByTeacherId(otherTeacher.getId());

        assertThat(result).isEmpty();
    }

    // ─── findByCourseCode ────────────────────────────────────────────────────

    @Test
    @DisplayName("findByCourseCode – returns course when code matches")
    void findByCourseCode_match() {
        Optional<Course> result = courseRepository.findByCourseCode("CS101");

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Java Basics");
        assertThat(result.get().getCredits()).isEqualTo(3);
    }

    @Test
    @DisplayName("findByCourseCode – returns empty Optional when code not found")
    void findByCourseCode_noMatch() {
        Optional<Course> result = courseRepository.findByCourseCode("UNKNOWN");

        assertThat(result).isEmpty();
    }

    // ─── CRUD ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("save and findById – persists course and retrieves it")
    void saveAndFindById() {
        Course newCourse = new Course(null, "Data Structures", "CS201", 4, teacher);
        Course saved = courseRepository.save(newCourse);

        Optional<Course> found = courseRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Data Structures");
        assertThat(found.get().getCredits()).isEqualTo(4);
    }

    @Test
    @DisplayName("findAll – returns all persisted courses")
    void findAll_returnsAll() {
        Course second = new Course(null, "Algorithms", "CS301", 3, teacher);
        entityManager.persistAndFlush(second);

        List<Course> all = courseRepository.findAll();

        assertThat(all).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("delete – removes course from database")
    void delete_removesRecord() {
        Long id = course.getId();
        courseRepository.deleteById(id);
        entityManager.flush();

        assertThat(courseRepository.findById(id)).isEmpty();
    }
}
