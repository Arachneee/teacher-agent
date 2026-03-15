package com.teacher.agent.dto;

import com.teacher.agent.domain.Student;

import java.time.LocalDateTime;

public record StudentResponse(
        Long id,
        String name,
        String memo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static StudentResponse from(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getName(),
                student.getMemo(),
                student.getCreatedAt(),
                student.getUpdatedAt()
        );
    }
}
