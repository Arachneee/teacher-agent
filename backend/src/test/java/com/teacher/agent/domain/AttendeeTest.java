package com.teacher.agent.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AttendeeTest {

  private static final LocalDateTime START = LocalDateTime.of(2026, 3, 16, 9, 0);
  private static final LocalDateTime END = LocalDateTime.of(2026, 3, 16, 10, 0);

  private Lesson createLesson() {
    return Lesson.create(1L, "수학", START, END);
  }

  @Test
  void 수업_참가자를_생성한다() {
    Lesson lesson = createLesson();

    Attendee attendee = Attendee.create(lesson, 1L);

    assertThat(attendee.getLesson()).isSameAs(lesson);
    assertThat(attendee.getStudentId()).isEqualTo(1L);
  }

  @Test
  void lesson이_null이면_생성에_실패한다() {
    assertThatThrownBy(() -> Attendee.create(null, 1L))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void studentId가_0이하면_생성에_실패한다() {
    Lesson lesson = createLesson();

    assertThatThrownBy(() -> Attendee.create(lesson, 0L))
        .isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(() -> Attendee.create(lesson, -1L))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void Lesson_addAttendee_메서드가_참가자를_추가한다() {
    Lesson lesson = createLesson();

    lesson.addAttendee(1L);

    assertThat(lesson.getAttendees()).hasSize(1);
    assertThat(lesson.getAttendees().get(0).getStudentId()).isEqualTo(1L);
  }

  @Test
  void 중복_학생_추가_시_예외가_발생한다() {
    Lesson lesson = createLesson();
    lesson.addAttendee(1L);

    assertThatThrownBy(() -> lesson.addAttendee(1L)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void Lesson_removeAttendee_메서드가_참가자를_제거한다() {
    Lesson lesson = createLesson();
    lesson.addAttendee(1L);
    Attendee attendee = lesson.getAttendees().get(0);

    // 단위 테스트 환경에서는 DB 없이 실행되므로 getId()는 null
    lesson.removeAttendee(attendee.getId());

    assertThat(lesson.getAttendees()).isEmpty();
  }

  @Test
  void 없는_참가자_제거_시_예외가_발생한다() {
    Lesson lesson = createLesson();

    assertThatThrownBy(() -> lesson.removeAttendee(999L))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
