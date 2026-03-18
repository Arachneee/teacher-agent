package com.teacher.agent.dto;

import com.teacher.agent.domain.Lesson;
import java.time.LocalDateTime;
import java.util.List;

public record LessonDetailResponse(
    Long id,
    String title,
    LocalDateTime startTime,
    LocalDateTime endTime,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<AttendeeDetailItem> attendees) {

  public static LessonDetailResponse from(Lesson lesson, List<AttendeeDetailItem> attendees) {
    return new LessonDetailResponse(lesson.getId(), lesson.getTitle(), lesson.getStartTime(),
        lesson.getEndTime(), lesson.getCreatedAt(), lesson.getUpdatedAt(), attendees);
  }

  public record AttendeeDetailItem(
      Long attendeeId,
      StudentResponse student,
      FeedbackResponse feedback) {
  }
}
