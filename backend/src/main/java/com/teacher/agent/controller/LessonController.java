package com.teacher.agent.controller;

import com.teacher.agent.domain.UpdateScope;
import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.LessonCreateRequest;
import com.teacher.agent.dto.LessonDetailResponse;
import com.teacher.agent.dto.LessonResponse;
import com.teacher.agent.dto.LessonUpdateRequest;
import com.teacher.agent.service.LessonCommandService;
import com.teacher.agent.service.LessonDetailQueryService;
import com.teacher.agent.service.LessonQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
  private final LessonDetailQueryService lessonDetailQueryService;

  @PostMapping
  public ResponseEntity<LessonResponse> create(UserId userId,
      @RequestBody @Valid LessonCreateRequest request) {
    return ResponseEntity.status(201).body(lessonCommandService.create(userId, request));
  }

  @GetMapping
  public ResponseEntity<List<LessonResponse>> getAll(UserId userId,
      @RequestParam(required = true)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
    return ResponseEntity.ok(lessonQueryService.getByTeacherAndWeek(userId, weekStart));
  }

  @GetMapping("/{id}")
  public ResponseEntity<LessonResponse> getOne(UserId userId, @PathVariable @Positive Long id) {
    return ResponseEntity.ok(lessonQueryService.getOne(userId, id));
  }

  @GetMapping("/{id}/detail")
  public ResponseEntity<LessonDetailResponse> getDetail(UserId userId,
      @PathVariable @Positive Long id) {
    return ResponseEntity.ok(lessonDetailQueryService.getDetail(userId, id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<LessonResponse> update(UserId userId, @PathVariable @Positive Long id,
      @RequestBody @Valid LessonUpdateRequest request) {
    return ResponseEntity.ok(lessonCommandService.update(userId, id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(UserId userId, @PathVariable @Positive Long id,
      @RequestParam(defaultValue = "SINGLE") UpdateScope scope) {
    lessonCommandService.delete(userId, id, scope);
    return ResponseEntity.noContent().build();
  }
}
