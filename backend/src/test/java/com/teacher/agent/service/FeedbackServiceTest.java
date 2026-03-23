package com.teacher.agent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.teacher.agent.domain.FeedbackLike;
import com.teacher.agent.domain.Lesson;
import com.teacher.agent.domain.Student;
import com.teacher.agent.domain.Teacher;
import com.teacher.agent.domain.repository.FeedbackLikeRepository;
import com.teacher.agent.domain.repository.FeedbackRepository;
import com.teacher.agent.domain.repository.LessonRepository;
import com.teacher.agent.domain.repository.StudentRepository;
import com.teacher.agent.domain.repository.TeacherRepository;
import com.teacher.agent.domain.vo.SchoolGrade;
import com.teacher.agent.domain.vo.UserId;
import com.teacher.agent.dto.FeedbackResponse;
import com.teacher.agent.exception.BadRequestException;
import com.teacher.agent.exception.BusinessException;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({LessonQueryService.class, FeedbackQueryService.class, FeedbackCommandService.class,
    FeedbackKeywordService.class, FeedbackLikeService.class})
class FeedbackServiceTest {

  @Autowired
  private FeedbackQueryService feedbackQueryService;

  @Autowired
  private FeedbackCommandService feedbackCommandService;

  @Autowired
  private FeedbackKeywordService feedbackKeywordService;

  @Autowired
  private FeedbackLikeService feedbackLikeService;

  @Autowired
  private StudentRepository studentRepository;

  @Autowired
  private FeedbackRepository feedbackRepository;

  @Autowired
  private LessonRepository lessonRepository;

  @Autowired
  private TeacherRepository teacherRepository;

  @Autowired
  private EntityManager entityManager;

  @MockitoBean
  private FeedbackAiService feedbackAiService;

  private static final LocalDateTime START = LocalDateTime.of(2026, 3, 17, 9, 0);
  private static final LocalDateTime END = LocalDateTime.of(2026, 3, 17, 10, 0);

  private UserId userId;
  private UserId otherUserId;
  private Long studentId;
  private Long lessonId;

  @BeforeEach
  void setUp() {
    Teacher teacher = teacherRepository.save(Teacher.create("teacher1", "encoded", "선생님1", "수학"));
    Teacher otherTeacher =
        teacherRepository.save(Teacher.create("teacher2", "encoded", "선생님2", "영어"));
    userId = teacher.getUserId();
    otherUserId = otherTeacher.getUserId();
    Student student = studentRepository
        .save(Student.create(teacher.getUserId(), "홍길동", "메모", SchoolGrade.ELEMENTARY_1));
    studentId = student.getId();
    Lesson lesson = lessonRepository.save(Lesson.create(teacher.getUserId(), "수학", START, END));
    lesson.addAttendee(studentId);
    lesson = lessonRepository.save(lesson);
    lessonId = lesson.getId();
  }

  @Autowired
  private FeedbackLikeRepository feedbackLikeRepository;

  @AfterEach
  void tearDown() {
    feedbackLikeRepository.deleteAllInBatch();
    feedbackRepository.deleteAll();
    lessonRepository.deleteAll();
    studentRepository.deleteAllInBatch();
    teacherRepository.deleteAllInBatch();
  }

  @Test
  void 피드백을_생성한다() {
    FeedbackResponse response =
        feedbackCommandService.create(userId, studentId, lessonId);

    assertThat(response.id()).isNotNull();
    assertThat(response.studentId()).isEqualTo(studentId);
    assertThat(response.keywords()).isEmpty();
    assertThat(response.aiContent()).isNull();
    assertThat(response.liked()).isFalse();
  }

  @Test
  void 같은_수업에_동일_학생의_피드백을_중복_생성_시_예외가_발생한다() {
    feedbackCommandService.create(userId, studentId, lessonId);

    assertThatThrownBy(
        () -> feedbackCommandService.create(userId, studentId, lessonId))
        .isInstanceOf(Exception.class);
  }

