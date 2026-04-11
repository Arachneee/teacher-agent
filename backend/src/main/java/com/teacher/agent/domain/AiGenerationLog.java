package com.teacher.agent.domain;

import static com.teacher.agent.util.Parameter.COMPLETION_CONTENT;
import static com.teacher.agent.util.Parameter.DURATION_MS;
import static com.teacher.agent.util.Parameter.FEEDBACK_ID;
import static com.teacher.agent.util.Parameter.PROMPT_CONTENT;
import static com.teacher.agent.util.ValidationUtil.checkNotBlank;
import static com.teacher.agent.util.ValidationUtil.checkNotNegative;
import static com.teacher.agent.util.ValidationUtil.checkPositive;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id", callSuper = false)
@Table(indexes = @Index(name = "idx_ai_generation_log_feedback_id", columnList = "feedbackId"))
public class AiGenerationLog extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long feedbackId;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String promptContent;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String completionContent;

  @Column(nullable = false)
  private boolean streaming;

  @Column(nullable = false)
  private long durationMs;

  @Column
  private Integer promptTokens;

  @Column
  private Integer completionTokens;

  @Column(columnDefinition = "TEXT")
  private String instruction;

  public static AiGenerationLog create(Long feedbackId, String promptContent,
      String completionContent, long durationMs, boolean streaming,
      Integer promptTokens, Integer completionTokens, String instruction) {
    AiGenerationLog aiGenerationLog = new AiGenerationLog();

    aiGenerationLog.feedbackId = checkPositive(feedbackId, FEEDBACK_ID);
    aiGenerationLog.promptContent = checkNotBlank(promptContent, PROMPT_CONTENT);
    aiGenerationLog.completionContent = checkNotBlank(completionContent, COMPLETION_CONTENT);
    aiGenerationLog.durationMs = checkNotNegative(durationMs, DURATION_MS);
    aiGenerationLog.streaming = streaming;
    aiGenerationLog.promptTokens = promptTokens;
    aiGenerationLog.completionTokens = completionTokens;
    aiGenerationLog.instruction = toNullIfAbsent(instruction);

    return aiGenerationLog;
  }

  private static String toNullIfAbsent(String instruction) {
    if (instruction == null || instruction.isBlank() || instruction.strip().equals("없음")) {
      return null;
    }
    return instruction.strip();
  }
}
