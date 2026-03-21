package com.teacher.agent.dto;

import com.teacher.agent.domain.Recurrence;
import com.teacher.agent.domain.UserId;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record GenerationContext(
    UserId userId,
    String title,
    LocalDateTime startTime,
    long durationMinutes,
    Recurrence recurrence,
    UUID groupId) {

  public static GenerationContext from(UserId userId, LessonCreateRequest request,
      Recurrence recurrence, UUID groupId) {
    long duration = Duration.between(request.startTime(), request.endTime()).toMinutes();
    return new GenerationContext(userId, request.title(), request.startTime(), duration,
        recurrence, groupId);
  }

  public LocalDate startDate() {
    return startTime.toLocalDate();
  }

  public LocalDate endDate() {
    return recurrence.getEndDate();
  }
}
