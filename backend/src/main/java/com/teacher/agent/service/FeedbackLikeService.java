package com.teacher.agent.service;

import com.teacher.agent.domain.Feedback;
import com.teacher.agent.domain.FeedbackLike;
import com.teacher.agent.domain.repository.FeedbackLikeRepository;
import com.teacher.agent.domain.vo.UserId;
import com.teacher.agent.dto.FeedbackResponse;
import com.teacher.agent.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackLikeService {

  private final FeedbackQueryService feedbackQueryService;
  private final FeedbackLikeRepository feedbackLikeRepository;

  @Transactional
  public FeedbackResponse like(UserId userId, Long feedbackId) {
    Feedback feedback = feedbackQueryService.findByIdAndVerifyOwner(feedbackId, userId);

    try {
      feedback.like();
    } catch (IllegalStateException e) {
      log.warn("좋아요 실패 - AI 콘텐츠 없음 또는 이미 좋아요: feedbackId={}", feedbackId);
      throw BadRequestException.feedbackLikeRequiresAiContent();
    }

    feedbackLikeRepository.save(
        FeedbackLike.create(feedbackId, feedback.getAiContent(), feedback.buildKeywordsSnapshot()));

    return FeedbackResponse.withKeywords(feedback, true);
  }

  public void deleteAllByFeedbackId(Long feedbackId) {
    feedbackLikeRepository.deleteAllByFeedbackId(feedbackId);
  }
}
