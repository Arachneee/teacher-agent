package com.teacher.agent.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class FeedbackTest {

  @Test
  void 학생_ID와_레슨_ID로_피드백을_생성한다() {
    // when
    Feedback feedback = Feedback.create(1L, 1L);

    // then
    assertThat(feedback.getStudentId()).isEqualTo(1L);
    assertThat(feedback.getLessonId()).isEqualTo(1L);
    assertThat(feedback.getAiContent()).isNull();
    assertThat(feedback.getKeywords()).isEmpty();
  }

  @Test
  void 학생_ID가_0이면_생성에_실패한다() {
    // when & then
    assertThatThrownBy(() -> Feedback.create(0L, 1L))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 학생_ID가_음수이면_생성에_실패한다() {
    // when & then
    assertThatThrownBy(() -> Feedback.create(-1L, 1L))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 키워드를_추가한다() {
    // given
    Feedback feedback = Feedback.create(1L, 1L);

    // when
    feedback.addKeyword("성실함");

    // then
    assertThat(feedback.getKeywords()).hasSize(1);
    assertThat(feedback.getKeywords().get(0).getKeyword()).isEqualTo("성실함");
  }

  @Test
  void 여러_키워드를_추가한다() {
    // given
    Feedback feedback = Feedback.create(1L, 1L);

    // when
    feedback.addKeyword("성실함");
    feedback.addKeyword("리더십");
    feedback.addKeyword("협동심");

    // then
    assertThat(feedback.getKeywords()).hasSize(3);
  }

  @Test
  void AI_콘텐츠를_업데이트한다() {
    // given
    Feedback feedback = Feedback.create(1L, 1L);

    // when
    feedback.updateAiContent("AI가 생성한 피드백 내용입니다.");

    // then
    assertThat(feedback.getAiContent()).isEqualTo("AI가 생성한 피드백 내용입니다.");
  }

  @Test
  void AI_콘텐츠가_null이면_업데이트에_실패한다() {
    // given
    Feedback feedback = Feedback.create(1L, 1L);

    // when & then
    assertThatThrownBy(() -> feedback.updateAiContent(null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void AI_콘텐츠가_빈_문자열이면_업데이트에_실패한다() {
    // given
    Feedback feedback = Feedback.create(1L, 1L);

    // when & then
    assertThatThrownBy(() -> feedback.updateAiContent(""))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void AI_콘텐츠를_초기화한다() {
    // given
    Feedback feedback = Feedback.create(1L, 1L);
    feedback.updateAiContent("AI 피드백");

    // when
    feedback.clearAiContent();

    // then
    assertThat(feedback.getAiContent()).isNull();
  }

  @Test
  void 존재하지_않는_키워드_삭제_시_예외가_발생한다() {
    // given
    Feedback feedback = Feedback.create(1L, 1L);

    // when & then
    assertThatThrownBy(() -> feedback.removeKeyword(999L))
        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("999");
  }

  @Test
  void 좋아요를_추가한다() {
    // given
    Feedback feedback = Feedback.create(1L, 1L);
    feedback.updateAiContent("AI 피드백");

    // when
    feedback.like();

    // then
    assertThat(feedback.isLiked()).isTrue();
  }

  @Test
  void AI_콘텐츠가_없으면_좋아요에_실패한다() {
    // given
    Feedback feedback = Feedback.create(1L, 1L);

    // when & then
    assertThatThrownBy(() -> feedback.like())
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void 같은_내용에_좋아요를_중복으로_누르면_실패한다() {
    // given
    Feedback feedback = Feedback.create(1L, 1L);
    feedback.updateAiContent("AI 피드백");
    feedback.like();

    // when & then
    assertThatThrownBy(() -> feedback.like())
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void 내용_수정_후_다시_좋아요할_수_있다() {
    // given
    Feedback feedback = Feedback.create(1L, 1L);
    feedback.updateAiContent("버전 1");
    feedback.like();

    // when
    feedback.updateAiContent("버전 2");

    // then
    assertThat(feedback.isLiked()).isFalse();
    feedback.like();
    assertThat(feedback.isLiked()).isTrue();
  }

  @Test
  void 좋아요가_없으면_isLiked는_false를_반환한다() {
    // given
    Feedback feedback = Feedback.create(1L, 1L);
    feedback.updateAiContent("AI 피드백");

    // then
    assertThat(feedback.isLiked()).isFalse();
  }
}
