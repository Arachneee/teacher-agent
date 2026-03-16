package com.teacher.agent.service;

import com.teacher.agent.domain.Attendee;
import com.teacher.agent.domain.Lesson;
import com.teacher.agent.domain.LessonRepository;
import com.teacher.agent.domain.StudentRepository;
import com.teacher.agent.domain.Teacher;
import com.teacher.agent.domain.TeacherRepository;
import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.AttendeeCreateRequest;
import com.teacher.agent.dto.AttendeeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendeeService {

  private final LessonRepository lessonRepository;
  private final TeacherRepository teacherRepository;
  private final StudentRepository studentRepository;

  @Transactional
  public AttendeeResponse add(UserId userId, Long lessonId, AttendeeCreateRequest request) {
    Lesson lesson = findLessonByIdAndVerifyOwner(lessonId, userId);
    Teacher teacher = findTeacherByUserId(userId);
    findStudentByIdAndTeacher(request.studentId(), teacher.getId());
    try {
      lesson.addAttendee(request.studentId());
    } catch (IllegalArgumentException exception) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
    }
    lessonRepository.flush();
    List<Attendee> attendees = lesson.getAttendees();
    return AttendeeResponse.from(attendees.get(attendees.size() - 1));
  }

  public List<AttendeeResponse> getAll(UserId userId, Long lessonId) {
    Lesson lesson = findLessonByIdAndVerifyOwner(lessonId, userId);
    return lesson.getAttendees().stream().map(AttendeeResponse::from).toList();
  }

  @Transactional
  public void remove(UserId userId, Long lessonId, Long attendeeId) {
    Lesson lesson = findLessonByIdAndVerifyOwner(lessonId, userId);
    try {
      lesson.removeAttendee(attendeeId);
    } catch (IllegalArgumentException exception) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
    }
  }

  private Lesson findLessonById(Long lessonId) {
    return lessonRepository.findById(lessonId).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found: " + lessonId));
  }

  private Lesson findLessonByIdAndVerifyOwner(Long lessonId, UserId userId) {
    Lesson lesson = findLessonById(lessonId);
    Teacher teacher = findTeacherByUserId(userId);
    if (!lesson.getTeacherId().equals(teacher.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
          "Lesson does not belong to this teacher");
    }
    return lesson;
  }

  private Teacher findTeacherByUserId(UserId userId) {
    return teacherRepository.findByUserId(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Teacher not found: " + userId.value()));
  }

  private void findStudentByIdAndTeacher(Long studentId, Long teacherId) {
    studentRepository.findByIdAndTeacherId(studentId, teacherId).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found: " + studentId));
  }
}
