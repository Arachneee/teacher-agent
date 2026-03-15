package com.teacher.agent.service;

import com.teacher.agent.domain.Student;
import com.teacher.agent.dto.StudentCreateRequest;
import com.teacher.agent.dto.StudentResponse;
import com.teacher.agent.dto.StudentUpdateRequest;
import com.teacher.agent.domain.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;

    @Transactional
    public StudentResponse create(StudentCreateRequest request) {
        return StudentResponse.from(studentRepository.save(request.toEntity()));
    }

    public List<StudentResponse> getAll() {
        return studentRepository.findAll().stream()
                .map(StudentResponse::from)
                .toList();
    }

    public StudentResponse getOne(Long id) {
        return StudentResponse.from(findById(id));
    }

    @Transactional
    public StudentResponse update(Long id, StudentUpdateRequest request) {
        Student student = findById(id);
        student.update(request.name(), request.memo());
        return StudentResponse.from(student);
    }

    @Transactional
    public void delete(Long id) {
        findById(id);
        studentRepository.deleteById(id);
    }

    private Student findById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found: " + id));
    }
}
