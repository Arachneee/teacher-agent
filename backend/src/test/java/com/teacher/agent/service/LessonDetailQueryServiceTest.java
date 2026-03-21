package com.teacher.agent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.teacher.agent.domain.Feedback;
import com.teacher.agent.domain.FeedbackRepository;
import com.teacher.agent.domain.Lesson;
import com.teacher.agent.domain.LessonRepository;
import com.teacher.agent.domain.Student;
import com.teacher.agent.domain.StudentRepository;
import com.teacher.agent.domain.Teacher;
import com.teacher.agent.domain.TeacherRepository;
import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.LessonDetailResponse;
import com.teacher.agent.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({LessonQueryService.class, AttendeeCommandService.class, LessonDetailQueryService.class})
class LessonDetailQueryServiceTest {

  @Autowired
  private LessonDetailQueryService lessonDetailQueryService;

  @Autowired
  private AttendeeCommandService attendeeCommandService;

  @Autowired
  private LessonRepository lessonRepository;

  @Autowired
  private StudentRepository studentRepository;

  @Autowired
  private TeacherRepository teacherRepository;

  @Autowired
  private FeedbackRepository feedbackRepository;

  private static final LocalDateTime START = LocalDateTime.of(2026, 3, 16, 9, 0);
  private static final LocalDateTime END = LocalDateTime.of(2026, 3, 16, 10, 0);

  private Teacher teacher;
  private UserId userId;
  private Lesson lesson;

  @BeforeEach
  void setUp() {
    teacher = teacherRepository.save(Teacher.create("testteacher", "password", "선생님", ""));
    userId = teacher.getUserId();
    lesson = lessonRepository.save(Lesson.create(userId, "수학", START, END));
  }

  @AfterEach
  void tearDown() {
    feedbackRepository.deleteAll();
    lessonRepository.deleteAll();
    studentRepository.deleteAllInBatch();
    teacherRepository.deleteAllInBatch();
  }

  @Test
  void 수업_상세_조회_시_참가자와_피드백이_반환된다() {
    Student student = studentRepository.save(Student.create(userId, "홍길동", "메모"));
    attendeeCommandService.add(userId, lesson.getId(), student.getId(), null);

    LessonDetailResponse response = lessonDetailQueryService.getDetail(userId, lesson.getId());

    assertThat(response.id()).isEqualTo(lesson.getId());
    assertThat(response.title()).isEqualTo("수학");
    assertThat(response.attendees()).hasSize(1);

    LessonDetailResponse.AttendeeDetailItem attendeeItem = response.attendees().get(0);
    assertThat(attendeeItem.student().id()).isEqualTo(student.getId());
    assertThat(attendeeItem.student().name()).isEqualTo("홍길동");
    assertThat(attendeeItem.feedback().studentId()).isEqualTo(student.getId());
    assertThat(attendeeItem.feedback().lessonId()).isEqualTo(lesson.getId());
    assertThat(attendeeItem.feedback().keywords()).isEmpty();
    assertThat(attendeeItem.feedback().liked()).isFalse();
  }

  @Test
  void 참가자가_없는_수업은_빈_목록이_반환된다() {
    LessonDetailResponse response = lessonDetailQueryService.getDetail(userId, lesson.getId());

    assertThat(response.attendees()).isEmpty();
  }

  @Test
  void 여러_참가자가_각자의_피드백을_가진다() {
    Student student1 = studentRepository.save(Student.create(userId, "홍길동", null));
    Student student2 = studentRepository.save(Student.create(userId, "김철수", null));
    attendeeCommandService.add(userId, lesson.getId(), student1.getId(), null);
    attendeeCommandService.add(userId, lesson.getId(), student2.getId(), null);

    LessonDetailResponse response = lessonDetailQueryService.getDetail(userId, lesson.getId());

    assertThat(response.attendees()).hasSize(2);
    assertThat(response.attendees().get(0).student().id()).isEqualTo(student1.getId());
    assertThat(response.attendees().get(1).student().id()).isEqualTo(student2.getId());
  }

  @Test
  void 피드백에_키워드가_있으면_키워드_목록이_반환된다() {
    Student student = studentRepository.save(Student.create(userId, "홍길동", null));
    attendeeCommandService.add(userId, lesson.getId(), student.getId(), null);
    Long feedbackId = feedbackRepository.findByStudentIdAndLessonId(student.getId(), lesson.getId())
        .orElseThrow().getId();
    Feedback feedback = feedbackRepository.findById(feedbackId).orElseThrow();
    feedback.addKeyword("집중력");
    feedback.addKeyword("수학");
    feedbackRepository.save(feedback);

    LessonDetailResponse response = lessonDetailQueryService.getDetail(userId, lesson.getId());

    assertThat(response.attendees().get(0).feedback().keywords()).hasSize(2);
    assertThat(response.attendees().get(0).feedback().keywords()).extracting(k -> k.keyword())
        .containsExactly("집중력", "수학");
  }

  @Test
  void 피드백에_좋아요가_있으면_liked가_true다() {
    Student student = studentRepository.save(Student.create(userId, "홍길동", null));
    attendeeCommandService.add(userId, lesson.getId(), student.getId(), null);
    Long feedbackId = feedbackRepository.findByStudentIdAndLessonId(student.getId(), lesson.getId())
        .orElseThrow().getId();
    Feedback feedback = feedbackRepository.findById(feedbackId).orElseThrow();
    feedback.updateAiContent("훌륭한 학생입니다.");
    feedback.like();
    feedbackRepository.save(feedback);

    LessonDetailResponse response = lessonDetailQueryService.getDetail(userId, lesson.getId());

    assertThat(response.attendees().get(0).feedback().liked()).isTrue();
  }

  @Test
  void 다른_교사의_수업은_조회되지_않는다() {
    Teacher otherTeacher =
        teacherRepository.save(Teacher.create("other", "password", "다른 선생님", ""));

    assertThatThrownBy(
        () -> lessonDetailQueryService.getDetail(otherTeacher.getUserId(), lesson.getId()))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void 참가자_재추가_시_기존_피드백이_유지된다() {
    Student student = studentRepository.save(Student.create(userId, "홍길동", null));
    attendeeCommandService.add(userId, lesson.getId(), student.getId(), null);
    Long originalFeedbackId = feedbackRepository
        .findByStudentIdAndLessonId(student.getId(), lesson.getId()).orElseThrow().getId();

    attendeeCommandService.remove(userId, lesson.getId(),
        lessonDetailQueryService.getDetail(userId, lesson.getId()).attendees().get(0).attendeeId(),
        null);
    attendeeCommandService.add(userId, lesson.getId(), student.getId(), null);

    LessonDetailResponse response = lessonDetailQueryService.getDetail(userId, lesson.getId());
    assertThat(response.attendees().get(0).feedback().id()).isEqualTo(originalFeedbackId);
  }
}
