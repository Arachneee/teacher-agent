package com.teacher.agent.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  // 400 Bad Request
  INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "잘못된 입력입니다."),
  KEYWORD_REQUIRED(HttpStatus.BAD_REQUEST, "KEYWORD_REQUIRED", "키워드가 없습니다. 먼저 키워드를 추가해주세요."),
  STUDENT_NOT_ENROLLED(HttpStatus.BAD_REQUEST, "STUDENT_NOT_ENROLLED", "해당 학생은 이 수업에 등록되어 있지 않습니다."),
  NO_LESSON_GENERATED(HttpStatus.BAD_REQUEST, "NO_LESSON_GENERATED", "선택한 기간과 요일 설정으로는 수업이 생성되지 않아요. 종료일을 늘리거나 요일을 변경해주세요."),
  FEEDBACK_LIKE_REQUIRES_AI_CONTENT(HttpStatus.BAD_REQUEST, "FEEDBACK_LIKE_REQUIRES_AI_CONTENT", "AI 피드백이 없으면 좋아요를 할 수 없습니다."),

  // 401 Unauthorized
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증 정보가 존재하지 않습니다."),

  // 404 Not Found
  RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "리소스를 찾을 수 없습니다."),
  TEACHER_NOT_FOUND(HttpStatus.NOT_FOUND, "TEACHER_NOT_FOUND", "선생님을 찾을 수 없습니다."),
  STUDENT_NOT_FOUND(HttpStatus.NOT_FOUND, "STUDENT_NOT_FOUND", "학생을 찾을 수 없습니다."),
  LESSON_NOT_FOUND(HttpStatus.NOT_FOUND, "LESSON_NOT_FOUND", "수업을 찾을 수 없습니다."),
  FEEDBACK_NOT_FOUND(HttpStatus.NOT_FOUND, "FEEDBACK_NOT_FOUND", "피드백을 찾을 수 없습니다."),
  FEEDBACK_KEYWORD_NOT_FOUND(HttpStatus.NOT_FOUND, "FEEDBACK_KEYWORD_NOT_FOUND", "피드백 키워드를 찾을 수 없습니다."),
  ATTENDEE_NOT_FOUND(HttpStatus.NOT_FOUND, "ATTENDEE_NOT_FOUND", "수강생을 찾을 수 없습니다."),

  // 409 Conflict
  DUPLICATE_ATTENDEE(HttpStatus.CONFLICT, "DUPLICATE_ATTENDEE", "이미 등록된 수강생입니다."),

  // 500 Internal Server Error
  INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "서버 내부 오류가 발생했습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
