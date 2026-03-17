package com.teacher.agent.service;

import static com.teacher.agent.util.RepositoryUtil.findTeacherByUserIdOrThrow;

import com.teacher.agent.domain.Teacher;
import com.teacher.agent.domain.TeacherRepository;
import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.TeacherResponse;
import com.teacher.agent.dto.TeacherUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeacherCommandService {

  private final TeacherRepository teacherRepository;

  @Transactional
  public TeacherResponse updateByUserId(UserId userId, TeacherUpdateRequest request) {
    Teacher teacher = findTeacherByUserIdOrThrow(teacherRepository, userId);
    teacher.updateProfile(request.name(), request.subject());
    return TeacherResponse.from(teacher);
  }
}
