package com.teacher.agent.service;

import static com.teacher.agent.util.RepositoryUtil.findStudentByIdAndUserIdOrThrow;

import com.teacher.agent.domain.Feedback;
import com.teacher.agent.domain.Lesson;
import com.teacher.agent.domain.repository.FeedbackRepository;
import com.teacher.agent.domain.repository.LessonRepository;
import com.teacher.agent.domain.repository.StudentRepository;
import com.teacher.agent.domain.vo.UserId;
import com.teacher.agent.dto.FeedbackResponse;
import com.teacher.agent.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackQueryService {

  private final FeedbackRepository feedbackRepository;
  private final StudentRepository studentRepository;
  private final LessonRepository lessonRepository;

  public List<FeedbackResponse> getAll(UserId userId, Long studentId) {
    findStudentByIdAndVerifyOwner(studentId, userId);
    List<Feedback> feedbacks = feedbackRepository.findAllByStudentId(studentId);

    List<Long> lessonIds = feedbacks.stream()
        .map(Feedback::getLessonId)
        .distinct()
        .toList();
    Map<Long, Lesson> lessonMap = lessonRepository.findAllById(lessonIds).stream()
        .collect(Collectors.toMap(Lesson::getId, lesson -> lesson));

    return feedbacks.stream()
        .filter(feedback -> lessonMap.containsKey(feedback.getLessonId()))
        .filter(feedback -> feedback.getAiContent() != null)
        .map(feedback -> {
          Lesson lesson = lessonMap.get(feedback.getLessonId());
          return FeedbackResponse.withLesson(feedback, feedback.isLiked(),
              lesson.getTitle(), lesson.getStartTime());
        })
        .toList();
  }

  public FeedbackResponse getOne(UserId userId, Long feedbackId) {
    return toResponse(findByIdAndVerifyOwner(feedbackId, userId));
  }

  Feedback findByIdAndVerifyOwner(Long feedbackId, UserId userId) {
    Feedback feedback = feedbackRepository.findById(feedbackId)
        .orElseThrow(() -> ResourceNotFoundException.feedback(feedbackId));
    findStudentByIdAndVerifyOwner(feedback.getStudentId(), userId);
    return feedback;
  }

  void findStudentByIdAndVerifyOwner(Long studentId, UserId userId) {
    findStudentByIdAndUserIdOrThrow(studentRepository, studentId, userId);
  }

  FeedbackResponse toResponse(Feedback feedback) {
    return FeedbackResponse.withKeywords(feedback, feedback.isLiked());
  }
}
