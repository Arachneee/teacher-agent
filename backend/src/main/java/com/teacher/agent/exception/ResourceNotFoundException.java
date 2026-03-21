package com.teacher.agent.exception;

import static com.teacher.agent.util.ErrorMessages.*;

public class ResourceNotFoundException extends BusinessException {

  public ResourceNotFoundException(ErrorCode errorCode) {
    super(errorCode);
  }

  public ResourceNotFoundException(ErrorCode errorCode, String detail) {
    super(errorCode, detail);
  }

  public static ResourceNotFoundException teacher(String userId) {
    return new ResourceNotFoundException(ErrorCode.TEACHER_NOT_FOUND,
        TEACHER_NOT_FOUND_TEMPLATE.formatted(userId));
  }

  public static ResourceNotFoundException student(Long studentId) {
    return new ResourceNotFoundException(ErrorCode.STUDENT_NOT_FOUND,
        STUDENT_NOT_FOUND_TEMPLATE.formatted(studentId));
  }

  public static ResourceNotFoundException lesson(Long lessonId) {
    return new ResourceNotFoundException(ErrorCode.LESSON_NOT_FOUND,
        LESSON_NOT_FOUND_TEMPLATE.formatted(lessonId));
  }

  public static ResourceNotFoundException feedback(Long feedbackId) {
    return new ResourceNotFoundException(ErrorCode.FEEDBACK_NOT_FOUND,
        FEEDBACK_NOT_FOUND_TEMPLATE.formatted(feedbackId));
  }

  public static ResourceNotFoundException feedbackKeyword(Long keywordId) {
    return new ResourceNotFoundException(ErrorCode.FEEDBACK_KEYWORD_NOT_FOUND,
        FEEDBACK_KEYWORD_NOT_FOUND_TEMPLATE.formatted(keywordId));
  }

  public static ResourceNotFoundException attendee(Long attendeeId) {
    return new ResourceNotFoundException(ErrorCode.ATTENDEE_NOT_FOUND,
        ATTENDEE_NOT_FOUND_TEMPLATE.formatted(attendeeId));
  }
}
