package com.teacher.agent.dto;

import com.teacher.agent.domain.vo.Recurrence;
import com.teacher.agent.domain.vo.RecurrenceType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public record RecurrenceCreateRequest(
    @NotNull RecurrenceType recurrenceType,
    @NotNull @Positive Integer intervalValue,
    List<DayOfWeek> daysOfWeek,
    @NotNull @Future LocalDate endDate) {
  public Recurrence toEntity() {
    return Recurrence.create(recurrenceType, intervalValue, daysOfWeek, endDate);
  }
}
