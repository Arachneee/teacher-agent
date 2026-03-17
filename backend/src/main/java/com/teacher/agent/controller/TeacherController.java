package com.teacher.agent.controller;

import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.TeacherResponse;
import com.teacher.agent.dto.TeacherUpdateRequest;
import com.teacher.agent.service.TeacherCommandService;
import com.teacher.agent.service.TeacherQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/teachers")
@RequiredArgsConstructor
public class TeacherController {

  private final TeacherQueryService teacherQueryService;
  private final TeacherCommandService teacherCommandService;

  @GetMapping("/me")
  public ResponseEntity<TeacherResponse> getMe(UserId userId) {
    return ResponseEntity.ok(teacherQueryService.getByUserId(userId));
  }

  @PutMapping("/me")
  public ResponseEntity<TeacherResponse> updateMe(UserId userId,
      @RequestBody @Valid TeacherUpdateRequest request) {
    return ResponseEntity.ok(teacherCommandService.updateByUserId(userId, request));
  }
}
