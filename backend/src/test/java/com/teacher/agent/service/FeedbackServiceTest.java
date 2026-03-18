package com.teacher.agent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.teacher.agent.domain.*;
import com.teacher.agent.dto.*;
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
import org.springframework.web.server.ResponseStatusException;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({LessonQueryService.class, FeedbackQueryService.class, FeedbackCommandService.class})
class FeedbackServiceTest {

  @Autowired
  private FeedbackQueryService feedbackQueryService;

  @Autowired
  private FeedbackCommandService feedbackCommandService;

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
    Student student = studentRepository.save(Student.create(teacher.getUserId(), "홍길동", "메모"));
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
        feedbackCommandService.create(userId, new FeedbackCreateRequest(studentId, lessonId));

    assertThat(response.id()).isNotNull();
    assertThat(response.studentId()).isEqualTo(studentId);
    assertThat(response.keywords()).isEmpty();
    assertThat(response.aiContent()).isNull();
    assertThat(response.liked()).isFalse();
  }

  @Test
  void 같은_수업에_동일_학생의_피드백을_중복_생성_시_예외가_발생한다() {
    feedbackCommandService.create(userId, new FeedbackCreateRequest(studentId, lessonId));

    assertThatThrownBy(
        () -> feedbackCommandService.create(userId, new FeedbackCreateRequest(studentId, lessonId)))
        .isInstanceOf(Exception.class);
  }

  @Test
  void 다른_선생님_학생으로_피드백_생성_시_예외가_발생한다() {
    assertThatThrownBy(() -> feedbackCommandService.create(otherUserId,
        new FeedbackCreateRequest(studentId, lessonId)))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void 존재하지_않는_학생으로_피드백_생성_시_예외가_발생한다() {
    assertThatThrownBy(
        () -> feedbackCommandService.create(userId, new FeedbackCreateRequest(999L, lessonId)))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void 수업에_등록되지_않은_학생으로_피드백_생성_시_예외가_발생한다() {
    Student unenrolledStudent = studentRepository.save(Student.create(userId, "미등록학생", null));

    assertThatThrownBy(() -> feedbackCommandService.create(userId,
        new FeedbackCreateRequest(unenrolledStudent.getId(), lessonId)))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void 학생_ID로_피드백_목록을_조회한다() {
    feedbackCommandService.create(userId, new FeedbackCreateRequest(studentId, lessonId));

    var feedbacks = feedbackQueryService.getAll(userId, studentId);

    assertThat(feedbacks).hasSize(1);
    assertThat(feedbacks.get(0).studentId()).isEqualTo(studentId);
  }

  @Test
  void 다른_선생님_학생의_피드백_목록_조회_시_예외가_발생한다() {
    assertThatThrownBy(() -> feedbackQueryService.getAll(otherUserId, studentId))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void 피드백을_단건_조회한다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, new FeedbackCreateRequest(studentId, lessonId));

    FeedbackResponse found = feedbackQueryService.getOne(userId, created.id());

    assertThat(found.id()).isEqualTo(created.id());
  }

  @Test
  void 다른_선생님의_피드백_단건_조회_시_예외가_발생한다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, new FeedbackCreateRequest(studentId, lessonId));

    assertThatThrownBy(() -> feedbackQueryService.getOne(otherUserId, created.id()))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void 존재하지_않는_피드백_조회_시_예외가_발생한다() {
    assertThatThrownBy(() -> feedbackQueryService.getOne(userId, 999L))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void 피드백의_AI_콘텐츠를_수정한다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, new FeedbackCreateRequest(studentId, lessonId));

    FeedbackResponse updated = feedbackCommandService.update(userId, created.id(),
        new FeedbackUpdateRequest("수정된 AI 콘텐츠"));

    assertThat(updated.aiContent()).isEqualTo("수정된 AI 콘텐츠");
  }

  @Test
  void 빈_문자열로_수정하면_AI_콘텐츠가_초기화된다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, new FeedbackCreateRequest(studentId, lessonId));
    feedbackCommandService.update(userId, created.id(), new FeedbackUpdateRequest("AI 콘텐츠"));

    FeedbackResponse updated =
        feedbackCommandService.update(userId, created.id(), new FeedbackUpdateRequest(""));

    assertThat(updated.aiContent()).isNull();
  }

  @Test
  void null로_수정하면_AI_콘텐츠가_초기화된다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, new FeedbackCreateRequest(studentId, lessonId));
    feedbackCommandService.update(userId, created.id(), new FeedbackUpdateRequest("AI 콘텐츠"));

    FeedbackResponse updated =
        feedbackCommandService.update(userId, created.id(), new FeedbackUpdateRequest(null));

    assertThat(updated.aiContent()).isNull();
  }

  @Test
  void 피드백을_삭제한다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, new FeedbackCreateRequest(studentId, lessonId));

    feedbackCommandService.delete(userId, created.id());

    assertThatThrownBy(() -> feedbackQueryService.getOne(userId, created.id()))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void 다른_선생님의_피드백_삭제_시_예외가_발생한다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, new FeedbackCreateRequest(studentId, lessonId));

    assertThatThrownBy(() -> feedbackCommandService.delete(otherUserId, created.id()))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void 키워드를_추가한다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, new FeedbackCreateRequest(studentId, lessonId));

    FeedbackResponse updated = feedbackCommandService.addKeyword(userId, created.id(),
        new FeedbackKeywordCreateRequest("성실함"));

    assertThat(updated.keywords()).hasSize(1);
    assertThat(updated.keywords().get(0).keyword()).isEqualTo("성실함");
    assertThat(updated.keywords().get(0).id()).isNotNull();
  }

  @Test
  void 키워드를_삭제한다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, new FeedbackCreateRequest(studentId, lessonId));
    FeedbackResponse withKeyword = feedbackCommandService.addKeyword(userId, created.id(),
        new FeedbackKeywordCreateRequest("성실함"));
    Long keywordId = withKeyword.keywords().get(0).id();

    feedbackCommandService.removeKeyword(userId, created.id(), keywordId);

    FeedbackResponse afterRemoval = feedbackQueryService.getOne(userId, created.id());
    assertThat(afterRemoval.keywords()).isEmpty();
  }

  @Test
  void 존재하지_않는_키워드_삭제_시_예외가_발생한다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, new FeedbackCreateRequest(studentId, lessonId));

    assertThatThrownBy(() -> feedbackCommandService.removeKeyword(userId, created.id(), 999L))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void 키워드를_수정한다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, new FeedbackCreateRequest(studentId, lessonId));
    FeedbackResponse withKeyword = feedbackCommandService.addKeyword(userId, created.id(),
        new FeedbackKeywordCreateRequest("성실함"));
    Long keywordId = withKeyword.keywords().get(0).id();

    FeedbackResponse updated = feedbackCommandService.updateKeyword(userId, created.id(), keywordId,
        new FeedbackKeywordUpdateRequest("꼼꼼함"));

    assertThat(updated.keywords()).hasSize(1);
    assertThat(updated.keywords().get(0).keyword()).isEqualTo("꼼꼼함");
    assertThat(updated.keywords().get(0).id()).isEqualTo(keywordId);
  }

  @Test
  void 존재하지_않는_키워드_수정_시_예외가_발생한다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, new FeedbackCreateRequest(studentId, lessonId));

    assertThatThrownBy(() -> feedbackCommandService.updateKeyword(userId, created.id(), 999L,
        new FeedbackKeywordUpdateRequest("꼼꼼함")))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void 다른_선생님_피드백의_키워드_수정_시_예외가_발생한다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, new FeedbackCreateRequest(studentId, lessonId));
    FeedbackResponse withKeyword = feedbackCommandService.addKeyword(userId, created.id(),
        new FeedbackKeywordCreateRequest("성실함"));
    Long keywordId = withKeyword.keywords().get(0).id();

    assertThatThrownBy(() -> feedbackCommandService.updateKeyword(otherUserId, created.id(),
        keywordId, new FeedbackKeywordUpdateRequest("꼼꼼함")))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void AI_콘텐츠를_생성한다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, new FeedbackCreateRequest(studentId, lessonId));
    feedbackCommandService.addKeyword(userId, created.id(),
        new FeedbackKeywordCreateRequest("성실함"));
    given(feedbackAiService.generateFeedbackContent(any(), eq("홍길동"))).willReturn("AI가 생성한 피드백");

    FeedbackResponse result = feedbackCommandService.generateAiContent(userId, created.id());

    assertThat(result.aiContent()).isEqualTo("AI가 생성한 피드백");
  }

  @Test
  void 키워드_없이_AI_콘텐츠_생성_시_예외가_발생한다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, new FeedbackCreateRequest(studentId, lessonId));

    assertThatThrownBy(() -> feedbackCommandService.generateAiContent(userId, created.id()))
        .isInstanceOf(ResponseStatusException.class).hasMessageContaining("키워드");
  }

  @Test
  void 피드백에_좋아요를_누른다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, new FeedbackCreateRequest(studentId, lessonId));
    feedbackCommandService.update(userId, created.id(), new FeedbackUpdateRequest("AI 피드백 내용"));

    FeedbackResponse liked = feedbackCommandService.like(userId, created.id());

    assertThat(liked.liked()).isTrue();
  }

  @Test
  void AI_콘텐츠가_없으면_좋아요에_실패한다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, new FeedbackCreateRequest(studentId, lessonId));

    assertThatThrownBy(() -> feedbackCommandService.like(userId, created.id()))
        .isInstanceOf(ResponseStatusException.class).hasMessageContaining("AI 콘텐츠");
  }

  @Test
  void 같은_내용에_좋아요를_중복으로_누르면_실패한다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, new FeedbackCreateRequest(studentId, lessonId));
    feedbackCommandService.update(userId, created.id(), new FeedbackUpdateRequest("AI 피드백 내용"));
    feedbackCommandService.like(userId, created.id());

    assertThatThrownBy(() -> feedbackCommandService.like(userId, created.id()))
        .isInstanceOf(ResponseStatusException.class).hasMessageContaining("이미 좋아요");
  }

  @Test
  void 좋아요_후_수정하면_다시_좋아요할_수_있다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, new FeedbackCreateRequest(studentId, lessonId));
    feedbackCommandService.update(userId, created.id(), new FeedbackUpdateRequest("원본 내용"));
    feedbackCommandService.like(userId, created.id());

    FeedbackResponse updated =
        feedbackCommandService.update(userId, created.id(), new FeedbackUpdateRequest("수정된 내용"));
    assertThat(updated.liked()).isFalse();

    FeedbackResponse reLiked = feedbackCommandService.like(userId, created.id());
    assertThat(reLiked.liked()).isTrue();
  }

  @Test
  void 좋아요_후_AI_재생성하면_다시_좋아요할_수_있다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, new FeedbackCreateRequest(studentId, lessonId));
    feedbackCommandService.addKeyword(userId, created.id(),
        new FeedbackKeywordCreateRequest("성실함"));
    given(feedbackAiService.generateFeedbackContent(any(), eq("홍길동"))).willReturn("AI가 생성한 피드백");
    feedbackCommandService.generateAiContent(userId, created.id());
    feedbackCommandService.like(userId, created.id());

    given(feedbackAiService.generateFeedbackContent(any(), eq("홍길동"))).willReturn("새로 생성한 피드백");
    FeedbackResponse regenerated = feedbackCommandService.generateAiContent(userId, created.id());
    assertThat(regenerated.liked()).isFalse();

    FeedbackResponse reLiked = feedbackCommandService.like(userId, created.id());
    assertThat(reLiked.liked()).isTrue();
  }

  @Test
  void 좋아요_이력은_누적_저장된다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, new FeedbackCreateRequest(studentId, lessonId));
    feedbackCommandService.update(userId, created.id(), new FeedbackUpdateRequest("버전 1"));
    feedbackCommandService.like(userId, created.id());
    feedbackCommandService.update(userId, created.id(), new FeedbackUpdateRequest("버전 2"));
    feedbackCommandService.like(userId, created.id());

    Long likeCount = entityManager
        .createQuery("SELECT COUNT(fl) FROM FeedbackLike fl WHERE fl.feedbackId = :feedbackId",
            Long.class)
        .setParameter("feedbackId", created.id()).getSingleResult();
    assertThat(likeCount).isEqualTo(2);
  }

  @Test
  void 좋아요_시_스냅샷이_저장된다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, new FeedbackCreateRequest(studentId, lessonId));
    feedbackCommandService.addKeyword(userId, created.id(),
        new FeedbackKeywordCreateRequest("성실함"));
    feedbackCommandService.update(userId, created.id(), new FeedbackUpdateRequest("AI 피드백 내용"));
    feedbackCommandService.like(userId, created.id());

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
        feedbackCommandService.create(userId, new FeedbackCreateRequest(studentId, lessonId));
    feedbackCommandService.update(userId, created.id(), new FeedbackUpdateRequest("AI 피드백 내용"));
    feedbackCommandService.like(userId, created.id());

    feedbackCommandService.delete(userId, created.id());

    Long likeCount = entityManager.createQuery("SELECT COUNT(fl) FROM FeedbackLike fl", Long.class)
        .getSingleResult();
    assertThat(likeCount).isZero();
  }

  @Test
  void 수정_사항이_없으면_좋아요_상태가_유지된다() {
    FeedbackResponse created =
        feedbackCommandService.create(userId, new FeedbackCreateRequest(studentId, lessonId));
    feedbackCommandService.update(userId, created.id(), new FeedbackUpdateRequest("AI 피드백 내용"));
    feedbackCommandService.like(userId, created.id());

    FeedbackResponse fetched = feedbackQueryService.getOne(userId, created.id());

    assertThat(fetched.liked()).isTrue();
  }
}
