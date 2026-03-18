package com.teacher.agent.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

  List<Feedback> findAllByStudentId(Long studentId);

  Optional<Feedback> findByStudentIdAndLessonId(Long studentId, Long lessonId);

  @Query("SELECT f FROM Feedback f LEFT JOIN FETCH f.keywords WHERE f.id = :id")
  Optional<Feedback> findById(@Param("id") Long id);
}
