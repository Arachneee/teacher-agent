package com.teacher.agent.dto;

import jakarta.validation.constraints.NotBlank;

public record UserEventRequest(
    @NotBlank String eventType,
    String metadata) {
}
