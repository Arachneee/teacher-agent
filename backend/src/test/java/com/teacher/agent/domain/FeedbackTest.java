package com.teacher.agent.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.List;
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
    feedback.addKeyword("성실함", false);

    // then
    assertThat(feedback.getKeywords()).hasSize(1);
    assertThat(feedback.getKeywords().get(0).getKeyword()).isEqualTo("성실함");
  }

  @Test
  void 여러_키워드를_추가한다() {
    // given
    Feedback feedback = Feedback.create(1L, 1L);

    // when
    feedback.addKeyword("성실함", false);
    feedback.addKeyword("리더십", false);
    feedback.addKeyword("협동심", false);

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

  @Test
  void 여러_학생의_피드백을_한번에_생성한다() {
    // given
    List<Long> studentIds = List.of(1L, 2L, 3L);

    // when
    List<Feedback> feedbacks = Feedback.createAll(studentIds, 10L);

    // then
    assertThat(feedbacks).hasSize(3);
    assertThat(feedbacks).allMatch(f -> f.getLessonId() == 10L);
    assertThat(feedbacks).extracting(Feedback::getStudentId)
        .containsExactly(1L, 2L, 3L);
  }

  @Test
  void 빈_학생_목록으로_createAll하면_빈_리스트를_반환한다() {
    // when
    List<Feedback> feedbacks = Feedback.createAll(Collections.emptyList(), 10L);

    // then
    assertThat(feedbacks).isEmpty();
  }

  @Test
  void 수정_지시를_추가하면_목록에_쌓인다() {
    // given
    Feedback feedback = Feedback.create(1L, 1L);

    // when
    feedback.addInstruction("더 따뜻하게");

    // then
    assertThat(feedback.getInstructions()).containsExactly("더 따뜻하게");
  }

  @Test
  void 빈_수정_지시는_추가되지_않는다() {
    // given
    Feedback feedback = Feedback.create(1L, 1L);

    // when
    feedback.addInstruction("  ");
    feedback.addInstruction(null);
    feedback.addInstruction("");

    // then
    assertThat(feedback.getInstructions()).isEmpty();
  }

  @Test
  void 여러_수정_지시가_순서대로_쌓인다() {
    // given
    Feedback feedback = Feedback.create(1L, 1L);

    // when
    feedback.addInstruction("더 따뜻하게");
    feedback.addInstruction("더 짧게");
    feedback.addInstruction("존댓말로");

    // then
    assertThat(feedback.getInstructions()).containsExactly("더 따뜻하게", "더 짧게", "존댓말로");
  }

  @Test
  void 수정_지시_목록을_새_목록으로_덮어쓴다() {
    // given
    Feedback feedback = Feedback.create(1L, 1L);
    feedback.addInstruction("더 따뜻하게");
    feedback.addInstruction("더 짧게");

    // when
    feedback.updateInstructions(List.of("더 따뜻하게", "존댓말로"));

    // then
    assertThat(feedback.getInstructions()).containsExactly("더 따뜻하게", "존댓말로");
  }

  @Test
  void 수정_지시_목록_덮어쓰기_시_빈_항목은_제외된다() {
    // given
    Feedback feedback = Feedback.create(1L, 1L);
    feedback.addInstruction("더 따뜻하게");

    // when
    feedback.updateInstructions(List.of("더 따뜻하게", "  ", "존댓말로"));

    // then
    assertThat(feedback.getInstructions()).containsExactly("더 따뜻하게", "존댓말로");
  }

  @Test
  void 수정_지시_목록을_빈_목록으로_초기화한다() {
    // given
    Feedback feedback = Feedback.create(1L, 1L);
    feedback.addInstruction("더 따뜻하게");

    // when
    feedback.updateInstructions(List.of());

    // then
    assertThat(feedback.getInstructions()).isEmpty();
  }
}
