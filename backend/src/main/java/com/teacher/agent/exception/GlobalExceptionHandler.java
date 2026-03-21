package com.teacher.agent.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException exception) {
    ErrorCode errorCode = exception.getErrorCode();
    String message = exception.getDetail() != null ? exception.getDetail() : errorCode.getMessage();

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

    return ResponseEntity
        .badRequest()
        .body(ErrorResponse.of(ErrorCode.INVALID_INPUT, message));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception exception) {
    return ResponseEntity
        .internalServerError()
        .body(ErrorResponse.of(ErrorCode.INTERNAL_ERROR, exception.getMessage()));
  }
}
