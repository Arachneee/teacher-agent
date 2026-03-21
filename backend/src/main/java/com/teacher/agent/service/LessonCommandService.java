package com.teacher.agent.service;

import com.teacher.agent.domain.Lesson;
import com.teacher.agent.domain.LessonRepository;
import com.teacher.agent.domain.Recurrence;
import com.teacher.agent.domain.RecurrenceRepository;
import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.LessonCreateRequest;
import com.teacher.agent.dto.LessonResponse;
import com.teacher.agent.dto.LessonUpdateRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LessonCommandService {

  private final LessonQueryService lessonQueryService;
  private final LessonRepository lessonRepository;
  private final RecurrenceRepository recurrenceRepository;
  private final LessonFactory lessonFactory;

  @Transactional
  public LessonResponse create(UserId userId, LessonCreateRequest request) {
    Recurrence recurrence = (request.recurrence() != null) ? request.recurrence().toEntity() : null;
    if (recurrence != null) {
      recurrenceRepository.save(recurrence);
    }
    List<Lesson> lessons = lessonFactory.createFrom(userId, request.title(), request.startTime(),
        request.endTime(), recurrence);
    lessonRepository.saveAll(lessons);
    return LessonResponse.from(lessons.get(0));
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
