package com.teacher.agent.domain;

import static com.teacher.agent.util.ErrorMessages.ATTENDEE_ALREADY_EXISTS;
import static com.teacher.agent.util.ErrorMessages.ATTENDEE_NOT_FOUND;
import static com.teacher.agent.util.Parameter.*;
import static com.teacher.agent.util.ValidationUtil.*;

import com.teacher.agent.domain.vo.Recurrence;
import com.teacher.agent.domain.vo.UserId;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id", callSuper = false)
@Table(
    indexes = {
        @Index(name = "idx_lesson_user_id", columnList = "userId"),
        @Index(name = "idx_lesson_recurrence_group_id", columnList = "recurrenceGroupId")
    })
public class Lesson extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private UserId userId;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private LocalDateTime startTime;

  @Column(nullable = false)
  private LocalDateTime endTime;

  @OneToMany(
      mappedBy = "lesson",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<Attendee> attendees = new ArrayList<>();

  @Embedded
  private Recurrence recurrence;

  private UUID recurrenceGroupId;

  public static Lesson create(
      UserId userId, String title, LocalDateTime startTime, LocalDateTime endTime) {
    return create(userId, title, startTime, endTime, null, null);
  }

  public static Lesson create(
      UserId userId,
      String title,
      LocalDateTime startTime,
      LocalDateTime endTime,
      Recurrence recurrence,
      UUID recurrenceGroupId) {
    Lesson lesson = new Lesson();

    lesson.userId = checkNotNull(userId, USER_ID);
    lesson.title = checkNotBlank(title, TITLE);
    lesson.startTime = checkNotNull(startTime, START_TIME);
    lesson.endTime = checkNotNull(endTime, END_TIME);
    lesson.recurrence = recurrence;
    lesson.recurrenceGroupId = recurrenceGroupId;

    checkArgument(lesson.endTime.isAfter(lesson.startTime), END_TIME);

    return lesson;
  }

  public void addAttendee(Long studentId) {
    boolean isDuplicate =
        attendees.stream().anyMatch(attendee -> attendee.getStudentId().equals(studentId));

    if (isDuplicate) {
      throw new IllegalArgumentException(ATTENDEE_ALREADY_EXISTS + studentId);
    }

    attendees.add(Attendee.create(this, studentId));
  }

  public void addAttendees(List<Long> studentIds) {
    studentIds.forEach(this::addAttendee);
  }

  public void addAttendeesIfAbsent(List<Long> studentIds) {
    studentIds.stream()
        .filter(studentId -> !hasAttendee(studentId))
        .forEach(studentId -> attendees.add(Attendee.create(this, studentId)));
  }

  public boolean hasAttendee(Long studentId) {
    return attendees.stream().anyMatch(a -> a.getStudentId().equals(studentId));
  }

  public void removeAttendee(Long attendeeId) {
    boolean removed = attendees.removeIf(attendee -> Objects.equals(attendee.getId(), attendeeId));

    if (!removed) {
      throw new IllegalArgumentException(ATTENDEE_NOT_FOUND + attendeeId);
    }
  }

  public void removeAttendeesByStudentIds(List<Long> studentIds) {
    attendees.removeIf(attendee -> studentIds.contains(attendee.getStudentId()));
  }

  public void convertToRecurring(Recurrence recurrence, UUID recurrenceGroupId) {
    this.recurrence = checkNotNull(recurrence, "recurrence");
    this.recurrenceGroupId = checkNotNull(recurrenceGroupId, "recurrenceGroupId");
  }

  public void update(String title, LocalDateTime startTime, LocalDateTime endTime) {
    this.title = checkNotBlank(title, TITLE);
    this.startTime = checkNotNull(startTime, START_TIME);
    this.endTime = checkNotNull(endTime, END_TIME);

    checkArgument(this.endTime.isAfter(this.startTime), END_TIME);
  }

  public void updateTime(String title, LocalTime newTime, long durationMinutes) {
    this.title = checkNotBlank(title, TITLE);
    this.startTime = this.startTime.toLocalDate().atTime(newTime);
    this.endTime = this.startTime.plusMinutes(durationMinutes);

    checkArgument(this.endTime.isAfter(this.startTime), END_TIME);
  }
}
