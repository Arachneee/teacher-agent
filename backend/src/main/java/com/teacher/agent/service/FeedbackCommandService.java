package com.teacher.agent.service;

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
import com.teacher.agent.exception.BadRequestException;
import com.teacher.agent.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

  public FeedbackResponse generateAiContent(UserId userId, Long feedbackId) {
    Feedback feedback = feedbackQueryService.findByIdAndVerifyOwner(feedbackId, userId);

    if (feedback.getKeywords().isEmpty()) {
      throw BadRequestException.keywordRequired();
    }

    Student student = studentRepository.findById(feedback.getStudentId())
        .orElseThrow(() -> ResourceNotFoundException.student(feedback.getStudentId()));

    String aiContent = feedbackAiService.generateFeedbackContent(feedback, student.getName());

    updateFeedbackAiContent(feedbackId, aiContent);

    return feedbackQueryService.toResponse(
        feedbackQueryService.findByIdAndVerifyOwner(feedbackId, userId));
  }

  @Transactional
  protected void updateFeedbackAiContent(Long feedbackId, String aiContent) {
    Feedback feedback = feedbackRepository.findById(feedbackId)
        .orElseThrow(() -> ResourceNotFoundException.feedback(feedbackId));
    feedback.updateAiContent(aiContent);
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
      throw ResourceNotFoundException.feedbackKeyword(keywordId);
    }
  }

  @Transactional
  public FeedbackResponse updateKeyword(UserId userId, Long feedbackId, Long keywordId,
      FeedbackKeywordUpdateRequest request) {
    Feedback feedback = feedbackQueryService.findByIdAndVerifyOwner(feedbackId, userId);

    try {
      feedback.updateKeyword(keywordId, request.keyword());
    } catch (IllegalArgumentException exception) {
      throw ResourceNotFoundException.feedbackKeyword(keywordId);
    }

    return feedbackQueryService.toResponse(feedback);
  }

  @Transactional
  public FeedbackResponse like(UserId userId, Long feedbackId) {
    Feedback feedback = feedbackQueryService.findByIdAndVerifyOwner(feedbackId, userId);

    try {
      feedback.like();
    } catch (IllegalStateException exception) {
      throw BadRequestException.feedbackLikeRequiresAiContent();
    }

    feedbackLikeRepository.save(
        FeedbackLike.create(feedbackId, feedback.getAiContent(), feedback.buildKeywordsSnapshot()));

    return FeedbackResponse.withKeywords(feedback, true);
  }

  private void verifyStudentEnrolled(Lesson lesson, Long studentId) {
    boolean enrolled = lesson.getAttendees().stream()
        .anyMatch(attendee -> attendee.getStudentId().equals(studentId));

    if (!enrolled) {
      throw BadRequestException.studentNotEnrolled();
    }
  }
}
