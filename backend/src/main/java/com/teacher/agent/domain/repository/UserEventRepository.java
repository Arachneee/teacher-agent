package com.teacher.agent.domain.repository;

import com.teacher.agent.domain.UserEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserEventRepository extends JpaRepository<UserEvent, Long> {
}
