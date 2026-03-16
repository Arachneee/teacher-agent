package com.teacher.agent.dto;

import com.teacher.agent.domain.Teacher;

public record AuthResponse(String userId, String name, String subject) {
    public static AuthResponse from(Teacher teacher) {
        return new AuthResponse(teacher.getUserId().value(), teacher.getName(), teacher.getSubject());
    }
}
