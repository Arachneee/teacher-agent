package com.teacher.agent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.teacher.agent.domain.Lesson;
import com.teacher.agent.domain.LessonRepository;
import com.teacher.agent.domain.Student;
import com.teacher.agent.domain.StudentRepository;
import com.teacher.agent.domain.Teacher;
import com.teacher.agent.domain.TeacherRepository;
import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.AttendeeCreateRequest;
import com.teacher.agent.dto.AttendeeResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({LessonQueryService.class, AttendeeQueryService.class, AttendeeCommandService.class})
class AttendeeServiceTest {

  @Autowired
  private AttendeeQueryService attendeeQueryService;

  @Autowired
  private AttendeeCommandService attendeeCommandService;

  @Autowired
  private LessonRepository lessonRepository;

  @Autowired
  private TeacherRepository teacherRepository;

  @Autowired
  private StudentRepository studentRepository;

  private static final LocalDateTime START = LocalDateTime.of(2026, 3, 16, 9, 0);
  private static final LocalDateTime END = LocalDateTime.of(2026, 3, 16, 10, 0);

  private Teacher teacher;
  private Student student;
  private UserId userId;

  @BeforeEach
  void setUp() {
    teacher =
        teacherRepository.save(Teacher.create("testteacher", "encodedPassword", "테스트 선생님", ""));
    userId = teacher.getUserId();
    student = studentRepository.save(Student.create(teacher.getUserId(), "홍길동", "메모"));
  }

  @AfterEach
  void tearDown() {
    lessonRepository.deleteAll();
    studentRepository.deleteAllInBatch();
    teacherRepository.deleteAllInBatch();
  }

  private Lesson saveLesson() {
    return lessonRepository.save(Lesson.create(teacher.getUserId(), "수학", START, END));
  }

  @Test
  void 수업에_학생을_추가한다() {
    Lesson lesson = saveLesson();

    AttendeeResponse response = attendeeCommandService.add(userId, lesson.getId(),
        new AttendeeCreateRequest(student.getId()));

    assertThat(response.id()).isNotNull();
    assertThat(response.lessonId()).isEqualTo(lesson.getId());
    assertThat(response.studentId()).isEqualTo(student.getId());
    assertThat(response.createdAt()).isNotNull();
  }

  @Test
  void 존재하지_않는_수업에_학생_추가_시_예외가_발생한다() {
    assertThatThrownBy(
        () -> attendeeCommandService.add(userId, 999L, new AttendeeCreateRequest(student.getId())))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(exception -> assertThat(((ResponseStatusException) exception).getStatusCode())
            .isEqualTo(NOT_FOUND));
  }

  @Test
  void 다른_교사의_수업에_학생_추가_시_예외가_발생한다() {
    Teacher otherTeacher =
        teacherRepository.save(Teacher.create("otherteacher", "encodedPassword", "다른 선생님", ""));
    Lesson lesson = saveLesson();

    assertThatThrownBy(() -> attendeeCommandService.add(otherTeacher.getUserId(), lesson.getId(),
        new AttendeeCreateRequest(student.getId()))).isInstanceOf(ResponseStatusException.class)
        .satisfies(exception -> assertThat(((ResponseStatusException) exception).getStatusCode())
            .isEqualTo(NOT_FOUND));
  }

  @Test
  void 존재하지_않는_학생_추가_시_예외가_발생한다() {
    Lesson lesson = saveLesson();

    assertThatThrownBy(
        () -> attendeeCommandService.add(userId, lesson.getId(), new AttendeeCreateRequest(999L)))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(exception -> assertThat(((ResponseStatusException) exception).getStatusCode())
            .isEqualTo(NOT_FOUND));
  }

  @Test
  void 이미_추가된_학생_추가_시_예외가_발생한다() {
    Lesson lesson = saveLesson();
    attendeeCommandService.add(userId, lesson.getId(), new AttendeeCreateRequest(student.getId()));

    assertThatThrownBy(() -> attendeeCommandService.add(userId, lesson.getId(),
        new AttendeeCreateRequest(student.getId()))).isInstanceOf(ResponseStatusException.class)
        .satisfies(exception -> assertThat(((ResponseStatusException) exception).getStatusCode())
            .isEqualTo(CONFLICT));
  }

  @Test
  void 수업의_참가자_목록을_조회한다() {
    Lesson lesson = saveLesson();
    Student anotherStudent =
        studentRepository.save(Student.create(teacher.getUserId(), "김철수", null));
    attendeeCommandService.add(userId, lesson.getId(), new AttendeeCreateRequest(student.getId()));
    attendeeCommandService.add(userId, lesson.getId(),
        new AttendeeCreateRequest(anotherStudent.getId()));

    List<AttendeeResponse> attendees = attendeeQueryService.getAll(userId, lesson.getId());

    assertThat(attendees).hasSize(2);
  }

  @Test
  void 다른_수업의_참가자는_조회되지_않는다() {
    Lesson lesson1 = saveLesson();
    Lesson lesson2 = lessonRepository.save(Lesson.create(teacher.getUserId(), "영어", START, END));
    Student anotherStudent =
        studentRepository.save(Student.create(teacher.getUserId(), "김철수", null));
    attendeeCommandService.add(userId, lesson1.getId(), new AttendeeCreateRequest(student.getId()));
    attendeeCommandService.add(userId, lesson2.getId(),
        new AttendeeCreateRequest(anotherStudent.getId()));

    List<AttendeeResponse> attendees = attendeeQueryService.getAll(userId, lesson1.getId());

    assertThat(attendees).hasSize(1);
    assertThat(attendees.get(0).studentId()).isEqualTo(student.getId());
  }

  @Test
  void 수업_참가자를_삭제한다() {
    Lesson lesson = saveLesson();
    AttendeeResponse added = attendeeCommandService.add(userId, lesson.getId(),
        new AttendeeCreateRequest(student.getId()));

    attendeeCommandService.remove(userId, lesson.getId(), added.id());

    List<AttendeeResponse> attendees = attendeeQueryService.getAll(userId, lesson.getId());
    assertThat(attendees).isEmpty();
  }

  @Test
  void 존재하지_않는_참가자_삭제_시_예외가_발생한다() {
    Lesson lesson = saveLesson();

    assertThatThrownBy(() -> attendeeCommandService.remove(userId, lesson.getId(), 999L))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(exception -> assertThat(((ResponseStatusException) exception).getStatusCode())
            .isEqualTo(NOT_FOUND));
  }

  @Test
  void 다른_교사의_수업_참가자_삭제_시_예외가_발생한다() {
    Teacher otherTeacher =
        teacherRepository.save(Teacher.create("otherteacher2", "encodedPassword", "다른 선생님2", ""));
    Lesson lesson = saveLesson();
    AttendeeResponse added = attendeeCommandService.add(userId, lesson.getId(),
        new AttendeeCreateRequest(student.getId()));

    assertThatThrownBy(
        () -> attendeeCommandService.remove(otherTeacher.getUserId(), lesson.getId(), added.id()))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(exception -> assertThat(((ResponseStatusException) exception).getStatusCode())
            .isEqualTo(NOT_FOUND));
  }
}
