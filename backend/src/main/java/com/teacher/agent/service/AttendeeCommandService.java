package com.teacher.agent.service;

import static com.teacher.agent.util.RepositoryUtil.findStudentByIdAndUserIdOrThrow;

import com.teacher.agent.domain.Attendee;
import com.teacher.agent.domain.Feedback;
import com.teacher.agent.domain.FeedbackRepository;
import com.teacher.agent.domain.Lesson;
import com.teacher.agent.domain.LessonRepository;
import com.teacher.agent.domain.StudentRepository;
import com.teacher.agent.domain.UpdateScope;
import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.AttendeeResponse;
import com.teacher.agent.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendeeCommandService {

  private final LessonQueryService lessonQueryService;
  private final LessonRepository lessonRepository;
  private final StudentRepository studentRepository;
  private final FeedbackRepository feedbackRepository;

  @Transactional
  public AttendeeResponse add(UserId userId, Long lessonId, Long studentId, UpdateScope scope) {
    Lesson lesson = lessonQueryService.findByIdAndVerifyOwner(lessonId, userId);
    findStudentByIdAndUserIdOrThrow(studentRepository, studentId, userId);

    List<Lesson> targets = lessonQueryService.findSeriesLessons(lesson, userId, scope);

    for (Lesson target : targets) {
      try {
        target.addAttendee(studentId);
      } catch (IllegalArgumentException e) {
        log.warn("수강생 추가 실패 - 이미 등록됨: lessonId={}, studentId={}", target.getId(), studentId);
      }
    }

    lessonRepository.flush();

    for (Lesson target : targets) {
      if (feedbackRepository.findByStudentIdAndLessonId(studentId, target.getId()).isEmpty()) {
        feedbackRepository.save(Feedback.create(studentId, target.getId()));
      }
    }

    List<Attendee> attendees = lesson.getAttendees();
    return AttendeeResponse.from(attendees.getLast());
  }

  @Transactional
  public void remove(UserId userId, Long lessonId, Long attendeeId, UpdateScope scope) {
    Lesson lesson = lessonQueryService.findByIdAndVerifyOwner(lessonId, userId);

    Attendee attendee =
        lesson.getAttendees().stream()
            .filter(a -> Objects.equals(a.getId(), attendeeId))
            .findFirst()
            .orElseThrow(() -> ResourceNotFoundException.attendee(attendeeId));

    Long studentId = attendee.getStudentId();
    UpdateScope resolvedScope = scope != null ? scope : UpdateScope.SINGLE;

    List<Lesson> targets = lessonQueryService.findSeriesLessons(lesson, userId, resolvedScope);

    for (Lesson target : targets) {
      target.getAttendees().stream()
          .filter(a -> Objects.equals(a.getStudentId(), studentId))
          .findFirst()
          .ifPresent(a -> target.removeAttendee(a.getId()));
    }
  }
}
