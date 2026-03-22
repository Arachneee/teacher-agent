package com.teacher.agent.service;

import static com.teacher.agent.util.RepositoryUtil.findTeacherByUserIdOrThrow;

import com.teacher.agent.domain.Teacher;
import com.teacher.agent.domain.repository.TeacherRepository;
import com.teacher.agent.domain.vo.UserId;
import com.teacher.agent.dto.TeacherResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeacherCommandService {

  private final TeacherRepository teacherRepository;

  @Transactional
  public TeacherResponse updateByUserId(UserId userId, String name, String subject) {
    Teacher teacher = findTeacherByUserIdOrThrow(teacherRepository, userId);

    teacher.updateProfile(name, subject);

    return TeacherResponse.from(teacher);
  }
}
