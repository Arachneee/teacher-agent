package com.teacher.agent.controller;

import com.teacher.agent.domain.vo.UserId;
import com.teacher.agent.dto.UserEventRequest;
import com.teacher.agent.dto.UserEventResponse;
import com.teacher.agent.service.UserEventCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Validated
public class UserEventController {

  private final UserEventCommandService userEventCommandService;

  @PostMapping
  public ResponseEntity<UserEventResponse> create(UserId userId,
      @RequestBody @Valid UserEventRequest request) {
    return ResponseEntity.ok(userEventCommandService.save(
        userId.value(), request.eventType(), request.metadata()));
  }
}
