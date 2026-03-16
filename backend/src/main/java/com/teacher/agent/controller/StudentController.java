package com.teacher.agent.controller;

import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.StudentCreateRequest;
import com.teacher.agent.dto.StudentResponse;
import com.teacher.agent.dto.StudentUpdateRequest;
import com.teacher.agent.service.StudentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
@Validated
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    public ResponseEntity<StudentResponse> create(UserId userId, @RequestBody @Valid StudentCreateRequest request) {
        return ResponseEntity.status(201).body(studentService.create(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<StudentResponse>> getAll(UserId userId) {
        return ResponseEntity.ok(studentService.getAll(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getOne(UserId userId, @PathVariable @Positive Long id) {
        return ResponseEntity.ok(studentService.getOne(userId, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> update(UserId userId, @PathVariable @Positive Long id, @RequestBody @Valid StudentUpdateRequest request) {
        return ResponseEntity.ok(studentService.update(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(UserId userId, @PathVariable @Positive Long id) {
        studentService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
