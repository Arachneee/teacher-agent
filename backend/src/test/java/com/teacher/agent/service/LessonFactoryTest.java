package com.teacher.agent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.teacher.agent.domain.Lesson;
import com.teacher.agent.domain.RecurrenceType;
import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.LessonCreateRequest;
import com.teacher.agent.dto.RecurrenceCreateRequest;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class LessonFactoryTest {

  private LessonFactory lessonFactory;
  private static final UserId USER_ID = new UserId("teacher1");
  private static final String TITLE = "수학 수업";
  private static final LocalDateTime START = LocalDateTime.of(2026, 3, 16, 9, 0);
  private static final LocalDateTime END = LocalDateTime.of(2026, 3, 16, 10, 0);

  @BeforeEach
  void setUp() {
    lessonFactory = new LessonFactory();
  }

  @Test
  void 반복_없이_단일_수업을_생성한다() {
    LessonCreateRequest request = new LessonCreateRequest(TITLE, START, END, null);

    List<Lesson> lessons = lessonFactory.createFrom(USER_ID, request);

    assertThat(lessons).hasSize(1);
    Lesson lesson = lessons.get(0);
    assertThat(lesson.getUserId()).isEqualTo(USER_ID);
    assertThat(lesson.getTitle()).isEqualTo(TITLE);
    assertThat(lesson.getStartTime()).isEqualTo(START);
    assertThat(lesson.getEndTime()).isEqualTo(END);
    assertThat(lesson.getRecurrence()).isNull();
  }

  @Nested
  class 매일_반복 {

    @Test
    void 매일_반복_수업을_생성한다() {
      // 3/16 ~ 3/31 = 16일
      RecurrenceCreateRequest recurrence =
          new RecurrenceCreateRequest(RecurrenceType.DAILY, 1, null, LocalDate.of(2026, 3, 31));
      LessonCreateRequest request = new LessonCreateRequest(TITLE, START, END, recurrence);

      List<Lesson> lessons = lessonFactory.createFrom(USER_ID, request);

      assertThat(lessons).hasSize(16);
      assertThat(lessons.get(0).getStartTime().toLocalDate()).isEqualTo(LocalDate.of(2026, 3, 16));
      assertThat(lessons.get(15).getStartTime().toLocalDate()).isEqualTo(LocalDate.of(2026, 3, 31));
    }

    @Test
    void 격일_반복_수업을_생성한다() {
      // 3/16, 3/18, 3/20, 3/22, 3/24, 3/26, 3/28, 3/30 = 8일
      RecurrenceCreateRequest recurrence =
          new RecurrenceCreateRequest(RecurrenceType.DAILY, 2, null, LocalDate.of(2026, 3, 31));
      LessonCreateRequest request = new LessonCreateRequest(TITLE, START, END, recurrence);

      List<Lesson> lessons = lessonFactory.createFrom(USER_ID, request);

      assertThat(lessons).hasSize(8);
      assertThat(lessons.get(0).getStartTime().toLocalDate()).isEqualTo(LocalDate.of(2026, 3, 16));
      assertThat(lessons.get(1).getStartTime().toLocalDate()).isEqualTo(LocalDate.of(2026, 3, 18));
      assertThat(lessons.get(7).getStartTime().toLocalDate()).isEqualTo(LocalDate.of(2026, 3, 30));
    }

    @Test
    void 매일_반복_수업의_시간이_올바르게_설정된다() {
      RecurrenceCreateRequest recurrence =
          new RecurrenceCreateRequest(RecurrenceType.DAILY, 1, null, LocalDate.of(2026, 3, 18));
      LessonCreateRequest request = new LessonCreateRequest(TITLE, START, END, recurrence);

      List<Lesson> lessons = lessonFactory.createFrom(USER_ID, request);

      for (Lesson lesson : lessons) {
        assertThat(lesson.getStartTime().toLocalTime()).isEqualTo(START.toLocalTime());
        assertThat(lesson.getEndTime().toLocalTime()).isEqualTo(END.toLocalTime());
      }
    }

    @Test
    void 시작일과_종료일이_같으면_하나만_생성한다() {
      RecurrenceCreateRequest recurrence =
          new RecurrenceCreateRequest(RecurrenceType.DAILY, 1, null, LocalDate.of(2026, 3, 16));
      LessonCreateRequest request = new LessonCreateRequest(TITLE, START, END, recurrence);

      List<Lesson> lessons = lessonFactory.createFrom(USER_ID, request);

      assertThat(lessons).hasSize(1);
    }
  }

  @Nested
  class 매주_반복 {

    @Test
    void 매주_특정_요일에_반복_수업을_생성한다() {
      // 3/16(월) ~ 4/30, 매주 월/수
      // 월: 3/16, 3/23, 3/30, 4/6, 4/13, 4/20, 4/27 = 7
      // 수: 3/18, 3/25, 4/1, 4/8, 4/15, 4/22, 4/29 = 7
      // total = 14
      RecurrenceCreateRequest recurrence =
          new RecurrenceCreateRequest(RecurrenceType.WEEKLY, 1,
              List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), LocalDate.of(2026, 4, 30));
      LessonCreateRequest request = new LessonCreateRequest(TITLE, START, END, recurrence);

      List<Lesson> lessons = lessonFactory.createFrom(USER_ID, request);

      assertThat(lessons).hasSize(14);
    }

    @Test
    void 격주_반복_수업을_생성한다() {
      // 3/16(월) ~ 4/30, 격주 월요일
      // 3/16, 3/30, 4/13, 4/27 = 4
      RecurrenceCreateRequest recurrence =
          new RecurrenceCreateRequest(RecurrenceType.WEEKLY, 2,
              List.of(DayOfWeek.MONDAY), LocalDate.of(2026, 4, 30));
      LessonCreateRequest request = new LessonCreateRequest(TITLE, START, END, recurrence);

      List<Lesson> lessons = lessonFactory.createFrom(USER_ID, request);

      assertThat(lessons).hasSize(4);
    }

    @Test
    void 시작일_이전_요일은_생성하지_않는다() {
      // 3/16(월) 시작, 매주 금요일 → 3/20부터 시작
      RecurrenceCreateRequest recurrence =
          new RecurrenceCreateRequest(RecurrenceType.WEEKLY, 1,
              List.of(DayOfWeek.FRIDAY), LocalDate.of(2026, 4, 10));
      LessonCreateRequest request = new LessonCreateRequest(TITLE, START, END, recurrence);

      List<Lesson> lessons = lessonFactory.createFrom(USER_ID, request);

      assertThat(lessons).isNotEmpty();
      assertThat(lessons.get(0).getStartTime().toLocalDate()).isEqualTo(LocalDate.of(2026, 3, 20));
      for (Lesson lesson : lessons) {
        assertThat(lesson.getStartTime().getDayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);
      }
    }

    @Test
    void 매주_반복_수업에_반복_정보가_설정된다() {
      RecurrenceCreateRequest recurrence =
          new RecurrenceCreateRequest(RecurrenceType.WEEKLY, 1,
              List.of(DayOfWeek.MONDAY), LocalDate.of(2026, 3, 23));
      LessonCreateRequest request = new LessonCreateRequest(TITLE, START, END, recurrence);

      List<Lesson> lessons = lessonFactory.createFrom(USER_ID, request);

      for (Lesson lesson : lessons) {
        assertThat(lesson.getRecurrence()).isNotNull();
        assertThat(lesson.getRecurrence().getRecurrenceType()).isEqualTo(RecurrenceType.WEEKLY);
      }
    }
  }

  @Nested
  class 매월_반복 {

    @Test
    void 매월_반복_수업을_생성한다() {
      // 3/16 ~ 6/16, 매월 = 3/16, 4/16, 5/16, 6/16 = 4
      RecurrenceCreateRequest recurrence =
          new RecurrenceCreateRequest(RecurrenceType.MONTHLY, 1, null, LocalDate.of(2026, 6, 16));
      LessonCreateRequest request = new LessonCreateRequest(TITLE, START, END, recurrence);

      List<Lesson> lessons = lessonFactory.createFrom(USER_ID, request);

      assertThat(lessons).hasSize(4);
      assertThat(lessons.get(0).getStartTime().toLocalDate()).isEqualTo(LocalDate.of(2026, 3, 16));
      assertThat(lessons.get(1).getStartTime().toLocalDate()).isEqualTo(LocalDate.of(2026, 4, 16));
      assertThat(lessons.get(2).getStartTime().toLocalDate()).isEqualTo(LocalDate.of(2026, 5, 16));
      assertThat(lessons.get(3).getStartTime().toLocalDate()).isEqualTo(LocalDate.of(2026, 6, 16));
    }

    @Test
    void 격월_반복_수업을_생성한다() {
      // 3/16 ~ 9/16, 격월 = 3/16, 5/16, 7/16, 9/16 = 4
      RecurrenceCreateRequest recurrence =
          new RecurrenceCreateRequest(RecurrenceType.MONTHLY, 2, null, LocalDate.of(2026, 9, 16));
      LessonCreateRequest request = new LessonCreateRequest(TITLE, START, END, recurrence);

      List<Lesson> lessons = lessonFactory.createFrom(USER_ID, request);

      assertThat(lessons).hasSize(4);
      assertThat(lessons.get(0).getStartTime().toLocalDate()).isEqualTo(LocalDate.of(2026, 3, 16));
      assertThat(lessons.get(1).getStartTime().toLocalDate()).isEqualTo(LocalDate.of(2026, 5, 16));
      assertThat(lessons.get(2).getStartTime().toLocalDate()).isEqualTo(LocalDate.of(2026, 7, 16));
      assertThat(lessons.get(3).getStartTime().toLocalDate()).isEqualTo(LocalDate.of(2026, 9, 16));
    }

    @Test
    void 월말_날짜가_없는_달은_해당_월의_마지막_날로_조정된다() {
      // 1/31 시작, 매월 → 2월은 28일, 3월은 31일...
      LocalDateTime janStart = LocalDateTime.of(2026, 1, 31, 9, 0);
      LocalDateTime janEnd = LocalDateTime.of(2026, 1, 31, 10, 0);
      RecurrenceCreateRequest recurrence =
          new RecurrenceCreateRequest(RecurrenceType.MONTHLY, 1, null, LocalDate.of(2026, 3, 31));
      LessonCreateRequest request = new LessonCreateRequest(TITLE, janStart, janEnd, recurrence);

      List<Lesson> lessons = lessonFactory.createFrom(USER_ID, request);

      assertThat(lessons).hasSize(3);
      assertThat(lessons.get(0).getStartTime().toLocalDate()).isEqualTo(LocalDate.of(2026, 1, 31));
      assertThat(lessons.get(1).getStartTime().toLocalDate()).isEqualTo(LocalDate.of(2026, 2, 28));
      assertThat(lessons.get(2).getStartTime().toLocalDate()).isEqualTo(LocalDate.of(2026, 3, 28));
    }
  }

  @Nested
  class 수업_시간_검증 {

    @Test
    void 반복_수업의_수업_시간이_유지된다() {
      // 2시간 수업 (9:00 ~ 11:00)
      LocalDateTime longEnd = LocalDateTime.of(2026, 3, 16, 11, 0);
      RecurrenceCreateRequest recurrence =
          new RecurrenceCreateRequest(RecurrenceType.DAILY, 1, null, LocalDate.of(2026, 3, 18));
      LessonCreateRequest request = new LessonCreateRequest(TITLE, START, longEnd, recurrence);

      List<Lesson> lessons = lessonFactory.createFrom(USER_ID, request);

      for (Lesson lesson : lessons) {
        assertThat(java.time.Duration.between(lesson.getStartTime(), lesson.getEndTime())
            .toMinutes()).isEqualTo(120);
      }
    }
  }

  @Nested
  class 반복_기간_제한 {

    @Test
    void 반복_종료일이_6개월을_초과하면_예외가_발생한다() {
      // 3/16 + 6개월 = 9/16, 종료일 9/17 → 초과
      RecurrenceCreateRequest recurrence =
          new RecurrenceCreateRequest(RecurrenceType.DAILY, 1, null, LocalDate.of(2026, 9, 17));
      LessonCreateRequest request = new LessonCreateRequest(TITLE, START, END, recurrence);

      assertThatThrownBy(() -> lessonFactory.createFrom(USER_ID, request))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("6개월");
    }

    @Test
    void 반복_종료일이_정확히_6개월이면_정상_생성된다() {
      // 3/16 + 6개월 = 9/16
      RecurrenceCreateRequest recurrence =
          new RecurrenceCreateRequest(RecurrenceType.DAILY, 1, null, LocalDate.of(2026, 9, 16));
      LessonCreateRequest request = new LessonCreateRequest(TITLE, START, END, recurrence);

      List<Lesson> lessons = lessonFactory.createFrom(USER_ID, request);

      assertThat(lessons).isNotEmpty();
    }

    @Test
    void 반복_종료일이_6개월_이내이면_정상_생성된다() {
      RecurrenceCreateRequest recurrence =
          new RecurrenceCreateRequest(RecurrenceType.DAILY, 1, null, LocalDate.of(2026, 4, 16));
      LessonCreateRequest request = new LessonCreateRequest(TITLE, START, END, recurrence);

      List<Lesson> lessons = lessonFactory.createFrom(USER_ID, request);

      assertThat(lessons).isNotEmpty();
    }
  }

  @Nested
  class 반복_정보_설정 {

    @Test
    void 반복_수업에는_반복_정보가_포함된다() {
      RecurrenceCreateRequest recurrence =
          new RecurrenceCreateRequest(RecurrenceType.DAILY, 1, null, LocalDate.of(2026, 3, 18));
      LessonCreateRequest request = new LessonCreateRequest(TITLE, START, END, recurrence);

      List<Lesson> lessons = lessonFactory.createFrom(USER_ID, request);

      for (Lesson lesson : lessons) {
        assertThat(lesson.getRecurrence()).isNotNull();
        assertThat(lesson.getRecurrence().getRecurrenceType()).isEqualTo(RecurrenceType.DAILY);
        assertThat(lesson.getRecurrence().getIntervalValue()).isEqualTo(1);
        assertThat(lesson.getRecurrence().getEndDate()).isEqualTo(LocalDate.of(2026, 3, 18));
      }
    }

    @Test
    void 단일_수업에는_반복_정보가_없다() {
      LessonCreateRequest request = new LessonCreateRequest(TITLE, START, END, null);

      List<Lesson> lessons = lessonFactory.createFrom(USER_ID, request);

      assertThat(lessons.get(0).getRecurrence()).isNull();
    }
  }
}
