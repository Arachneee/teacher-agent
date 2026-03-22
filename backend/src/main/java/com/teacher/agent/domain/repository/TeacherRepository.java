package com.teacher.agent.domain.repository;

import com.teacher.agent.domain.Teacher;
import com.teacher.agent.domain.vo.UserId;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
  Optional<Teacher> findByUserId(UserId userId);
}
