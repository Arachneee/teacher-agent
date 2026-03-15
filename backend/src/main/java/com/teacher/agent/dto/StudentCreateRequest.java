package com.teacher.agent.dto;

import com.teacher.agent.domain.Student;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudentCreateRequest(
        @NotBlank String name,
        @Size(max = 500) String memo
) {
    public Student toEntity() {
        return Student.create(name, memo);
    }
}
