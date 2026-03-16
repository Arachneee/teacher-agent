package com.teacher.agent.service;

import com.teacher.agent.domain.*;
import com.teacher.agent.dto.FeedbackCreateRequest;
import com.teacher.agent.dto.FeedbackKeywordCreateRequest;
import com.teacher.agent.dto.FeedbackResponse;
import com.teacher.agent.dto.FeedbackUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final StudentRepository studentRepository;
    private final FeedbackAiService feedbackAiService;

    @Transactional
    public FeedbackResponse create(FeedbackCreateRequest request) {
        findStudentById(request.studentId());
        Feedback feedback = feedbackRepository.findByStudentId(request.studentId())
                .orElseGet(() -> feedbackRepository.save(Feedback.create(request.studentId())));
        return toResponse(feedback);
    }

    public List<FeedbackResponse> getAll(Long studentId) {
        findStudentById(studentId);
        return feedbackRepository.findAllByStudentId(studentId).stream()
                .map(this::toResponse)
                .toList();
    }

    public FeedbackResponse getOne(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public FeedbackResponse update(Long feedbackId, FeedbackUpdateRequest request) {
        Feedback feedback = findById(feedbackId);
        if (request.aiContent() == null || request.aiContent().isBlank()) {
            feedback.clearAiContent();
        } else {
            feedback.updateAiContent(request.aiContent());
        }
        return toResponse(feedback);
    }

    @Transactional
    public void delete(Long id) {
        findById(id);
        feedbackRepository.deleteById(id);
    }

    @Transactional
    public FeedbackResponse addKeyword(Long feedbackId, FeedbackKeywordCreateRequest request) {
        Feedback feedback = findById(feedbackId);
        feedback.addKeyword(request.keyword());
        feedbackRepository.flush();
        return toResponse(feedback);
    }

    @Transactional
    public void removeKeyword(Long feedbackId, Long keywordId) {
        Feedback feedback = findById(feedbackId);
        try {
            feedback.removeKeyword(keywordId);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        }
    }

    @Transactional
    public FeedbackResponse generateAiContent(Long feedbackId) {
        Feedback feedback = findById(feedbackId);
        if (feedback.getKeywords().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "키워드가 없습니다. 먼저 키워드를 추가해주세요.");
        }
        Student student = findStudentById(feedback.getStudentId());
        String aiContent = feedbackAiService.generateFeedbackContent(feedback, student.getName());
        feedback.updateAiContent(aiContent);
        return toResponse(feedback);
    }

    @Transactional
    public FeedbackResponse like(Long feedbackId) {
        Feedback feedback = findById(feedbackId);
        try {
            feedback.like();
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
        feedbackRepository.flush();
        return FeedbackResponse.withKeywords(feedback, true);
    }

    private FeedbackResponse toResponse(Feedback feedback) {
        return FeedbackResponse.withKeywords(feedback, feedback.isLiked());
    }

    private Feedback findById(Long id) {
        return feedbackRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feedback not found: " + id));
    }

    private Student findStudentById(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found: " + studentId));
    }
}
