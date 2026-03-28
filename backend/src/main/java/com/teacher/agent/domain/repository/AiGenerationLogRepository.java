package com.teacher.agent.domain.repository;

import com.teacher.agent.domain.AiGenerationLog;
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
}
