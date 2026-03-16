package com.teacher.agent.controller;

import com.teacher.agent.dto.TeacherResponse;
import com.teacher.agent.dto.TeacherUpdateRequest;
import com.teacher.agent.service.TeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping("/me")
    public ResponseEntity<TeacherResponse> getMe(Authentication authentication) {
        return ResponseEntity.ok(teacherService.getByUserId(authentication.getName()));
    }

    @PutMapping("/me")
    public ResponseEntity<TeacherResponse> updateMe(
            Authentication authentication,
            @RequestBody @Valid TeacherUpdateRequest request) {
        return ResponseEntity.ok(teacherService.updateByUserId(authentication.getName(), request));
    }
}
