package com.teacher.agent.domain;

import com.teacher.agent.dto.LessonDetailRow;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

  List<Lesson> findAllByUserId(UserId userId);

  List<Lesson> findAllByUserIdAndStartTimeBetween(UserId userId, LocalDateTime from,
      LocalDateTime to);

  Optional<Lesson> findByIdAndUserId(Long id, UserId userId);

  @Query("SELECT new com.teacher.agent.dto.LessonDetailRow(" + " a.id,"
      + " s.id, s.name, s.memo, s.createdAt, s.updatedAt,"
      + " f.id, f.studentId, f.lessonId, f.aiContent, f.liked, f.createdAt, f.updatedAt,"
      + " fk.id, fk.keyword, fk.createdAt)" + " FROM Lesson l"
      + " JOIN l.attendees a, Student s, Feedback f" + " LEFT JOIN f.keywords fk"
      + " WHERE s.id = a.studentId" + " AND f.studentId = a.studentId AND f.lessonId = l.id"
      + " AND l.id = :lessonId AND l.userId = :userId" + " ORDER BY a.id, fk.id")
  List<LessonDetailRow> findDetailRows(@Param("lessonId") Long lessonId,
      @Param("userId") UserId userId);
}
