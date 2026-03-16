package com.teacher.agent.controller;

import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.AttendeeCreateRequest;
import com.teacher.agent.dto.AttendeeResponse;
import com.teacher.agent.service.AttendeeService;
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

    private final AttendeeService attendeeService;

    @PostMapping
    public ResponseEntity<AttendeeResponse> add(UserId userId,
                                                @PathVariable @Positive Long lessonId,
                                                @RequestBody @Valid AttendeeCreateRequest request) {
        return ResponseEntity.status(201).body(attendeeService.add(userId, lessonId, request));
    }

    @GetMapping
    public ResponseEntity<List<AttendeeResponse>> getAll(UserId userId, @PathVariable @Positive Long lessonId) {
        return ResponseEntity.ok(attendeeService.getAll(userId, lessonId));
    }

    @DeleteMapping("/{attendeeId}")
    public ResponseEntity<Void> remove(UserId userId,
                                       @PathVariable @Positive Long lessonId,
                                       @PathVariable @Positive Long attendeeId) {
        attendeeService.remove(userId, lessonId, attendeeId);
        return ResponseEntity.noContent().build();
    }
}
