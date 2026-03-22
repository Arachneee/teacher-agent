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

  @Value("${app.warmup-teacher.user-id}")
  private String warmupUserId;

  @Value("${app.warmup-teacher.password}")
  private String warmupPassword;

  @Override
  @Transactional
  public void run(String... args) {
    upsertTeacher(initialUserId, initialPassword, initialName, initialSubject);
    upsertTeacher(warmupUserId, warmupPassword, "워밍업 계정", "");
  }

  private void upsertTeacher(String userId, String password, String name, String subject) {
    String encodedPassword = passwordEncoder.encode(password);
    teacherRepository.findByUserId(new UserId(userId))
        .ifPresentOrElse(
            teacher -> teacher.updatePassword(encodedPassword),
            () -> teacherRepository.save(Teacher.create(userId, encodedPassword, name, subject)));
  }
}
