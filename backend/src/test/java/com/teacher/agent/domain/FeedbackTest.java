package com.teacher.agent.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeedbackTest {

    @Test
    void 학생_ID로_피드백을_생성한다() {
        Feedback feedback = Feedback.create(1L);

        assertThat(feedback.getStudentId()).isEqualTo(1L);
        assertThat(feedback.getAiContent()).isNull();
        assertThat(feedback.getKeywords()).isEmpty();
    }

    @Test
    void 학생_ID가_0이면_생성에_실패한다() {
        assertThatThrownBy(() -> Feedback.create(0L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 학생_ID가_음수이면_생성에_실패한다() {
        assertThatThrownBy(() -> Feedback.create(-1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 키워드를_추가한다() {
        Feedback feedback = Feedback.create(1L);

        feedback.addKeyword("성실함");

        assertThat(feedback.getKeywords()).hasSize(1);
        assertThat(feedback.getKeywords().get(0).getKeyword()).isEqualTo("성실함");
    }

    @Test
    void 여러_키워드를_추가한다() {
        Feedback feedback = Feedback.create(1L);

        feedback.addKeyword("성실함");
        feedback.addKeyword("리더십");
        feedback.addKeyword("협동심");

        assertThat(feedback.getKeywords()).hasSize(3);
    }

    @Test
    void AI_콘텐츠를_업데이트한다() {
        Feedback feedback = Feedback.create(1L);

        feedback.updateAiContent("AI가 생성한 피드백 내용입니다.");

        assertThat(feedback.getAiContent()).isEqualTo("AI가 생성한 피드백 내용입니다.");
    }

    @Test
    void AI_콘텐츠가_null이면_업데이트에_실패한다() {
        Feedback feedback = Feedback.create(1L);

        assertThatThrownBy(() -> feedback.updateAiContent(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void AI_콘텐츠가_빈_문자열이면_업데이트에_실패한다() {
        Feedback feedback = Feedback.create(1L);

        assertThatThrownBy(() -> feedback.updateAiContent(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void AI_콘텐츠를_초기화한다() {
        Feedback feedback = Feedback.create(1L);
        feedback.updateAiContent("AI 피드백");

        feedback.clearAiContent();

        assertThat(feedback.getAiContent()).isNull();
    }

    @Test
    void 존재하지_않는_키워드_삭제_시_예외가_발생한다() {
        Feedback feedback = Feedback.create(1L);

        assertThatThrownBy(() -> feedback.removeKeyword(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("999");
    }
}
