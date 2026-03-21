package com.teacher.agent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.teacher.agent.domain.FeedbackRepository;
import com.teacher.agent.domain.LessonRepository;
import com.teacher.agent.domain.RecurrenceType;
import com.teacher.agent.domain.Student;
import com.teacher.agent.domain.StudentRepository;
import com.teacher.agent.domain.Teacher;
import com.teacher.agent.domain.TeacherRepository;
import com.teacher.agent.domain.UpdateScope;
import com.teacher.agent.dto.LessonCreateRequest;
import com.teacher.agent.dto.LessonResponse;
import com.teacher.agent.dto.LessonUpdateRequest;
import com.teacher.agent.dto.RecurrenceCreateRequest;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({LessonQueryService.class, LessonCommandService.class, LessonFactory.class})
class LessonServiceTest {

  @Autowired
  private LessonQueryService lessonQueryService;

  @Autowired
  private LessonCommandService lessonCommandService;

  @Autowired
  private LessonRepository lessonRepository;

  @Autowired
  private TeacherRepository teacherRepository;

  @Autowired
  private FeedbackRepository feedbackRepository;

  @Autowired
  private StudentRepository studentRepository;

  private static final LocalDateTime START = LocalDateTime.of(2026, 3, 16, 9, 0);
  private static final LocalDateTime END = LocalDateTime.of(2026, 3, 16, 10, 0);

  private Teacher teacher;

  @BeforeEach
  void setUp() {
    teacher =
        teacherRepository.save(Teacher.create("testteacher", "encodedPassword", "테스트교사", null));
  }

  @AfterEach
  void tearDown() {
    feedbackRepository.deleteAllInBatch();
    lessonRepository.deleteAllInBatch();
    studentRepository.deleteAllInBatch();
    teacherRepository.deleteAllInBatch();
  }

  @Test
  void 수업을_생성한다() {
    LessonResponse response = lessonCommandService.create(teacher.getUserId(),
        new LessonCreateRequest("수학 1교시", START, END, null, null));

    assertThat(response.id()).isNotNull();
    assertThat(response.userId()).isEqualTo(teacher.getUserId().value());
    assertThat(response.title()).isEqualTo("수학 1교시");
    assertThat(response.startTime()).isEqualTo(START);
    assertThat(response.endTime()).isEqualTo(END);
    assertThat(response.createdAt()).isNotNull();
    assertThat(response.updatedAt()).isNotNull();
  }

  @Test
  void 수업을_단건_조회한다() {
    LessonResponse created =
        lessonCommandService.create(teacher.getUserId(),
            new LessonCreateRequest("수학", START, END, null, null));

    LessonResponse found = lessonQueryService.getOne(teacher.getUserId(), created.id());

    assertThat(found.id()).isEqualTo(created.id());
    assertThat(found.title()).isEqualTo("수학");
  }

