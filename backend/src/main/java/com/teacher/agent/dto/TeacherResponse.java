package com.teacher.agent.dto;

import com.teacher.agent.domain.Teacher;
import java.time.LocalDateTime;

public record TeacherResponse(Long id, String userId, String name, String subject,
    LocalDateTime createdAt, LocalDateTime updatedAt) {
  public static TeacherResponse from(Teacher teacher) {
    return new TeacherResponse(teacher.getId(), teacher.getUserId().value(), teacher.getName(),
        teacher.getSubject(), teacher.getCreatedAt(), teacher.getUpdatedAt());
  }
}
