package com.teacher.agent.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record FeedbackInstructionsUpdateRequest(
    @NotNull @Size(max = 50) List<@Size(max = 200) String> instructions) {
}
