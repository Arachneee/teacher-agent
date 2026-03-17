package com.teacher.agent.domain;

import static com.teacher.agent.util.Parameter.*;
import static com.teacher.agent.util.ValidationUtil.*;

import jakarta.persistence.*;
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

  @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<Attendee> attendees = new ArrayList<>();

  public static Lesson create(UserId userId, String title, LocalDateTime startTime,
      LocalDateTime endTime) {
    Lesson lesson = new Lesson();
    checkNotNull(userId, USER_ID);
    lesson.userId = new UserId(checkNotBlank(userId.value(), USER_ID));
    lesson.title = checkNotBlank(title, TITLE);
    lesson.startTime = checkNotNull(startTime, START_TIME);
    lesson.endTime = checkNotNull(endTime, END_TIME);
    checkArgument(endTime.isAfter(startTime), END_TIME);
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
    checkNotBlank(title, TITLE);
    checkNotNull(startTime, START_TIME);
    checkNotNull(endTime, END_TIME);
    checkArgument(endTime.isAfter(startTime), END_TIME);
    this.title = title;
    this.startTime = startTime;
    this.endTime = endTime;
  }
}
