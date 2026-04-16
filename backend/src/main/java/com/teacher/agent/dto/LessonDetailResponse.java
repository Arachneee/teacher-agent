package com.teacher.agent.dto;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.teacher.agent.domain.Lesson;
import com.teacher.agent.service.vo.LessonDetailRow;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;

public record LessonDetailResponse(
    Long id,
    String title,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String recurrenceGroupId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<AttendeeDetailItem> attendees) {

  public static LessonDetailResponse from(Lesson lesson, List<LessonDetailRow> rows) {
    List<AttendeeDetailItem> attendees = rows.stream()
        .collect(groupingBy(LessonDetailRow::attendeeId, LinkedHashMap::new, toList())).entrySet()
        .stream().map(entry -> AttendeeDetailItem.from(entry.getKey(), entry.getValue())).toList();
    return new LessonDetailResponse(lesson.getId(), lesson.getTitle(), lesson.getStartTime(),
        lesson.getEndTime(),
        lesson.getRecurrenceGroupId() != null ? lesson.getRecurrenceGroupId().toString() : null,
        lesson.getCreatedAt(), lesson.getUpdatedAt(), attendees);
  }

  public record AttendeeDetailItem(
      Long attendeeId,
      StudentResponse student,
      FeedbackResponse feedback) {

    static AttendeeDetailItem from(Long attendeeId, List<LessonDetailRow> rows) {
      LessonDetailRow first = rows.getFirst();

      StudentResponse student = new StudentResponse(first.studentId(), first.studentName(),
          first.studentMemo(), first.studentGrade(), first.studentCreatedAt(),
          first.studentUpdatedAt());

      List<FeedbackResponse.KeywordItem> keywords = rows.stream()
          .filter(row -> row.keywordId() != null)
          .collect(toMap(LessonDetailRow::keywordId,
              row -> new FeedbackResponse.KeywordItem(row.keywordId(), row.keyword(),
                  Boolean.TRUE.equals(row.keywordRequired()), row.keywordCreatedAt()),
              (existing, duplicate) -> existing, LinkedHashMap::new))
          .values().stream().toList();

      FeedbackResponse feedback = new FeedbackResponse(first.feedbackId(),
          first.feedbackStudentId(), first.feedbackLessonId(), null, null,
          first.aiContent(), first.instructions(), keywords, first.liked(),
          first.feedbackCreatedAt(), first.feedbackUpdatedAt());

      return new AttendeeDetailItem(attendeeId, student, feedback);
    }
  }
}
