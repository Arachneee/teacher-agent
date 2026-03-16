package com.teacher.agent.service;

import com.teacher.agent.domain.Lesson;
import com.teacher.agent.domain.LessonRepository;
import com.teacher.agent.domain.Teacher;
import com.teacher.agent.domain.TeacherRepository;
import com.teacher.agent.dto.LessonCreateRequest;
import com.teacher.agent.dto.LessonResponse;
import com.teacher.agent.dto.LessonUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonService {

    private final LessonRepository lessonRepository;
    private final TeacherRepository teacherRepository;

    @Transactional
    public LessonResponse create(String username, LessonCreateRequest request) {
        Teacher teacher = findTeacherByUsername(username);
        Lesson lesson = request.toEntity(teacher.getId());
        return LessonResponse.from(lessonRepository.save(lesson));
    }

    public List<LessonResponse> getAllByTeacher(String username) {
        Teacher teacher = findTeacherByUsername(username);
        return lessonRepository.findAllByTeacherId(teacher.getId()).stream()
                .map(LessonResponse::from)
                .toList();
    }

    public LessonResponse getOne(Long id) {
        return LessonResponse.from(findById(id));
    }

    @Transactional
    public LessonResponse update(String username, Long id, LessonUpdateRequest request) {
        Lesson lesson = findByIdAndVerifyOwner(id, username);
        lesson.update(request.title(), request.startTime(), request.endTime());
        return LessonResponse.from(lesson);
    }

    @Transactional
    public void delete(String username, Long id) {
        findByIdAndVerifyOwner(id, username);
        lessonRepository.deleteById(id);
    }

    private Lesson findById(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found: " + id));
    }

    private Lesson findByIdAndVerifyOwner(Long id, String username) {
        Lesson lesson = findById(id);
        Teacher teacher = findTeacherByUsername(username);
        if (!lesson.getTeacherId().equals(teacher.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Lesson does not belong to this teacher");
        }
        return lesson;
    }

    private Teacher findTeacherByUsername(String username) {
        return teacherRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found: " + username));
    }
}
