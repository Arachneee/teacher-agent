package com.teacher.agent.domain.repository;

import com.teacher.agent.domain.AiGenerationLog;
import com.teacher.agent.domain.vo.UserId;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AiGenerationLogRepository extends JpaRepository<AiGenerationLog, Long> {

  long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

  @Query("SELECT COALESCE(AVG(a.durationMs), 0) FROM AiGenerationLog a")
  double averageDurationMs();

  @Query("SELECT COALESCE(AVG(a.durationMs), 0) FROM AiGenerationLog a "
      + "WHERE a.createdAt BETWEEN :start AND :end")
  double averageDurationMsBetween(
      @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

  @Query("SELECT COUNT(a) FROM AiGenerationLog a "
      + "WHERE a.feedbackId IN ("
      + "SELECT f.id FROM Feedback f "
      + "WHERE f.studentId IN ("
      + "SELECT s.id FROM Student s WHERE s.userId = :userId))")
  long countByTeacherId(@Param("userId") UserId userId);

  @Query("SELECT COALESCE(AVG(a.durationMs), 0) FROM AiGenerationLog a "
      + "WHERE a.feedbackId IN ("
      + "SELECT f.id FROM Feedback f "
      + "WHERE f.studentId IN ("
      + "SELECT s.id FROM Student s WHERE s.userId = :userId))")
  double averageDurationMsByTeacherId(@Param("userId") UserId userId);
}
