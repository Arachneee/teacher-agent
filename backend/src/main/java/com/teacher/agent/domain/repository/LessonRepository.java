package com.teacher.agent.domain.repository;

import com.teacher.agent.domain.Lesson;
import com.teacher.agent.domain.vo.UserId;
import com.teacher.agent.service.vo.LessonDetailRow;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

  List<Lesson> findAllByUserId(UserId userId);

  List<Lesson> findAllByUserIdAndStartTimeBetween(UserId userId, LocalDateTime from,
      LocalDateTime to);

  Optional<Lesson> findByIdAndUserId(Long id, UserId userId);

  @EntityGraph(attributePaths = "attendees")
  Optional<Lesson> findWithAttendeesByIdAndUserId(Long id, UserId userId);

  @EntityGraph(attributePaths = "attendees")
  List<Lesson> findAllWithAttendeesByRecurrenceGroupIdAndUserId(UUID recurrenceGroupId,
      UserId userId);

  @EntityGraph(attributePaths = "attendees")
  List<Lesson> findAllWithAttendeesByRecurrenceGroupIdAndUserIdAndStartTimeGreaterThanEqual(
      UUID recurrenceGroupId, UserId userId, LocalDateTime startTime);

  List<Lesson> findAllByRecurrenceGroupIdAndUserId(UUID recurrenceGroupId, UserId userId);

  List<Lesson> findAllByRecurrenceGroupIdAndUserIdAndStartTimeGreaterThanEqual(
      UUID recurrenceGroupId, UserId userId, LocalDateTime startTime);

  @Query("""
      SELECT new com.teacher.agent.service.vo.LessonDetailRow(
          a.id,
          s.id,
          s.name,
          s.memo,
          s.grade,
          s.createdAt,
          s.updatedAt,
          f.id,
          f.studentId,
          f.lessonId,
          f.aiContent,
          f.instructions,
          f.liked,
          f.createdAt,
          f.updatedAt,
          fk.id,
          fk.keyword,
          fk.required,
          fk.createdAt
      )
      FROM Lesson l
      JOIN l.attendees a
      JOIN Student s ON s.id = a.studentId
      JOIN Feedback f ON f.studentId = a.studentId AND f.lessonId = l.id
      LEFT JOIN f.keywords fk
      WHERE l.id = :lessonId
        AND l.userId = :userId
      ORDER BY a.id, fk.id
      """)
  List<LessonDetailRow> findDetailRows(
      @Param("lessonId") Long lessonId,
      @Param("userId") UserId userId);
}
