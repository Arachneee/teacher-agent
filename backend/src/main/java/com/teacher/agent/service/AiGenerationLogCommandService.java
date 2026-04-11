package com.teacher.agent.service;

import com.teacher.agent.domain.AiGenerationLog;
import com.teacher.agent.domain.repository.AiGenerationLogRepository;
import com.teacher.agent.service.vo.AiGenerationLogSaveCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiGenerationLogCommandService {

  private final AiGenerationLogRepository aiGenerationLogRepository;

  @Transactional
  public void save(AiGenerationLogSaveCommand command) {
    aiGenerationLogRepository.save(
        AiGenerationLog.create(command.feedbackId(), command.promptContent(),
            command.completionContent(), command.durationMs(), command.streaming(),
            command.promptTokens(), command.completionTokens(), command.instruction()));
  }
}
