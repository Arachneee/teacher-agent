package com.teacher.agent.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.teacher.agent.domain.vo.UserId;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class AttendeeTest {

  private static final LocalDateTime START = LocalDateTime.of(2026, 3, 16, 9, 0);
  private static final LocalDateTime END = LocalDateTime.of(2026, 3, 16, 10, 0);

  private static final UserId USER_ID = new UserId("teacher1");

  private Lesson createLesson() {
    return Lesson.create(USER_ID, "수학", START, END);
  }

  @Test
  void 수업_참가자를_생성한다() {
    // given
    Lesson lesson = createLesson();

    // when
    Attendee attendee = Attendee.create(lesson, 1L);

    // then
    assertThat(attendee.getLesson()).isSameAs(lesson);
    assertThat(attendee.getStudentId()).isEqualTo(1L);
  }

  @Test
  void lesson이_null이면_생성에_실패한다() {
    // when & then
    assertThatThrownBy(() -> Attendee.create(null, 1L))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void studentId가_0이하면_생성에_실패한다() {
    // given
    Lesson lesson = createLesson();

    // when & then
    assertThatThrownBy(() -> Attendee.create(lesson, 0L))
        .isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(() -> Attendee.create(lesson, -1L))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void Lesson_addAttendee_메서드가_참가자를_추가한다() {
    // given
    Lesson lesson = createLesson();

    // when
    lesson.addAttendee(1L);

    // then
    assertThat(lesson.getAttendees()).hasSize(1);
    assertThat(lesson.getAttendees().get(0).getStudentId()).isEqualTo(1L);
  }

  @Test
  void 중복_학생_추가_시_예외가_발생한다() {
    // given
    Lesson lesson = createLesson();
    lesson.addAttendee(1L);

    // when & then
    assertThatThrownBy(() -> lesson.addAttendee(1L))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void Lesson_removeAttendee_메서드가_참가자를_제거한다() {
    // given
    Lesson lesson = createLesson();
    lesson.addAttendee(1L);
    Attendee attendee = lesson.getAttendees().get(0);

    // when
    // 단위 테스트 환경에서는 DB 없이 실행되므로 getId()는 null
    lesson.removeAttendee(attendee.getId());

    // then
    assertThat(lesson.getAttendees()).isEmpty();
  }

  @Test
  void 없는_참가자_제거_시_예외가_발생한다() {
    // given
    Lesson lesson = createLesson();

    // when & then
    assertThatThrownBy(() -> lesson.removeAttendee(999L))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
