package com.teacher.agent.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recurrence extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RecurrenceType recurrenceType;

  @Column(nullable = false)
  private Integer intervalValue;

  @Convert(converter = DayOfWeekConverter.class)
  private List<DayOfWeek> daysOfWeek;

  @Column(nullable = false)
  private LocalDate endDate;

  public static Recurrence create(RecurrenceType recurrenceType, Integer intervalValue,
      List<DayOfWeek> daysOfWeek, LocalDate endDate) {
    Recurrence recurrence = new Recurrence();
    recurrence.recurrenceType = recurrenceType;
    recurrence.intervalValue = intervalValue;
    recurrence.daysOfWeek = daysOfWeek;
    recurrence.endDate = endDate;
    return recurrence;
  }
}
