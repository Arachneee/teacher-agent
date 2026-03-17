package com.teacher.agent.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

  List<Lesson> findAllByUserId(UserId userId);

  Optional<Lesson> findByIdAndUserId(Long id, UserId userId);
}
