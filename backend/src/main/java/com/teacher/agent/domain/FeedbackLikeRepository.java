package com.teacher.agent.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackLikeRepository extends JpaRepository<FeedbackLike, Long> {

  void deleteAllByFeedbackId(Long feedbackId);
}
