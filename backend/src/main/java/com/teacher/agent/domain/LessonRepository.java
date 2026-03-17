package com.teacher.agent.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

  List<Lesson> findAllByUserId(UserId userId);

  Optional<Lesson> findByIdAndUserId(Long id, UserId userId);
}
