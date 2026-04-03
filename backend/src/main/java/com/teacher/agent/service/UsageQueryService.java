package com.teacher.agent.service;

import com.teacher.agent.domain.repository.AiGenerationLogRepository;
import com.teacher.agent.domain.repository.FeedbackLikeRepository;
import com.teacher.agent.domain.repository.FeedbackRepository;
import com.teacher.agent.domain.repository.UserEventRepository;
import com.teacher.agent.domain.vo.UserId;
import com.teacher.agent.dto.DailyUsageResponse;
import com.teacher.agent.dto.TopKeywordResponse;
import com.teacher.agent.dto.UsageSummaryResponse;
import com.teacher.agent.service.vo.DailyEventCountRow;
import com.teacher.agent.service.vo.KeywordCountRow;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsageQueryService {

  private static final String EVENT_FEEDBACK_COPY = "feedback_copy";
  private static final String EVENT_FEEDBACK_REGENERATE = "feedback_regenerate";
  private static final String EVENT_FEEDBACK_GENERATE = "feedback_generate";
  private static final String EVENT_FEEDBACK_LIKE = "feedback_like";

  private final AiGenerationLogRepository aiGenerationLogRepository;
  private final FeedbackLikeRepository feedbackLikeRepository;
  private final FeedbackRepository feedbackRepository;
  private final UserEventRepository userEventRepository;

  public UsageSummaryResponse getUsageSummary(UserId userId) {
    long totalAiGenerations = aiGenerationLogRepository.countByTeacherId(userId);
    long totalLikes = feedbackLikeRepository.countByTeacherId(userId);
    long aiGeneratedFeedbackCount =
        feedbackRepository.countByAiContentIsNotNullAndTeacherId(userId);
    long totalCopyClicks = userEventRepository.countByEventTypeAndUserId(
        EVENT_FEEDBACK_COPY, userId.value());
    long totalRegenerations = userEventRepository.countByEventTypeAndUserId(
        EVENT_FEEDBACK_REGENERATE, userId.value());
    long totalGenerateClicks = userEventRepository.countByEventTypeAndUserId(
        EVENT_FEEDBACK_GENERATE, userId.value());
    double avgGenerationDurationMs = aiGenerationLogRepository.averageDurationMsByTeacherId(userId);

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime sevenDaysAgo = now.minusDays(7);
    LocalDateTime thirtyDaysAgo = now.minusDays(30);

    int activeDaysLast7 = userEventRepository.countDistinctActiveDaysByUserId(
        sevenDaysAgo, now, userId.value());
    int activeDaysLast30 = userEventRepository.countDistinctActiveDaysByUserId(
        thirtyDaysAgo, now, userId.value());

    // likeRate: 좋아요 수 / AI 생성된 피드백 수 (피드백 품질 만족도)
    // copyRate: 복사 클릭 수 / AI 생성 클릭 수 (생성 → 복사 전환율, ROADMAP KPI)
    // regenerationRate: 재생성 수 / 생성 클릭 수 (첫 생성 불만족률)
    double likeRate = calculateRate(totalLikes, aiGeneratedFeedbackCount);
    double copyRate = calculateRate(totalCopyClicks, totalGenerateClicks);
    double regenerationRate = calculateRate(totalRegenerations, totalGenerateClicks);

    return new UsageSummaryResponse(
        totalAiGenerations,
        totalLikes,
        likeRate,
        totalCopyClicks,
        copyRate,
        totalRegenerations,
        regenerationRate,
        avgGenerationDurationMs,
        activeDaysLast7,
        activeDaysLast30);
  }

  public List<DailyUsageResponse> getDailyUsage(int days, UserId userId) {
    LocalDateTime end = LocalDateTime.now();
    LocalDateTime start = end.minusDays(days);

    List<DailyEventCountRow> eventCounts =
        userEventRepository.findDailyEventCountsByUserId(start, end, userId.value());

    Map<LocalDate, Map<String, Long>> dailyEventMap = new HashMap<>();
    for (DailyEventCountRow row : eventCounts) {
      LocalDate date = convertToLocalDate(row.date());
      dailyEventMap.computeIfAbsent(date, k -> new HashMap<>()).put(row.eventType(), row.count());
    }

    List<DailyUsageResponse> result = new ArrayList<>();
    LocalDate currentDate = start.toLocalDate();
    LocalDate endDate = end.toLocalDate();

    while (!currentDate.isAfter(endDate)) {
      Map<String, Long> events = dailyEventMap.getOrDefault(currentDate, Map.of());

      long generations = events.getOrDefault(EVENT_FEEDBACK_GENERATE, 0L);
      long copies = events.getOrDefault(EVENT_FEEDBACK_COPY, 0L);
      long likes = events.getOrDefault(EVENT_FEEDBACK_LIKE, 0L);
      long regenerations = events.getOrDefault(EVENT_FEEDBACK_REGENERATE, 0L);

      result.add(new DailyUsageResponse(currentDate, generations, copies, likes, regenerations));
      currentDate = currentDate.plusDays(1);
    }

    return result;
  }

  public List<TopKeywordResponse> getTopKeywords(int limit, UserId userId) {
    List<KeywordCountRow> keywords =
        feedbackRepository.findTopKeywordsByTeacherId(userId, PageRequest.of(0, limit));

    return keywords.stream()
        .map(row -> new TopKeywordResponse(row.keyword(), row.count()))
        .toList();
  }

  private double calculateRate(long numerator, long denominator) {
    if (denominator == 0) {
      return 0.0;
    }
    return Math.round((double) numerator / denominator * 100) / 100.0;
  }

  private LocalDate convertToLocalDate(Object dateValue) {
    if (dateValue instanceof LocalDate localDate) {
      return localDate;
    }
    if (dateValue instanceof java.sql.Date sqlDate) {
      return sqlDate.toLocalDate();
    }
    throw new IllegalArgumentException("Unsupported date type: " + dateValue.getClass());
  }
}
