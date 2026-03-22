package com.teacher.agent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.teacher.agent.domain.Lesson;
import com.teacher.agent.domain.Student;
import com.teacher.agent.domain.Teacher;
import com.teacher.agent.domain.repository.FeedbackRepository;
import com.teacher.agent.domain.repository.LessonRepository;
import com.teacher.agent.domain.repository.StudentRepository;
import com.teacher.agent.domain.repository.TeacherRepository;
import com.teacher.agent.domain.vo.RecurrenceType;
import com.teacher.agent.domain.vo.UpdateScope;
import com.teacher.agent.domain.vo.UserId;
import com.teacher.agent.dto.AttendeeResponse;
import com.teacher.agent.dto.LessonCreateRequest;
import com.teacher.agent.dto.RecurrenceCreateRequest;
import com.teacher.agent.exception.ResourceNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class AttendeeServiceTest {

  @Autowired
  private AttendeeQueryService attendeeQueryService;

  @Autowired
  private AttendeeCommandService attendeeCommandService;

  @Autowired
  private LessonCommandService lessonCommandService;

  @Autowired
  private LessonRepository lessonRepository;

  @Autowired
  private TeacherRepository teacherRepository;

  @Autowired
  private StudentRepository studentRepository;

  @Autowired
  private FeedbackRepository feedbackRepository;

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

  private Lesson saveLesson() {
    return lessonRepository.save(Lesson.create(teacher.getUserId(), "수학", START, END));
  }

  @Test
  void 수업에_학생을_추가한다() {
    Lesson lesson = saveLesson();

    AttendeeResponse response =
        attendeeCommandService.add(userId, lesson.getId(), student.getId(), null);

    assertThat(response.id()).isNotNull();
    assertThat(response.lessonId()).isEqualTo(lesson.getId());
    assertThat(response.studentId()).isEqualTo(student.getId());
    assertThat(response.createdAt()).isNotNull();
  }

  @Test
  void 존재하지_않는_수업에_학생_추가_시_예외가_발생한다() {
    assertThatThrownBy(
        () -> attendeeCommandService.add(userId, 999L, student.getId(), null))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void 다른_교사의_수업에_학생_추가_시_예외가_발생한다() {
    Teacher otherTeacher =
        teacherRepository.save(Teacher.create("otherteacher", "encodedPassword", "다른 선생님", ""));
    Lesson lesson = saveLesson();

    assertThatThrownBy(() -> attendeeCommandService.add(
        otherTeacher.getUserId(), lesson.getId(), student.getId(), null))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void 존재하지_않는_학생_추가_시_예외가_발생한다() {
    Lesson lesson = saveLesson();

    assertThatThrownBy(
        () -> attendeeCommandService.add(userId, lesson.getId(), 999L, null))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void 이미_추가된_학생_추가_시_중복이_무시된다() {
    Lesson lesson = saveLesson();
    attendeeCommandService.add(userId, lesson.getId(), student.getId(), null);

    // add() now silently ignores duplicates (try-catch in loop)
    // For single non-recurring lesson, it should still return the existing attendee
    AttendeeResponse response =
        attendeeCommandService.add(userId, lesson.getId(), student.getId(), null);

    assertThat(response).isNotNull();
    List<AttendeeResponse> attendees = attendeeQueryService.getAll(userId, lesson.getId());
    assertThat(attendees).hasSize(1);
  }

  @Test
  void 수업의_참가자_목록을_조회한다() {
    Lesson lesson = saveLesson();
    Student anotherStudent =
        studentRepository.save(Student.create(teacher.getUserId(), "김철수", null));
    attendeeCommandService.add(userId, lesson.getId(), student.getId(), null);
    attendeeCommandService.add(userId, lesson.getId(), anotherStudent.getId(), null);

    List<AttendeeResponse> attendees = attendeeQueryService.getAll(userId, lesson.getId());

    assertThat(attendees).hasSize(2);
  }

  @Test
  void 다른_수업의_참가자는_조회되지_않는다() {
    Lesson lesson1 = saveLesson();
    Lesson lesson2 = lessonRepository.save(Lesson.create(teacher.getUserId(), "영어", START, END));
    Student anotherStudent =
        studentRepository.save(Student.create(teacher.getUserId(), "김철수", null));
    attendeeCommandService.add(userId, lesson1.getId(), student.getId(), null);
    attendeeCommandService.add(userId, lesson2.getId(), anotherStudent.getId(), null);

    List<AttendeeResponse> attendees = attendeeQueryService.getAll(userId, lesson1.getId());

    assertThat(attendees).hasSize(1);
    assertThat(attendees.get(0).studentId()).isEqualTo(student.getId());
  }

  @Test
  void 수업_참가자를_삭제한다() {
    Lesson lesson = saveLesson();
    AttendeeResponse added =
        attendeeCommandService.add(userId, lesson.getId(), student.getId(), null);

    attendeeCommandService.remove(userId, lesson.getId(), added.id(), null);

    List<AttendeeResponse> attendees = attendeeQueryService.getAll(userId, lesson.getId());
    assertThat(attendees).isEmpty();
  }

  @Test
  void 존재하지_않는_참가자_삭제_시_예외가_발생한다() {
    Lesson lesson = saveLesson();

    assertThatThrownBy(
        () -> attendeeCommandService.remove(userId, lesson.getId(), 999L, null))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void 다른_교사의_수업_참가자_삭제_시_예외가_발생한다() {
    Teacher otherTeacher =
        teacherRepository.save(Teacher.create("otherteacher2", "encodedPassword", "다른 선생님2", ""));
    Lesson lesson = saveLesson();
    AttendeeResponse added =
        attendeeCommandService.add(userId, lesson.getId(), student.getId(), null);

    assertThatThrownBy(
        () -> attendeeCommandService.remove(otherTeacher.getUserId(), lesson.getId(), added.id(),
            null))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void 반복_수업에_학생을_ALL_scope로_추가하면_모든_수업에_추가된다() {
    RecurrenceCreateRequest recurrence = new RecurrenceCreateRequest(
        RecurrenceType.DAILY, 1, null, LocalDate.of(2026, 3, 20));
    lessonCommandService.create(userId,
        new LessonCreateRequest("수학", START, END, recurrence, null).toCommand(userId));

    List<Lesson> lessons = lessonRepository.findAll();
    assertThat(lessons).hasSizeGreaterThan(1);

    Lesson firstLesson = lessons.stream()
        .min(Comparator.comparing(Lesson::getStartTime)).orElseThrow();

    attendeeCommandService.add(userId, firstLesson.getId(), student.getId(), UpdateScope.ALL);

    for (Lesson l : lessonRepository.findAll()) {
      assertThat(l.getAttendees()).hasSize(1);
    }
  }

  @Test
  void 반복_수업에서_학생을_ALL_scope로_삭제하면_모든_수업에서_삭제된다() {
    RecurrenceCreateRequest recurrence = new RecurrenceCreateRequest(
        RecurrenceType.DAILY, 1, null, LocalDate.of(2026, 3, 20));
    lessonCommandService.create(userId,
        new LessonCreateRequest("수학", START, END, recurrence, List.of(student.getId()))
            .toCommand(userId));

    List<Lesson> lessons = lessonRepository.findAll();
    for (Lesson l : lessons) {
      assertThat(l.getAttendees()).hasSize(1);
    }

    Lesson firstLesson = lessons.stream()
        .min(Comparator.comparing(Lesson::getStartTime)).orElseThrow();
    Long attendeeId = firstLesson.getAttendees().get(0).getId();

    attendeeCommandService.remove(userId, firstLesson.getId(), attendeeId, UpdateScope.ALL);

    for (Lesson l : lessonRepository.findAll()) {
      assertThat(l.getAttendees()).isEmpty();
    }
  }

  @Test
  void 반복_수업에_학생을_SINGLE_scope로_추가하면_해당_수업에만_추가된다() {
    RecurrenceCreateRequest recurrence = new RecurrenceCreateRequest(
        RecurrenceType.DAILY, 1, null, LocalDate.of(2026, 3, 20));
    lessonCommandService.create(userId,
        new LessonCreateRequest("수학", START, END, recurrence, null).toCommand(userId));

    List<Lesson> lessons = lessonRepository.findAll();
    assertThat(lessons).hasSizeGreaterThan(1);

    Lesson firstLesson = lessons.stream()
        .min(Comparator.comparing(Lesson::getStartTime)).orElseThrow();

    attendeeCommandService.add(userId, firstLesson.getId(), student.getId(), UpdateScope.SINGLE);

    Lesson refreshedFirst = lessonRepository.findById(firstLesson.getId()).orElseThrow();
    assertThat(refreshedFirst.getAttendees()).hasSize(1);

    long othersWithAttendees = lessonRepository.findAll().stream()
        .filter(l -> !l.getId().equals(firstLesson.getId()))
        .filter(l -> !l.getAttendees().isEmpty())
        .count();
    assertThat(othersWithAttendees).isZero();
  }
}
