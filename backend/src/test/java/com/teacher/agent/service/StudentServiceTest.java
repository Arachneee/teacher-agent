package com.teacher.agent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.teacher.agent.domain.StudentRepository;
import com.teacher.agent.domain.Teacher;
import com.teacher.agent.domain.TeacherRepository;
import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.StudentCreateRequest;
import com.teacher.agent.dto.StudentResponse;
import com.teacher.agent.dto.StudentUpdateRequest;
import com.teacher.agent.exception.BusinessException;
import java.util.List;
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
@Import({StudentQueryService.class, StudentCommandService.class})
class StudentServiceTest {

  @Autowired
  private StudentQueryService studentQueryService;

  @Autowired
  private StudentCommandService studentCommandService;

  @Autowired
  private StudentRepository studentRepository;

  @Autowired
  private TeacherRepository teacherRepository;

  private UserId userId;
  private UserId otherUserId;

  @BeforeEach
  void setUp() {
    Teacher teacher = teacherRepository.save(Teacher.create("teacher1", "encoded", "선생님1", "수학"));
    Teacher otherTeacher =
        teacherRepository.save(Teacher.create("teacher2", "encoded", "선생님2", "영어"));
    userId = teacher.getUserId();
    otherUserId = otherTeacher.getUserId();
  }

  @AfterEach
  void tearDown() {
    studentRepository.deleteAllInBatch();
    teacherRepository.deleteAllInBatch();
  }

  @Test
  void 학생을_생성한다() {
    StudentResponse response =
        studentCommandService.create(userId, new StudentCreateRequest("홍길동", "성실한 학생"));

    assertThat(response.id()).isNotNull();
    assertThat(response.name()).isEqualTo("홍길동");
    assertThat(response.memo()).isEqualTo("성실한 학생");
    assertThat(response.createdAt()).isNotNull();
    assertThat(response.updatedAt()).isNotNull();
  }

  @Test
  void 내_학생_목록을_조회한다() {
    studentCommandService.create(userId, new StudentCreateRequest("홍길동", null));
    studentCommandService.create(userId, new StudentCreateRequest("김철수", null));
    studentCommandService.create(otherUserId, new StudentCreateRequest("다른선생학생", null));

    List<StudentResponse> students = studentQueryService.getAll(userId);

    assertThat(students).hasSize(2);
  }

  @Test
  void 학생을_단건_조회한다() {
    StudentResponse created =
        studentCommandService.create(userId, new StudentCreateRequest("홍길동", "메모"));

    StudentResponse found = studentQueryService.getOne(userId, created.id());

    assertThat(found.id()).isEqualTo(created.id());
    assertThat(found.name()).isEqualTo("홍길동");
    assertThat(found.memo()).isEqualTo("메모");
  }

  @Test
  void 다른_선생님_학생_조회_시_예외가_발생한다() {
    StudentResponse created =
        studentCommandService.create(userId, new StudentCreateRequest("홍길동", "메모"));

    assertThatThrownBy(() -> studentQueryService.getOne(otherUserId, created.id()))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void 존재하지_않는_학생_조회_시_예외가_발생한다() {
    assertThatThrownBy(() -> studentQueryService.getOne(userId, 999L))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void 학생_정보를_수정한다() {
    StudentResponse created =
        studentCommandService.create(userId, new StudentCreateRequest("홍길동", "메모"));

    StudentResponse updated =
        studentCommandService.update(userId, created.id(), new StudentUpdateRequest("김철수", "새 메모"));

    assertThat(updated.id()).isEqualTo(created.id());
    assertThat(updated.name()).isEqualTo("김철수");
    assertThat(updated.memo()).isEqualTo("새 메모");
  }

  @Test
  void 다른_선생님_학생_수정_시_예외가_발생한다() {
    StudentResponse created =
        studentCommandService.create(userId, new StudentCreateRequest("홍길동", "메모"));

    assertThatThrownBy(() -> studentCommandService.update(otherUserId, created.id(),
        new StudentUpdateRequest("이름", "메모"))).isInstanceOf(BusinessException.class);
  }

  @Test
  void 존재하지_않는_학생_수정_시_예외가_발생한다() {
    assertThatThrownBy(
        () -> studentCommandService.update(userId, 999L, new StudentUpdateRequest("이름", "메모")))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void 학생을_삭제한다() {
    StudentResponse created =
        studentCommandService.create(userId, new StudentCreateRequest("홍길동", null));

    studentCommandService.delete(userId, created.id());

    assertThatThrownBy(() -> studentQueryService.getOne(userId, created.id()))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void 다른_선생님_학생_삭제_시_예외가_발생한다() {
    StudentResponse created =
        studentCommandService.create(userId, new StudentCreateRequest("홍길동", null));

    assertThatThrownBy(() -> studentCommandService.delete(otherUserId, created.id()))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void 존재하지_않는_학생_삭제_시_예외가_발생한다() {
    assertThatThrownBy(() -> studentCommandService.delete(userId, 999L))
        .isInstanceOf(BusinessException.class);
  }
}
