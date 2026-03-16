package com.teacher.agent.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record FeedbackCreateRequest(
        @NotNull @Positive Long studentId,
        @NotNull @Positive Long lessonId
) {
}
