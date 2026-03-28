package com.teacher.agent.domain.repository;

import com.teacher.agent.domain.FeedbackLike;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackLikeRepository extends JpaRepository<FeedbackLike, Long> {

  void deleteAllByFeedbackId(Long feedbackId);

  long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
