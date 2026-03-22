package com.teacher.agent.service;

import static com.teacher.agent.util.ErrorMessages.SOME_STUDENTS_NOT_FOUND;

import com.teacher.agent.domain.Feedback;
import com.teacher.agent.domain.Lesson;
import com.teacher.agent.domain.Student;
import com.teacher.agent.domain.repository.FeedbackRepository;
import com.teacher.agent.domain.repository.LessonRepository;
import com.teacher.agent.domain.repository.StudentRepository;
import com.teacher.agent.domain.vo.Recurrence;
import com.teacher.agent.domain.vo.UpdateScope;
import com.teacher.agent.domain.vo.UserId;
import com.teacher.agent.dto.LessonResponse;
import com.teacher.agent.exception.BadRequestException;
import com.teacher.agent.exception.ResourceNotFoundException;
import com.teacher.agent.service.vo.LessonCreateCommand;
import com.teacher.agent.service.vo.LessonUpdateCommand;
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
  public LessonResponse create(UserId userId, LessonCreateCommand command) {
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
          com.teacher.agent.exception.ErrorCode.STUDENT_NOT_FOUND, SOME_STUDENTS_NOT_FOUND);
    }
  }

  @Transactional
  public LessonResponse update(UserId userId, Long id, LessonUpdateCommand command) {
    Lesson lesson = lessonQueryService.findByIdAndVerifyOwner(id, userId);

    if (command.recurrence() != null && lesson.getRecurrenceGroupId() == null) {
      return convertToRecurring(lesson, command);
    }

    UpdateScope scope = command.scope();
    List<Lesson> targets = lessonQueryService.findSeriesLessons(lesson, userId, scope);

    if (scope == UpdateScope.SINGLE) {
      lesson.update(command.title(), command.startTime(), command.endTime());
    } else {
      long durationMinutes = Duration.between(command.startTime(), command.endTime()).toMinutes();
      for (Lesson target : targets) {
        target.updateTime(command.title(), command.startTime().toLocalTime(), durationMinutes);
      }
    }

    applyAttendeeChanges(userId, targets, command);

    return LessonResponse.from(lesson);
  }

  private void applyAttendeeChanges(
      UserId userId, List<Lesson> targets, LessonUpdateCommand command) {
    List<Long> addStudentIds = command.addStudentIds();
    List<Long> removeStudentIds = command.removeStudentIds();

    if (addStudentIds.isEmpty() && removeStudentIds.isEmpty()) {
      return;
    }

    if (!addStudentIds.isEmpty()) {
      addAttendees(userId, targets, addStudentIds);
    }

    if (!removeStudentIds.isEmpty()) {
      removeAttendees(targets, removeStudentIds);
    }
  }

  private void addAttendees(UserId userId, List<Lesson> targets, List<Long> studentIds) {
    validateStudentOwnership(userId, studentIds);

    for (Lesson target : targets) {
      target.addAttendeesIfAbsent(studentIds);
    }

    lessonRepository.flush();

    for (Lesson target : targets) {
      createFeedbacksIfAbsent(target, studentIds);
    }
  }

  private void createFeedbacksIfAbsent(Lesson lesson, List<Long> studentIds) {
    for (Long studentId : studentIds) {
      boolean feedbackExists =
          feedbackRepository.findByStudentIdAndLessonId(studentId, lesson.getId()).isPresent();
      if (!feedbackExists) {
        feedbackRepository.save(Feedback.create(studentId, lesson.getId()));
      }
    }
  }

  private void removeAttendees(List<Lesson> targets, List<Long> studentIds) {
    for (Lesson target : targets) {
      target.removeAttendeesByStudentIds(studentIds);
    }
  }

  private LessonResponse convertToRecurring(Lesson lesson, LessonUpdateCommand command) {
    Recurrence recurrence = command.recurrence();

    lesson.update(command.title(), command.startTime(), command.endTime());

    var createCommand =
        new LessonCreateCommand(
            lesson.getUserId(),
            command.title(),
            command.startTime(),
            command.endTime(),
            recurrence,
            null);
    List<Lesson> generated = lessonFactory.createFrom(createCommand);

    if (generated.isEmpty()) {
      throw BadRequestException.noLessonGenerated();
    }

    UUID groupId = generated.getFirst().getRecurrenceGroupId();
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
