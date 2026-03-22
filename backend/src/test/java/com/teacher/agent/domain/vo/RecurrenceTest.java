package com.teacher.agent.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class RecurrenceTest {

  private static final LocalDate END_DATE = LocalDate.of(2026, 6, 30);

  @Test
  void DAILY_반복을_생성한다() {
    Recurrence recurrence = Recurrence.create(RecurrenceType.DAILY, 1, null, END_DATE);

    assertThat(recurrence.getRecurrenceType()).isEqualTo(RecurrenceType.DAILY);
    assertThat(recurrence.getIntervalValue()).isEqualTo(1);
    assertThat(recurrence.getDaysOfWeek()).isNull();
    assertThat(recurrence.getEndDate()).isEqualTo(END_DATE);
  }

  @Test
  void WEEKLY_반복을_생성한다() {
    List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);

    Recurrence recurrence = Recurrence.create(RecurrenceType.WEEKLY, 1, days, END_DATE);

    assertThat(recurrence.getRecurrenceType()).isEqualTo(RecurrenceType.WEEKLY);
    assertThat(recurrence.getIntervalValue()).isEqualTo(1);
    assertThat(recurrence.getDaysOfWeek()).containsExactly(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
    assertThat(recurrence.getEndDate()).isEqualTo(END_DATE);
  }

  @Test
  void MONTHLY_반복을_생성한다() {
    Recurrence recurrence = Recurrence.create(RecurrenceType.MONTHLY, 2, null, END_DATE);

    assertThat(recurrence.getRecurrenceType()).isEqualTo(RecurrenceType.MONTHLY);
    assertThat(recurrence.getIntervalValue()).isEqualTo(2);
  }

  @Test
  void recurrenceType이_null이면_실패한다() {
    assertThatThrownBy(() -> Recurrence.create(null, 1, null, END_DATE))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void intervalValue가_null이면_실패한다() {
    assertThatThrownBy(() -> Recurrence.create(RecurrenceType.DAILY, null, null, END_DATE))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void intervalValue가_0이면_실패한다() {
    assertThatThrownBy(() -> Recurrence.create(RecurrenceType.DAILY, 0, null, END_DATE))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void intervalValue가_음수이면_실패한다() {
    assertThatThrownBy(() -> Recurrence.create(RecurrenceType.DAILY, -1, null, END_DATE))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void endDate가_null이면_실패한다() {
    assertThatThrownBy(() -> Recurrence.create(RecurrenceType.DAILY, 1, null, null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void WEEKLY인데_daysOfWeek가_null이면_실패한다() {
    assertThatThrownBy(() -> Recurrence.create(RecurrenceType.WEEKLY, 1, null, END_DATE))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void WEEKLY인데_daysOfWeek가_빈_리스트이면_실패한다() {
    assertThatThrownBy(() -> Recurrence.create(RecurrenceType.WEEKLY, 1, List.of(), END_DATE))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void DAILY에서_daysOfWeek가_null이어도_성공한다() {
    Recurrence recurrence = Recurrence.create(RecurrenceType.DAILY, 1, null, END_DATE);

    assertThat(recurrence.getDaysOfWeek()).isNull();
  }
}
