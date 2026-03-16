package com.teacher.agent.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static com.teacher.agent.util.Parameter.*;
import static com.teacher.agent.util.ValidationUtil.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = @Index(name = "idx_lesson_teacher_id", columnList = "teacherId"))
public class Lesson extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long teacherId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    public static Lesson create(Long teacherId, String title, LocalDateTime startTime, LocalDateTime endTime) {
        Lesson lesson = new Lesson();
        lesson.teacherId = checkPositive(teacherId, TEACHER_ID);
        lesson.title = checkNotBlank(title, TITLE);
        lesson.startTime = checkNotNull(startTime, START_TIME);
        lesson.endTime = checkNotNull(endTime, END_TIME);
        checkArgument(endTime.isAfter(startTime), END_TIME);
        return lesson;
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
