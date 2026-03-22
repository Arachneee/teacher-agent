package com.teacher.agent.service.vo;

import java.time.LocalDateTime;

public record LessonDetailRow(
    Long attendeeId,
    Long studentId,
    String studentName,
    String studentMemo,
    LocalDateTime studentCreatedAt,
    LocalDateTime studentUpdatedAt,
    Long feedbackId,
    Long feedbackStudentId,
    Long feedbackLessonId,
    String aiContent,
    boolean liked,
    LocalDateTime feedbackCreatedAt,
    LocalDateTime feedbackUpdatedAt,
    Long keywordId,
    String keyword,
    LocalDateTime keywordCreatedAt) {
}
