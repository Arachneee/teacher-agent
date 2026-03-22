package com.teacher.agent.controller;

import com.teacher.agent.domain.vo.UserId;
import com.teacher.agent.dto.StudentCreateRequest;
import com.teacher.agent.dto.StudentResponse;
import com.teacher.agent.dto.StudentUpdateRequest;
import com.teacher.agent.service.StudentCommandService;
import com.teacher.agent.service.StudentQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
@Validated
public class StudentController {

  private final StudentQueryService studentQueryService;
  private final StudentCommandService studentCommandService;

  @PostMapping
  public ResponseEntity<StudentResponse> create(UserId userId,
      @RequestBody @Valid StudentCreateRequest request) {
    return ResponseEntity
        .ok(studentCommandService.create(userId, request.name(), request.memo(), request.grade()));
  }

  @GetMapping
  public ResponseEntity<List<StudentResponse>> getAll(UserId userId) {
    return ResponseEntity.ok(studentQueryService.getAll(userId));
  }

  @GetMapping("/{id}")
  public ResponseEntity<StudentResponse> getOne(UserId userId, @PathVariable @Positive Long id) {
    return ResponseEntity.ok(studentQueryService.getOne(userId, id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<StudentResponse> update(UserId userId, @PathVariable @Positive Long id,
      @RequestBody @Valid StudentUpdateRequest request) {
    return ResponseEntity
        .ok(studentCommandService.update(userId, id, request.name(), request.memo(),
            request.grade()));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(UserId userId, @PathVariable @Positive Long id) {
    studentCommandService.delete(userId, id);
    return ResponseEntity.noContent().build();
  }
}
