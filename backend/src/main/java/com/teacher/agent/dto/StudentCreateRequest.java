package com.teacher.agent.dto;

import com.teacher.agent.domain.vo.SchoolGrade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StudentCreateRequest(
    @NotBlank String name,
    @Size(max = 500) String memo,
    @NotNull SchoolGrade grade) {
}
