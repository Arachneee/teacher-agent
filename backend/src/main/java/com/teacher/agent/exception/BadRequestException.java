package com.teacher.agent.exception;

public class BadRequestException extends BusinessException {

  public BadRequestException(ErrorCode errorCode) {
    super(errorCode);
  }

  public BadRequestException(ErrorCode errorCode, String detail) {
    super(errorCode, detail);
  }

  public static BadRequestException keywordRequired() {
    return new BadRequestException(ErrorCode.KEYWORD_REQUIRED);
  }

  public static BadRequestException studentNotEnrolled() {
    return new BadRequestException(ErrorCode.STUDENT_NOT_ENROLLED);
  }

  public static BadRequestException noLessonGenerated() {
    return new BadRequestException(ErrorCode.NO_LESSON_GENERATED);
  }

  public static BadRequestException feedbackLikeRequiresAiContent() {
    return new BadRequestException(ErrorCode.FEEDBACK_LIKE_REQUIRES_AI_CONTENT);
  }
}