  @Test
  void 다른_선생님_학생으로_피드백_생성_시_예외가_발생한다() {
    assertThatThrownBy(() -> feedbackCommandService.create(otherUserId, studentId, lessonId))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void 존재하지_않는_학생으로_피드백_생성_시_예외가_발생한다() {
    assertThatThrownBy(
        () -> feedbackCommandService.create(userId, 999L, lessonId))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void 수업에_등록되지_않은_학생으로_피드백_생성_시_예외가_발생한다() {
    Student unenrolledStudent =
        studentRepository.save(Student.create(userId, "미등록학생", null, SchoolGrade.ELEMENTARY_1));

    assertThatThrownBy(
        () -> feedbackCommandService.create(userId, unenrolledStudent.getId(), lessonId))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void AI_콘텐츠가_있는_피드백만_목록에_포함된다() {
    FeedbackResponse created = feedbackCommandService.create(userId, studentId, lessonId);
    feedbackCommandService.update(userId, created.id(), "AI 내용");

    var feedbacks = feedbackQueryService.getAll(userId, studentId);

    assertThat(feedbacks).hasSize(1);
    assertThat(feedbacks.get(0).studentId()).isEqualTo(studentId);
    assertThat(feedbacks.get(0).lessonTitle()).isEqualTo("수학");
    assertThat(feedbacks.get(0).lessonStartTime()).isEqualTo(START);
  }

  @Test
  void AI_콘텐츠가_없는_피드백은_목록에_포함되지_않는다() {
    feedbackCommandService.create(userId, studentId, lessonId);

    var feedbacks = feedbackQueryService.getAll(userId, studentId);

    assertThat(feedbacks).isEmpty();
  }

  @Test
  void 수업이_삭제된_피드백은_목록에_포함되지_않는다() {
    FeedbackResponse created = feedbackCommandService.create(userId, studentId, lessonId);
    feedbackCommandService.update(userId, created.id(), "AI 내용");
    lessonRepository.deleteById(lessonId);

    var feedbacks = feedbackQueryService.getAll(userId, studentId);

    assertThat(feedbacks).isEmpty();
  }

  @Test
  void 피드백_목록은_최신순으로_정렬된다() {
    LocalDateTime olderStart = LocalDateTime.of(2026, 3, 10, 9, 0);
    LocalDateTime olderEnd = LocalDateTime.of(2026, 3, 10, 10, 0);
    Lesson olderLesson = lessonRepository.save(
        Lesson.create(userId, "이전 수업", olderStart, olderEnd));
    olderLesson.addAttendee(studentId);
    lessonRepository.save(olderLesson);

    FeedbackResponse olderFeedback =
        feedbackCommandService.create(userId, studentId, olderLesson.getId());
    FeedbackResponse recentFeedback = feedbackCommandService.create(userId, studentId, lessonId);
    feedbackCommandService.update(userId, olderFeedback.id(), "AI 내용");
    feedbackCommandService.update(userId, recentFeedback.id(), "AI 내용");

    var feedbacks = feedbackQueryService.getAll(userId, studentId);

    assertThat(feedbacks).hasSize(2);
    assertThat(feedbacks.get(0).lessonTitle()).isEqualTo("수학");
    assertThat(feedbacks.get(1).lessonTitle()).isEqualTo("이전 수업");
  }

  @Test
  void 다른_선생님_학생의_피드백_목록_조회_시_예외가_발생한다() {
    assertThatThrownBy(() -> feedbackQueryService.getAll(otherUserId, studentId))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void 피드백을_단건_조회한다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, studentId, lessonId);

    FeedbackResponse found = feedbackQueryService.getOne(userId, created.id());

    assertThat(found.id()).isEqualTo(created.id());
  }

  @Test
  void 다른_선생님의_피드백_단건_조회_시_예외가_발생한다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, studentId, lessonId);

    assertThatThrownBy(() -> feedbackQueryService.getOne(otherUserId, created.id()))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void 존재하지_않는_피드백_조회_시_예외가_발생한다() {
    assertThatThrownBy(() -> feedbackQueryService.getOne(userId, 999L))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void 피드백의_AI_콘텐츠를_수정한다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, studentId, lessonId);

    FeedbackResponse updated = feedbackCommandService.update(userId, created.id(),
        "수정된 AI 콘텐츠");

    assertThat(updated.aiContent()).isEqualTo("수정된 AI 콘텐츠");
  }

