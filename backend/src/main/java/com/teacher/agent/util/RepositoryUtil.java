package com.teacher.agent.util;

import com.teacher.agent.domain.Lesson;
import com.teacher.agent.domain.Student;
import com.teacher.agent.domain.Teacher;
import com.teacher.agent.domain.repository.LessonRepository;
import com.teacher.agent.domain.repository.StudentRepository;
import com.teacher.agent.domain.repository.TeacherRepository;
import com.teacher.agent.domain.vo.UserId;
import com.teacher.agent.exception.ResourceNotFoundException;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.repository.JpaRepository;

@UtilityClass
public class RepositoryUtil {

  public static <T, ID> T findByIdOrThrow(JpaRepository<T, ID> repository, ID id,
      ResourceNotFoundException exception) {
    return repository.findById(id).orElseThrow(() -> exception);
  }

  public static Teacher findTeacherByUserIdOrThrow(TeacherRepository teacherRepository,
      UserId userId) {
    return teacherRepository.findByUserId(userId)
        .orElseThrow(() -> ResourceNotFoundException.teacher(userId.value()));
  }

  public static Lesson findLessonByIdAndUserIdOrThrow(LessonRepository lessonRepository,
      Long lessonId, UserId userId) {
    return lessonRepository.findByIdAndUserId(lessonId, userId)
        .orElseThrow(() -> ResourceNotFoundException.lesson(lessonId));
  }

  public static Student findStudentByIdAndUserIdOrThrow(StudentRepository studentRepository,
      Long studentId, UserId userId) {
    return studentRepository.findByIdAndUserId(studentId, userId)
        .orElseThrow(() -> ResourceNotFoundException.student(studentId));
  }
}
