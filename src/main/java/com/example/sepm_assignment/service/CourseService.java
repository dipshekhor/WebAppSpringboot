package com.example.sepm_assignment.service;

import com.example.sepm_assignment.model.Course;
import com.example.sepm_assignment.model.Teacher;
import com.example.sepm_assignment.repository.CourseRepository;
import com.example.sepm_assignment.repository.TeacherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;

    public CourseService(CourseRepository courseRepository, TeacherRepository teacherRepository) {
        this.courseRepository = courseRepository;
        this.teacherRepository = teacherRepository;
    }

    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    public Optional<Course> findById(Long id) {
        return courseRepository.findById(id);
    }

    public List<Course> findByTeacherId(Long teacherId) {
        return courseRepository.findByTeacherId(teacherId);
    }

    public Course save(Course course) {
        return courseRepository.save(course);
    }

    public Course saveWithTeacher(Long teacherId, Course course) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + teacherId));
        course.setTeacher(teacher);
        return courseRepository.save(course);
    }

    public Course update(Long id, Course courseDetails) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));

        course.setTitle(courseDetails.getTitle());
        course.setCourseCode(courseDetails.getCourseCode());
        course.setCredits(courseDetails.getCredits());

        return courseRepository.save(course);
    }

    public void delete(Long id) {
        courseRepository.deleteById(id);
    }
}
