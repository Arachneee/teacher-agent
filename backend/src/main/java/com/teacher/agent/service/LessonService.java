package com.teacher.agent.service;

import com.teacher.agent.domain.Lesson;
import com.teacher.agent.domain.LessonRepository;
import com.teacher.agent.domain.Teacher;
import com.teacher.agent.domain.TeacherRepository;
import com.teacher.agent.domain.UserId;
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
    public LessonResponse create(UserId userId, LessonCreateRequest request) {
        Teacher teacher = findTeacherByUserId(userId);
        Lesson lesson = request.toEntity(teacher.getId());
        return LessonResponse.from(lessonRepository.save(lesson));
    }

    public List<LessonResponse> getAllByTeacher(UserId userId) {
        Teacher teacher = findTeacherByUserId(userId);
        return lessonRepository.findAllByTeacherId(teacher.getId()).stream()
                .map(LessonResponse::from)
                .toList();
    }

    public LessonResponse getOne(UserId userId, Long id) {
        return LessonResponse.from(findByIdAndVerifyOwner(id, userId));
    }

    @Transactional
    public LessonResponse update(UserId userId, Long id, LessonUpdateRequest request) {
        Lesson lesson = findByIdAndVerifyOwner(id, userId);
        lesson.update(request.title(), request.startTime(), request.endTime());
        return LessonResponse.from(lesson);
    }

    @Transactional
    public void delete(UserId userId, Long id) {
        findByIdAndVerifyOwner(id, userId);
        lessonRepository.deleteById(id);
    }

    private Lesson findById(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found: " + id));
    }

    private Lesson findByIdAndVerifyOwner(Long id, UserId userId) {
        Lesson lesson = findById(id);
        Teacher teacher = findTeacherByUserId(userId);
        if (!lesson.getTeacherId().equals(teacher.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Lesson does not belong to this teacher");
        }
        return lesson;
    }

    private Teacher findTeacherByUserId(UserId userId) {
        return teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found: " + userId));
    }
}
