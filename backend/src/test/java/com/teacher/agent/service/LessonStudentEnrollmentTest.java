package com.teacher.agent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.teacher.agent.domain.Attendee;
import com.teacher.agent.domain.Lesson;
import com.teacher.agent.domain.Student;
import com.teacher.agent.domain.Teacher;
import com.teacher.agent.domain.repository.FeedbackRepository;
import com.teacher.agent.domain.repository.LessonRepository;
import com.teacher.agent.domain.repository.StudentRepository;
import com.teacher.agent.domain.repository.TeacherRepository;
import com.teacher.agent.domain.vo.RecurrenceType;
import com.teacher.agent.domain.vo.SchoolGrade;
import com.teacher.agent.dto.LessonCreateRequest;
import com.teacher.agent.dto.LessonResponse;
import com.teacher.agent.dto.RecurrenceCreateRequest;
import com.teacher.agent.exception.BusinessException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class LessonStudentEnrollmentTest {

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
  private Student student1;
  private Student student2;

  @BeforeEach
  void setUp() {
    teacher = teacherRepository.save(Teacher.create("testteacher", "password", "교사", null));
    student1 = studentRepository
        .save(Student.create(teacher.getUserId(), "학생1", null, SchoolGrade.ELEMENTARY_1));
    student2 = studentRepository
        .save(Student.create(teacher.getUserId(), "학생2", null, SchoolGrade.ELEMENTARY_1));
  }

  @AfterEach
  void tearDown() {
    feedbackRepository.deleteAll();
    lessonRepository.deleteAll();
    studentRepository.deleteAll();
    teacherRepository.deleteAll();
  }

  @Test
  void 수업_생성_시_학생을_수강생으로_등록한다() {
    LessonResponse response = lessonCommandService.create(teacher.getUserId(),
        new LessonCreateRequest("수학", START, END, null, List.of(student1.getId()))
            .toCommand(teacher.getUserId()));

    Lesson lesson = lessonRepository.findById(response.id()).orElseThrow();
    List<Attendee> attendees = lesson.getAttendees();

    assertThat(attendees).hasSize(1);
    assertThat(attendees.get(0).getStudentId()).isEqualTo(student1.getId());
  }

  @Test
  void 수업_생성_시_여러_학생을_수강생으로_등록한다() {
    LessonResponse response = lessonCommandService.create(teacher.getUserId(),
        new LessonCreateRequest("수학", START, END, null,
            List.of(student1.getId(), student2.getId())).toCommand(teacher.getUserId()));

    Lesson lesson = lessonRepository.findById(response.id()).orElseThrow();
    List<Attendee> attendees = lesson.getAttendees();

    assertThat(attendees).hasSize(2);
    assertThat(attendees).extracting(Attendee::getStudentId)
        .containsExactlyInAnyOrder(student1.getId(), student2.getId());
  }

  @Test
  void 수업_생성_시_수강생_등록과_함께_피드백이_생성된다() {
    LessonResponse response = lessonCommandService.create(teacher.getUserId(),
        new LessonCreateRequest("수학", START, END, null, List.of(student1.getId()))
            .toCommand(teacher.getUserId()));

    assertThat(feedbackRepository.findByStudentIdAndLessonId(student1.getId(), response.id()))
        .isPresent();
  }

  @Test
  void 반복_수업_생성_시_모든_수업에_수강생이_등록된다() {
    RecurrenceCreateRequest recurrence = new RecurrenceCreateRequest(
        RecurrenceType.DAILY, 1, null, LocalDate.of(2026, 3, 18));

    lessonCommandService.create(teacher.getUserId(),
        new LessonCreateRequest("수학", START, END, recurrence,
            List.of(student1.getId(), student2.getId())).toCommand(teacher.getUserId()));

    List<Lesson> lessons = lessonRepository.findAll();
    assertThat(lessons).hasSize(3);

    for (Lesson lesson : lessons) {
      assertThat(lesson.getAttendees()).hasSize(2);
      assertThat(lesson.getAttendees()).extracting(Attendee::getStudentId)
          .containsExactlyInAnyOrder(student1.getId(), student2.getId());
    }
  }

  @Test
  void 반복_수업_생성_시_모든_수업에_피드백이_생성된다() {
    RecurrenceCreateRequest recurrence = new RecurrenceCreateRequest(
        RecurrenceType.DAILY, 1, null, LocalDate.of(2026, 3, 18));

    lessonCommandService.create(teacher.getUserId(),
        new LessonCreateRequest("수학", START, END, recurrence, List.of(student1.getId()))
            .toCommand(teacher.getUserId()));

    List<Lesson> lessons = lessonRepository.findAll();
    assertThat(lessons).hasSize(3);

    for (Lesson lesson : lessons) {
      assertThat(feedbackRepository.findByStudentIdAndLessonId(student1.getId(), lesson.getId()))
          .isPresent();
    }
  }

  @Test
  void 주간_반복_수업_생성_시_모든_수업에_수강생이_등록된다() {
    RecurrenceCreateRequest recurrence = new RecurrenceCreateRequest(
        RecurrenceType.WEEKLY, 1, List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
        LocalDate.of(2026, 3, 25));

    lessonCommandService.create(teacher.getUserId(),
        new LessonCreateRequest("수학", START, END, recurrence, List.of(student1.getId()))
            .toCommand(teacher.getUserId()));

    List<Lesson> lessons = lessonRepository.findAll();
    assertThat(lessons).hasSizeGreaterThan(1);

    for (Lesson lesson : lessons) {
      assertThat(lesson.getAttendees()).hasSize(1);
      assertThat(lesson.getAttendees().get(0).getStudentId()).isEqualTo(student1.getId());
    }
  }

  @Test
  void 다른_교사의_학생으로_수강생_등록_시_예외가_발생한다() {
    Teacher otherTeacher = teacherRepository.save(
        Teacher.create("otherteacher", "password", "다른교사", null));
    Student otherStudent = studentRepository.save(
        Student.create(otherTeacher.getUserId(), "다른학생", null, SchoolGrade.ELEMENTARY_1));

    assertThatThrownBy(() -> lessonCommandService.create(teacher.getUserId(),
        new LessonCreateRequest("수학", START, END, null, List.of(otherStudent.getId()))
            .toCommand(teacher.getUserId())))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void studentIds가_null이면_수강생_없이_수업만_생성된다() {
    LessonResponse response = lessonCommandService.create(teacher.getUserId(),
        new LessonCreateRequest("수학", START, END, null, null).toCommand(teacher.getUserId()));

    Lesson lesson = lessonRepository.findById(response.id()).orElseThrow();
    assertThat(lesson.getAttendees()).isEmpty();
  }

  @Test
  void studentIds가_빈_리스트이면_수강생_없이_수업만_생성된다() {
    LessonResponse response = lessonCommandService.create(teacher.getUserId(),
        new LessonCreateRequest("수학", START, END, null, List.of()).toCommand(teacher.getUserId()));

    Lesson lesson = lessonRepository.findById(response.id()).orElseThrow();
    assertThat(lesson.getAttendees()).isEmpty();
  }
}
