package com.teacher.agent.controller;

import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.FeedbackCreateRequest;
import com.teacher.agent.dto.FeedbackKeywordCreateRequest;
import com.teacher.agent.dto.FeedbackResponse;
import com.teacher.agent.dto.FeedbackUpdateRequest;
import com.teacher.agent.service.FeedbackService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/feedbacks")
@RequiredArgsConstructor
@Validated
public class FeedbackController {

  private final FeedbackService feedbackService;

  @PostMapping
  public ResponseEntity<FeedbackResponse> create(UserId userId,
      @RequestBody @Valid FeedbackCreateRequest request) {
    return ResponseEntity.status(201).body(feedbackService.create(userId, request));
  }

  @GetMapping
  public ResponseEntity<List<FeedbackResponse>> getAll(UserId userId,
      @RequestParam @Positive Long studentId) {
    return ResponseEntity.ok(feedbackService.getAll(userId, studentId));
  }

  @GetMapping("/{id}")
  public ResponseEntity<FeedbackResponse> getOne(UserId userId, @PathVariable @Positive Long id) {
    return ResponseEntity.ok(feedbackService.getOne(userId, id));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<FeedbackResponse> update(UserId userId, @PathVariable @Positive Long id,
      @RequestBody @Valid FeedbackUpdateRequest request) {
    return ResponseEntity.ok(feedbackService.update(userId, id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(UserId userId, @PathVariable @Positive Long id) {
    feedbackService.delete(userId, id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/generate")
  public ResponseEntity<FeedbackResponse> generateAiContent(UserId userId,
      @PathVariable @Positive Long id) {
    return ResponseEntity.ok(feedbackService.generateAiContent(userId, id));
  }

  @PostMapping("/{id}/keywords")
  public ResponseEntity<FeedbackResponse> addKeyword(UserId userId, @PathVariable @Positive Long id,
      @RequestBody @Valid FeedbackKeywordCreateRequest request) {
    return ResponseEntity.status(201).body(feedbackService.addKeyword(userId, id, request));
  }

  @DeleteMapping("/{id}/keywords/{keywordId}")
  public ResponseEntity<Void> removeKeyword(UserId userId, @PathVariable @Positive Long id,
      @PathVariable @Positive Long keywordId) {
    feedbackService.removeKeyword(userId, id, keywordId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/like")
  public ResponseEntity<FeedbackResponse> like(UserId userId, @PathVariable @Positive Long id) {
    return ResponseEntity.status(201).body(feedbackService.like(userId, id));
  }
}
