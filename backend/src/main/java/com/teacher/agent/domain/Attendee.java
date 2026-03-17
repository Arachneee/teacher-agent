package com.teacher.agent.domain;

import static com.teacher.agent.util.Parameter.LESSON_ID;
import static com.teacher.agent.util.Parameter.STUDENT_ID;
import static com.teacher.agent.util.ValidationUtil.checkNotNull;
import static com.teacher.agent.util.ValidationUtil.checkPositive;
import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_attendee_lesson_student",
    columnNames = {"lesson_id", "student_id"}))
public class Attendee extends BaseEntity {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private Lesson lesson;

  @Column(nullable = false)
  private Long studentId;

  public static Attendee create(Lesson lesson, Long studentId) {
    Attendee attendee = new Attendee();
    attendee.lesson = checkNotNull(lesson, LESSON_ID);
    attendee.studentId = checkPositive(studentId, STUDENT_ID);
    return attendee;
  }
}
