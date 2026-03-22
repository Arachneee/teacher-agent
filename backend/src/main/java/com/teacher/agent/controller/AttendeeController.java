package com.teacher.agent.controller;

import com.teacher.agent.domain.vo.UpdateScope;
import com.teacher.agent.domain.vo.UserId;
import com.teacher.agent.dto.AttendeeCreateRequest;
import com.teacher.agent.dto.AttendeeResponse;
import com.teacher.agent.service.AttendeeCommandService;
import com.teacher.agent.service.AttendeeQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    return ResponseEntity.ok(
        attendeeCommandService.add(userId, lessonId, request.studentId(),
            request.resolvedScope()));
  }

  @GetMapping
  public ResponseEntity<List<AttendeeResponse>> getAll(UserId userId,
      @PathVariable @Positive Long lessonId) {
    return ResponseEntity.ok(attendeeQueryService.getAll(userId, lessonId));
  }

  @DeleteMapping("/{attendeeId}")
  public ResponseEntity<Void> remove(UserId userId, @PathVariable @Positive Long lessonId,
      @PathVariable @Positive Long attendeeId,
      @RequestParam(required = false) UpdateScope scope) {
    attendeeCommandService.remove(userId, lessonId, attendeeId, scope);
    return ResponseEntity.noContent().build();
  }
}
