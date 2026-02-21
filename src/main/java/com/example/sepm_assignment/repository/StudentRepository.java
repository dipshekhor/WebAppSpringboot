package com.example.sepm_assignment.repository;

import com.example.sepm_assignment.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByTeacherId(Long teacherId);
    Optional<Student> findByEmail(String email);
    Optional<Student> findByStudentId(String studentId);
}
