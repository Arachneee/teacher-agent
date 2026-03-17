package com.teacher.agent.service;

import static com.teacher.agent.util.RepositoryUtil.findStudentByIdAndUserIdOrThrow;

import com.teacher.agent.domain.Attendee;
import com.teacher.agent.domain.Lesson;
import com.teacher.agent.domain.LessonRepository;
import com.teacher.agent.domain.StudentRepository;
import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.AttendeeCreateRequest;
import com.teacher.agent.dto.AttendeeResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AttendeeCommandService {

  private final LessonQueryService lessonQueryService;
  private final LessonRepository lessonRepository;
  private final StudentRepository studentRepository;

  @Transactional
  public AttendeeResponse add(UserId userId, Long lessonId, AttendeeCreateRequest request) {
    Lesson lesson = lessonQueryService.findByIdAndVerifyOwner(lessonId, userId);
    findStudentByIdAndUserIdOrThrow(studentRepository, request.studentId(), userId);
    try {
      lesson.addAttendee(request.studentId());
    } catch (IllegalArgumentException exception) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
    }
    lessonRepository.flush();
    List<Attendee> attendees = lesson.getAttendees();
    return AttendeeResponse.from(attendees.get(attendees.size() - 1));
  }

  @Transactional
  public void remove(UserId userId, Long lessonId, Long attendeeId) {
    Lesson lesson = lessonQueryService.findByIdAndVerifyOwner(lessonId, userId);
    try {
      lesson.removeAttendee(attendeeId);
    } catch (IllegalArgumentException exception) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
    }
  }
}
