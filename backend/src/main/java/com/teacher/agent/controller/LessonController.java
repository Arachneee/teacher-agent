package com.teacher.agent.controller;

import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.LessonCreateRequest;
import com.teacher.agent.dto.LessonResponse;
import com.teacher.agent.dto.LessonUpdateRequest;
import com.teacher.agent.service.LessonCommandService;
import com.teacher.agent.service.LessonQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lessons")
@RequiredArgsConstructor
@Validated
public class LessonController {

  private final LessonQueryService lessonQueryService;
  private final LessonCommandService lessonCommandService;

  @PostMapping
  public ResponseEntity<LessonResponse> create(UserId userId,
      @RequestBody @Valid LessonCreateRequest request) {
    return ResponseEntity.status(201).body(lessonCommandService.create(userId, request));
  }

  @GetMapping
  public ResponseEntity<List<LessonResponse>> getAll(UserId userId) {
    return ResponseEntity.ok(lessonQueryService.getAllByTeacher(userId));
  }

  @GetMapping("/{id}")
  public ResponseEntity<LessonResponse> getOne(UserId userId, @PathVariable @Positive Long id) {
    return ResponseEntity.ok(lessonQueryService.getOne(userId, id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<LessonResponse> update(UserId userId, @PathVariable @Positive Long id,
      @RequestBody @Valid LessonUpdateRequest request) {
    return ResponseEntity.ok(lessonCommandService.update(userId, id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(UserId userId, @PathVariable @Positive Long id) {
    lessonCommandService.delete(userId, id);
    return ResponseEntity.noContent().build();
  }
}
