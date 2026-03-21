package com.teacher.agent.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorMessages {

  // ValidationUtil 메시지 템플릿
  public static final String NOT_NULL_TEMPLATE = "%s은(는) null일 수 없습니다.";
  public static final String NOT_BLANK_TEMPLATE = "%s은(는) 비어 있을 수 없습니다.";
  public static final String MAX_LENGTH_TEMPLATE = "%s은(는) %d자 이하여야 합니다.";
  public static final String POSITIVE_TEMPLATE = "%s은(는) 양수여야 합니다.";
  public static final String NOT_NEGATIVE_TEMPLATE = "%s은(는) 0 이상이어야 합니다.";
  public static final String NOT_EMPTY_TEMPLATE = "%s은(는) 비어 있을 수 없습니다.";
  public static final String INVALID_ARGUMENT_TEMPLATE = "%s이(가) 유효하지 않습니다.";

  // Domain 메시지
  public static final String ATTENDEE_ALREADY_EXISTS = "이미 등록된 수강생입니다: ";
  public static final String ATTENDEE_NOT_FOUND = "수강생을 찾을 수 없습니다: ";
  public static final String FEEDBACK_KEYWORD_NOT_FOUND = "피드백 키워드를 찾을 수 없습니다: ";
  public static final String AI_CONTENT_REQUIRED_FOR_LIKE = "AI 콘텐츠가 없으면 좋아요를 할 수 없습니다.";
  public static final String ALREADY_LIKED = "이미 좋아요를 누른 상태입니다.";

  // LessonFactory 메시지
  public static final String RECURRENCE_REQUIRED = "반복 수업 생성 시 recurrence 정보가 필요합니다.";
  public static final String MAX_RECURRENCE_PERIOD_ERROR = "반복 수업은 최대 6개월까지만 설정할 수 있습니다.";

  // LessonCommandService 메시지
  public static final String SOME_STUDENTS_NOT_FOUND = "일부 학생을 찾을 수 없습니다.";

  // ResourceNotFoundException 메시지 템플릿
  public static final String TEACHER_NOT_FOUND_TEMPLATE = "Teacher not found: %s";
  public static final String STUDENT_NOT_FOUND_TEMPLATE = "Student not found: %d";
  public static final String LESSON_NOT_FOUND_TEMPLATE = "Lesson not found: %d";
  public static final String FEEDBACK_NOT_FOUND_TEMPLATE = "Feedback not found: %d";
  public static final String FEEDBACK_KEYWORD_NOT_FOUND_TEMPLATE = "FeedbackKeyword not found: %d";
  public static final String ATTENDEE_NOT_FOUND_TEMPLATE = "Attendee not found: %d";

  // FeedbackAiService 메시지
  public static final String PROMPT_FILE_READ_ERROR = "프롬프트 파일을 읽을 수 없습니다: ";
}
