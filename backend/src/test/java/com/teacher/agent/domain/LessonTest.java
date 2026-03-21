package com.teacher.agent.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class LessonTest {

  private static final LocalDateTime START = LocalDateTime.of(2026, 3, 16, 9, 0);
  private static final LocalDateTime END = LocalDateTime.of(2026, 3, 16, 10, 0);
  private static final UserId USER_ID = new UserId("teacher1");

  @Test
  void 수업을_생성한다() {
    // when
    Lesson lesson = Lesson.create(USER_ID, "수학 1교시", START, END);

    // then
    assertThat(lesson.getUserId()).isEqualTo(USER_ID);
    assertThat(lesson.getTitle()).isEqualTo("수학 1교시");
    assertThat(lesson.getStartTime()).isEqualTo(START);
    assertThat(lesson.getEndTime()).isEqualTo(END);
  }

  @Test
  void userId가_null이면_생성에_실패한다() {
    // when & then
    assertThatThrownBy(() -> Lesson.create(null, "수학", START, END))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 제목이_null이면_생성에_실패한다() {
    // when & then
    assertThatThrownBy(() -> Lesson.create(USER_ID, null, START, END))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 제목이_빈_문자열이면_생성에_실패한다() {
    // when & then
    assertThatThrownBy(() -> Lesson.create(USER_ID, "", START, END))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 제목이_공백이면_생성에_실패한다() {
    // when & then
    assertThatThrownBy(() -> Lesson.create(USER_ID, "   ", START, END))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 시작시간이_null이면_생성에_실패한다() {
    // when & then
    assertThatThrownBy(() -> Lesson.create(USER_ID, "수학", null, END))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 종료시간이_null이면_생성에_실패한다() {
    // when & then
    assertThatThrownBy(() -> Lesson.create(USER_ID, "수학", START, null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 종료시간이_시작시간보다_같으면_생성에_실패한다() {
    // when & then
    assertThatThrownBy(() -> Lesson.create(USER_ID, "수학", START, START))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 종료시간이_시작시간보다_이전이면_생성에_실패한다() {
    // given
    LocalDateTime beforeStart = START.minusHours(1);

    // when & then
    assertThatThrownBy(() -> Lesson.create(USER_ID, "수학", START, beforeStart))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 수업_정보를_수정한다() {
    // given
    Lesson lesson = Lesson.create(USER_ID, "수학", START, END);
    LocalDateTime newStart = LocalDateTime.of(2026, 3, 17, 14, 0);
    LocalDateTime newEnd = LocalDateTime.of(2026, 3, 17, 15, 0);

    // when
    lesson.update("영어 2교시", newStart, newEnd);

    // then
    assertThat(lesson.getTitle()).isEqualTo("영어 2교시");
    assertThat(lesson.getStartTime()).isEqualTo(newStart);
    assertThat(lesson.getEndTime()).isEqualTo(newEnd);
  }

  @Test
  void 수정_시_제목이_빈_문자열이면_실패한다() {
    // given
    Lesson lesson = Lesson.create(USER_ID, "수학", START, END);

    // when & then
    assertThatThrownBy(() -> lesson.update("", START, END))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 수정_시_종료시간이_시작시간보다_이전이면_실패한다() {
    // given
    Lesson lesson = Lesson.create(USER_ID, "수학", START, END);
    LocalDateTime beforeStart = START.minusHours(1);

    // when & then
    assertThatThrownBy(() -> lesson.update("수학", START, beforeStart))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void Recurrence를_포함하여_수업을_생성한다() {
    // given
    Recurrence recurrence = Recurrence.create(RecurrenceType.DAILY, 1, null,
        LocalDate.of(2026, 6, 30));

    // when
    Lesson lesson = Lesson.create(USER_ID, "수학", START, END, recurrence);

    // then
    assertThat(lesson.getRecurrence()).isNotNull();
    assertThat(lesson.getRecurrence().getRecurrenceType()).isEqualTo(RecurrenceType.DAILY);
  }

  @Test
  void Recurrence_없이_수업을_생성하면_recurrence가_null이다() {
    // when
    Lesson lesson = Lesson.create(USER_ID, "수학", START, END);

    // then
    assertThat(lesson.getRecurrence()).isNull();
  }

  @Test
  void 여러_수강생을_한번에_등록한다() {
    // given
    Lesson lesson = Lesson.create(USER_ID, "수학", START, END);

    // when
    lesson.addAttendees(List.of(1L, 2L, 3L));

    // then
    assertThat(lesson.getAttendees()).hasSize(3);
  }

  @Test
  void 여러_수강생_등록_시_중복이_있으면_예외가_발생한다() {
    // given
    Lesson lesson = Lesson.create(USER_ID, "수학", START, END);

    // when & then
    assertThatThrownBy(() -> lesson.addAttendees(List.of(1L, 1L)))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
