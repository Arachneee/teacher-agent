package com.teacher.agent.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class FeedbackKeywordTest {

  @Test
  void 피드백과_키워드로_생성한다() {
    // given
    Feedback feedback = Feedback.create(1L, 1L);

    // when
    FeedbackKeyword feedbackKeyword = FeedbackKeyword.create(feedback, "성실함", false);

    // then
    assertThat(feedbackKeyword.getFeedback()).isSameAs(feedback);
    assertThat(feedbackKeyword.getKeyword()).isEqualTo("성실함");
  }

  @Test
  void 피드백이_null이면_생성에_실패한다() {
    // when & then
    assertThatThrownBy(() -> FeedbackKeyword.create(null, "성실함", false))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 키워드가_null이면_생성에_실패한다() {
    // given
    Feedback feedback = Feedback.create(1L, 1L);

    // when & then
    assertThatThrownBy(() -> FeedbackKeyword.create(feedback, null, false))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 키워드가_빈_문자열이면_생성에_실패한다() {
    // given
    Feedback feedback = Feedback.create(1L, 1L);

    // when & then
    assertThatThrownBy(() -> FeedbackKeyword.create(feedback, "", false))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 키워드가_공백이면_생성에_실패한다() {
    // given
    Feedback feedback = Feedback.create(1L, 1L);

    // when & then
    assertThatThrownBy(() -> FeedbackKeyword.create(feedback, "   ", false))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 키워드가_100자를_초과하면_생성에_실패한다() {
    // given
    Feedback feedback = Feedback.create(1L, 1L);
    String longKeyword = "가".repeat(101);

    // when & then
    assertThatThrownBy(() -> FeedbackKeyword.create(feedback, longKeyword, false))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 키워드가_100자이면_생성에_성공한다() {
    // given
    Feedback feedback = Feedback.create(1L, 1L);
    String maxKeyword = "가".repeat(100);

    // when
    FeedbackKeyword feedbackKeyword = FeedbackKeyword.create(feedback, maxKeyword, false);

    // then
    assertThat(feedbackKeyword.getKeyword()).hasSize(100);
  }

  @Test
  void 키워드를_수정한다() {
    // given
    Feedback feedback = Feedback.create(1L, 1L);
    FeedbackKeyword feedbackKeyword = FeedbackKeyword.create(feedback, "성실함", false);

    // when
    feedbackKeyword.update("꼼꼼함", false);

    // then
    assertThat(feedbackKeyword.getKeyword()).isEqualTo("꼼꼼함");
  }

  @Test
  void 빈_문자열로_수정하면_실패한다() {
    // given
    Feedback feedback = Feedback.create(1L, 1L);
    FeedbackKeyword feedbackKeyword = FeedbackKeyword.create(feedback, "성실함", false);

    // when & then
    assertThatThrownBy(() -> feedbackKeyword.update("", false))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 최대_길이를_초과하는_값으로_수정하면_실패한다() {
    // given
    Feedback feedback = Feedback.create(1L, 1L);
    FeedbackKeyword feedbackKeyword = FeedbackKeyword.create(feedback, "성실함", false);
    String longKeyword = "가".repeat(101);

    // when & then
    assertThatThrownBy(() -> feedbackKeyword.update(longKeyword, false))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
