package com.teacher.agent.dto;

import com.teacher.agent.domain.Recurrence;
import com.teacher.agent.domain.UserId;
import java.time.LocalDateTime;
import java.util.List;

public record LessonCreateCommand(
    UserId userId,
    String title,
    LocalDateTime startTime,
    LocalDateTime endTime,
    Recurrence recurrence,
    List<Long> studentIds) {
}
