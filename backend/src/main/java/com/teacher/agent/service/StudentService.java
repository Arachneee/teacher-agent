package com.teacher.agent.service;

import com.teacher.agent.domain.Student;
import com.teacher.agent.domain.StudentRepository;
import com.teacher.agent.domain.Teacher;
import com.teacher.agent.domain.TeacherRepository;
import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.StudentCreateRequest;
import com.teacher.agent.dto.StudentResponse;
import com.teacher.agent.dto.StudentUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

  private final StudentRepository studentRepository;
  private final TeacherRepository teacherRepository;

  @Transactional
  public StudentResponse create(UserId userId, StudentCreateRequest request) {
    Teacher teacher = findTeacherByUserId(userId);
    Student student = Student.create(teacher.getId(), request.name(), request.memo());
    return StudentResponse.from(studentRepository.save(student));
  }

  public List<StudentResponse> getAll(UserId userId) {
    Teacher teacher = findTeacherByUserId(userId);
    return studentRepository.findAllByTeacherId(teacher.getId()).stream().map(StudentResponse::from)
        .toList();
  }

  public StudentResponse getOne(UserId userId, Long id) {
    Teacher teacher = findTeacherByUserId(userId);
    return StudentResponse.from(findByIdAndTeacher(id, teacher.getId()));
  }

  @Transactional
  public StudentResponse update(UserId userId, Long id, StudentUpdateRequest request) {
    Teacher teacher = findTeacherByUserId(userId);
    Student student = findByIdAndTeacher(id, teacher.getId());
    student.update(request.name(), request.memo());
    return StudentResponse.from(student);
  }

  @Transactional
  public void delete(UserId userId, Long id) {
    Teacher teacher = findTeacherByUserId(userId);
    findByIdAndTeacher(id, teacher.getId());
    studentRepository.deleteById(id);
  }

  private Student findByIdAndTeacher(Long id, Long teacherId) {
    return studentRepository.findByIdAndTeacherId(id, teacherId).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found: " + id));
  }

  private Teacher findTeacherByUserId(UserId userId) {
    return teacherRepository.findByUserId(userId).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found: " + userId));
  }
}
