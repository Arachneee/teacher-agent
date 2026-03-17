package com.teacher.agent.controller;

import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.AttendeeCreateRequest;
import com.teacher.agent.dto.AttendeeResponse;
import com.teacher.agent.service.AttendeeCommandService;
import com.teacher.agent.service.AttendeeQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lessons/{lessonId}/attendees")
@RequiredArgsConstructor
@Validated
public class AttendeeController {

  private final AttendeeQueryService attendeeQueryService;
  private final AttendeeCommandService attendeeCommandService;

  @PostMapping
  public ResponseEntity<AttendeeResponse> add(UserId userId, @PathVariable @Positive Long lessonId,
      @RequestBody @Valid AttendeeCreateRequest request) {
    return ResponseEntity.status(201).body(attendeeCommandService.add(userId, lessonId, request));
  }

  @GetMapping
  public ResponseEntity<List<AttendeeResponse>> getAll(UserId userId,
      @PathVariable @Positive Long lessonId) {
    return ResponseEntity.ok(attendeeQueryService.getAll(userId, lessonId));
  }

  @DeleteMapping("/{attendeeId}")
  public ResponseEntity<Void> remove(UserId userId, @PathVariable @Positive Long lessonId,
      @PathVariable @Positive Long attendeeId) {
    attendeeCommandService.remove(userId, lessonId, attendeeId);
    return ResponseEntity.noContent().build();
  }
}
