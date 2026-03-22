package com.teacher.agent.dto;

import com.teacher.agent.domain.vo.UpdateScope;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AttendeeCreateRequest(@NotNull @Positive Long studentId, UpdateScope scope) {

  public UpdateScope resolvedScope() {
    return scope != null ? scope : UpdateScope.SINGLE;
  }
}
