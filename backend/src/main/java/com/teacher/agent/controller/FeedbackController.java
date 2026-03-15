package com.teacher.agent.controller;

import com.teacher.agent.dto.FeedbackCreateRequest;
import com.teacher.agent.dto.FeedbackKeywordCreateRequest;
import com.teacher.agent.dto.FeedbackResponse;
import com.teacher.agent.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<FeedbackResponse> create(@RequestBody @Valid FeedbackCreateRequest request) {
        return ResponseEntity.status(201).body(feedbackService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<FeedbackResponse>> getAll(@RequestParam Long studentId) {
        return ResponseEntity.ok(feedbackService.getAll(studentId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeedbackResponse> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(feedbackService.getOne(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        feedbackService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/keywords")
    public ResponseEntity<FeedbackResponse> addKeyword(
            @PathVariable Long id,
            @RequestBody @Valid FeedbackKeywordCreateRequest request) {
        return ResponseEntity.status(201).body(feedbackService.addKeyword(id, request));
    }

    @DeleteMapping("/{id}/keywords/{keywordId}")
    public ResponseEntity<Void> removeKeyword(@PathVariable Long id, @PathVariable Long keywordId) {
        feedbackService.removeKeyword(id, keywordId);
        return ResponseEntity.noContent().build();
    }
}
