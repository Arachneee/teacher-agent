package com.teacher.agent.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class FeedbackKeywordTest {

  @Test
  void 피드백과_키워드로_생성한다() {
    Feedback feedback = Feedback.create(1L, 1L);

    FeedbackKeyword feedbackKeyword = FeedbackKeyword.create(feedback, "성실함");

    assertThat(feedbackKeyword.getFeedback()).isSameAs(feedback);
    assertThat(feedbackKeyword.getKeyword()).isEqualTo("성실함");
  }

  @Test
  void 피드백이_null이면_생성에_실패한다() {
    assertThatThrownBy(() -> FeedbackKeyword.create(null, "성실함"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 키워드가_null이면_생성에_실패한다() {
    Feedback feedback = Feedback.create(1L, 1L);

    assertThatThrownBy(() -> FeedbackKeyword.create(feedback, null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 키워드가_빈_문자열이면_생성에_실패한다() {
    Feedback feedback = Feedback.create(1L, 1L);

    assertThatThrownBy(() -> FeedbackKeyword.create(feedback, ""))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 키워드가_공백이면_생성에_실패한다() {
    Feedback feedback = Feedback.create(1L, 1L);

    assertThatThrownBy(() -> FeedbackKeyword.create(feedback, "   "))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 키워드가_100자를_초과하면_생성에_실패한다() {
    Feedback feedback = Feedback.create(1L, 1L);
    String longKeyword = "가".repeat(101);

    assertThatThrownBy(() -> FeedbackKeyword.create(feedback, longKeyword))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 키워드가_100자이면_생성에_성공한다() {
    Feedback feedback = Feedback.create(1L, 1L);
    String maxKeyword = "가".repeat(100);

    FeedbackKeyword feedbackKeyword = FeedbackKeyword.create(feedback, maxKeyword);

    assertThat(feedbackKeyword.getKeyword()).hasSize(100);
  }
}
