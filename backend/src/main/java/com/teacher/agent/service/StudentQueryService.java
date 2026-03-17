package com.teacher.agent.service;

import com.teacher.agent.domain.StudentRepository;
import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.StudentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.teacher.agent.util.RepositoryUtil.findStudentByIdAndUserIdOrThrow;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentQueryService {

  private final StudentRepository studentRepository;

  public List<StudentResponse> getAll(UserId userId) {
    return studentRepository.findAllByUserId(userId).stream().map(StudentResponse::from).toList();
  }

  public StudentResponse getOne(UserId userId, Long id) {
    return StudentResponse.from(findStudentByIdAndUserIdOrThrow(studentRepository, id, userId));
  }
}
