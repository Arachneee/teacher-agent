package com.teacher.agent.service;

import com.teacher.agent.domain.Feedback;
import com.teacher.agent.domain.FeedbackRepository;
import com.teacher.agent.domain.Lesson;
import com.teacher.agent.domain.LessonRepository;
import com.teacher.agent.domain.Student;
import com.teacher.agent.domain.StudentRepository;
import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.LessonCreateRequest;
import com.teacher.agent.dto.LessonResponse;
import com.teacher.agent.dto.LessonUpdateRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class LessonCommandService {

  private final LessonQueryService lessonQueryService;
  private final LessonRepository lessonRepository;
  private final LessonFactory lessonFactory;
  private final StudentRepository studentRepository;
  private final FeedbackRepository feedbackRepository;

  @Transactional
  public LessonResponse create(UserId userId, LessonCreateRequest request) {
    List<Lesson> lessons = lessonFactory.createFrom(userId, request);
    if (lessons.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "선택한 기간과 요일 설정으로는 수업이 생성되지 않아요. 종료일을 늘리거나 요일을 변경해주세요.");
    }
    lessonRepository.saveAll(lessons);

    if (request.studentIds() != null && !request.studentIds().isEmpty()) {
      List<Long> studentIds = request.studentIds().stream().distinct().toList();
      validateStudentOwnership(userId, studentIds);

      for (Lesson lesson : lessons) {
        lesson.addAttendees(studentIds);
      }

      for (Lesson lesson : lessons) {
        for (Long studentId : studentIds) {
          feedbackRepository.save(Feedback.create(studentId, lesson.getId()));
        }
      }
    }

    return LessonResponse.from(lessons.get(0));
  }

  private void validateStudentOwnership(UserId userId, List<Long> studentIds) {
    List<Student> students = studentRepository.findAllByIdInAndUserId(studentIds, userId);
    if (students.size() != studentIds.size()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "일부 학생을 찾을 수 없습니다.");
    }
  }

  @Transactional
  public LessonResponse update(UserId userId, Long id, LessonUpdateRequest request) {
    Lesson lesson = lessonQueryService.findByIdAndVerifyOwner(id, userId);
    lesson.update(request.title(), request.startTime(), request.endTime());
    return LessonResponse.from(lesson);
  }

  @Transactional
  public void delete(UserId userId, Long id) {
    lessonQueryService.findByIdAndVerifyOwner(id, userId);
    lessonRepository.deleteById(id);
  }
}
