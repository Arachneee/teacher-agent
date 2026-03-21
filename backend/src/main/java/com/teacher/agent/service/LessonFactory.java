package com.teacher.agent.service;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.WEEKS;

import com.teacher.agent.domain.Lesson;
import com.teacher.agent.domain.Recurrence;
import com.teacher.agent.domain.UserId;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class LessonFactory {

  public List<Lesson> createFrom(UserId userId, String title, LocalDateTime startTime,
      LocalDateTime endTime, Recurrence recurrence) {
    if (recurrence == null) {
      return List.of(Lesson.create(userId, title, startTime, endTime));
    }

    if (recurrence.getEndDate().isAfter(startTime.toLocalDate().plusMonths(6))) {
      throw new IllegalArgumentException("반복 수업은 최대 6개월까지만 설정할 수 있습니다.");
    }

    return createRecurringLessons(userId, title, startTime, endTime, recurrence);
  }

  private List<Lesson> createRecurringLessons(UserId userId, String title, LocalDateTime startTime,
      LocalDateTime endTime, Recurrence recurrence) {
    List<Lesson> lessons = new ArrayList<>();
    long durationMinutes = Duration.between(startTime, endTime).toMinutes();

    LocalDateTime current = startTime;
    while (!current.toLocalDate().isAfter(recurrence.getEndDate())) {
      if (shouldCreateLesson(current, startTime, recurrence)) {
        LocalDateTime lessonEndTime = current.plusMinutes(durationMinutes);
        Lesson lesson = Lesson.create(userId, title, current, lessonEndTime);
        lesson.setRecurrence(recurrence);
        lessons.add(lesson);
      }

      current = current.plusDays(1);
    }
    return lessons;
  }

  private boolean shouldCreateLesson(LocalDateTime current, LocalDateTime lessonStartTime,
      Recurrence recurrence) {
    switch (recurrence.getRecurrenceType()) {
      case DAILY:
        return DAYS.between(lessonStartTime.toLocalDate(), current.toLocalDate())
            % recurrence.getIntervalValue() == 0;
      case WEEKLY:
        if (recurrence.getDaysOfWeek() != null
            && recurrence.getDaysOfWeek().contains(current.getDayOfWeek())) {
          long weeksBetween = WEEKS.between(lessonStartTime.toLocalDate(), current.toLocalDate());
          return weeksBetween % recurrence.getIntervalValue() == 0;
        }
        return false;
      case MONTHLY:
        if (current.getDayOfMonth() == lessonStartTime.getDayOfMonth()) {
          long monthsBetween = MONTHS.between(lessonStartTime.toLocalDate(), current.toLocalDate());
          return monthsBetween % recurrence.getIntervalValue() == 0;
        }
        return false;
    }
    return false;
  }
}
