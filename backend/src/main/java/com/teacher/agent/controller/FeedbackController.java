package com.teacher.agent.controller;

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
    public ResponseEntity<FeedbackResponse> create(@RequestBody @Valid FeedbackCreateRequest request) {
        return ResponseEntity.status(201).body(feedbackService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<FeedbackResponse>> getAll(@RequestParam @Positive Long studentId) {
        return ResponseEntity.ok(feedbackService.getAll(studentId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeedbackResponse> getOne(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(feedbackService.getOne(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FeedbackResponse> update(
            @PathVariable @Positive Long id,
            @RequestBody @Valid FeedbackUpdateRequest request) {
        return ResponseEntity.ok(feedbackService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long id) {
        feedbackService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/generate")
    public ResponseEntity<FeedbackResponse> generateAiContent(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(feedbackService.generateAiContent(id));
    }

    @PostMapping("/{id}/keywords")
    public ResponseEntity<FeedbackResponse> addKeyword(
            @PathVariable @Positive Long id,
            @RequestBody @Valid FeedbackKeywordCreateRequest request) {
        return ResponseEntity.status(201).body(feedbackService.addKeyword(id, request));
    }

    @DeleteMapping("/{id}/keywords/{keywordId}")
    public ResponseEntity<Void> removeKeyword(@PathVariable @Positive Long id, @PathVariable @Positive Long keywordId) {
        feedbackService.removeKeyword(id, keywordId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<FeedbackResponse> like(@PathVariable @Positive Long id) {
        return ResponseEntity.status(201).body(feedbackService.like(id));
    }
}