  @Test
  void 빈_문자열로_수정하면_AI_콘텐츠가_초기화된다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, studentId, lessonId);
    feedbackCommandService.update(userId, created.id(), "AI 콘텐츠");

    FeedbackResponse updated =
        feedbackCommandService.update(userId, created.id(), "");

    assertThat(updated.aiContent()).isNull();
  }

  @Test
  void null로_수정하면_AI_콘텐츠가_초기화된다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, studentId, lessonId);
    feedbackCommandService.update(userId, created.id(), "AI 콘텐츠");

    FeedbackResponse updated =
        feedbackCommandService.update(userId, created.id(), null);

    assertThat(updated.aiContent()).isNull();
  }

  @Test
  void 피드백을_삭제한다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, studentId, lessonId);

    feedbackCommandService.delete(userId, created.id());

    assertThatThrownBy(() -> feedbackQueryService.getOne(userId, created.id()))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void 다른_선생님의_피드백_삭제_시_예외가_발생한다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, studentId, lessonId);

    assertThatThrownBy(() -> feedbackCommandService.delete(otherUserId, created.id()))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void 키워드를_추가한다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, studentId, lessonId);

    FeedbackResponse updated = feedbackKeywordService.addKeyword(userId, created.id(),
        "성실함", false);

    assertThat(updated.keywords()).hasSize(1);
    assertThat(updated.keywords().get(0).keyword()).isEqualTo("성실함");
    assertThat(updated.keywords().get(0).id()).isNotNull();
  }

  @Test
  void 키워드를_삭제한다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, studentId, lessonId);
    FeedbackResponse withKeyword = feedbackKeywordService.addKeyword(userId, created.id(),
        "성실함", false);
    Long keywordId = withKeyword.keywords().get(0).id();

    feedbackKeywordService.removeKeyword(userId, created.id(), keywordId);

    FeedbackResponse afterRemoval = feedbackQueryService.getOne(userId, created.id());
    assertThat(afterRemoval.keywords()).isEmpty();
  }

  @Test
  void 존재하지_않는_키워드_삭제_시_예외가_발생한다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, studentId, lessonId);

    assertThatThrownBy(() -> feedbackKeywordService.removeKeyword(userId, created.id(), 999L))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void 키워드를_수정한다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, studentId, lessonId);
    FeedbackResponse withKeyword = feedbackKeywordService.addKeyword(userId, created.id(),
        "성실함", false);
    Long keywordId = withKeyword.keywords().get(0).id();

    FeedbackResponse updated = feedbackKeywordService.updateKeyword(userId, created.id(), keywordId,
        "꼼꼼함", false);

    assertThat(updated.keywords()).hasSize(1);
    assertThat(updated.keywords().get(0).keyword()).isEqualTo("꼼꼼함");
    assertThat(updated.keywords().get(0).id()).isEqualTo(keywordId);
  }

  @Test
  void 존재하지_않는_키워드_수정_시_예외가_발생한다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, studentId, lessonId);

    assertThatThrownBy(() -> feedbackKeywordService.updateKeyword(userId, created.id(), 999L,
        "꼼꼼함", false))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void 다른_선생님_피드백의_키워드_수정_시_예외가_발생한다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, studentId, lessonId);
    FeedbackResponse withKeyword = feedbackKeywordService.addKeyword(userId, created.id(),
        "성실함", false);
    Long keywordId = withKeyword.keywords().get(0).id();

    assertThatThrownBy(() -> feedbackKeywordService.updateKeyword(otherUserId, created.id(),
        keywordId, "꼼꼼함", false))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void AI_콘텐츠를_생성한다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, studentId, lessonId);
    feedbackKeywordService.addKeyword(userId, created.id(),
        "성실함", false);
    given(feedbackAiService.generateFeedbackContent(any(), eq("홍길동"), eq("초1")))
        .willReturn("AI가 생성한 피드백");

    feedbackCommandService.generateAiContent(userId, created.id());

    verify(feedbackAiService).generateFeedbackContent(any(), eq("홍길동"), eq("초1"));
  }

  @Test
  void 키워드_없이_AI_콘텐츠_생성_시_예외가_발생한다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, studentId, lessonId);

    assertThatThrownBy(() -> feedbackCommandService.generateAiContent(userId, created.id()))
        .isInstanceOf(BadRequestException.class);
  }

  @Test
  void AI_콘텐츠를_스트리밍으로_생성하고_DB에_저장한다() {
    FeedbackResponse created = feedbackCommandService.create(userId, studentId, lessonId);
    feedbackKeywordService.addKeyword(userId, created.id(), "성실함", false);
    given(feedbackAiService.streamFeedbackContent(any(), eq("홍길동"), eq("초1")))
        .willReturn(Flux.just("AI가 ", "생성한 ", "피드백"));

    StepVerifier.create(feedbackCommandService.streamAiContent(userId, created.id()))
        .expectNext("AI가 ", "생성한 ", "피드백")
        .verifyComplete();

    FeedbackResponse saved = feedbackQueryService.getOne(userId, created.id());
    assertThat(saved.aiContent()).isEqualTo("AI가 생성한 피드백");
  }

  @Test
  void 키워드_없이_AI_콘텐츠_스트리밍_생성_시_예외가_발생한다() {
    FeedbackResponse created = feedbackCommandService.create(userId, studentId, lessonId);

    assertThatThrownBy(() -> feedbackCommandService.streamAiContent(userId, created.id()))
        .isInstanceOf(BadRequestException.class);
  }

  @Test
  void 다른_선생님_피드백의_AI_콘텐츠_스트리밍_시_예외가_발생한다() {
    FeedbackResponse created = feedbackCommandService.create(userId, studentId, lessonId);

    assertThatThrownBy(() -> feedbackCommandService.streamAiContent(otherUserId, created.id()))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void 피드백에_좋아요를_누른다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, studentId, lessonId);
    feedbackCommandService.update(userId, created.id(), "AI 피드백 내용");

    FeedbackResponse liked = feedbackLikeService.like(userId, created.id());

    assertThat(liked.liked()).isTrue();
  }

  @Test
  void AI_콘텐츠가_없으면_좋아요에_실패한다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, studentId, lessonId);

    assertThatThrownBy(() -> feedbackLikeService.like(userId, created.id()))
        .isInstanceOf(BadRequestException.class);
  }

  @Test
  void 같은_내용에_좋아요를_중복으로_누르면_실패한다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, studentId, lessonId);
    feedbackCommandService.update(userId, created.id(), "AI 피드백 내용");
    feedbackLikeService.like(userId, created.id());

    assertThatThrownBy(() -> feedbackLikeService.like(userId, created.id()))
        .isInstanceOf(BadRequestException.class);
  }

  @Test
  void 좋아요_후_수정하면_다시_좋아요할_수_있다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, studentId, lessonId);
    feedbackCommandService.update(userId, created.id(), "원본 내용");
    feedbackLikeService.like(userId, created.id());

    FeedbackResponse updated =
        feedbackCommandService.update(userId, created.id(), "수정된 내용");
    assertThat(updated.liked()).isFalse();

    FeedbackResponse reLiked = feedbackLikeService.like(userId, created.id());
    assertThat(reLiked.liked()).isTrue();
  }

  @Test
  void 좋아요_후_AI_재생성하면_다시_좋아요할_수_있다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, studentId, lessonId);
    feedbackKeywordService.addKeyword(userId, created.id(),
        "성실함", false);
    feedbackCommandService.update(userId, created.id(), "AI가 생성한 피드백");
    feedbackLikeService.like(userId, created.id());

    FeedbackResponse regenerated =
        feedbackCommandService.update(userId, created.id(),
            "새로 생성한 피드백");
    assertThat(regenerated.liked()).isFalse();

    FeedbackResponse reLiked = feedbackLikeService.like(userId, created.id());
    assertThat(reLiked.liked()).isTrue();
  }

  @Test
  void 좋아요_이력은_누적_저장된다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, studentId, lessonId);
    feedbackCommandService.update(userId, created.id(), "버전 1");
    feedbackLikeService.like(userId, created.id());
    feedbackCommandService.update(userId, created.id(), "버전 2");
    feedbackLikeService.like(userId, created.id());

    Long likeCount = entityManager
        .createQuery("SELECT COUNT(fl) FROM FeedbackLike fl WHERE fl.feedbackId = :feedbackId",
            Long.class)
        .setParameter("feedbackId", created.id()).getSingleResult();
    assertThat(likeCount).isEqualTo(2);
  }

  @Test
  void 좋아요_시_스냅샷이_저장된다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, studentId, lessonId);
    feedbackKeywordService.addKeyword(userId, created.id(),
        "성실함", false);
    feedbackCommandService.update(userId, created.id(), "AI 피드백 내용");
    feedbackLikeService.like(userId, created.id());

    FeedbackLike feedbackLike = entityManager
        .createQuery("SELECT fl FROM FeedbackLike fl WHERE fl.feedbackId = :feedbackId",
            FeedbackLike.class)
        .setParameter("feedbackId", created.id()).getSingleResult();
    assertThat(feedbackLike.getAiContentSnapshot()).isEqualTo("AI 피드백 내용");
    assertThat(feedbackLike.getKeywordsSnapshot()).isEqualTo("성실함");
  }

  @Test
  void 피드백_삭제_시_좋아요_이력도_삭제된다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, studentId, lessonId);
    feedbackCommandService.update(userId, created.id(), "AI 피드백 내용");
    feedbackLikeService.like(userId, created.id());

    feedbackCommandService.delete(userId, created.id());

    Long likeCount = entityManager.createQuery("SELECT COUNT(fl) FROM FeedbackLike fl", Long.class)
        .getSingleResult();
    assertThat(likeCount).isZero();
  }

  @Test
  void 수정_사항이_없으면_좋아요_상태가_유지된다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, studentId, lessonId);
    feedbackCommandService.update(userId, created.id(), "AI 피드백 내용");
    feedbackLikeService.like(userId, created.id());

    FeedbackResponse fetched = feedbackQueryService.getOne(userId, created.id());

    assertThat(fetched.liked()).isTrue();
  }
}
