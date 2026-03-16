package com.teacher.agent.service;

import com.teacher.agent.domain.Teacher;
import com.teacher.agent.domain.TeacherRepository;
import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.TeacherResponse;
import com.teacher.agent.dto.TeacherUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeacherService {

    private final TeacherRepository teacherRepository;

    public TeacherResponse getByUserId(String userId) {
        return TeacherResponse.from(findByUserId(userId));
    }

    @Transactional
    public TeacherResponse updateByUserId(String userId, TeacherUpdateRequest request) {
        Teacher teacher = findByUserId(userId);
        teacher.updateProfile(request.name(), request.subject());
        return TeacherResponse.from(teacher);
    }

    private Teacher findByUserId(String userId) {
        return teacherRepository.findByUserId(new UserId(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found: " + userId));
    }
}
