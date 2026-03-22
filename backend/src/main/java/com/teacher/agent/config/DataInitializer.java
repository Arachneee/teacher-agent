package com.teacher.agent.config;

import com.teacher.agent.domain.Teacher;
import com.teacher.agent.domain.repository.TeacherRepository;
import com.teacher.agent.domain.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

  private final TeacherRepository teacherRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${app.initial-teacher.user-id}")
  private String initialUserId;

  @Value("${app.initial-teacher.password}")
  private String initialPassword;

  @Value("${app.initial-teacher.name}")
  private String initialName;

  @Value("${app.initial-teacher.subject:}")
  private String initialSubject;

  @Override
  @Transactional
  public void run(String... args) {
    String encodedPassword = passwordEncoder.encode(initialPassword);

    teacherRepository.findByUserId(new UserId(initialUserId))
        .ifPresentOrElse(teacher -> teacher.updatePassword(encodedPassword), () -> teacherRepository
            .save(Teacher.create(initialUserId, encodedPassword, initialName, initialSubject)));
  }
}
