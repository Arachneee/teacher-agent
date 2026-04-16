package com.teacher.agent.dto;

import jakarta.validation.constraints.Size;

public record FeedbackGenerateRequest(
    @Size(max = 200) String instruction) {
}
