package com.teacher.agent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.teacher.agent.domain.LessonRepository;
import com.teacher.agent.domain.RecurrenceRepository;
import com.teacher.agent.domain.RecurrenceType;
import com.teacher.agent.domain.Teacher;
import com.teacher.agent.domain.TeacherRepository;
import com.teacher.agent.dto.LessonCreateRequest;
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

  @Autowired
  private RecurrenceRepository recurrenceRepository;

  private Teacher teacher;

  @BeforeEach
  void setUp() {
    teacher = teacherRepository.save(Teacher.create("testteacher", "password", "교사", null));
  }

  @AfterEach
  void tearDown() {
    lessonRepository.deleteAll();
    recurrenceRepository.deleteAll();
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
        new LessonCreateRequest("수학", start, end, recurrence));

    assertThat(lessonRepository.findAll()).hasSize(14);
  }

  @Test
  void cannot_exceed_6_months() {
    LocalDateTime start = LocalDateTime.now();
    LocalDateTime end = start.plusHours(1);
    RecurrenceCreateRequest recurrence = new RecurrenceCreateRequest(
        RecurrenceType.DAILY, 1, null, start.toLocalDate().plusMonths(7));

    assertThatThrownBy(() -> lessonCommandService.create(teacher.getUserId(),
        new LessonCreateRequest("수학", start, end, recurrence)))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
