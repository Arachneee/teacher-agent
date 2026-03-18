package com.teacher.agent.service;

import com.teacher.agent.domain.Lesson;
import com.teacher.agent.domain.LessonRepository;
import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.LessonCreateRequest;
import com.teacher.agent.dto.LessonResponse;
import com.teacher.agent.dto.LessonUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LessonCommandService {

  private final LessonQueryService lessonQueryService;
  private final LessonRepository lessonRepository;

  @Transactional
  public LessonResponse create(UserId userId, LessonCreateRequest request) {
    return LessonResponse.from(lessonRepository.save(request.toEntity(userId)));
  }

  @Transactional
  public LessonResponse update(UserId userId, Long id, LessonUpdateRequest request) {
    Lesson lesson = lessonQueryService.findByIdAndVerifyOwner(id, userId);

    lesson.update(request.title(), request.startTime(), request.endTime());

    return LessonResponse.from(lesson);
  }

  @Transactional
  public void delete(UserId userId, Long id) {
    lessonQueryService.findByIdAndVerifyOwner(id, userId);

    lessonRepository.deleteById(id);
  }
}
