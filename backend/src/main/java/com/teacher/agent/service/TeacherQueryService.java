package com.teacher.agent.service;

import com.teacher.agent.domain.Teacher;
import com.teacher.agent.domain.repository.TeacherRepository;
import com.teacher.agent.domain.vo.UserId;
import com.teacher.agent.dto.TeacherResponse;
import com.teacher.agent.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeacherQueryService {

  private final TeacherRepository teacherRepository;

  public TeacherResponse getByUserId(UserId userId) {
    return TeacherResponse.from(findByUserId(userId));
  }

  Teacher findByUserId(UserId userId) {
    return teacherRepository.findByUserId(userId)
        .orElseThrow(() -> ResourceNotFoundException.teacher(userId.value()));
  }
}
