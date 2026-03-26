package com.teacher.agent.domain.repository;

import com.teacher.agent.domain.AiGenerationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiGenerationLogRepository extends JpaRepository<AiGenerationLog, Long> {
}
