package com.teacher.agent.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackKeywordRepository extends JpaRepository<FeedbackKeyword, Long> {

    List<FeedbackKeyword> findAllByFeedbackId(Long feedbackId);
}
