package com.teacher.agent.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

  List<Student> findAllByUserId(UserId userId);

  Optional<Student> findByIdAndUserId(Long id, UserId userId);
}
