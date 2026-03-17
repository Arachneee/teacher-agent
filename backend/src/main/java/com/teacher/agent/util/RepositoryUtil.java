package com.teacher.agent.util;

import com.teacher.agent.domain.Lesson;
import com.teacher.agent.domain.LessonRepository;
import com.teacher.agent.domain.Student;
import com.teacher.agent.domain.StudentRepository;
import com.teacher.agent.domain.Teacher;
import com.teacher.agent.domain.TeacherRepository;
import com.teacher.agent.domain.UserId;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@UtilityClass
public class RepositoryUtil {

  public static <T> T findByIdOrThrow(JpaRepository<T, Long> repository, Long id, String message) {
    return repository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, message));
  }

  public static Teacher findTeacherByUserIdOrThrow(TeacherRepository teacherRepository,
      UserId userId) {
    return teacherRepository.findByUserId(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Teacher not found: " + userId.value()));
  }

  public static Lesson findLessonByIdAndUserIdOrThrow(LessonRepository lessonRepository,
      Long lessonId, UserId userId) {
    return lessonRepository.findByIdAndUserId(lessonId, userId).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found: " + lessonId));
  }

  public static Student findStudentByIdAndUserIdOrThrow(StudentRepository studentRepository,
      Long studentId, UserId userId) {
    return studentRepository.findByIdAndUserId(studentId, userId).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found: " + studentId));
  }
}
