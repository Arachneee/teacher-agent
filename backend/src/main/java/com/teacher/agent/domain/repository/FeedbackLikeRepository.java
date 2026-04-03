package com.teacher.agent.domain.repository;

import com.teacher.agent.domain.FeedbackLike;
import com.teacher.agent.domain.vo.UserId;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeedbackLikeRepository extends JpaRepository<FeedbackLike, Long> {

  void deleteAllByFeedbackId(Long feedbackId);

  long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

  @Query("SELECT fl FROM FeedbackLike fl WHERE fl.feedbackId IN "
      + "(SELECT f.id FROM Feedback f WHERE f.studentId IN "
      + "(SELECT s.id FROM Student s WHERE s.userId = :userId)) "
      + "ORDER BY fl.createdAt DESC LIMIT 3")
  List<FeedbackLike> findRecentLikedByUserId(@Param("userId") UserId userId);
}
