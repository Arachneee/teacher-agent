package com.teacher.agent.domain.vo;

import static com.teacher.agent.util.ValidationUtil.checkArgument;
import static com.teacher.agent.util.ValidationUtil.checkNotNull;
import static com.teacher.agent.util.ValidationUtil.checkPositive;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recurrence {

  @Enumerated(EnumType.STRING)
  @Column(name = "recurrence_type")
  private RecurrenceType recurrenceType;

  @Column(name = "recurrence_interval")
  private Integer intervalValue;

  @Convert(converter = DayOfWeekConverter.class)
  @Column(name = "recurrence_days_of_week")
  private List<DayOfWeek> daysOfWeek;

  @Column(name = "recurrence_end_date")
  private LocalDate endDate;

  public static Recurrence create(RecurrenceType recurrenceType, Integer intervalValue,
      List<DayOfWeek> daysOfWeek, LocalDate endDate) {
    Recurrence recurrence = new Recurrence();
    recurrence.recurrenceType = checkNotNull(recurrenceType, "recurrenceType");
    recurrence.intervalValue = (int) checkPositive(checkNotNull(intervalValue, "intervalValue"),
        "intervalValue");
    recurrence.endDate = checkNotNull(endDate, "endDate");

    if (recurrenceType == RecurrenceType.WEEKLY) {
      checkArgument(daysOfWeek != null && !daysOfWeek.isEmpty(), "daysOfWeek");
    }
    recurrence.daysOfWeek = daysOfWeek;

    return recurrence;
  }
}
