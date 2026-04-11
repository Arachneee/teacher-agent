package com.teacher.agent.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class AiGenerationLogTest {

  @Test
  void 동기_AI_생성_로그를_생성한다() {
    AiGenerationLog log =
        AiGenerationLog.create(1L, "프롬프트 내용", "응답 내용", 500L, false, 100, 200, null);

    assertThat(log.getFeedbackId()).isEqualTo(1L);
    assertThat(log.getPromptContent()).isEqualTo("프롬프트 내용");
    assertThat(log.getCompletionContent()).isEqualTo("응답 내용");
    assertThat(log.getDurationMs()).isEqualTo(500L);
    assertThat(log.isStreaming()).isFalse();
    assertThat(log.getPromptTokens()).isEqualTo(100);
    assertThat(log.getCompletionTokens()).isEqualTo(200);
  }

  @Test
  void 스트리밍_AI_생성_로그를_생성한다() {
    AiGenerationLog log =
        AiGenerationLog.create(1L, "프롬프트 내용", "응답 내용", 1000L, true, 50, 150, null);

    assertThat(log.isStreaming()).isTrue();
    assertThat(log.getPromptTokens()).isEqualTo(50);
    assertThat(log.getCompletionTokens()).isEqualTo(150);
  }

  @Test
  void 토큰_정보가_없는_로그를_생성한다() {
    AiGenerationLog log =
        AiGenerationLog.create(1L, "프롬프트 내용", "응답 내용", 1000L, true, null, null, null);

    assertThat(log.getPromptTokens()).isNull();
    assertThat(log.getCompletionTokens()).isNull();
  }

  @Test
  void feedbackId가_0이면_생성에_실패한다() {
    assertThatThrownBy(() -> AiGenerationLog.create(0L, "프롬프트", "응답", 500L, false, 100, 200, null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void feedbackId가_음수이면_생성에_실패한다() {
    assertThatThrownBy(() -> AiGenerationLog.create(-1L, "프롬프트", "응답", 500L, false, 100, 200, null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void promptContent가_blank이면_생성에_실패한다() {
    assertThatThrownBy(() -> AiGenerationLog.create(1L, "  ", "응답", 500L, false, 100, 200, null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void completionContent가_blank이면_생성에_실패한다() {
    assertThatThrownBy(() -> AiGenerationLog.create(1L, "프롬프트", "  ", 500L, false, 100, 200, null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void durationMs가_음수이면_생성에_실패한다() {
    assertThatThrownBy(() -> AiGenerationLog.create(1L, "프롬프트", "응답", -1L, false, 100, 200, null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void instruction이_있으면_저장된다() {
    AiGenerationLog log =
        AiGenerationLog.create(1L, "프롬프트", "응답", 500L, false, 100, 200, "더 짧게 써줘");

    assertThat(log.getInstruction()).isEqualTo("더 짧게 써줘");
  }

  @Test
  void instruction이_null이면_null로_저장된다() {
    AiGenerationLog log = AiGenerationLog.create(1L, "프롬프트", "응답", 500L, false, 100, 200, null);

    assertThat(log.getInstruction()).isNull();
  }

  @Test
  void instruction이_없음이면_null로_저장된다() {
    AiGenerationLog log = AiGenerationLog.create(1L, "프롬프트", "응답", 500L, false, 100, 200, "없음");

    assertThat(log.getInstruction()).isNull();
  }

  @Test
  void instruction이_빈_문자열이면_null로_저장된다() {
    AiGenerationLog log = AiGenerationLog.create(1L, "프롬프트", "응답", 500L, false, 100, 200, "  ");

    assertThat(log.getInstruction()).isNull();
  }

  @Test
  void instruction_앞뒤_공백은_제거되어_저장된다() {
    AiGenerationLog log =
        AiGenerationLog.create(1L, "프롬프트", "응답", 500L, false, 100, 200, "  더 짧게 써줘  ");

    assertThat(log.getInstruction()).isEqualTo("더 짧게 써줘");
  }
}
