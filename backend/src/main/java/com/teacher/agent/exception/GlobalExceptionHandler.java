package com.teacher.agent.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException exception) {
    ErrorCode errorCode = exception.getErrorCode();
    String message = exception.getDetail() != null ? exception.getDetail() : errorCode.getMessage();

    log.warn("비즈니스 예외 발생: code={}, message={}", errorCode.getCode(), message);

    return ResponseEntity
        .status(errorCode.getStatus())
        .body(ErrorResponse.of(errorCode, message));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException exception) {
    String message = exception.getBindingResult().getFieldErrors().stream()
        .findFirst()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .orElse("유효성 검사에 실패했습니다.");

    log.warn("유효성 검사 실패: {}", message);

    return ResponseEntity
        .badRequest()
        .body(ErrorResponse.of(ErrorCode.INVALID_INPUT, message));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolationException(
      ConstraintViolationException exception) {
    String message = exception.getConstraintViolations().stream()
        .findFirst()
        .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
        .orElse("유효성 검사에 실패했습니다.");

    log.warn("제약 조건 위반: {}", message);

    return ResponseEntity
        .badRequest()
        .body(ErrorResponse.of(ErrorCode.INVALID_INPUT, message));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
      IllegalArgumentException exception) {
    log.warn("잘못된 인자: {}", exception.getMessage());

    return ResponseEntity
        .badRequest()
        .body(ErrorResponse.of(ErrorCode.INVALID_INPUT, exception.getMessage()));
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ErrorResponse> handleIllegalStateException(
      IllegalStateException exception) {
    log.warn("잘못된 상태: {}", exception.getMessage());

    return ResponseEntity
        .badRequest()
        .body(ErrorResponse.of(ErrorCode.INVALID_INPUT, exception.getMessage()));
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleBadCredentialsException(
      BadCredentialsException exception) {
    log.warn("인증 실패: {}", exception.getMessage());

    return ResponseEntity
        .status(ErrorCode.UNAUTHORIZED.getStatus())
        .body(ErrorResponse.of(ErrorCode.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception exception) {
    log.error("예상치 못한 오류 발생", exception);

    return ResponseEntity
        .internalServerError()
        .body(ErrorResponse.of(ErrorCode.INTERNAL_ERROR, "서버 내부 오류가 발생했습니다."));
  }
}
