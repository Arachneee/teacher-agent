package com.teacher.agent.service;

import com.teacher.agent.domain.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeacherUserDetailsService implements UserDetailsService {

    private final TeacherRepository teacherRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        return teacherRepository.findByUserId(userId)
                .map(teacher -> User.builder()
                        .username(teacher.getUserId())
                        .password(teacher.getPassword())
                        .roles("TEACHER")
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Teacher not found: " + userId));
    }
}
