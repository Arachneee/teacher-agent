package com.teacher.agent.dto;

import com.teacher.agent.domain.UpdateScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record LessonUpdateRequest(
    @NotBlank String title,
    @NotNull LocalDateTime startTime,
    @NotNull LocalDateTime endTime,
    UpdateScope scope) {

  public LessonUpdateRequest(String title, LocalDateTime startTime, LocalDateTime endTime) {
    this(title, startTime, endTime, UpdateScope.SINGLE);
  }

  public UpdateScope resolvedScope() {
    return scope != null ? scope : UpdateScope.SINGLE;
  }
}
