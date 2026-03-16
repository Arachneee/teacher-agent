package com.teacher.agent.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeedbackLikeTest {

    @Test
    void 피드백_좋아요를_생성한다() {
        Feedback feedback = Feedback.create(1L, 1L);
        FeedbackLike feedbackLike = FeedbackLike.create(feedback, "AI 피드백 내용", "[\"성실함\",\"리더십\"]");

        assertThat(feedbackLike.getFeedback()).isEqualTo(feedback);
        assertThat(feedbackLike.getAiContentSnapshot()).isEqualTo("AI 피드백 내용");
        assertThat(feedbackLike.getKeywordsSnapshot()).isEqualTo("[\"성실함\",\"리더십\"]");
    }

    @Test
    void feedback이_null이면_생성에_실패한다() {
        assertThatThrownBy(() -> FeedbackLike.create(null, "내용", "[]"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aiContentSnapshot이_null이면_생성에_실패한다() {
        Feedback feedback = Feedback.create(1L, 1L);

        assertThatThrownBy(() -> FeedbackLike.create(feedback, null, "[]"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aiContentSnapshot이_빈_문자열이면_생성에_실패한다() {
        Feedback feedback = Feedback.create(1L, 1L);

        assertThatThrownBy(() -> FeedbackLike.create(feedback, "", "[]"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void keywordsSnapshot이_null이면_생성에_실패한다() {
        Feedback feedback = Feedback.create(1L, 1L);

        assertThatThrownBy(() -> FeedbackLike.create(feedback, "내용", null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
