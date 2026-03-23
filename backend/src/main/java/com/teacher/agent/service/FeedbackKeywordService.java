package com.teacher.agent.service;

import com.teacher.agent.domain.Feedback;
import com.teacher.agent.domain.repository.FeedbackRepository;
import com.teacher.agent.domain.vo.UserId;
import com.teacher.agent.dto.FeedbackResponse;
import com.teacher.agent.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackKeywordService {

  private final FeedbackQueryService feedbackQueryService;
  private final FeedbackRepository feedbackRepository;

  @Transactional
  public FeedbackResponse addKeyword(UserId userId, Long feedbackId, String keyword,
      boolean required) {
    Feedback feedback = feedbackQueryService.findByIdAndVerifyOwner(feedbackId, userId);

    feedback.addKeyword(keyword, required);
    feedbackRepository.flush();

    return feedbackQueryService.toResponse(feedback);
  }

  @Transactional
  public void removeKeyword(UserId userId, Long feedbackId, Long keywordId) {
    Feedback feedback = feedbackQueryService.findByIdAndVerifyOwner(feedbackId, userId);

    try {
      feedback.removeKeyword(keywordId);
    } catch (IllegalArgumentException e) {
      log.warn("키워드 삭제 실패 - 존재하지 않음: feedbackId={}, keywordId={}", feedbackId, keywordId);
      throw ResourceNotFoundException.feedbackKeyword(keywordId);
    }
  }

  @Transactional
  public FeedbackResponse updateKeyword(UserId userId, Long feedbackId, Long keywordId,
      String keyword, boolean required) {
    Feedback feedback = feedbackQueryService.findByIdAndVerifyOwner(feedbackId, userId);

    try {
      feedback.updateKeyword(keywordId, keyword, required);
    } catch (IllegalArgumentException e) {
      log.warn("키워드 수정 실패 - 존재하지 않음: feedbackId={}, keywordId={}", feedbackId, keywordId);
      throw ResourceNotFoundException.feedbackKeyword(keywordId);
    }

    return feedbackQueryService.toResponse(feedback);
  }
}
