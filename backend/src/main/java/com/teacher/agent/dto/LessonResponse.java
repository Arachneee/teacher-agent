package com.teacher.agent.dto;

import com.teacher.agent.domain.Lesson;

import java.time.LocalDateTime;

public record LessonResponse(
        Long id,
        Long teacherId,
        String title,
        LocalDateTime startTime,
        LocalDateTime endTime,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static LessonResponse from(Lesson lesson) {
        return new LessonResponse(
                lesson.getId(),
                lesson.getTeacherId(),
                lesson.getTitle(),
                lesson.getStartTime(),
                lesson.getEndTime(),
                lesson.getCreatedAt(),
                lesson.getUpdatedAt()
        );
    }
}
