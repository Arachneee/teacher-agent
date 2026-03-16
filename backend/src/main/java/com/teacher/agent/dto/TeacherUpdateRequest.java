package com.teacher.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TeacherUpdateRequest(
        @NotBlank String name,
        @Size(max = 100) String subject
) {
}
