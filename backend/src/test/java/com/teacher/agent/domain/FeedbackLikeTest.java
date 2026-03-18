package com.teacher.agent.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class FeedbackLikeTest {

  @Test
  void 피드백_좋아요를_생성한다() {
    FeedbackLike feedbackLike = FeedbackLike.create(1L, "AI 피드백 내용", "[\"성실함\",\"리더십\"]");

    assertThat(feedbackLike.getFeedbackId()).isEqualTo(1L);
    assertThat(feedbackLike.getAiContentSnapshot()).isEqualTo("AI 피드백 내용");
    assertThat(feedbackLike.getKeywordsSnapshot()).isEqualTo("[\"성실함\",\"리더십\"]");
  }

  @Test
  void feedbackId가_null이면_생성에_실패한다() {
    assertThatThrownBy(() -> FeedbackLike.create(null, "내용", "[]"))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void feedbackId가_0이면_생성에_실패한다() {
    assertThatThrownBy(() -> FeedbackLike.create(0L, "내용", "[]"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void aiContentSnapshot이_null이면_생성에_실패한다() {
    assertThatThrownBy(() -> FeedbackLike.create(1L, null, "[]"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void aiContentSnapshot이_빈_문자열이면_생성에_실패한다() {
    assertThatThrownBy(() -> FeedbackLike.create(1L, "", "[]"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void keywordsSnapshot이_null이면_생성에_실패한다() {
    assertThatThrownBy(() -> FeedbackLike.create(1L, "내용", null))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
