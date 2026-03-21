package com.teacher.agent.dto;

import com.teacher.agent.domain.Lesson;
import java.time.LocalDateTime;
import java.util.UUID;

public record LessonResponse(
    Long id,
    String userId,
    String title,
    LocalDateTime startTime,
    LocalDateTime endTime,
    UUID recurrenceGroupId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  public static LessonResponse from(Lesson lesson) {
    return new LessonResponse(lesson.getId(), lesson.getUserId().value(), lesson.getTitle(),
        lesson.getStartTime(), lesson.getEndTime(), lesson.getRecurrenceGroupId(),
        lesson.getCreatedAt(), lesson.getUpdatedAt());
  }
}
