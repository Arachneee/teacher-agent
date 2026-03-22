package com.teacher.agent.service;

import static com.teacher.agent.util.RepositoryUtil.findStudentByIdAndUserIdOrThrow;

import com.teacher.agent.domain.Student;
import com.teacher.agent.domain.repository.StudentRepository;
import com.teacher.agent.domain.vo.SchoolGrade;
import com.teacher.agent.domain.vo.UserId;
import com.teacher.agent.dto.StudentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentCommandService {

  private final StudentRepository studentRepository;

  @Transactional
  public StudentResponse create(UserId userId, String name, String memo, SchoolGrade grade) {
    Student student = Student.create(userId, name, memo, grade);

    return StudentResponse.from(studentRepository.save(student));
  }

  @Transactional
  public StudentResponse update(UserId userId, Long id, String name, String memo,
      SchoolGrade grade) {
    Student student = findStudentByIdAndUserIdOrThrow(studentRepository, id, userId);

    student.update(name, memo, grade);

    return StudentResponse.from(student);
  }

  @Transactional
  public void delete(UserId userId, Long id) {
    findStudentByIdAndUserIdOrThrow(studentRepository, id, userId);

    studentRepository.deleteById(id);
  }
}
