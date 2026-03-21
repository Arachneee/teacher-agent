package com.teacher.agent.service;

import com.teacher.agent.domain.Lesson;
import com.teacher.agent.domain.Recurrence;
import com.teacher.agent.domain.RecurrenceType;
import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.LessonCreateRequest;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class LessonFactory {

  public List<Lesson> createFrom(UserId userId, LessonCreateRequest request) {
    if (request.recurrence() == null) {
      return List.of(Lesson.create(userId, request.title(), request.startTime(), request.endTime()));
    }

    Recurrence recurrence = request.recurrence().toEntity();

    if (recurrence.getEndDate().isAfter(request.startTime().toLocalDate().plusMonths(6))) {
      throw new IllegalArgumentException("반복 수업은 최대 6개월까지만 설정할 수 있습니다.");
    }

    return createRecurringLessons(userId, request.title(), request.startTime(), request.endTime(),
        recurrence);
  }

  private List<Lesson> createRecurringLessons(UserId userId, String title, LocalDateTime startTime,
      LocalDateTime endTime, Recurrence recurrence) {
    List<Lesson> lessons = new ArrayList<>();
    long durationMinutes = Duration.between(startTime, endTime).toMinutes();
    LocalDate start = startTime.toLocalDate();
    LocalDate end = recurrence.getEndDate();

    switch (recurrence.getRecurrenceType()) {
      case DAILY -> generateDaily(lessons, userId, title, startTime, durationMinutes, start, end,
          recurrence);
      case WEEKLY -> generateWeekly(lessons, userId, title, startTime, durationMinutes, start, end,
          recurrence);
      case MONTHLY -> generateMonthly(lessons, userId, title, startTime, durationMinutes, start,
          end, recurrence);
      default -> throw new UnsupportedOperationException(
          "지원하지 않는 반복 유형: " + recurrence.getRecurrenceType());
    }

    return lessons;
  }

  private void generateDaily(List<Lesson> lessons, UserId userId, String title,
      LocalDateTime startTime, long durationMinutes, LocalDate start, LocalDate end,
      Recurrence recurrence) {
    LocalDate current = start;
    while (!current.isAfter(end)) {
      addLesson(lessons, userId, title, startTime, current, durationMinutes, recurrence);
      current = current.plusDays(recurrence.getIntervalValue());
    }
  }

  private void generateWeekly(List<Lesson> lessons, UserId userId, String title,
      LocalDateTime startTime, long durationMinutes, LocalDate start, LocalDate end,
      Recurrence recurrence) {
    List<DayOfWeek> daysOfWeek = recurrence.getDaysOfWeek();
    LocalDate weekStart = start.with(TemporalAdjusters.previousOrSame(
        DayOfWeek.MONDAY));
    LocalDate current = weekStart;

    while (!current.isAfter(end)) {
      for (DayOfWeek day : daysOfWeek) {
        LocalDate lessonDate = current.with(day);
        if (!lessonDate.isBefore(start) && !lessonDate.isAfter(end)) {
          addLesson(lessons, userId, title, startTime, lessonDate, durationMinutes, recurrence);
        }
      }
      current = current.plusWeeks(recurrence.getIntervalValue());
    }
  }

  private void generateMonthly(List<Lesson> lessons, UserId userId, String title,
      LocalDateTime startTime, long durationMinutes, LocalDate start, LocalDate end,
      Recurrence recurrence) {
    LocalDate current = start;
    int targetDay = start.getDayOfMonth();

    while (!current.isAfter(end)) {
      addLesson(lessons, userId, title, startTime, current, durationMinutes, recurrence);
      current = current.plusMonths(recurrence.getIntervalValue());
      int maxDay = current.lengthOfMonth();
      current = current.withDayOfMonth(Math.min(targetDay, maxDay));
    }
  }

  private void addLesson(List<Lesson> lessons, UserId userId, String title,
      LocalDateTime startTime, LocalDate lessonDate, long durationMinutes,
      Recurrence recurrence) {
    LocalDateTime lessonStart = lessonDate.atTime(startTime.toLocalTime());
    LocalDateTime lessonEnd = lessonStart.plusMinutes(durationMinutes);
    Lesson lesson = Lesson.create(userId, title, lessonStart, lessonEnd, recurrence);
    lessons.add(lesson);
  }
}
