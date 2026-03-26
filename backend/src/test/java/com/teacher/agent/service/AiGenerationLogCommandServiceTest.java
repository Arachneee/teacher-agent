package com.teacher.agent.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.teacher.agent.domain.AiGenerationLog;
import com.teacher.agent.domain.repository.AiGenerationLogRepository;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import(AiGenerationLogCommandService.class)
class AiGenerationLogCommandServiceTest {

  @Autowired
  private AiGenerationLogCommandService aiGenerationLogCommandService;

  @Autowired
  private AiGenerationLogRepository aiGenerationLogRepository;

  @AfterEach
  void tearDown() {
    aiGenerationLogRepository.deleteAllInBatch();
  }

  @Test
  void 동기_AI_생성_로그를_저장한다() {
    aiGenerationLogCommandService.save(1L, "프롬프트 내용", "응답 내용", 500L, false, 100, 200);

    List<AiGenerationLog> logs = aiGenerationLogRepository.findAll();
    assertThat(logs).hasSize(1);

    AiGenerationLog log = logs.get(0);
    assertThat(log.getFeedbackId()).isEqualTo(1L);
    assertThat(log.getPromptContent()).isEqualTo("프롬프트 내용");
    assertThat(log.getCompletionContent()).isEqualTo("응답 내용");
    assertThat(log.getDurationMs()).isEqualTo(500L);
    assertThat(log.isStreaming()).isFalse();
    assertThat(log.getPromptTokens()).isEqualTo(100);
    assertThat(log.getCompletionTokens()).isEqualTo(200);
    assertThat(log.getCreatedAt()).isNotNull();
  }

  @Test
  void 스트리밍_AI_생성_로그를_저장한다() {
    aiGenerationLogCommandService.save(2L, "프롬프트", "응답", 1200L, true, 80, 120);

    List<AiGenerationLog> logs = aiGenerationLogRepository.findAll();
    assertThat(logs).hasSize(1);

    AiGenerationLog log = logs.get(0);
    assertThat(log.isStreaming()).isTrue();
    assertThat(log.getPromptTokens()).isEqualTo(80);
    assertThat(log.getCompletionTokens()).isEqualTo(120);
  }

  @Test
  void 토큰_정보가_없는_로그를_저장한다() {
    aiGenerationLogCommandService.save(1L, "프롬프트", "응답", 800L, true, null, null);

    List<AiGenerationLog> logs = aiGenerationLogRepository.findAll();
    assertThat(logs).hasSize(1);

    AiGenerationLog log = logs.get(0);
    assertThat(log.getPromptTokens()).isNull();
    assertThat(log.getCompletionTokens()).isNull();
  }
}
