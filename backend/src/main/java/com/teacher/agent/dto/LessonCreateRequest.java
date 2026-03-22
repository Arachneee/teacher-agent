package com.teacher.agent.dto;

import com.teacher.agent.domain.vo.Recurrence;
import com.teacher.agent.domain.vo.UserId;
import com.teacher.agent.service.vo.LessonCreateCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public record LessonCreateRequest(
    @NotBlank String title,
    @NotNull LocalDateTime startTime,
    @NotNull LocalDateTime endTime,
    RecurrenceCreateRequest recurrence,
    List<Long> studentIds) {

  public LessonCreateCommand toCommand(UserId userId) {
    Recurrence recurrenceEntity = recurrence != null ? recurrence.toEntity() : null;
    return new LessonCreateCommand(userId, title, startTime, endTime, recurrenceEntity, studentIds);
  }
}
