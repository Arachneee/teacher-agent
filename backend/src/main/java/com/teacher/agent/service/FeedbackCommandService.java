package com.teacher.agent.service;

import com.teacher.agent.domain.Feedback;
import com.teacher.agent.domain.FeedbackLike;
import com.teacher.agent.domain.Lesson;
import com.teacher.agent.domain.Student;
import com.teacher.agent.domain.Teacher;
import com.teacher.agent.domain.repository.FeedbackLikeRepository;
import com.teacher.agent.domain.repository.FeedbackRepository;
import com.teacher.agent.domain.repository.StudentRepository;
import com.teacher.agent.domain.repository.TeacherRepository;
import com.teacher.agent.domain.vo.UserId;
import com.teacher.agent.dto.FeedbackResponse;
import com.teacher.agent.exception.BadRequestException;
import com.teacher.agent.exception.ResourceNotFoundException;
import java.util.ArrayList;
import java.util.List;
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
  private final TeacherRepository teacherRepository;
  private final FeedbackLikeRepository feedbackLikeRepository;

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

  public FeedbackResponse generateAiContent(UserId userId, Long feedbackId, String instruction) {
    Feedback feedback = feedbackQueryService.findByIdAndVerifyOwner(feedbackId, userId);

    if (feedback.getKeywords().isEmpty()) {
      throw BadRequestException.keywordRequired();
    }

    Student student = studentRepository.findById(feedback.getStudentId())
        .orElseThrow(() -> ResourceNotFoundException.student(feedback.getStudentId()));
    lessonQueryService.findByIdAndVerifyOwner(feedback.getLessonId(), userId);
    String subject = resolveSubject(userId);
    List<FeedbackLike> likedExamples = feedbackLikeRepository.findRecentLikedByUserId(userId);

    List<String> allInstructions = buildAllInstructions(feedback.getInstructions(), instruction);

    String aiContent =
        feedbackAiService.generateFeedbackContent(feedback, student, subject, likedExamples,
            allInstructions);

    feedback.updateAiContent(aiContent);
    feedback.addInstruction(instruction);
    feedbackRepository.save(feedback);

    return feedbackQueryService.toResponse(
        feedbackQueryService.findByIdAndVerifyOwner(feedbackId, userId));
  }

  public Flux<String> streamAiContent(UserId userId, Long feedbackId, String instruction) {
    Feedback feedback = feedbackQueryService.findByIdAndVerifyOwner(feedbackId, userId);

    if (feedback.getKeywords().isEmpty()) {
      throw BadRequestException.keywordRequired();
    }

    Student student = studentRepository.findById(feedback.getStudentId())
        .orElseThrow(() -> ResourceNotFoundException.student(feedback.getStudentId()));
    lessonQueryService.findByIdAndVerifyOwner(feedback.getLessonId(), userId);
    String subject = resolveSubject(userId);
    List<FeedbackLike> likedExamples = feedbackLikeRepository.findRecentLikedByUserId(userId);

    List<String> allInstructions = buildAllInstructions(feedback.getInstructions(), instruction);
    StringBuilder fullContent = new StringBuilder();

    return feedbackAiService
        .streamFeedbackContent(feedback, student, subject, likedExamples, allInstructions)
        .doOnNext(fullContent::append)
        .doOnComplete(() -> {
          feedback.updateAiContent(fullContent.toString());
          feedback.addInstruction(instruction);
          feedbackRepository.save(feedback);
        });
  }

  @Transactional
  public FeedbackResponse updateInstructions(UserId userId, Long feedbackId,
      List<String> instructions) {
    Feedback feedback = feedbackQueryService.findByIdAndVerifyOwner(feedbackId, userId);
    feedback.updateInstructions(instructions);
    feedbackRepository.save(feedback);
    return feedbackQueryService.toResponse(feedback);
  }

  private void verifyStudentEnrolled(Lesson lesson, Long studentId) {
    boolean enrolled = lesson.getAttendees().stream()
        .anyMatch(attendee -> attendee.getStudentId().equals(studentId));

    if (!enrolled) {
      throw BadRequestException.studentNotEnrolled();
    }
  }

  private List<String> buildAllInstructions(List<String> pastInstructions,
      String currentInstruction) {
    List<String> allInstructions = new ArrayList<>(pastInstructions);
    if (currentInstruction != null && !currentInstruction.isBlank()) {
      allInstructions.add(currentInstruction.strip());
    }
    return allInstructions;
  }

  private String resolveSubject(UserId userId) {
    return teacherRepository.findByUserId(userId)
        .map(Teacher::getSubject)
        .orElse(null);
  }
}
