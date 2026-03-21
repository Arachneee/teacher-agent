package com.teacher.agent.service;

import com.teacher.agent.domain.Feedback;
import com.teacher.agent.domain.FeedbackRepository;
import com.teacher.agent.domain.Lesson;
import com.teacher.agent.domain.LessonRepository;
import com.teacher.agent.domain.Recurrence;
import com.teacher.agent.domain.Student;
import com.teacher.agent.domain.StudentRepository;
import com.teacher.agent.domain.UpdateScope;
import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.LessonCreateCommand;
import com.teacher.agent.dto.LessonCreateRequest;
import com.teacher.agent.dto.LessonResponse;
import com.teacher.agent.dto.LessonUpdateRequest;
import com.teacher.agent.exception.BadRequestException;
import com.teacher.agent.exception.ResourceNotFoundException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    var command = request.toCommand(userId);
    List<Lesson> lessons = lessonFactory.createFrom(command);
    if (lessons.isEmpty()) {
      throw BadRequestException.noLessonGenerated();
    }
    lessonRepository.saveAll(lessons);

    if (command.studentIds() != null && !command.studentIds().isEmpty()) {
      List<Long> studentIds = command.studentIds().stream().distinct().toList();
      validateStudentOwnership(userId, studentIds);

      for (Lesson lesson : lessons) {
        lesson.addAttendees(studentIds);
        feedbackRepository.saveAll(Feedback.createAll(studentIds, lesson.getId()));
      }
    }

    return LessonResponse.from(lessons.get(0));
  }

  private void validateStudentOwnership(UserId userId, List<Long> studentIds) {
    List<Student> students = studentRepository.findAllByIdInAndUserId(studentIds, userId);
    if (students.size() != studentIds.size()) {
      throw new ResourceNotFoundException(
          com.teacher.agent.exception.ErrorCode.STUDENT_NOT_FOUND, "일부 학생을 찾을 수 없습니다.");
    }
  }

  @Transactional
  public LessonResponse update(UserId userId, Long id, LessonUpdateRequest request) {
    Lesson lesson = lessonQueryService.findByIdAndVerifyOwner(id, userId);

    if (request.recurrence() != null && lesson.getRecurrenceGroupId() == null) {
      return convertToRecurring(lesson, request);
    }

    UpdateScope scope = request.resolvedScope();
    List<Lesson> targets = lessonQueryService.findSeriesLessons(lesson, userId, scope);

    if (scope == UpdateScope.SINGLE) {
      lesson.update(request.title(), request.startTime(), request.endTime());
    } else {
      long durationMinutes = Duration.between(request.startTime(), request.endTime()).toMinutes();
      for (Lesson target : targets) {
        target.updateTime(request.title(), request.startTime().toLocalTime(), durationMinutes);
      }
    }

    applyAttendeeChanges(userId, targets, request);

    return LessonResponse.from(lesson);
  }

  private void applyAttendeeChanges(
      UserId userId, List<Lesson> targets, LessonUpdateRequest request) {
    List<Long> addStudentIds = request.resolvedAddStudentIds();
    List<Long> removeStudentIds = request.resolvedRemoveStudentIds();

    if (addStudentIds.isEmpty() && removeStudentIds.isEmpty()) {
      return;
    }

    if (!addStudentIds.isEmpty()) {
      validateStudentOwnership(userId, addStudentIds);
      for (Lesson target : targets) {
        for (Long studentId : addStudentIds) {
          try {
            target.addAttendee(studentId);
          } catch (IllegalArgumentException ignored) {
          }
        }
      }
      lessonRepository.flush();
      for (Lesson target : targets) {
        for (Long studentId : addStudentIds) {
          if (feedbackRepository.findByStudentIdAndLessonId(studentId, target.getId()).isEmpty()) {
            feedbackRepository.save(Feedback.create(studentId, target.getId()));
          }
        }
      }
    }

    if (!removeStudentIds.isEmpty()) {
      for (Lesson target : targets) {
        for (Long studentId : removeStudentIds) {
          target.getAttendees().stream()
              .filter(a -> a.getStudentId().equals(studentId))
              .findFirst()
              .ifPresent(a -> target.removeAttendee(a.getId()));
        }
      }
    }
  }

  private LessonResponse convertToRecurring(Lesson lesson, LessonUpdateRequest request) {
    Recurrence recurrence = request.recurrence().toEntity();

    lesson.update(request.title(), request.startTime(), request.endTime());

    var command =
        new LessonCreateCommand(
            lesson.getUserId(),
            request.title(),
            request.startTime(),
            request.endTime(),
            recurrence,
            null);
    List<Lesson> generated = lessonFactory.createFrom(command);

    if (generated.isEmpty()) {
      throw BadRequestException.noLessonGenerated();
    }

    UUID groupId = generated.get(0).getRecurrenceGroupId();
    lesson.convertToRecurring(recurrence, groupId);

    List<Lesson> additionalLessons =
        generated.stream()
            .filter(
                l -> !l.getStartTime().toLocalDate().equals(lesson.getStartTime().toLocalDate()))
            .toList();

    if (!additionalLessons.isEmpty()) {
      lessonRepository.saveAll(additionalLessons);
    }

    return LessonResponse.from(lesson);
  }

  @Transactional
  public void delete(UserId userId, Long id, UpdateScope scope) {
    Lesson lesson = lessonQueryService.findByIdAndVerifyOwner(id, userId);

    if (scope == null || scope == UpdateScope.SINGLE) {
      feedbackRepository.deleteAllByLessonIdInAndAiContentIsNull(List.of(id));
      lessonRepository.deleteById(id);
      return;
    }

    List<Lesson> targets = lessonQueryService.findSeriesLessons(lesson, userId, scope);
    List<Long> targetIds = targets.stream().map(Lesson::getId).toList();

    feedbackRepository.deleteAllByLessonIdInAndAiContentIsNull(targetIds);
    lessonRepository.deleteAllById(targetIds);
  }
}
