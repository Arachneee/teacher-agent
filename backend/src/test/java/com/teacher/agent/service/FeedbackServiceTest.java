package com.teacher.agent.service;

import com.teacher.agent.domain.*;
import com.teacher.agent.dto.*;
import jakarta.persistence.EntityManager;
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

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import(FeedbackService.class)
class FeedbackServiceTest {

    @Autowired
    private FeedbackService feedbackService;

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
        Teacher otherTeacher = teacherRepository.save(Teacher.create("teacher2", "encoded", "선생님2", "영어"));
        userId = teacher.getUserId();
        otherUserId = otherTeacher.getUserId();
        Student student = studentRepository.save(Student.create(teacher.getId(), "홍길동", "메모"));
        studentId = student.getId();
        Lesson lesson = lessonRepository.save(Lesson.create(teacher.getId(), "수학", START, END));
        lesson.addAttendee(studentId);
        lesson = lessonRepository.save(lesson);
        lessonId = lesson.getId();
    }

    @AfterEach
    void tearDown() {
        feedbackRepository.deleteAll();
        lessonRepository.deleteAll();
        studentRepository.deleteAllInBatch();
        teacherRepository.deleteAllInBatch();
    }

    @Test
    void 피드백을_생성한다() {
        FeedbackResponse response = feedbackService.create(userId, new FeedbackCreateRequest(studentId, lessonId));

        assertThat(response.id()).isNotNull();
        assertThat(response.studentId()).isEqualTo(studentId);
        assertThat(response.keywords()).isEmpty();
        assertThat(response.aiContent()).isNull();
        assertThat(response.liked()).isFalse();
    }

    @Test
    void 같은_학생에_대해_여러_피드백을_생성할_수_있다() {
        FeedbackResponse first = feedbackService.create(userId, new FeedbackCreateRequest(studentId, lessonId));
        FeedbackResponse second = feedbackService.create(userId, new FeedbackCreateRequest(studentId, lessonId));

        assertThat(second.id()).isNotEqualTo(first.id());
        assertThat(feedbackService.getAll(userId, studentId)).hasSize(2);
    }

    @Test
    void 다른_선생님_학생으로_피드백_생성_시_예외가_발생한다() {
        assertThatThrownBy(() -> feedbackService.create(otherUserId, new FeedbackCreateRequest(studentId, lessonId)))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void 존재하지_않는_학생으로_피드백_생성_시_예외가_발생한다() {
        assertThatThrownBy(() -> feedbackService.create(userId, new FeedbackCreateRequest(999L, lessonId)))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void 수업에_등록되지_않은_학생으로_피드백_생성_시_예외가_발생한다() {
        Teacher teacher = teacherRepository.findByUserId(userId).orElseThrow();
        Student unenrolledStudent = studentRepository.save(Student.create(teacher.getId(), "미등록학생", null));

        assertThatThrownBy(() -> feedbackService.create(userId, new FeedbackCreateRequest(unenrolledStudent.getId(), lessonId)))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void 학생_ID로_피드백_목록을_조회한다() {
        feedbackService.create(userId, new FeedbackCreateRequest(studentId, lessonId));

        var feedbacks = feedbackService.getAll(userId, studentId);

        assertThat(feedbacks).hasSize(1);
        assertThat(feedbacks.get(0).studentId()).isEqualTo(studentId);
    }

    @Test
    void 다른_선생님_학생의_피드백_목록_조회_시_예외가_발생한다() {
        assertThatThrownBy(() -> feedbackService.getAll(otherUserId, studentId))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void 피드백을_단건_조회한다() {
        FeedbackResponse created = feedbackService.create(userId, new FeedbackCreateRequest(studentId, lessonId));

        FeedbackResponse found = feedbackService.getOne(userId, created.id());

        assertThat(found.id()).isEqualTo(created.id());
    }

    @Test
    void 다른_선생님의_피드백_단건_조회_시_예외가_발생한다() {
        FeedbackResponse created = feedbackService.create(userId, new FeedbackCreateRequest(studentId, lessonId));

        assertThatThrownBy(() -> feedbackService.getOne(otherUserId, created.id()))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void 존재하지_않는_피드백_조회_시_예외가_발생한다() {
        assertThatThrownBy(() -> feedbackService.getOne(userId, 999L))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void 피드백의_AI_콘텐츠를_수정한다() {
        FeedbackResponse created = feedbackService.create(userId, new FeedbackCreateRequest(studentId, lessonId));

        FeedbackResponse updated = feedbackService.update(userId, created.id(), new FeedbackUpdateRequest("수정된 AI 콘텐츠"));

        assertThat(updated.aiContent()).isEqualTo("수정된 AI 콘텐츠");
    }

    @Test
    void 빈_문자열로_수정하면_AI_콘텐츠가_초기화된다() {
        FeedbackResponse created = feedbackService.create(userId, new FeedbackCreateRequest(studentId, lessonId));
        feedbackService.update(userId, created.id(), new FeedbackUpdateRequest("AI 콘텐츠"));

        FeedbackResponse updated = feedbackService.update(userId, created.id(), new FeedbackUpdateRequest(""));

        assertThat(updated.aiContent()).isNull();
    }

    @Test
    void null로_수정하면_AI_콘텐츠가_초기화된다() {
        FeedbackResponse created = feedbackService.create(userId, new FeedbackCreateRequest(studentId, lessonId));
        feedbackService.update(userId, created.id(), new FeedbackUpdateRequest("AI 콘텐츠"));

        FeedbackResponse updated = feedbackService.update(userId, created.id(), new FeedbackUpdateRequest(null));

        assertThat(updated.aiContent()).isNull();
    }

    @Test
    void 피드백을_삭제한다() {
        FeedbackResponse created = feedbackService.create(userId, new FeedbackCreateRequest(studentId, lessonId));

        feedbackService.delete(userId, created.id());

        assertThatThrownBy(() -> feedbackService.getOne(userId, created.id()))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void 다른_선생님의_피드백_삭제_시_예외가_발생한다() {
        FeedbackResponse created = feedbackService.create(userId, new FeedbackCreateRequest(studentId, lessonId));

        assertThatThrownBy(() -> feedbackService.delete(otherUserId, created.id()))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void 키워드를_추가한다() {
        FeedbackResponse created = feedbackService.create(userId, new FeedbackCreateRequest(studentId, lessonId));

        FeedbackResponse updated = feedbackService.addKeyword(userId, created.id(), new FeedbackKeywordCreateRequest("성실함"));

        assertThat(updated.keywords()).hasSize(1);
        assertThat(updated.keywords().get(0).keyword()).isEqualTo("성실함");
        assertThat(updated.keywords().get(0).id()).isNotNull();
    }

    @Test
    void 키워드를_삭제한다() {
        FeedbackResponse created = feedbackService.create(userId, new FeedbackCreateRequest(studentId, lessonId));
        FeedbackResponse withKeyword = feedbackService.addKeyword(userId, created.id(), new FeedbackKeywordCreateRequest("성실함"));
        Long keywordId = withKeyword.keywords().get(0).id();

        feedbackService.removeKeyword(userId, created.id(), keywordId);

        FeedbackResponse afterRemoval = feedbackService.getOne(userId, created.id());
        assertThat(afterRemoval.keywords()).isEmpty();
    }

    @Test
    void 존재하지_않는_키워드_삭제_시_예외가_발생한다() {
        FeedbackResponse created = feedbackService.create(userId, new FeedbackCreateRequest(studentId, lessonId));

        assertThatThrownBy(() -> feedbackService.removeKeyword(userId, created.id(), 999L))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void AI_콘텐츠를_생성한다() {
        FeedbackResponse created = feedbackService.create(userId, new FeedbackCreateRequest(studentId, lessonId));
        feedbackService.addKeyword(userId, created.id(), new FeedbackKeywordCreateRequest("성실함"));
        given(feedbackAiService.generateFeedbackContent(any(), eq("홍길동")))
                .willReturn("AI가 생성한 피드백");

        FeedbackResponse result = feedbackService.generateAiContent(userId, created.id());

        assertThat(result.aiContent()).isEqualTo("AI가 생성한 피드백");
    }

    @Test
    void 키워드_없이_AI_콘텐츠_생성_시_예외가_발생한다() {
        FeedbackResponse created = feedbackService.create(userId, new FeedbackCreateRequest(studentId, lessonId));

        assertThatThrownBy(() -> feedbackService.generateAiContent(userId, created.id()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("키워드");
    }

    @Test
    void 피드백에_좋아요를_누른다() {
        FeedbackResponse created = feedbackService.create(userId, new FeedbackCreateRequest(studentId, lessonId));
        feedbackService.update(userId, created.id(), new FeedbackUpdateRequest("AI 피드백 내용"));

        FeedbackResponse liked = feedbackService.like(userId, created.id());

        assertThat(liked.liked()).isTrue();
    }

    @Test
    void AI_콘텐츠가_없으면_좋아요에_실패한다() {
        FeedbackResponse created = feedbackService.create(userId, new FeedbackCreateRequest(studentId, lessonId));

        assertThatThrownBy(() -> feedbackService.like(userId, created.id()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("AI 콘텐츠");
    }

    @Test
    void 같은_내용에_좋아요를_중복으로_누르면_실패한다() {
        FeedbackResponse created = feedbackService.create(userId, new FeedbackCreateRequest(studentId, lessonId));
        feedbackService.update(userId, created.id(), new FeedbackUpdateRequest("AI 피드백 내용"));
        feedbackService.like(userId, created.id());

        assertThatThrownBy(() -> feedbackService.like(userId, created.id()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("이미 좋아요");
    }

    @Test
    void 좋아요_후_수정하면_다시_좋아요할_수_있다() {
        FeedbackResponse created = feedbackService.create(userId, new FeedbackCreateRequest(studentId, lessonId));
        feedbackService.update(userId, created.id(), new FeedbackUpdateRequest("원본 내용"));
        feedbackService.like(userId, created.id());

        FeedbackResponse updated = feedbackService.update(userId, created.id(), new FeedbackUpdateRequest("수정된 내용"));
        assertThat(updated.liked()).isFalse();

        FeedbackResponse reLiked = feedbackService.like(userId, created.id());
        assertThat(reLiked.liked()).isTrue();
    }

    @Test
    void 좋아요_후_AI_재생성하면_다시_좋아요할_수_있다() {
        FeedbackResponse created = feedbackService.create(userId, new FeedbackCreateRequest(studentId, lessonId));
        feedbackService.addKeyword(userId, created.id(), new FeedbackKeywordCreateRequest("성실함"));
        given(feedbackAiService.generateFeedbackContent(any(), eq("홍길동")))
                .willReturn("AI가 생성한 피드백");
        feedbackService.generateAiContent(userId, created.id());
        feedbackService.like(userId, created.id());

        given(feedbackAiService.generateFeedbackContent(any(), eq("홍길동")))
                .willReturn("새로 생성한 피드백");
        FeedbackResponse regenerated = feedbackService.generateAiContent(userId, created.id());
        assertThat(regenerated.liked()).isFalse();

        FeedbackResponse reLiked = feedbackService.like(userId, created.id());
        assertThat(reLiked.liked()).isTrue();
    }

    @Test
    @Transactional
    void 좋아요_이력은_누적_저장된다() {
        FeedbackResponse created = feedbackService.create(userId, new FeedbackCreateRequest(studentId, lessonId));
        feedbackService.update(userId, created.id(), new FeedbackUpdateRequest("버전 1"));
        feedbackService.like(userId, created.id());
        feedbackService.update(userId, created.id(), new FeedbackUpdateRequest("버전 2"));
        feedbackService.like(userId, created.id());

        Feedback feedback = feedbackRepository.findById(created.id()).orElseThrow();
        assertThat(feedback.getLikes()).hasSize(2);
    }

    @Test
    @Transactional
    void 좋아요_시_스냅샷이_저장된다() {
        FeedbackResponse created = feedbackService.create(userId, new FeedbackCreateRequest(studentId, lessonId));
        feedbackService.addKeyword(userId, created.id(), new FeedbackKeywordCreateRequest("성실함"));
        feedbackService.update(userId, created.id(), new FeedbackUpdateRequest("AI 피드백 내용"));
        feedbackService.like(userId, created.id());

        Feedback feedback = feedbackRepository.findById(created.id()).orElseThrow();
        FeedbackLike feedbackLike = feedback.getLikes().get(0);
        assertThat(feedbackLike.getAiContentSnapshot()).isEqualTo("AI 피드백 내용");
        assertThat(feedbackLike.getKeywordsSnapshot()).isEqualTo("성실함");
    }

    @Test
    void 피드백_삭제_시_좋아요_이력도_삭제된다() {
        FeedbackResponse created = feedbackService.create(userId, new FeedbackCreateRequest(studentId, lessonId));
        feedbackService.update(userId, created.id(), new FeedbackUpdateRequest("AI 피드백 내용"));
        feedbackService.like(userId, created.id());

        feedbackService.delete(userId, created.id());

        Long likeCount = entityManager.createQuery("SELECT COUNT(fl) FROM FeedbackLike fl", Long.class)
                .getSingleResult();
        assertThat(likeCount).isZero();
    }

    @Test
    void 수정_사항이_없으면_좋아요_상태가_유지된다() {
        FeedbackResponse created = feedbackService.create(userId, new FeedbackCreateRequest(studentId, lessonId));
        feedbackService.update(userId, created.id(), new FeedbackUpdateRequest("AI 피드백 내용"));
        feedbackService.like(userId, created.id());

        FeedbackResponse fetched = feedbackService.getOne(userId, created.id());

        assertThat(fetched.liked()).isTrue();
    }
}
