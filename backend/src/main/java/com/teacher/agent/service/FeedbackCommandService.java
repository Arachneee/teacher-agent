package com.teacher.agent.service;

import com.teacher.agent.domain.Feedback;
import com.teacher.agent.domain.Lesson;
import com.teacher.agent.domain.Student;
import com.teacher.agent.domain.repository.FeedbackRepository;
import com.teacher.agent.domain.repository.StudentRepository;
import com.teacher.agent.domain.vo.UserId;
import com.teacher.agent.dto.FeedbackResponse;
import com.teacher.agent.exception.BadRequestException;
import com.teacher.agent.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class FeedbackCommandService {

  private final FeedbackQueryService feedbackQueryService;
  private final LessonQueryService lessonQueryService;
  private final FeedbackAiService feedbackAiService;
  private final FeedbackLikeService feedbackLikeService;
  private final FeedbackRepository feedbackRepository;
  private final StudentRepository studentRepository;

  @Transactional
  public FeedbackResponse create(UserId userId, Long studentId, Long lessonId) {
    feedbackQueryService.findStudentByIdAndVerifyOwner(studentId, userId);
    Lesson lesson = lessonQueryService.findByIdAndVerifyOwner(lessonId, userId);
    verifyStudentEnrolled(lesson, studentId);

    return feedbackQueryService.toResponse(
        feedbackRepository.save(Feedback.create(studentId, lessonId)));
  }

  @Transactional
  public FeedbackResponse update(UserId userId, Long feedbackId, String aiContent) {
    Feedback feedback = feedbackQueryService.findByIdAndVerifyOwner(feedbackId, userId);

    if (aiContent == null || aiContent.isBlank()) {
      feedback.clearAiContent();
    } else {
      feedback.updateAiContent(aiContent);
    }

    return feedbackQueryService.toResponse(feedback);
  }

  @Transactional
  public void delete(UserId userId, Long feedbackId) {
    feedbackQueryService.findByIdAndVerifyOwner(feedbackId, userId);
    feedbackLikeService.deleteAllByFeedbackId(feedbackId);
    feedbackRepository.deleteById(feedbackId);
  }

  public FeedbackResponse generateAiContent(UserId userId, Long feedbackId) {
    Feedback feedback = feedbackQueryService.findByIdAndVerifyOwner(feedbackId, userId);

    if (feedback.getKeywords().isEmpty()) {
      throw BadRequestException.keywordRequired();
    }

    Student student = studentRepository.findById(feedback.getStudentId())
        .orElseThrow(() -> ResourceNotFoundException.student(feedback.getStudentId()));

    String aiContent = feedbackAiService.generateFeedbackContent(feedback, student.getName(),
        student.getGrade().displayName());

    feedback.updateAiContent(aiContent);
    feedbackRepository.save(feedback);

    return feedbackQueryService.toResponse(
        feedbackQueryService.findByIdAndVerifyOwner(feedbackId, userId));
  }

  public Flux<String> streamAiContent(UserId userId, Long feedbackId) {
    Feedback feedback = feedbackQueryService.findByIdAndVerifyOwner(feedbackId, userId);

    if (feedback.getKeywords().isEmpty()) {
      throw BadRequestException.keywordRequired();
    }

    Student student = studentRepository.findById(feedback.getStudentId())
        .orElseThrow(() -> ResourceNotFoundException.student(feedback.getStudentId()));

    StringBuilder fullContent = new StringBuilder();

    return feedbackAiService
        .streamFeedbackContent(feedback, student.getName(), student.getGrade().displayName())
        .doOnNext(fullContent::append)
        .doOnComplete(() -> {
          feedback.updateAiContent(fullContent.toString());
          feedbackRepository.save(feedback);
        });
  }

  private void verifyStudentEnrolled(Lesson lesson, Long studentId) {
    boolean enrolled = lesson.getAttendees().stream()
        .anyMatch(attendee -> attendee.getStudentId().equals(studentId));

    if (!enrolled) {
      throw BadRequestException.studentNotEnrolled();
    }
  }
}
