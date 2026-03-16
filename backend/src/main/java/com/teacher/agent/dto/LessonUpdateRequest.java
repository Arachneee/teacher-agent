package com.teacher.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record LessonUpdateRequest(
        @NotBlank String title,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime
) {
}
