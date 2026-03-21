package com.teacher.agent.domain;

import static com.teacher.agent.util.Parameter.*;
import static com.teacher.agent.util.ValidationUtil.*;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = @Index(name = "idx_lesson_user_id", columnList = "userId"))
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

  @Embedded private Recurrence recurrence;

  public static Lesson create(
      UserId userId, String title, LocalDateTime startTime, LocalDateTime endTime) {
    return create(userId, title, startTime, endTime, null);
  }

  public static Lesson create(
      UserId userId,
      String title,
      LocalDateTime startTime,
      LocalDateTime endTime,
      Recurrence recurrence) {
    Lesson lesson = new Lesson();

    lesson.userId = checkNotNull(userId, USER_ID);
    lesson.title = checkNotBlank(title, TITLE);
    lesson.startTime = checkNotNull(startTime, START_TIME);
    lesson.endTime = checkNotNull(endTime, END_TIME);
    lesson.recurrence = recurrence;

    checkArgument(lesson.endTime.isAfter(lesson.startTime), END_TIME);

    return lesson;
  }

  public void addAttendee(Long studentId) {
    boolean isDuplicate =
        attendees.stream().anyMatch(attendee -> attendee.getStudentId().equals(studentId));

    if (isDuplicate) {
      throw new IllegalArgumentException("Attendee already exists: " + studentId);
    }

    attendees.add(Attendee.create(this, studentId));
  }

  public void removeAttendee(Long attendeeId) {
    boolean removed = attendees.removeIf(attendee -> Objects.equals(attendee.getId(), attendeeId));

    if (!removed) {
      throw new IllegalArgumentException("Attendee not found: " + attendeeId);
    }
  }

  public void update(String title, LocalDateTime startTime, LocalDateTime endTime) {
    this.title = checkNotBlank(title, TITLE);
    this.startTime = checkNotNull(startTime, START_TIME);
    this.endTime = checkNotNull(endTime, END_TIME);

    checkArgument(this.endTime.isAfter(this.startTime), END_TIME);
  }
}
