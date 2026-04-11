package com.teacher.agent.service.vo;

public record AiGenerationLogSaveCommand(
    Long feedbackId,
    String promptContent,
    String completionContent,
    long durationMs,
    boolean streaming,
    Integer promptTokens,
    Integer completionTokens,
    String instruction) {
}
