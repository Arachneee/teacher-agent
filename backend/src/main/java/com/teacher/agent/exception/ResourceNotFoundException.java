package com.teacher.agent.exception;

public class ResourceNotFoundException extends BusinessException {

  public ResourceNotFoundException(ErrorCode errorCode) {
    super(errorCode);
  }

  public ResourceNotFoundException(ErrorCode errorCode, String detail) {
    super(errorCode, detail);
  }

  public static ResourceNotFoundException teacher(String userId) {
    return new ResourceNotFoundException(ErrorCode.TEACHER_NOT_FOUND,
        "Teacher not found: " + userId);
  }

  public static ResourceNotFoundException student(Long studentId) {
    return new ResourceNotFoundException(ErrorCode.STUDENT_NOT_FOUND,
        "Student not found: " + studentId);
  }

  public static ResourceNotFoundException lesson(Long lessonId) {
    return new ResourceNotFoundException(ErrorCode.LESSON_NOT_FOUND,
        "Lesson not found: " + lessonId);
  }

  public static ResourceNotFoundException feedback(Long feedbackId) {
    return new ResourceNotFoundException(ErrorCode.FEEDBACK_NOT_FOUND,
        "Feedback not found: " + feedbackId);
  }

  public static ResourceNotFoundException feedbackKeyword(Long keywordId) {
    return new ResourceNotFoundException(ErrorCode.FEEDBACK_KEYWORD_NOT_FOUND,
        "FeedbackKeyword not found: " + keywordId);
  }

  public static ResourceNotFoundException attendee(Long attendeeId) {
    return new ResourceNotFoundException(ErrorCode.ATTENDEE_NOT_FOUND,
        "Attendee not found: " + attendeeId);
  }
}
