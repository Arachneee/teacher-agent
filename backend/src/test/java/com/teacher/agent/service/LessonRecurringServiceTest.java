package com.teacher.agent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.teacher.agent.domain.LessonRepository;
import com.teacher.agent.domain.RecurrenceType;
import com.teacher.agent.domain.Teacher;
import com.teacher.agent.domain.TeacherRepository;
import com.teacher.agent.domain.Lesson;
import com.teacher.agent.dto.LessonCreateRequest;
import com.teacher.agent.dto.LessonUpdateRequest;
import com.teacher.agent.dto.RecurrenceCreateRequest;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class LessonRecurringServiceTest {

  @Autowired
  private LessonCommandService lessonCommandService;

  @Autowired
  private LessonRepository lessonRepository;

  @Autowired
  private TeacherRepository teacherRepository;

  private Teacher teacher;

  @BeforeEach
  void setUp() {
    teacher = teacherRepository.save(Teacher.create("testteacher", "password", "교사", null));
  }

  @AfterEach
  void tearDown() {
    lessonRepository.deleteAll();
    teacherRepository.deleteAll();
  }

  @Test
  void 매주_월화_수업_생성() {
    LocalDateTime start = LocalDateTime.of(2026, 3, 16, 9, 0);
    LocalDateTime end = LocalDateTime.of(2026, 3, 16, 10, 0);
    RecurrenceCreateRequest recurrence = new RecurrenceCreateRequest(
        RecurrenceType.WEEKLY, 1, List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
        LocalDate.of(2026, 4, 30));

    lessonCommandService.create(teacher.getUserId(),
        new LessonCreateRequest("수학", start, end, recurrence, null));

    assertThat(lessonRepository.findAll()).hasSize(14);
  }

  @Test
  void cannot_exceed_6_months() {
    LocalDateTime start = LocalDateTime.now();
    LocalDateTime end = start.plusHours(1);
    RecurrenceCreateRequest recurrence = new RecurrenceCreateRequest(
        RecurrenceType.DAILY, 1, null, start.toLocalDate().plusMonths(7));

    assertThatThrownBy(() -> lessonCommandService.create(teacher.getUserId(),
        new LessonCreateRequest("수학", start, end, recurrence, null)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 매일_수업_생성() {
    LocalDateTime start = LocalDateTime.of(2026, 3, 16, 9, 0);
    LocalDateTime end = LocalDateTime.of(2026, 3, 16, 10, 0);
    RecurrenceCreateRequest recurrence = new RecurrenceCreateRequest(
        RecurrenceType.DAILY, 1, null, LocalDate.of(2026, 3, 31));

    lessonCommandService.create(teacher.getUserId(),
        new LessonCreateRequest("수학", start, end, recurrence, null));

    assertThat(lessonRepository.findAll()).hasSize(16);
  }

  @Test
  void 격주_수업_생성() {
    LocalDateTime start = LocalDateTime.of(2026, 3, 16, 9, 0);
    LocalDateTime end = LocalDateTime.of(2026, 3, 16, 10, 0);
    RecurrenceCreateRequest recurrence = new RecurrenceCreateRequest(
        RecurrenceType.WEEKLY, 2, List.of(DayOfWeek.MONDAY),
        LocalDate.of(2026, 4, 30));

    lessonCommandService.create(teacher.getUserId(),
        new LessonCreateRequest("수학", start, end, recurrence, null));

    // 3/16, 3/30, 4/13, 4/27 = 4 lessons (every other Monday)
    assertThat(lessonRepository.findAll()).hasSize(4);
  }

  @Test
  void 매월_수업_생성() {
    LocalDateTime start = LocalDateTime.of(2026, 3, 16, 9, 0);
    LocalDateTime end = LocalDateTime.of(2026, 3, 16, 10, 0);
    RecurrenceCreateRequest recurrence = new RecurrenceCreateRequest(
        RecurrenceType.MONTHLY, 1, null, LocalDate.of(2026, 6, 30));

    lessonCommandService.create(teacher.getUserId(),
        new LessonCreateRequest("수학", start, end, recurrence, null));

    // 3/16, 4/16, 5/16, 6/16 = 4 lessons
    assertThat(lessonRepository.findAll()).hasSize(4);
  }

  @Test
  void 매주_반복_기간_내_해당_요일_없으면_실패() {
    LocalDateTime start = LocalDateTime.of(2026, 3, 16, 9, 0); // 월요일
    LocalDateTime end = LocalDateTime.of(2026, 3, 16, 10, 0);
    RecurrenceCreateRequest recurrence = new RecurrenceCreateRequest(
        RecurrenceType.WEEKLY, 1, List.of(DayOfWeek.TUESDAY),
        LocalDate.of(2026, 3, 16)); // 종료일이 같은 월요일 → 화요일 없음

    assertThatThrownBy(() -> lessonCommandService.create(teacher.getUserId(),
        new LessonCreateRequest("수학", start, end, recurrence, null)))
        .isInstanceOf(com.teacher.agent.exception.BadRequestException.class);
  }

  @Test
  void 비반복_수업을_반복_수업으로_전환한다() {
    LocalDateTime start = LocalDateTime.of(2026, 3, 16, 9, 0);
    LocalDateTime end = LocalDateTime.of(2026, 3, 16, 10, 0);

    lessonCommandService.create(teacher.getUserId(),
        new LessonCreateRequest("수학", start, end, null, null));

    Lesson original = lessonRepository.findAll().get(0);
    assertThat(original.getRecurrenceGroupId()).isNull();

    RecurrenceCreateRequest recurrence = new RecurrenceCreateRequest(
        RecurrenceType.DAILY, 1, null, LocalDate.of(2026, 3, 31));
    LessonUpdateRequest updateRequest = new LessonUpdateRequest(
        "수학", start, end, null, recurrence);

    lessonCommandService.update(teacher.getUserId(), original.getId(), updateRequest);

    Lesson updated = lessonRepository.findById(original.getId()).orElseThrow();
    assertThat(updated.getRecurrenceGroupId()).isNotNull();
    assertThat(updated.getRecurrence()).isNotNull();

    List<Lesson> allLessons = lessonRepository.findAll();
    assertThat(allLessons).hasSizeGreaterThan(1);

    java.util.UUID groupId = updated.getRecurrenceGroupId();
    for (Lesson l : allLessons) {
      assertThat(l.getRecurrenceGroupId()).isEqualTo(groupId);
    }
  }
}
