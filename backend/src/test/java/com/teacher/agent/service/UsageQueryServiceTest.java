package com.teacher.agent.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.teacher.agent.domain.AiGenerationLog;
import com.teacher.agent.domain.Feedback;
import com.teacher.agent.domain.FeedbackLike;
import com.teacher.agent.domain.UserEvent;
import com.teacher.agent.domain.repository.AiGenerationLogRepository;
import com.teacher.agent.domain.repository.FeedbackLikeRepository;
import com.teacher.agent.domain.repository.FeedbackRepository;
import com.teacher.agent.domain.repository.UserEventRepository;
import com.teacher.agent.dto.DailyUsageResponse;
import com.teacher.agent.dto.TopKeywordResponse;
import com.teacher.agent.dto.UsageSummaryResponse;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import(UsageQueryService.class)
class UsageQueryServiceTest {

  private static final AtomicLong ID_GENERATOR = new AtomicLong(System.currentTimeMillis());

  @Autowired
  private UsageQueryService usageQueryService;

  @Autowired
  private AiGenerationLogRepository aiGenerationLogRepository;

  @Autowired
  private FeedbackLikeRepository feedbackLikeRepository;

  @Autowired
  private FeedbackRepository feedbackRepository;

  @Autowired
  private UserEventRepository userEventRepository;

  @BeforeEach
  void setUp() {
    userEventRepository.deleteAllInBatch();
    feedbackLikeRepository.deleteAllInBatch();
    aiGenerationLogRepository.deleteAllInBatch();
    feedbackRepository.findAll().forEach(f -> feedbackRepository.delete(f));
  }

  private long nextId() {
    return ID_GENERATOR.incrementAndGet();
  }

  @Test
  void 데이터가_없을_때_요약_조회시_0을_반환한다() {
    UsageSummaryResponse response = usageQueryService.getUsageSummary();

    assertThat(response.totalAiGenerations()).isZero();
    assertThat(response.totalLikes()).isZero();
    assertThat(response.likeRate()).isZero();
    assertThat(response.totalCopyClicks()).isZero();
    assertThat(response.copyRate()).isZero();
    assertThat(response.totalRegenerations()).isZero();
    assertThat(response.regenerationRate()).isZero();
    assertThat(response.avgGenerationDurationMs()).isZero();
    assertThat(response.activeDaysLast7()).isZero();
    assertThat(response.activeDaysLast30()).isZero();
  }

  @Test
  void 요약_조회시_집계_데이터를_반환한다() {
    long uniqueId = nextId();
    Feedback feedback = feedbackRepository.save(Feedback.create(uniqueId, uniqueId));
    feedback.updateAiContent("AI 생성 콘텐츠");
    feedbackRepository.save(feedback);

    aiGenerationLogRepository.save(
        AiGenerationLog.create(feedback.getId(), "prompt", "completion", 1000L, false, 10, 20));
    aiGenerationLogRepository.save(
        AiGenerationLog.create(feedback.getId(), "prompt2", "completion2", 2000L, false, 10, 20));

    feedbackLikeRepository.save(FeedbackLike.create(feedback.getId(), "content", "keywords"));

    userEventRepository.save(UserEvent.create("admin", "feedback_copy", null));
    userEventRepository.save(UserEvent.create("admin", "feedback_regenerate", null));
    userEventRepository.save(UserEvent.create("admin", "feedback_generate", null));
    userEventRepository.save(UserEvent.create("admin", "feedback_generate", null));

    UsageSummaryResponse response = usageQueryService.getUsageSummary();

    assertThat(response.totalAiGenerations()).isEqualTo(2);
    assertThat(response.totalLikes()).isEqualTo(1);
    assertThat(response.likeRate()).isEqualTo(1.0);
    assertThat(response.totalCopyClicks()).isEqualTo(1);
    assertThat(response.copyRate()).isEqualTo(1.0);
    assertThat(response.totalRegenerations()).isEqualTo(1);
    assertThat(response.regenerationRate()).isEqualTo(0.5);
    assertThat(response.avgGenerationDurationMs()).isEqualTo(1500.0);
  }

  @Test
  void 분모가_0일_때_비율은_0을_반환한다() {
    UsageSummaryResponse response = usageQueryService.getUsageSummary();

    assertThat(response.likeRate()).isZero();
    assertThat(response.copyRate()).isZero();
    assertThat(response.regenerationRate()).isZero();
  }

  @Test
  void 데이터가_없을_때_일별_사용량_조회시_빈_리스트를_반환한다() {
    List<DailyUsageResponse> response = usageQueryService.getDailyUsage(7);

    assertThat(response).hasSize(8);
    assertThat(response).allMatch(
        daily -> daily.generations() == 0 && daily.copies() == 0 && daily.likes() == 0
            && daily.regenerations() == 0);
  }

  @Test
  void 일별_사용량_조회시_기간에_맞는_데이터를_반환한다() {
    userEventRepository.save(UserEvent.create("admin", "feedback_generate", null));
    userEventRepository.save(UserEvent.create("admin", "feedback_copy", null));

    List<DailyUsageResponse> response = usageQueryService.getDailyUsage(7);

    assertThat(response).isNotEmpty();
  }

  @Test
  void 데이터가_없을_때_인기_키워드_조회시_빈_리스트를_반환한다() {
    List<TopKeywordResponse> response = usageQueryService.getTopKeywords(10);

    assertThat(response).isEmpty();
  }

  @Test
  void 인기_키워드_조회시_카운트_내림차순으로_반환한다() {
    long id1 = nextId();
    long id2 = nextId();
    Feedback feedback1 = feedbackRepository.save(Feedback.create(id1, id1));
    feedback1.addKeyword("성실함", false);
    feedback1.addKeyword("성실함", false);
    feedback1.addKeyword("집중력", false);
    feedbackRepository.save(feedback1);

    Feedback feedback2 = feedbackRepository.save(Feedback.create(id2, id2));
    feedback2.addKeyword("성실함", false);
    feedbackRepository.save(feedback2);

    List<TopKeywordResponse> response = usageQueryService.getTopKeywords(10);

    assertThat(response).hasSize(2);
    assertThat(response.get(0).keyword()).isEqualTo("성실함");
    assertThat(response.get(0).count()).isEqualTo(3);
    assertThat(response.get(1).keyword()).isEqualTo("집중력");
    assertThat(response.get(1).count()).isEqualTo(1);
  }

  @Test
  void 인기_키워드_조회시_limit만큼만_반환한다() {
    long id = nextId();
    Feedback feedback = feedbackRepository.save(Feedback.create(id, id));
    feedback.addKeyword("키워드1", false);
    feedback.addKeyword("키워드2", false);
    feedback.addKeyword("키워드3", false);
    feedbackRepository.save(feedback);

    List<TopKeywordResponse> response = usageQueryService.getTopKeywords(2);

    assertThat(response).hasSize(2);
  }
}
