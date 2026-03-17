package com.teacher.agent.service;

import com.teacher.agent.domain.Teacher;
import com.teacher.agent.domain.TeacherRepository;
import com.teacher.agent.dto.TeacherResponse;
import com.teacher.agent.dto.TeacherUpdateRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({TeacherQueryService.class, TeacherCommandService.class})
class TeacherServiceTest {

  @Autowired
  private TeacherQueryService teacherQueryService;

  @Autowired
  private TeacherCommandService teacherCommandService;

  @Autowired
  private TeacherRepository teacherRepository;

  @AfterEach
  void tearDown() {
    teacherRepository.deleteAllInBatch();
  }

  private Teacher createTeacher(String userId, String name, String subject) {
    return teacherRepository.save(Teacher.create(userId, "encodedPassword", name, subject));
  }

  @Test
  void 사용자_아이디로_교사를_조회한다() {
    createTeacher("teacher1", "김선생", "수학");

    TeacherResponse response = teacherQueryService.getByUserId("teacher1");

    assertThat(response.id()).isNotNull();
    assertThat(response.userId()).isEqualTo("teacher1");
    assertThat(response.name()).isEqualTo("김선생");
    assertThat(response.subject()).isEqualTo("수학");
    assertThat(response.createdAt()).isNotNull();
    assertThat(response.updatedAt()).isNotNull();
  }

  @Test
  void 존재하지_않는_사용자_아이디로_조회_시_예외가_발생한다() {
    assertThatThrownBy(() -> teacherQueryService.getByUserId("nonexistent"))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void 교사_프로필을_수정한다() {
    createTeacher("teacher1", "김선생", "수학");

    TeacherResponse response =
        teacherCommandService.updateByUserId("teacher1", new TeacherUpdateRequest("박선생", "영어"));

    assertThat(response.name()).isEqualTo("박선생");
    assertThat(response.subject()).isEqualTo("영어");
  }

  @Test
  void 교사_프로필_수정_시_과목을_null로_변경할_수_있다() {
    createTeacher("teacher1", "김선생", "수학");

    TeacherResponse response =
        teacherCommandService.updateByUserId("teacher1", new TeacherUpdateRequest("김선생", null));

    assertThat(response.subject()).isNull();
  }

  @Test
  void 존재하지_않는_교사의_프로필_수정_시_예외가_발생한다() {
    assertThatThrownBy(() -> teacherCommandService.updateByUserId("nonexistent",
        new TeacherUpdateRequest("박선생", "영어"))).isInstanceOf(ResponseStatusException.class);
  }
}
