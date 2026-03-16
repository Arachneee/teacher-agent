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
    private final LessonRepository lessonRepository;
    private final TeacherRepository teacherRepository;
    private final FeedbackAiService feedbackAiService;

    @Transactional
    public FeedbackResponse create(UserId userId, FeedbackCreateRequest request) {
        findStudentByIdAndVerifyOwner(request.studentId(), userId);
        Lesson lesson = findLessonByIdAndVerifyOwner(request.lessonId(), userId);
        verifyStudentEnrolled(lesson, request.studentId());
        return toResponse(feedbackRepository.save(Feedback.create(request.studentId(), request.lessonId())));
    }

    public List<FeedbackResponse> getAll(UserId userId, Long studentId) {
        findStudentByIdAndVerifyOwner(studentId, userId);
        return feedbackRepository.findAllByStudentId(studentId).stream()
                .map(this::toResponse)
                .toList();
    }

    public FeedbackResponse getOne(UserId userId, Long feedbackId) {
        return toResponse(findByIdAndVerifyOwner(feedbackId, userId));
    }

    @Transactional
    public FeedbackResponse update(UserId userId, Long feedbackId, FeedbackUpdateRequest request) {
        Feedback feedback = findByIdAndVerifyOwner(feedbackId, userId);
        if (request.aiContent() == null || request.aiContent().isBlank()) {
            feedback.clearAiContent();
        } else {
            feedback.updateAiContent(request.aiContent());
        }
        return toResponse(feedback);
    }

    @Transactional
    public void delete(UserId userId, Long feedbackId) {
        findByIdAndVerifyOwner(feedbackId, userId);
        feedbackRepository.deleteById(feedbackId);
    }

    @Transactional
    public FeedbackResponse generateAiContent(UserId userId, Long feedbackId) {
        Feedback feedback = findByIdAndVerifyOwner(feedbackId, userId);
        if (feedback.getKeywords().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "키워드가 없습니다. 먼저 키워드를 추가해주세요.");
        }
        Student student = findStudentById(feedback.getStudentId());
        String aiContent = feedbackAiService.generateFeedbackContent(feedback, student.getName());
        feedback.updateAiContent(aiContent);
        return toResponse(feedback);
    }

    @Transactional
    public FeedbackResponse addKeyword(UserId userId, Long feedbackId, FeedbackKeywordCreateRequest request) {
        Feedback feedback = findByIdAndVerifyOwner(feedbackId, userId);
        feedback.addKeyword(request.keyword());
        feedbackRepository.flush();
        return toResponse(feedback);
    }

    @Transactional
    public void removeKeyword(UserId userId, Long feedbackId, Long keywordId) {
        Feedback feedback = findByIdAndVerifyOwner(feedbackId, userId);
        try {
            feedback.removeKeyword(keywordId);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        }
    }

    @Transactional
    public FeedbackResponse like(UserId userId, Long feedbackId) {
        Feedback feedback = findByIdAndVerifyOwner(feedbackId, userId);
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

    private Feedback findByIdAndVerifyOwner(Long feedbackId, UserId userId) {
        Feedback feedback = findById(feedbackId);
        findStudentByIdAndVerifyOwner(feedback.getStudentId(), userId);
        return feedback;
    }

    private void findStudentByIdAndVerifyOwner(Long studentId, UserId userId) {
        Teacher teacher = findTeacherByUserId(userId);
        studentRepository.findByIdAndTeacherId(studentId, teacher.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found: " + studentId));
    }

    private Lesson findLessonByIdAndVerifyOwner(Long lessonId, UserId userId) {
        Teacher teacher = findTeacherByUserId(userId);
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found: " + lessonId));
        if (!lesson.getTeacherId().equals(teacher.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Lesson does not belong to this teacher");
        }
        return lesson;
    }

    private void verifyStudentEnrolled(Lesson lesson, Long studentId) {
        boolean enrolled = lesson.getAttendees().stream()
                .anyMatch(attendee -> attendee.getStudentId().equals(studentId));
        if (!enrolled) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student is not enrolled in this lesson");
        }
    }

    private Feedback findById(Long feedbackId) {
        return feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feedback not found: " + feedbackId));
    }

    private Student findStudentById(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found: " + studentId));
    }

    private Teacher findTeacherByUserId(UserId userId) {
        return teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found: " + userId));
    }
}
