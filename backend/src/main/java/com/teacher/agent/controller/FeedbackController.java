package com.teacher.agent.controller;

import com.teacher.agent.domain.vo.UserId;
import com.teacher.agent.dto.FeedbackCreateRequest;
import com.teacher.agent.dto.FeedbackKeywordCreateRequest;
import com.teacher.agent.dto.FeedbackKeywordUpdateRequest;
import com.teacher.agent.dto.FeedbackResponse;
import com.teacher.agent.dto.FeedbackUpdateRequest;
import com.teacher.agent.service.FeedbackCommandService;
import com.teacher.agent.service.FeedbackKeywordService;
import com.teacher.agent.service.FeedbackLikeService;
import com.teacher.agent.service.FeedbackQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/feedbacks")
@RequiredArgsConstructor
@Validated
public class FeedbackController {

  private final FeedbackQueryService feedbackQueryService;
  private final FeedbackCommandService feedbackCommandService;
  private final FeedbackKeywordService feedbackKeywordService;
  private final FeedbackLikeService feedbackLikeService;

  @PostMapping
  public ResponseEntity<FeedbackResponse> create(UserId userId,
      @RequestBody @Valid FeedbackCreateRequest request) {
    return ResponseEntity
        .ok(feedbackCommandService.create(userId, request.studentId(), request.lessonId()));
  }

  @GetMapping
  public ResponseEntity<List<FeedbackResponse>> getAll(UserId userId,
      @RequestParam @Positive Long studentId) {
    return ResponseEntity.ok(feedbackQueryService.getAll(userId, studentId));
  }

  @GetMapping("/{id}")
  public ResponseEntity<FeedbackResponse> getOne(UserId userId, @PathVariable @Positive Long id) {
    return ResponseEntity.ok(feedbackQueryService.getOne(userId, id));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<FeedbackResponse> update(UserId userId, @PathVariable @Positive Long id,
      @RequestBody @Valid FeedbackUpdateRequest request) {
    return ResponseEntity.ok(feedbackCommandService.update(userId, id, request.aiContent()));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(UserId userId, @PathVariable @Positive Long id) {
    feedbackCommandService.delete(userId, id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/generate")
  public ResponseEntity<FeedbackResponse> generateAiContent(UserId userId,
      @PathVariable @Positive Long id) {
    return ResponseEntity.ok(feedbackCommandService.generateAiContent(userId, id));
  }

  @PostMapping("/{id}/keywords")
  public ResponseEntity<FeedbackResponse> addKeyword(UserId userId, @PathVariable @Positive Long id,
      @RequestBody @Valid FeedbackKeywordCreateRequest request) {
    return ResponseEntity.ok(feedbackKeywordService.addKeyword(userId, id, request.keyword()));
  }

  @PutMapping("/{id}/keywords/{keywordId}")
  public ResponseEntity<FeedbackResponse> updateKeyword(UserId userId,
      @PathVariable @Positive Long id, @PathVariable @Positive Long keywordId,
      @RequestBody @Valid FeedbackKeywordUpdateRequest request) {
    return ResponseEntity
        .ok(feedbackKeywordService.updateKeyword(userId, id, keywordId, request.keyword()));
  }

  @DeleteMapping("/{id}/keywords/{keywordId}")
  public ResponseEntity<Void> removeKeyword(UserId userId, @PathVariable @Positive Long id,
      @PathVariable @Positive Long keywordId) {
    feedbackKeywordService.removeKeyword(userId, id, keywordId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/like")
  public ResponseEntity<FeedbackResponse> like(UserId userId, @PathVariable @Positive Long id) {
    return ResponseEntity.ok(feedbackLikeService.like(userId, id));
  }
}
