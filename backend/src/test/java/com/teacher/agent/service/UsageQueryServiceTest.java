package com.teacher.agent.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.teacher.agent.domain.AiGenerationLog;
import com.teacher.agent.domain.Feedback;
import com.teacher.agent.domain.FeedbackLike;
import com.teacher.agent.domain.Student;
import com.teacher.agent.domain.UserEvent;
import com.teacher.agent.domain.repository.AiGenerationLogRepository;
import com.teacher.agent.domain.repository.FeedbackLikeRepository;
import com.teacher.agent.domain.repository.FeedbackRepository;
import com.teacher.agent.domain.repository.StudentRepository;
import com.teacher.agent.domain.repository.UserEventRepository;
import com.teacher.agent.domain.vo.SchoolGrade;
import com.teacher.agent.domain.vo.UserId;
import com.teacher.agent.dto.DailyUsageResponse;
import com.teacher.agent.dto.TopKeywordResponse;
import com.teacher.agent.dto.UsageSummaryResponse;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
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

  private static final UserId TEACHER_A = new UserId("teacher-a");
  private static final UserId TEACHER_B = new UserId("teacher-b");

  @Autowired
  private UsageQueryService usageQueryService;

  @Autowired
  private AiGenerationLogRepository aiGenerationLogRepository;

  @Autowired
  private FeedbackLikeRepository feedbackLikeRepository;

  @Autowired
  private FeedbackRepository feedbackRepository;

  @Autowired
  private StudentRepository studentRepository;

  @Autowired
  private UserEventRepository userEventRepository;

  @BeforeEach
  void setUp() {
    tearDown();
  }

  @AfterEach
  void tearDown() {
    userEventRepository.deleteAllInBatch();
    feedbackLikeRepository.deleteAllInBatch();
    aiGenerationLogRepository.deleteAllInBatch();
    feedbackRepository.findAll().forEach(feedbackRepository::delete);
    studentRepository.deleteAllInBatch();
  }

  @Test
  void 데이터가_없을_때_요약_조회시_0을_반환한다() {
    UsageSummaryResponse response = usageQueryService.getUsageSummary(TEACHER_A);

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
  void 요약_조회시_해당_선생님의_집계_데이터를_반환한다() {
    Student student = studentRepository.save(
        Student.create(TEACHER_A, "홍길동", null, SchoolGrade.ELEMENTARY_3));

    Feedback feedback = feedbackRepository.save(Feedback.create(student.getId(), 1L));
    feedback.updateAiContent("AI 생성 콘텐츠");
    feedbackRepository.save(feedback);

    aiGenerationLogRepository.save(
        AiGenerationLog.create(feedback.getId(), "prompt", "completion", 1000L, false, 10, 20));
    aiGenerationLogRepository.save(
        AiGenerationLog.create(feedback.getId(), "prompt2", "completion2", 2000L, false, 10, 20));

    feedbackLikeRepository.save(FeedbackLike.create(feedback.getId(), "content", "keywords"));

    userEventRepository.save(UserEvent.create(TEACHER_A.value(), "feedback_copy", null));
    userEventRepository.save(UserEvent.create(TEACHER_A.value(), "feedback_regenerate", null));
    userEventRepository.save(UserEvent.create(TEACHER_A.value(), "feedback_generate", null));
    userEventRepository.save(UserEvent.create(TEACHER_A.value(), "feedback_generate", null));

    UsageSummaryResponse response = usageQueryService.getUsageSummary(TEACHER_A);

    assertThat(response.totalAiGenerations()).isEqualTo(2);
    assertThat(response.totalLikes()).isEqualTo(1);
    assertThat(response.likeRate()).isEqualTo(1.0);
    assertThat(response.totalCopyClicks()).isEqualTo(1);
    // copyRate = 복사(1) / 생성클릭(2) = 0.5
    assertThat(response.copyRate()).isEqualTo(0.5);
    assertThat(response.totalRegenerations()).isEqualTo(1);
    // regenerationRate = 재생성(1) / 생성클릭(2) = 0.5
    assertThat(response.regenerationRate()).isEqualTo(0.5);
    assertThat(response.avgGenerationDurationMs()).isEqualTo(1500.0);
  }

  @Test
  void 다른_선생님의_데이터가_집계에_포함되지_않는다() {
    Student studentA = studentRepository.save(
        Student.create(TEACHER_A, "선생님A 학생", null, SchoolGrade.ELEMENTARY_3));
    Student studentB = studentRepository.save(
        Student.create(TEACHER_B, "선생님B 학생", null, SchoolGrade.ELEMENTARY_3));

    Feedback feedbackA = feedbackRepository.save(Feedback.create(studentA.getId(), 1L));
    feedbackA.updateAiContent("A의 AI 콘텐츠");
    feedbackRepository.save(feedbackA);

    Feedback feedbackB = feedbackRepository.save(Feedback.create(studentB.getId(), 2L));
    feedbackB.updateAiContent("B의 AI 콘텐츠");
    feedbackRepository.save(feedbackB);

    aiGenerationLogRepository.save(
        AiGenerationLog.create(feedbackA.getId(), "prompt", "completion", 1000L, false, 10, 20));
    aiGenerationLogRepository.save(
        AiGenerationLog.create(feedbackB.getId(), "prompt", "completion", 2000L, false, 10, 20));

    feedbackLikeRepository.save(FeedbackLike.create(feedbackA.getId(), "content", "keywords"));
    feedbackLikeRepository.save(FeedbackLike.create(feedbackB.getId(), "content", "keywords"));

    userEventRepository.save(UserEvent.create(TEACHER_A.value(), "feedback_copy", null));
    userEventRepository.save(UserEvent.create(TEACHER_B.value(), "feedback_copy", null));

    UsageSummaryResponse responseA = usageQueryService.getUsageSummary(TEACHER_A);

    assertThat(responseA.totalAiGenerations()).isEqualTo(1);
    assertThat(responseA.totalLikes()).isEqualTo(1);
    assertThat(responseA.totalCopyClicks()).isEqualTo(1);
    assertThat(responseA.avgGenerationDurationMs()).isEqualTo(1000.0);
  }

  @Test
  void 분모가_0일_때_비율은_0을_반환한다() {
    UsageSummaryResponse response = usageQueryService.getUsageSummary(TEACHER_A);

    assertThat(response.likeRate()).isZero();
    assertThat(response.copyRate()).isZero();
    assertThat(response.regenerationRate()).isZero();
  }

  @Test
  void 데이터가_없을_때_일별_사용량_조회시_빈_리스트를_반환한다() {
    List<DailyUsageResponse> response = usageQueryService.getDailyUsage(7, TEACHER_A);

    assertThat(response).hasSize(8);
    assertThat(response).allMatch(
        daily -> daily.generations() == 0 && daily.copies() == 0 && daily.likes() == 0
            && daily.regenerations() == 0);
  }

  @Test
  void 일별_사용량_조회시_해당_선생님_데이터만_반환한다() {
    userEventRepository.save(UserEvent.create(TEACHER_A.value(), "feedback_generate", null));
    userEventRepository.save(UserEvent.create(TEACHER_A.value(), "feedback_copy", null));
    userEventRepository.save(UserEvent.create(TEACHER_B.value(), "feedback_generate", null));

    List<DailyUsageResponse> response = usageQueryService.getDailyUsage(7, TEACHER_A);

    long totalGenerations = response.stream().mapToLong(DailyUsageResponse::generations).sum();
    long totalCopies = response.stream().mapToLong(DailyUsageResponse::copies).sum();

    assertThat(totalGenerations).isEqualTo(1);
    assertThat(totalCopies).isEqualTo(1);
  }

  @Test
  void 데이터가_없을_때_인기_키워드_조회시_빈_리스트를_반환한다() {
    List<TopKeywordResponse> response = usageQueryService.getTopKeywords(10, TEACHER_A);

    assertThat(response).isEmpty();
  }

  @Test
  void 인기_키워드_조회시_해당_선생님_키워드만_카운트_내림차순으로_반환한다() {
    Student studentA = studentRepository.save(
        Student.create(TEACHER_A, "선생님A 학생1", null, SchoolGrade.ELEMENTARY_3));
    Student studentA2 = studentRepository.save(
        Student.create(TEACHER_A, "선생님A 학생2", null, SchoolGrade.ELEMENTARY_3));
    Student studentB = studentRepository.save(
        Student.create(TEACHER_B, "선생님B 학생", null, SchoolGrade.ELEMENTARY_3));

    Feedback feedbackA1 = feedbackRepository.save(Feedback.create(studentA.getId(), 1L));
    feedbackA1.addKeyword("성실함", false);
    feedbackA1.addKeyword("성실함", false);
    feedbackA1.addKeyword("집중력", false);
    feedbackRepository.save(feedbackA1);

    Feedback feedbackA2 = feedbackRepository.save(Feedback.create(studentA2.getId(), 2L));
    feedbackA2.addKeyword("성실함", false);
    feedbackRepository.save(feedbackA2);

    Feedback feedbackB = feedbackRepository.save(Feedback.create(studentB.getId(), 3L));
    feedbackB.addKeyword("배려심", false);
    feedbackB.addKeyword("배려심", false);
    feedbackRepository.save(feedbackB);

    List<TopKeywordResponse> response = usageQueryService.getTopKeywords(10, TEACHER_A);

    assertThat(response).hasSize(2);
    assertThat(response.get(0).keyword()).isEqualTo("성실함");
    assertThat(response.get(0).count()).isEqualTo(3);
    assertThat(response.get(1).keyword()).isEqualTo("집중력");
    assertThat(response.get(1).count()).isEqualTo(1);
  }

  @Test
  void 인기_키워드_조회시_limit만큼만_반환한다() {
    Student student = studentRepository.save(
        Student.create(TEACHER_A, "홍길동", null, SchoolGrade.ELEMENTARY_3));

    Feedback feedback = feedbackRepository.save(Feedback.create(student.getId(), 1L));
    feedback.addKeyword("키워드1", false);
    feedback.addKeyword("키워드2", false);
    feedback.addKeyword("키워드3", false);
    feedbackRepository.save(feedback);

    List<TopKeywordResponse> response = usageQueryService.getTopKeywords(2, TEACHER_A);

    assertThat(response).hasSize(2);
  }
}
