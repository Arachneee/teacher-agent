package com.teacher.agent.dto;

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
}
