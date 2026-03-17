package com.teacher.agent.service;

import com.teacher.agent.domain.Teacher;
import com.teacher.agent.domain.TeacherRepository;
import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.TeacherResponse;
import com.teacher.agent.dto.TeacherUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.teacher.agent.util.RepositoryUtil.findTeacherByUserIdOrThrow;

@Service
@RequiredArgsConstructor
public class TeacherCommandService {

  private final TeacherRepository teacherRepository;

  @Transactional
  public TeacherResponse updateByUserId(String userId, TeacherUpdateRequest request) {
    Teacher teacher = findTeacherByUserIdOrThrow(teacherRepository, new UserId(userId));
    teacher.updateProfile(request.name(), request.subject());
    return TeacherResponse.from(teacher);
  }
}
