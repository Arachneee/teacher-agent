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

  public static GenerationContext from(LessonCreateCommand command,
      Recurrence recurrence, UUID groupId) {
    long duration = Duration.between(command.startTime(), command.endTime()).toMinutes();
    return new GenerationContext(command.userId(), command.title(), command.startTime(), duration,
        recurrence, groupId);
  }

  public LocalDate startDate() {
    return startTime.toLocalDate();
  }

  public LocalDate endDate() {
    return recurrence.getEndDate();
  }
}
