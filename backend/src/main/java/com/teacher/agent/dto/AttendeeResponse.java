package com.teacher.agent.dto;

import com.teacher.agent.domain.Attendee;

import java.time.LocalDateTime;

public record AttendeeResponse(
        Long id,
        Long lessonId,
        Long studentId,
        LocalDateTime createdAt
) {
    public static AttendeeResponse from(Attendee attendee) {
        return new AttendeeResponse(
                attendee.getId(),
                attendee.getLesson().getId(),
                attendee.getStudentId(),
                attendee.getCreatedAt()
        );
    }
}
