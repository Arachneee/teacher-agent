package com.teacher.agent.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

  List<Lesson> findAllByTeacherId(Long teacherId);
}
