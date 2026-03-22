package com.teacher.agent.service.vo;

import com.teacher.agent.domain.vo.Recurrence;
import com.teacher.agent.domain.vo.UserId;
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
