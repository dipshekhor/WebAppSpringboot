package com.example.sepm_assignment.repository;

import com.example.sepm_assignment.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByTeacherId(Long teacherId);
    Optional<Course> findByCourseCode(String courseCode);
}
