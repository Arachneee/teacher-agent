package com.teacher.agent.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {

  List<Student> findAllByUserId(UserId userId);

  Optional<Student> findByIdAndUserId(Long id, UserId userId);

  List<Student> findAllByIdInAndUserId(List<Long> ids, UserId userId);
}
