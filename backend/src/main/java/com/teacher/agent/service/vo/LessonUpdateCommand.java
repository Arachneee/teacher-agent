package com.teacher.agent.service.vo;

import com.teacher.agent.domain.vo.Recurrence;
import com.teacher.agent.domain.vo.UpdateScope;
import java.time.LocalDateTime;
import java.util.List;

public record LessonUpdateCommand(
    String title,
    LocalDateTime startTime,
    LocalDateTime endTime,
    UpdateScope scope,
    Recurrence recurrence,
    List<Long> addStudentIds,
    List<Long> removeStudentIds) {
}
