package com.teacher.agent.service;

import com.teacher.agent.domain.Lesson;
import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.AttendeeResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendeeQueryService {

  private final LessonQueryService lessonQueryService;

  public List<AttendeeResponse> getAll(UserId userId, Long lessonId) {
    Lesson lesson = lessonQueryService.findByIdAndVerifyOwner(lessonId, userId);
    return lesson.getAttendees().stream().map(AttendeeResponse::from).toList();
  }
}
