package com.teacher.agent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.teacher.agent.domain.LessonRepository;
import com.teacher.agent.domain.Teacher;
import com.teacher.agent.domain.TeacherRepository;
import com.teacher.agent.dto.LessonCreateRequest;
import com.teacher.agent.dto.LessonResponse;
import com.teacher.agent.dto.LessonUpdateRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
    lessonRepository.deleteAllInBatch();
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

    lessonCommandService.delete(teacher.getUserId(), created.id());

    assertThatThrownBy(() -> lessonQueryService.getOne(teacher.getUserId(), created.id()))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void 존재하지_않는_수업_삭제_시_예외가_발생한다() {
    assertThatThrownBy(() -> lessonCommandService.delete(teacher.getUserId(), 999L))
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

    assertThatThrownBy(() -> lessonCommandService.delete(otherTeacher.getUserId(), created.id()))
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

}
