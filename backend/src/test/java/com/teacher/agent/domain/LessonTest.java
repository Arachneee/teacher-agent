package com.teacher.agent.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
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
  void userId_값이_빈_문자열이면_생성에_실패한다() {
    // when & then
    assertThatThrownBy(() -> Lesson.create(new UserId(""), "수학", START, END))
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
}
