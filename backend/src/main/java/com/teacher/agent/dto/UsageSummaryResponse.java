package com.teacher.agent.dto;

public record UsageSummaryResponse(
    long totalAiGenerations,
    long totalLikes,
    double likeRate,
    long totalCopyClicks,
    double copyRate,
    long totalRegenerations,
    double regenerationRate,
    double avgGenerationDurationMs,
    int activeDaysLast7,
    int activeDaysLast30) {
}
