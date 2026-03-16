package com.teacher.agent.dto;

import com.teacher.agent.domain.Lesson;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record LessonCreateRequest(
        @NotBlank String title,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime
) {
    public Lesson toEntity(Long teacherId) {
        return Lesson.create(teacherId, title, startTime, endTime);
    }
}
