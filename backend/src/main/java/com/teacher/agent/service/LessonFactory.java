package com.teacher.agent.service;

import com.teacher.agent.domain.Lesson;
import com.teacher.agent.domain.Recurrence;
import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.GenerationContext;
import com.teacher.agent.dto.LessonCreateRequest;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class LessonFactory {

  public List<Lesson> createFrom(UserId userId, LessonCreateRequest request) {
    if (request.recurrence() == null) {
      return List.of(
          Lesson.create(userId, request.title(), request.startTime(), request.endTime()));
    }

    Recurrence recurrence = request.recurrence().toEntity();

    if (recurrence.getEndDate().isAfter(request.startTime().toLocalDate().plusMonths(6))) {
      throw new IllegalArgumentException("반복 수업은 최대 6개월까지만 설정할 수 있습니다.");
    }

    GenerationContext context = GenerationContext.from(userId, request, recurrence,
        UUID.randomUUID());
    return createRecurringLessons(context);
  }

  private List<Lesson> createRecurringLessons(GenerationContext context) {
    return switch (context.recurrence().getRecurrenceType()) {
      case DAILY -> generateDaily(context);
      case WEEKLY -> generateWeekly(context);
      case MONTHLY -> generateMonthly(context);
    };
  }

  private List<Lesson> generateDaily(GenerationContext context) {
    List<Lesson> lessons = new ArrayList<>();
    LocalDate current = context.startDate();
    while (!current.isAfter(context.endDate())) {
      lessons.add(createLesson(context, current));
      current = current.plusDays(context.recurrence().getIntervalValue());
    }
    return lessons;
  }

  private List<Lesson> generateWeekly(GenerationContext context) {
    List<Lesson> lessons = new ArrayList<>();
    List<DayOfWeek> daysOfWeek = context.recurrence().getDaysOfWeek();
    LocalDate current = context.startDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

    while (!current.isAfter(context.endDate())) {
      for (DayOfWeek day : daysOfWeek) {
        LocalDate lessonDate = current.with(day);
        if (!lessonDate.isBefore(context.startDate()) && !lessonDate.isAfter(context.endDate())) {
          lessons.add(createLesson(context, lessonDate));
        }
      }
      current = current.plusWeeks(context.recurrence().getIntervalValue());
    }
    return lessons;
  }

  private List<Lesson> generateMonthly(GenerationContext context) {
    List<Lesson> lessons = new ArrayList<>();
    LocalDate current = context.startDate();
    int targetDay = current.getDayOfMonth();

    while (!current.isAfter(context.endDate())) {
      lessons.add(createLesson(context, current));
      current = current.plusMonths(context.recurrence().getIntervalValue());
      int maxDay = current.lengthOfMonth();
      current = current.withDayOfMonth(Math.min(targetDay, maxDay));
    }
    return lessons;
  }

  private Lesson createLesson(GenerationContext context, LocalDate lessonDate) {
    LocalDateTime lessonStart = lessonDate.atTime(context.startTime().toLocalTime());
    LocalDateTime lessonEnd = lessonStart.plusMinutes(context.durationMinutes());
    return Lesson.create(context.userId(), context.title(), lessonStart, lessonEnd,
        context.recurrence(), context.groupId());
  }
}