  @Test
  void 존재하지_않는_수업_조회_시_예외가_발생한다() {
    assertThatThrownBy(() -> lessonQueryService.getOne(teacher.getUserId(), 999L))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void 수업_정보를_수정한다() {
    LessonResponse created =
        lessonCommandService.create(teacher.getUserId(),
            new LessonCreateRequest("수학", START, END, null, null));
    LocalDateTime newStart = LocalDateTime.of(2026, 3, 17, 14, 0);
    LocalDateTime newEnd = LocalDateTime.of(2026, 3, 17, 15, 0);

    LessonResponse updated = lessonCommandService.update(teacher.getUserId(), created.id(),
        new LessonUpdateRequest("영어 2교시", newStart, newEnd));

    assertThat(updated.id()).isEqualTo(created.id());
    assertThat(updated.title()).isEqualTo("영어 2교시");
    assertThat(updated.startTime()).isEqualTo(newStart);
    assertThat(updated.endTime()).isEqualTo(newEnd);
  }

  @Test
  void 존재하지_않는_수업_수정_시_예외가_발생한다() {
    assertThatThrownBy(() -> lessonCommandService.update(teacher.getUserId(), 999L,
        new LessonUpdateRequest("수학", START, END))).isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void 수업을_삭제한다() {
    LessonResponse created =
        lessonCommandService.create(teacher.getUserId(),
            new LessonCreateRequest("수학", START, END, null, null));

    lessonCommandService.delete(teacher.getUserId(), created.id(), UpdateScope.SINGLE);

    assertThatThrownBy(() -> lessonQueryService.getOne(teacher.getUserId(), created.id()))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void 존재하지_않는_수업_삭제_시_예외가_발생한다() {
    assertThatThrownBy(() -> lessonCommandService.delete(teacher.getUserId(), 999L,
        UpdateScope.SINGLE))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void 다른_교사의_수업_수정_시_예외가_발생한다() {
    Teacher otherTeacher =
        teacherRepository.save(Teacher.create("otherteacher2", "encodedPassword", "다른교사2", null));
    LessonResponse created =
        lessonCommandService.create(teacher.getUserId(),
            new LessonCreateRequest("수학", START, END, null, null));

    assertThatThrownBy(() -> lessonCommandService.update(otherTeacher.getUserId(), created.id(),
        new LessonUpdateRequest("영어", START, END))).isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void 다른_교사의_수업_삭제_시_예외가_발생한다() {
    Teacher otherTeacher =
        teacherRepository.save(Teacher.create("otherteacher3", "encodedPassword", "다른교사3", null));
    LessonResponse created =
        lessonCommandService.create(teacher.getUserId(),
            new LessonCreateRequest("수학", START, END, null, null));

    assertThatThrownBy(() -> lessonCommandService.delete(otherTeacher.getUserId(), created.id(),
        UpdateScope.SINGLE))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void 주간_수업_목록을_조회한다() {
    lessonCommandService.create(teacher.getUserId(),
        new LessonCreateRequest("수학", START, END, null, null));
    LocalDateTime nextWeekStart = LocalDateTime.of(2026, 3, 23, 9, 0);
    LocalDateTime nextWeekEnd = LocalDateTime.of(2026, 3, 23, 10, 0);
    lessonCommandService.create(teacher.getUserId(),
        new LessonCreateRequest("영어", nextWeekStart, nextWeekEnd, null, null));

    List<LessonResponse> lessons =
        lessonQueryService.getByTeacherAndWeek(teacher.getUserId(), LocalDate.of(2026, 3, 16));

    assertThat(lessons).hasSize(1);
    assertThat(lessons.get(0).title()).isEqualTo("수학");
  }

  @Test
  void 해당_주에_수업이_없으면_빈_목록을_반환한다() {
    lessonCommandService.create(teacher.getUserId(),
        new LessonCreateRequest("수학", START, END, null, null));

    List<LessonResponse> lessons =
        lessonQueryService.getByTeacherAndWeek(teacher.getUserId(), LocalDate.of(2026, 3, 23));

    assertThat(lessons).isEmpty();
  }

  private static final LocalDate RECURRENCE_END = LocalDate.of(2026, 4, 6);

  private LessonResponse createWeeklyRecurringLessons() {
    RecurrenceCreateRequest recurrence = new RecurrenceCreateRequest(
        RecurrenceType.WEEKLY, 1, List.of(DayOfWeek.MONDAY), RECURRENCE_END);
    return lessonCommandService.create(teacher.getUserId(),
        new LessonCreateRequest("반복수학", START, END, recurrence, null));
  }

  private LessonResponse createWeeklyRecurringLessonsWithStudents(List<Long> studentIds) {
    RecurrenceCreateRequest recurrence = new RecurrenceCreateRequest(
        RecurrenceType.WEEKLY, 1, List.of(DayOfWeek.MONDAY), RECURRENCE_END);
    return lessonCommandService.create(teacher.getUserId(),
        new LessonCreateRequest("반복수학", START, END, recurrence, studentIds));
  }

  @Nested
  class 반복_수업_일괄_수정 {

    @Test
    void SINGLE_스코프로_수정하면_해당_수업만_변경된다() {
      // given
      createWeeklyRecurringLessons();
      List<LessonResponse> all = lessonQueryService.getByTeacherAndWeek(
          teacher.getUserId(), LocalDate.of(2026, 3, 16));
      assertThat(all).hasSize(1);
      Long targetId = all.get(0).id();

      // when
      lessonCommandService.update(teacher.getUserId(), targetId,
          new LessonUpdateRequest("변경된수학", START, END, UpdateScope.SINGLE));

      // then
      LessonResponse updated = lessonQueryService.getOne(teacher.getUserId(), targetId);
      assertThat(updated.title()).isEqualTo("변경된수학");

      List<LessonResponse> nextWeek = lessonQueryService.getByTeacherAndWeek(
          teacher.getUserId(), LocalDate.of(2026, 3, 23));
      assertThat(nextWeek).hasSize(1);
      assertThat(nextWeek.get(0).title()).isEqualTo("반복수학");
    }

    @Test
    void ALL_스코프로_수정하면_시리즈_전체가_변경된다() {
      // given
      createWeeklyRecurringLessons();
      List<LessonResponse> week1 = lessonQueryService.getByTeacherAndWeek(
          teacher.getUserId(), LocalDate.of(2026, 3, 16));
      Long targetId = week1.get(0).id();
      LocalDateTime newStart = LocalDateTime.of(2026, 3, 16, 14, 0);
      LocalDateTime newEnd = LocalDateTime.of(2026, 3, 16, 15, 0);

      // when
      lessonCommandService.update(teacher.getUserId(), targetId,
          new LessonUpdateRequest("전체변경", newStart, newEnd, UpdateScope.ALL));

      // then
      List<LessonResponse> allLessons = lessonRepository.findAllByUserId(teacher.getUserId())
          .stream().map(LessonResponse::from).toList();
      assertThat(allLessons).allSatisfy(lesson -> {
        assertThat(lesson.title()).isEqualTo("전체변경");
        assertThat(lesson.startTime().toLocalTime()).isEqualTo(newStart.toLocalTime());
      });
    }

    @Test
    void THIS_AND_FOLLOWING_스코프로_수정하면_해당_수업_이후만_변경된다() {
      // given
      createWeeklyRecurringLessons();
      List<LessonResponse> allLessons = lessonRepository.findAllByUserId(teacher.getUserId())
          .stream().map(LessonResponse::from)
          .sorted((a, b) -> a.startTime().compareTo(b.startTime()))
          .toList();
      assertThat(allLessons).hasSize(4);
      Long secondId = allLessons.get(1).id();
      LocalDateTime newStart = LocalDateTime.of(2026, 3, 23, 14, 0);
      LocalDateTime newEnd = LocalDateTime.of(2026, 3, 23, 15, 0);

      // when
      lessonCommandService.update(teacher.getUserId(), secondId,
          new LessonUpdateRequest("이후변경", newStart, newEnd, UpdateScope.THIS_AND_FOLLOWING));

      // then
      LessonResponse first = lessonQueryService.getOne(teacher.getUserId(), allLessons.get(0).id());
      assertThat(first.title()).isEqualTo("반복수학");

      LessonResponse second = lessonQueryService.getOne(teacher.getUserId(), secondId);
      assertThat(second.title()).isEqualTo("이후변경");

      LessonResponse third = lessonQueryService.getOne(teacher.getUserId(), allLessons.get(2).id());
      assertThat(third.title()).isEqualTo("이후변경");
    }

    @Test
    void 반복이_아닌_수업을_ALL로_수정하면_해당_수업만_변경된다() {
      // given
      LessonResponse created = lessonCommandService.create(teacher.getUserId(),
          new LessonCreateRequest("단일수학", START, END, null, null));

      // when
      lessonCommandService.update(teacher.getUserId(), created.id(),
          new LessonUpdateRequest("변경됨", START, END, UpdateScope.ALL));

      // then
      LessonResponse updated = lessonQueryService.getOne(teacher.getUserId(), created.id());
      assertThat(updated.title()).isEqualTo("변경됨");
    }
  }

  @Nested
  class 반복_수업_일괄_삭제 {

    @Test
    void SINGLE_스코프로_삭제하면_해당_수업만_삭제된다() {
      // given
      createWeeklyRecurringLessons();
      List<LessonResponse> allLessons = lessonRepository.findAllByUserId(teacher.getUserId())
          .stream().map(LessonResponse::from)
          .sorted((a, b) -> a.startTime().compareTo(b.startTime()))
          .toList();
      assertThat(allLessons).hasSize(4);

      // when
      lessonCommandService.delete(teacher.getUserId(), allLessons.get(0).id(), UpdateScope.SINGLE);

      // then
      assertThat(lessonRepository.findAllByUserId(teacher.getUserId())).hasSize(3);
    }

    @Test
    void ALL_스코프로_삭제하면_시리즈_전체가_삭제된다() {
      // given
      createWeeklyRecurringLessons();
      List<LessonResponse> allLessons = lessonRepository.findAllByUserId(teacher.getUserId())
          .stream().map(LessonResponse::from).toList();
      assertThat(allLessons).hasSize(4);

      // when
      lessonCommandService.delete(teacher.getUserId(), allLessons.get(0).id(), UpdateScope.ALL);

      // then
      assertThat(lessonRepository.findAllByUserId(teacher.getUserId())).isEmpty();
    }

    @Test
    void THIS_AND_FOLLOWING_스코프로_삭제하면_해당_수업_이후만_삭제된다() {
      // given
      createWeeklyRecurringLessons();
      List<LessonResponse> allLessons = lessonRepository.findAllByUserId(teacher.getUserId())
          .stream().map(LessonResponse::from)
          .sorted((a, b) -> a.startTime().compareTo(b.startTime()))
          .toList();
      assertThat(allLessons).hasSize(4);

      // when
      lessonCommandService.delete(teacher.getUserId(), allLessons.get(1).id(),
          UpdateScope.THIS_AND_FOLLOWING);

      // then
      List<LessonResponse> remaining = lessonRepository.findAllByUserId(teacher.getUserId())
          .stream().map(LessonResponse::from).toList();
      assertThat(remaining).hasSize(1);
      assertThat(remaining.get(0).id()).isEqualTo(allLessons.get(0).id());
    }

    @Test
    void 삭제_시_aiContent가_없는_Feedback만_삭제된다() {
      // given
      Student student = studentRepository.save(
          Student.create(teacher.getUserId(), "학생1", null));
      createWeeklyRecurringLessonsWithStudents(List.of(student.getId()));
      List<LessonResponse> allLessons = lessonRepository.findAllByUserId(teacher.getUserId())
          .stream().map(LessonResponse::from).toList();
      assertThat(allLessons).hasSize(4);
      assertThat(feedbackRepository.count()).isEqualTo(4);

      // when
      lessonCommandService.delete(teacher.getUserId(), allLessons.get(0).id(), UpdateScope.ALL);

      // then
      assertThat(lessonRepository.findAllByUserId(teacher.getUserId())).isEmpty();
      assertThat(feedbackRepository.count()).isZero();
    }

    @Test
    void 삭제_시_aiContent가_있는_Feedback은_보존된다() {
      // given
      Student student = studentRepository.save(
          Student.create(teacher.getUserId(), "학생1", null));
      createWeeklyRecurringLessonsWithStudents(List.of(student.getId()));
      List<LessonResponse> allLessons = lessonRepository.findAllByUserId(teacher.getUserId())
          .stream().map(LessonResponse::from).toList();
      assertThat(allLessons).hasSize(4);

      var feedbacks = feedbackRepository.findAll();
      feedbacks.get(0).updateAiContent("AI 피드백 내용");
      feedbackRepository.save(feedbacks.get(0));

      // when
      lessonCommandService.delete(teacher.getUserId(), allLessons.get(0).id(), UpdateScope.ALL);

      // then
      assertThat(lessonRepository.findAllByUserId(teacher.getUserId())).isEmpty();
      assertThat(feedbackRepository.count()).isEqualTo(1);
      assertThat(feedbackRepository.findAll().get(0).getAiContent()).isEqualTo("AI 피드백 내용");
    }

    @Test
    void 반복이_아닌_수업을_ALL로_삭제하면_해당_수업만_삭제된다() {
      // given
      LessonResponse created = lessonCommandService.create(teacher.getUserId(),
          new LessonCreateRequest("단일수학", START, END, null, null));

      // when
      lessonCommandService.delete(teacher.getUserId(), created.id(), UpdateScope.ALL);

      // then
      assertThatThrownBy(() -> lessonQueryService.getOne(teacher.getUserId(), created.id()))
          .isInstanceOf(ResponseStatusException.class);
    }
  }

}
