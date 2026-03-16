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
    private EntityManager entityManager;

    @MockitoBean
    private FeedbackAiService feedbackAiService;

    private Long studentId;

    @BeforeEach
    void setUp() {
        Student student = studentRepository.save(Student.create("홍길동", "메모"));
        studentId = student.getId();
    }

    @AfterEach
    void tearDown() {
        feedbackRepository.deleteAll();
        studentRepository.deleteAllInBatch();
    }

    @Test
    void 피드백을_생성한다() {
        FeedbackResponse response = feedbackService.create(new FeedbackCreateRequest(studentId));

        assertThat(response.id()).isNotNull();
        assertThat(response.studentId()).isEqualTo(studentId);
        assertThat(response.keywords()).isEmpty();
        assertThat(response.aiContent()).isNull();
        assertThat(response.liked()).isFalse();
    }

    @Test
    void 같은_학생에_대해_중복_생성하면_기존_피드백을_반환한다() {
        FeedbackResponse first = feedbackService.create(new FeedbackCreateRequest(studentId));
        FeedbackResponse second = feedbackService.create(new FeedbackCreateRequest(studentId));

        assertThat(second.id()).isEqualTo(first.id());
    }

    @Test
    void 존재하지_않는_학생으로_피드백_생성_시_예외가_발생한다() {
        assertThatThrownBy(() -> feedbackService.create(new FeedbackCreateRequest(999L)))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void 학생_ID로_피드백_목록을_조회한다() {
        feedbackService.create(new FeedbackCreateRequest(studentId));

        var feedbacks = feedbackService.getAll(studentId);

        assertThat(feedbacks).hasSize(1);
        assertThat(feedbacks.get(0).studentId()).isEqualTo(studentId);
    }

    @Test
    void 존재하지_않는_학생의_피드백_목록_조회_시_예외가_발생한다() {
        assertThatThrownBy(() -> feedbackService.getAll(999L))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void 피드백을_단건_조회한다() {
        FeedbackResponse created = feedbackService.create(new FeedbackCreateRequest(studentId));

        FeedbackResponse found = feedbackService.getOne(created.id());

        assertThat(found.id()).isEqualTo(created.id());
    }

    @Test
    void 존재하지_않는_피드백_조회_시_예외가_발생한다() {
        assertThatThrownBy(() -> feedbackService.getOne(999L))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void 피드백의_AI_콘텐츠를_수정한다() {
        FeedbackResponse created = feedbackService.create(new FeedbackCreateRequest(studentId));

        FeedbackResponse updated = feedbackService.update(created.id(), new FeedbackUpdateRequest("수정된 AI 콘텐츠"));

        assertThat(updated.aiContent()).isEqualTo("수정된 AI 콘텐츠");
    }

    @Test
    void 빈_문자열로_수정하면_AI_콘텐츠가_초기화된다() {
        FeedbackResponse created = feedbackService.create(new FeedbackCreateRequest(studentId));
        feedbackService.update(created.id(), new FeedbackUpdateRequest("AI 콘텐츠"));

        FeedbackResponse updated = feedbackService.update(created.id(), new FeedbackUpdateRequest(""));

        assertThat(updated.aiContent()).isNull();
    }

    @Test
    void null로_수정하면_AI_콘텐츠가_초기화된다() {
        FeedbackResponse created = feedbackService.create(new FeedbackCreateRequest(studentId));
        feedbackService.update(created.id(), new FeedbackUpdateRequest("AI 콘텐츠"));

        FeedbackResponse updated = feedbackService.update(created.id(), new FeedbackUpdateRequest(null));

        assertThat(updated.aiContent()).isNull();
    }

    @Test
    void 피드백을_삭제한다() {
        FeedbackResponse created = feedbackService.create(new FeedbackCreateRequest(studentId));

        feedbackService.delete(created.id());

        assertThatThrownBy(() -> feedbackService.getOne(created.id()))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void 존재하지_않는_피드백_삭제_시_예외가_발생한다() {
        assertThatThrownBy(() -> feedbackService.delete(999L))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void 키워드를_추가한다() {
        FeedbackResponse created = feedbackService.create(new FeedbackCreateRequest(studentId));

        FeedbackResponse updated = feedbackService.addKeyword(created.id(), new FeedbackKeywordCreateRequest("성실함"));

        assertThat(updated.keywords()).hasSize(1);
        assertThat(updated.keywords().get(0).keyword()).isEqualTo("성실함");
        assertThat(updated.keywords().get(0).id()).isNotNull();
    }

    @Test
    void 존재하지_않는_피드백에_키워드_추가_시_예외가_발생한다() {
        assertThatThrownBy(() -> feedbackService.addKeyword(999L, new FeedbackKeywordCreateRequest("키워드")))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void 키워드를_삭제한다() {
        FeedbackResponse created = feedbackService.create(new FeedbackCreateRequest(studentId));
        FeedbackResponse withKeyword = feedbackService.addKeyword(created.id(), new FeedbackKeywordCreateRequest("성실함"));
        Long keywordId = withKeyword.keywords().get(0).id();

        feedbackService.removeKeyword(created.id(), keywordId);

        FeedbackResponse afterRemoval = feedbackService.getOne(created.id());
        assertThat(afterRemoval.keywords()).isEmpty();
    }

    @Test
    void 존재하지_않는_키워드_삭제_시_예외가_발생한다() {
        FeedbackResponse created = feedbackService.create(new FeedbackCreateRequest(studentId));

        assertThatThrownBy(() -> feedbackService.removeKeyword(created.id(), 999L))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void AI_콘텐츠를_생성한다() {
        FeedbackResponse created = feedbackService.create(new FeedbackCreateRequest(studentId));
        feedbackService.addKeyword(created.id(), new FeedbackKeywordCreateRequest("성실함"));
        given(feedbackAiService.generateFeedbackContent(any(), eq("홍길동")))
                .willReturn("AI가 생성한 피드백");

        FeedbackResponse result = feedbackService.generateAiContent(created.id());

        assertThat(result.aiContent()).isEqualTo("AI가 생성한 피드백");
    }

    @Test
    void 키워드_없이_AI_콘텐츠_생성_시_예외가_발생한다() {
        FeedbackResponse created = feedbackService.create(new FeedbackCreateRequest(studentId));

        assertThatThrownBy(() -> feedbackService.generateAiContent(created.id()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("키워드");
    }

    @Test
    void 피드백에_좋아요를_누른다() {
        FeedbackResponse created = feedbackService.create(new FeedbackCreateRequest(studentId));
        feedbackService.update(created.id(), new FeedbackUpdateRequest("AI 피드백 내용"));

        FeedbackResponse liked = feedbackService.like(created.id());

        assertThat(liked.liked()).isTrue();
    }

    @Test
    void AI_콘텐츠가_없으면_좋아요에_실패한다() {
        FeedbackResponse created = feedbackService.create(new FeedbackCreateRequest(studentId));

        assertThatThrownBy(() -> feedbackService.like(created.id()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("AI 콘텐츠");
    }

    @Test
    void 같은_내용에_좋아요를_중복으로_누르면_실패한다() {
        FeedbackResponse created = feedbackService.create(new FeedbackCreateRequest(studentId));
        feedbackService.update(created.id(), new FeedbackUpdateRequest("AI 피드백 내용"));
        feedbackService.like(created.id());

        assertThatThrownBy(() -> feedbackService.like(created.id()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("이미 좋아요");
    }

    @Test
    void 좋아요_후_수정하면_다시_좋아요할_수_있다() {
        FeedbackResponse created = feedbackService.create(new FeedbackCreateRequest(studentId));
        feedbackService.update(created.id(), new FeedbackUpdateRequest("원본 내용"));
        feedbackService.like(created.id());

        FeedbackResponse updated = feedbackService.update(created.id(), new FeedbackUpdateRequest("수정된 내용"));
        assertThat(updated.liked()).isFalse();

        FeedbackResponse reLiked = feedbackService.like(created.id());
        assertThat(reLiked.liked()).isTrue();
    }

    @Test
    void 좋아요_후_AI_재생성하면_다시_좋아요할_수_있다() {
        FeedbackResponse created = feedbackService.create(new FeedbackCreateRequest(studentId));
        feedbackService.addKeyword(created.id(), new FeedbackKeywordCreateRequest("성실함"));
        given(feedbackAiService.generateFeedbackContent(any(), eq("홍길동")))
                .willReturn("AI가 생성한 피드백");
        feedbackService.generateAiContent(created.id());
        feedbackService.like(created.id());

        given(feedbackAiService.generateFeedbackContent(any(), eq("홍길동")))
                .willReturn("새로 생성한 피드백");
        FeedbackResponse regenerated = feedbackService.generateAiContent(created.id());
        assertThat(regenerated.liked()).isFalse();

        FeedbackResponse reLiked = feedbackService.like(created.id());
        assertThat(reLiked.liked()).isTrue();
    }

    @Test
    @Transactional
    void 좋아요_이력은_누적_저장된다() {
        FeedbackResponse created = feedbackService.create(new FeedbackCreateRequest(studentId));
        feedbackService.update(created.id(), new FeedbackUpdateRequest("버전 1"));
        feedbackService.like(created.id());
        feedbackService.update(created.id(), new FeedbackUpdateRequest("버전 2"));
        feedbackService.like(created.id());

        Feedback feedback = feedbackRepository.findById(created.id()).orElseThrow();
        assertThat(feedback.getLikes()).hasSize(2);
    }

    @Test
    @Transactional
    void 좋아요_시_스냅샷이_저장된다() {
        FeedbackResponse created = feedbackService.create(new FeedbackCreateRequest(studentId));
        feedbackService.addKeyword(created.id(), new FeedbackKeywordCreateRequest("성실함"));
        feedbackService.update(created.id(), new FeedbackUpdateRequest("AI 피드백 내용"));
        feedbackService.like(created.id());

        Feedback feedback = feedbackRepository.findById(created.id()).orElseThrow();
        FeedbackLike feedbackLike = feedback.getLikes().get(0);
        assertThat(feedbackLike.getAiContentSnapshot()).isEqualTo("AI 피드백 내용");
        assertThat(feedbackLike.getKeywordsSnapshot()).isEqualTo("성실함");
    }

    @Test
    void 피드백_삭제_시_좋아요_이력도_삭제된다() {
        FeedbackResponse created = feedbackService.create(new FeedbackCreateRequest(studentId));
        feedbackService.update(created.id(), new FeedbackUpdateRequest("AI 피드백 내용"));
        feedbackService.like(created.id());

        feedbackService.delete(created.id());

        Long likeCount = entityManager.createQuery("SELECT COUNT(fl) FROM FeedbackLike fl", Long.class)
                .getSingleResult();
        assertThat(likeCount).isZero();
    }

    @Test
    void 수정_사항이_없으면_좋아요_상태가_유지된다() {
        FeedbackResponse created = feedbackService.create(new FeedbackCreateRequest(studentId));
        feedbackService.update(created.id(), new FeedbackUpdateRequest("AI 피드백 내용"));
        feedbackService.like(created.id());

        FeedbackResponse fetched = feedbackService.getOne(created.id());

        assertThat(fetched.liked()).isTrue();
    }
}
