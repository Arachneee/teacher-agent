package com.teacher.agent.service;

import static com.teacher.agent.util.RepositoryUtil.findLessonByIdAndUserIdOrThrow;

import com.teacher.agent.domain.Lesson;
import com.teacher.agent.domain.LessonRepository;
import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.LessonResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonQueryService {

  private final LessonRepository lessonRepository;

  public List<LessonResponse> getByTeacherAndWeek(UserId userId, LocalDate weekStart) {
    LocalDateTime from = weekStart.atStartOfDay();
    LocalDateTime to = weekStart.plusDays(7).atStartOfDay();

    return lessonRepository.findAllByUserIdAndStartTimeBetween(userId, from, to)
        .stream().map(LessonResponse::from).toList();
  }

  public LessonResponse getOne(UserId userId, Long id) {
    return LessonResponse.from(findByIdAndVerifyOwner(id, userId));
  }

  Lesson findByIdAndVerifyOwner(Long id, UserId userId) {
    return findLessonByIdAndUserIdOrThrow(lessonRepository, id, userId);
  }
}
