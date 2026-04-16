package com.teacher.agent.service.vo;

import com.teacher.agent.domain.vo.SchoolGrade;
import java.time.LocalDateTime;
import java.util.List;

public record LessonDetailRow(
    Long attendeeId,
    Long studentId,
    String studentName,
    String studentMemo,
    SchoolGrade studentGrade,
    LocalDateTime studentCreatedAt,
    LocalDateTime studentUpdatedAt,
    Long feedbackId,
    Long feedbackStudentId,
    Long feedbackLessonId,
    String aiContent,
    List<String> instructions,
    boolean liked,
    LocalDateTime feedbackCreatedAt,
    LocalDateTime feedbackUpdatedAt,
    Long keywordId,
    String keyword,
    Boolean keywordRequired,
    LocalDateTime keywordCreatedAt) {
}
