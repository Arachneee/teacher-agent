package com.teacher.agent.domain;

import static com.teacher.agent.util.ValidationUtil.checkNotBlank;

public record UserId(String value) {
  public UserId {
    checkNotBlank(value, "userId");
  }
}
