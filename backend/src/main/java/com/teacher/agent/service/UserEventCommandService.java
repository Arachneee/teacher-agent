package com.teacher.agent.service;

import com.teacher.agent.domain.UserEvent;
import com.teacher.agent.domain.repository.UserEventRepository;
import com.teacher.agent.dto.UserEventResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserEventCommandService {

  private final UserEventRepository userEventRepository;

  @Transactional
  public UserEventResponse save(String userId, String eventType, String metadata) {
    UserEvent userEvent = UserEvent.create(userId, eventType, metadata);
    userEventRepository.save(userEvent);
    return UserEventResponse.from(userEvent);
  }
}
