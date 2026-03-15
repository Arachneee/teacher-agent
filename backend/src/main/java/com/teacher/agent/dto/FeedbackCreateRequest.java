package com.teacher.agent.dto;

import jakarta.validation.constraints.NotNull;

public record FeedbackCreateRequest(
        @NotNull Long studentId
) {
}
