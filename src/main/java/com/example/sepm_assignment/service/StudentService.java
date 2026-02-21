package com.example.sepm_assignment.service;

import com.example.sepm_assignment.model.Student;
import com.example.sepm_assignment.model.Teacher;
import com.example.sepm_assignment.repository.StudentRepository;
import com.example.sepm_assignment.repository.TeacherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    public StudentService(StudentRepository studentRepository, TeacherRepository teacherRepository) {
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
    }

    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    public Optional<Student> findById(Long id) {
        return studentRepository.findById(id);
    }

    public List<Student> findByTeacherId(Long teacherId) {
        return studentRepository.findByTeacherId(teacherId);
    }

    public Student save(Student student) {
        return studentRepository.save(student);
    }

    public Student saveWithTeacher(Long teacherId, Student student) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + teacherId));
        student.setTeacher(teacher);
        return studentRepository.save(student);
    }

    public Student update(Long id, Student studentDetails) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));

        student.setName(studentDetails.getName());
        student.setEmail(studentDetails.getEmail());
        student.setStudentId(studentDetails.getStudentId());

        return studentRepository.save(student);
    }

    public void delete(Long id) {
        studentRepository.deleteById(id);
    }
}
