package com.teacher.agent.service;

import com.teacher.agent.domain.Feedback;
import com.teacher.agent.domain.FeedbackRepository;
import com.teacher.agent.domain.StudentRepository;
import com.teacher.agent.dto.FeedbackCreateRequest;
import com.teacher.agent.dto.FeedbackKeywordCreateRequest;
import com.teacher.agent.dto.FeedbackResponse;
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

    @Transactional
    public FeedbackResponse create(FeedbackCreateRequest request) {
        findStudentById(request.studentId());
        Feedback feedback = feedbackRepository.findByStudentId(request.studentId())
                .orElseGet(() -> feedbackRepository.save(Feedback.create(request.studentId())));
        return FeedbackResponse.from(feedback);
    }

    public List<FeedbackResponse> getAll(Long studentId) {
        findStudentById(studentId);
        return feedbackRepository.findAllByStudentId(studentId).stream()
                .map(FeedbackResponse::from)
                .toList();
    }

    public FeedbackResponse getOne(Long id) {
        return FeedbackResponse.withKeywords(findById(id));
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
        return FeedbackResponse.withKeywords(feedback);
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

    private Feedback findById(Long id) {
        return feedbackRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feedback not found: " + id));
    }

    private void findStudentById(Long studentId) {
        studentRepository.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found: " + studentId));
    }
}
