package com.teacher.agent.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

  List<Feedback> findAllByStudentId(Long studentId);
}
