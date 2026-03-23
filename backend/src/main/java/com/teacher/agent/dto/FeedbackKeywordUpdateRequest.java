package com.teacher.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FeedbackKeywordUpdateRequest(
    @NotBlank @Size(max = 100) String keyword,
    boolean required) {
}
