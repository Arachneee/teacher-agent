package com.teacher.agent.domain.repository;

import com.teacher.agent.domain.UserEvent;
import com.teacher.agent.service.vo.DailyEventCountRow;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserEventRepository extends JpaRepository<UserEvent, Long> {

  long countByEventType(String eventType);

  long countByEventTypeAndCreatedAtBetween(
      String eventType, LocalDateTime start, LocalDateTime end);

  long countByEventTypeAndUserId(String eventType, String userId);

  @Query("SELECT COUNT(DISTINCT CAST(e.createdAt AS DATE)) FROM UserEvent e "
      + "WHERE e.createdAt BETWEEN :start AND :end")
  int countDistinctActiveDays(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

  @Query("SELECT COUNT(DISTINCT CAST(e.createdAt AS DATE)) FROM UserEvent e "
      + "WHERE e.createdAt BETWEEN :start AND :end AND e.userId = :userId")
  int countDistinctActiveDaysByUserId(
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end,
      @Param("userId") String userId);

  @Query("SELECT new com.teacher.agent.service.vo.DailyEventCountRow("
      + "CAST(e.createdAt AS DATE), e.eventType, COUNT(e)) FROM UserEvent e "
      + "WHERE e.createdAt BETWEEN :start AND :end "
      + "GROUP BY CAST(e.createdAt AS DATE), e.eventType "
      + "ORDER BY CAST(e.createdAt AS DATE)")
  List<DailyEventCountRow> findDailyEventCounts(
      @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

  @Query("SELECT new com.teacher.agent.service.vo.DailyEventCountRow("
      + "CAST(e.createdAt AS DATE), e.eventType, COUNT(e)) FROM UserEvent e "
      + "WHERE e.createdAt BETWEEN :start AND :end AND e.userId = :userId "
      + "GROUP BY CAST(e.createdAt AS DATE), e.eventType "
      + "ORDER BY CAST(e.createdAt AS DATE)")
  List<DailyEventCountRow> findDailyEventCountsByUserId(
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end,
      @Param("userId") String userId);
}
