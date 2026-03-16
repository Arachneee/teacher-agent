package com.teacher.agent.controller;

import com.teacher.agent.dto.AttendeeCreateRequest;
import com.teacher.agent.dto.AttendeeResponse;
import com.teacher.agent.service.AttendeeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<AttendeeResponse> add(Authentication authentication,
                                                @PathVariable @Positive Long lessonId,
                                                @RequestBody @Valid AttendeeCreateRequest request) {
        return ResponseEntity.status(201).body(attendeeService.add(authentication.getName(), lessonId, request));
    }

    @GetMapping
    public ResponseEntity<List<AttendeeResponse>> getAll(@PathVariable @Positive Long lessonId) {
        return ResponseEntity.ok(attendeeService.getAll(lessonId));
    }

    @DeleteMapping("/{attendeeId}")
    public ResponseEntity<Void> remove(Authentication authentication,
                                       @PathVariable @Positive Long lessonId,
                                       @PathVariable @Positive Long attendeeId) {
        attendeeService.remove(authentication.getName(), lessonId, attendeeId);
        return ResponseEntity.noContent().build();
    }
}
