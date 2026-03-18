package com.teacher.agent.service;

import static com.teacher.agent.util.RepositoryUtil.findByIdOrThrow;

import com.teacher.agent.domain.Feedback;
import com.teacher.agent.domain.FeedbackLike;
import com.teacher.agent.domain.FeedbackLikeRepository;
import com.teacher.agent.domain.FeedbackRepository;
import com.teacher.agent.domain.Lesson;
import com.teacher.agent.domain.Student;
import com.teacher.agent.domain.StudentRepository;
import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.FeedbackCreateRequest;
import com.teacher.agent.dto.FeedbackKeywordCreateRequest;
import com.teacher.agent.dto.FeedbackKeywordUpdateRequest;
import com.teacher.agent.dto.FeedbackResponse;
import com.teacher.agent.dto.FeedbackUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class FeedbackCommandService {

  private final FeedbackQueryService feedbackQueryService;
  private final LessonQueryService lessonQueryService;
  private final FeedbackAiService feedbackAiService;
  private final FeedbackRepository feedbackRepository;
  private final FeedbackLikeRepository feedbackLikeRepository;
  private final StudentRepository studentRepository;

  @Transactional
  public FeedbackResponse create(UserId userId, FeedbackCreateRequest request) {
    feedbackQueryService.findStudentByIdAndVerifyOwner(request.studentId(), userId);
    Lesson lesson = lessonQueryService.findByIdAndVerifyOwner(request.lessonId(), userId);
    verifyStudentEnrolled(lesson, request.studentId());
    return feedbackQueryService.toResponse(
        feedbackRepository.save(Feedback.create(request.studentId(), request.lessonId())));
  }

  @Transactional
  public FeedbackResponse update(UserId userId, Long feedbackId, FeedbackUpdateRequest request) {
    Feedback feedback = feedbackQueryService.findByIdAndVerifyOwner(feedbackId, userId);
    if (request.aiContent() == null || request.aiContent().isBlank()) {
      feedback.clearAiContent();
    } else {
      feedback.updateAiContent(request.aiContent());
    }
    return feedbackQueryService.toResponse(feedback);
  }

  @Transactional
  public void delete(UserId userId, Long feedbackId) {
    feedbackQueryService.findByIdAndVerifyOwner(feedbackId, userId);
    feedbackLikeRepository.deleteAllByFeedbackId(feedbackId);
    feedbackRepository.deleteById(feedbackId);
  }

  @Transactional
  public FeedbackResponse generateAiContent(UserId userId, Long feedbackId) {
    Feedback feedback = feedbackQueryService.findByIdAndVerifyOwner(feedbackId, userId);
    if (feedback.getKeywords().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "키워드가 없습니다. 먼저 키워드를 추가해주세요.");
    }
    Student student = findByIdOrThrow(studentRepository, feedback.getStudentId(),
        "Student not found: " + feedback.getStudentId());
    String aiContent = feedbackAiService.generateFeedbackContent(feedback, student.getName());
    feedback.updateAiContent(aiContent);
    return feedbackQueryService.toResponse(feedback);
  }

  @Transactional
  public FeedbackResponse addKeyword(UserId userId, Long feedbackId,
      FeedbackKeywordCreateRequest request) {
    Feedback feedback = feedbackQueryService.findByIdAndVerifyOwner(feedbackId, userId);
    feedback.addKeyword(request.keyword());
    feedbackRepository.flush();
    return feedbackQueryService.toResponse(feedback);
  }

  @Transactional
  public void removeKeyword(UserId userId, Long feedbackId, Long keywordId) {
    Feedback feedback = feedbackQueryService.findByIdAndVerifyOwner(feedbackId, userId);
    try {
      feedback.removeKeyword(keywordId);
    } catch (IllegalArgumentException exception) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
    }
  }

  @Transactional
  public FeedbackResponse updateKeyword(UserId userId, Long feedbackId, Long keywordId,
      FeedbackKeywordUpdateRequest request) {
    Feedback feedback = feedbackQueryService.findByIdAndVerifyOwner(feedbackId, userId);
    try {
      feedback.updateKeyword(keywordId, request.keyword());
    } catch (IllegalArgumentException exception) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
    }
    return feedbackQueryService.toResponse(feedback);
  }

  @Transactional
  public FeedbackResponse like(UserId userId, Long feedbackId) {
    Feedback feedback = feedbackQueryService.findByIdAndVerifyOwner(feedbackId, userId);
    try {
      feedback.like();
    } catch (IllegalStateException exception) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
    }
    feedbackLikeRepository.save(
        FeedbackLike.create(feedbackId, feedback.getAiContent(), feedback.buildKeywordsSnapshot()));
    return FeedbackResponse.withKeywords(feedback, true);
  }

  private void verifyStudentEnrolled(Lesson lesson, Long studentId) {
    boolean enrolled = lesson.getAttendees().stream()
        .anyMatch(attendee -> attendee.getStudentId().equals(studentId));
    if (!enrolled) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Student is not enrolled in this lesson");
    }
  }
}
