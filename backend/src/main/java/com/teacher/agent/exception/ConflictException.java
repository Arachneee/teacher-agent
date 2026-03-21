package com.teacher.agent.exception;

public class ConflictException extends BusinessException {

  public ConflictException(ErrorCode errorCode) {
    super(errorCode);
  }

  public ConflictException(ErrorCode errorCode, String detail) {
    super(errorCode, detail);
  }

  public static ConflictException duplicateAttendee() {
    return new ConflictException(ErrorCode.DUPLICATE_ATTENDEE);
  }
}
