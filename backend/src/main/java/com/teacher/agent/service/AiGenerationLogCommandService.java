package com.teacher.agent.service;

import com.teacher.agent.domain.AiGenerationLog;
import com.teacher.agent.domain.repository.AiGenerationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiGenerationLogCommandService {

  private final AiGenerationLogRepository aiGenerationLogRepository;

  @Transactional
  public void save(Long feedbackId, String promptContent, String completionContent,
      long durationMs, boolean streaming, Integer promptTokens, Integer completionTokens) {
    aiGenerationLogRepository.save(
        AiGenerationLog.create(feedbackId, promptContent, completionContent, durationMs,
            streaming, promptTokens, completionTokens));
  }
}
