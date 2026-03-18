package com.teacher.agent.service;

import static com.teacher.agent.util.RepositoryUtil.findStudentByIdAndUserIdOrThrow;

import com.teacher.agent.domain.Student;
import com.teacher.agent.domain.StudentRepository;
import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.StudentCreateRequest;
import com.teacher.agent.dto.StudentResponse;
import com.teacher.agent.dto.StudentUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentCommandService {

  private final StudentRepository studentRepository;

  @Transactional
  public StudentResponse create(UserId userId, StudentCreateRequest request) {
    Student student = Student.create(userId, request.name(), request.memo());

    return StudentResponse.from(studentRepository.save(student));
  }

  @Transactional
  public StudentResponse update(UserId userId, Long id, StudentUpdateRequest request) {
    Student student = findStudentByIdAndUserIdOrThrow(studentRepository, id, userId);

    student.update(request.name(), request.memo());

    return StudentResponse.from(student);
  }

  @Transactional
  public void delete(UserId userId, Long id) {
    findStudentByIdAndUserIdOrThrow(studentRepository, id, userId);

    studentRepository.deleteById(id);
  }
}
