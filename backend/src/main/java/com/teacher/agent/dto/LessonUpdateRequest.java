package com.teacher.agent.dto;

import com.teacher.agent.domain.vo.UpdateScope;
import com.teacher.agent.service.vo.LessonUpdateCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public record LessonUpdateRequest(
    @NotBlank String title,
    @NotNull LocalDateTime startTime,
    @NotNull LocalDateTime endTime,
    UpdateScope scope,
    @Valid RecurrenceCreateRequest recurrence,
    List<Long> addStudentIds,
    List<Long> removeStudentIds) {

  public LessonUpdateRequest(String title, LocalDateTime startTime, LocalDateTime endTime) {
    this(title, startTime, endTime, UpdateScope.SINGLE, null, null, null);
  }

  public UpdateScope resolvedScope() {
    return scope != null ? scope : UpdateScope.SINGLE;
  }

  public List<Long> resolvedAddStudentIds() {
    return addStudentIds != null ? addStudentIds : List.of();
  }

  public List<Long> resolvedRemoveStudentIds() {
    return removeStudentIds != null ? removeStudentIds : List.of();
  }

  public LessonUpdateCommand toUpdateCommand() {
    return new LessonUpdateCommand(
        title,
        startTime,
        endTime,
        resolvedScope(),
        recurrence != null ? recurrence.toEntity() : null,
        resolvedAddStudentIds(),
        resolvedRemoveStudentIds());
  }
}
