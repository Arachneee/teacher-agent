package com.teacher.agent.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LessonTest {

  private static final LocalDateTime START = LocalDateTime.of(2026, 3, 16, 9, 0);
  private static final LocalDateTime END = LocalDateTime.of(2026, 3, 16, 10, 0);

  @Test
  void 수업을_생성한다() {
    Lesson lesson = Lesson.create(1L, "수학 1교시", START, END);

    assertThat(lesson.getTeacherId()).isEqualTo(1L);
    assertThat(lesson.getTitle()).isEqualTo("수학 1교시");
    assertThat(lesson.getStartTime()).isEqualTo(START);
    assertThat(lesson.getEndTime()).isEqualTo(END);
  }

  @Test
  void teacherId가_0이면_생성에_실패한다() {
    assertThatThrownBy(() -> Lesson.create(0L, "수학", START, END))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void teacherId가_음수면_생성에_실패한다() {
    assertThatThrownBy(() -> Lesson.create(-1L, "수학", START, END))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 제목이_null이면_생성에_실패한다() {
    assertThatThrownBy(() -> Lesson.create(1L, null, START, END))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 제목이_빈_문자열이면_생성에_실패한다() {
    assertThatThrownBy(() -> Lesson.create(1L, "", START, END))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 제목이_공백이면_생성에_실패한다() {
    assertThatThrownBy(() -> Lesson.create(1L, "   ", START, END))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 시작시간이_null이면_생성에_실패한다() {
    assertThatThrownBy(() -> Lesson.create(1L, "수학", null, END))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 종료시간이_null이면_생성에_실패한다() {
    assertThatThrownBy(() -> Lesson.create(1L, "수학", START, null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 종료시간이_시작시간보다_같으면_생성에_실패한다() {
    assertThatThrownBy(() -> Lesson.create(1L, "수학", START, START))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 종료시간이_시작시간보다_이전이면_생성에_실패한다() {
    LocalDateTime beforeStart = START.minusHours(1);

    assertThatThrownBy(() -> Lesson.create(1L, "수학", START, beforeStart))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 수업_정보를_수정한다() {
    Lesson lesson = Lesson.create(1L, "수학", START, END);
    LocalDateTime newStart = LocalDateTime.of(2026, 3, 17, 14, 0);
    LocalDateTime newEnd = LocalDateTime.of(2026, 3, 17, 15, 0);

    lesson.update("영어 2교시", newStart, newEnd);

    assertThat(lesson.getTitle()).isEqualTo("영어 2교시");
    assertThat(lesson.getStartTime()).isEqualTo(newStart);
    assertThat(lesson.getEndTime()).isEqualTo(newEnd);
  }

  @Test
  void 수정_시_제목이_빈_문자열이면_실패한다() {
    Lesson lesson = Lesson.create(1L, "수학", START, END);

    assertThatThrownBy(() -> lesson.update("", START, END))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 수정_시_종료시간이_시작시간보다_이전이면_실패한다() {
    Lesson lesson = Lesson.create(1L, "수학", START, END);
    LocalDateTime beforeStart = START.minusHours(1);

    assertThatThrownBy(() -> lesson.update("수학", START, beforeStart))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
