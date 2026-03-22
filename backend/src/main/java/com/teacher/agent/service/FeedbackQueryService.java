package com.teacher.agent.service;

import static com.teacher.agent.util.RepositoryUtil.findStudentByIdAndUserIdOrThrow;

import com.teacher.agent.domain.Feedback;
import com.teacher.agent.domain.repository.FeedbackRepository;
import com.teacher.agent.domain.repository.StudentRepository;
import com.teacher.agent.domain.vo.UserId;
import com.teacher.agent.dto.FeedbackResponse;
import com.teacher.agent.exception.ResourceNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackQueryService {

  private final FeedbackRepository feedbackRepository;
  private final StudentRepository studentRepository;

  public List<FeedbackResponse> getAll(UserId userId, Long studentId) {
    findStudentByIdAndVerifyOwner(studentId, userId);
    return feedbackRepository.findAllByStudentId(studentId).stream().map(this::toResponse).toList();
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
