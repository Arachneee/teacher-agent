package com.teacher.agent.dto;

import com.teacher.agent.domain.UserEvent;

public record UserEventResponse(Long id) {

  public static UserEventResponse from(UserEvent userEvent) {
    return new UserEventResponse(userEvent.getId());
  }
}
