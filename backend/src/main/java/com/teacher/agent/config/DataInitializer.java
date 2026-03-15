package com.teacher.agent.config;

import com.teacher.agent.domain.Teacher;
import com.teacher.agent.domain.TeacherRepository;
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

    @Value("${app.initial-teacher.username}")
    private String initialUsername;

    @Value("${app.initial-teacher.password}")
    private String initialPassword;

    @Override
    @Transactional
    public void run(String... args) {
        String encodedPassword = passwordEncoder.encode(initialPassword);
        teacherRepository.findByUsername(initialUsername)
                .ifPresentOrElse(
                        teacher -> teacher.updatePassword(encodedPassword),
                        () -> teacherRepository.save(Teacher.create(initialUsername, encodedPassword))
                );
    }
}
