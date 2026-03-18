package com.teacher.agent.service;

import com.teacher.agent.domain.Lesson;
import com.teacher.agent.domain.LessonRepository;
import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.LessonDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonDetailQueryService {

  private final LessonQueryService lessonQueryService;
  private final LessonRepository lessonRepository;

  public LessonDetailResponse getDetail(UserId userId, Long lessonId) {
    Lesson lesson = lessonQueryService.findByIdAndVerifyOwner(lessonId, userId);
    var rows = lessonRepository.findDetailRows(lessonId, lesson.getUserId());
    return LessonDetailResponse.from(lesson, rows);
  }
}
