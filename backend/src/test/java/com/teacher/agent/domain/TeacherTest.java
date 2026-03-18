package com.teacher.agent.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class TeacherTest {

  @Test
  void 교사를_생성한다() {
    // when
    Teacher teacher = Teacher.create("teacher1", "encodedPassword", "김선생", "수학");

    // then
    assertThat(teacher.getUserId()).isEqualTo(new UserId("teacher1"));
    assertThat(teacher.getPassword()).isEqualTo("encodedPassword");
    assertThat(teacher.getName()).isEqualTo("김선생");
    assertThat(teacher.getSubject()).isEqualTo("수학");
  }

  @Test
  void 과목_없이_교사를_생성한다() {
    // when
    Teacher teacher = Teacher.create("teacher1", "encodedPassword", "김선생", null);

    // then
    assertThat(teacher.getSubject()).isNull();
  }

  @Test
  void 빈_과목으로_교사를_생성한다() {
    // when
    Teacher teacher = Teacher.create("teacher1", "encodedPassword", "김선생", "");

    // then
    assertThat(teacher.getSubject()).isEmpty();
  }

  @Test
  void 아이디가_null이면_생성에_실패한다() {
    // when & then
    assertThatThrownBy(() -> Teacher.create(null, "password", "김선생", "수학"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 아이디가_빈_문자열이면_생성에_실패한다() {
    // when & then
    assertThatThrownBy(() -> Teacher.create("", "password", "김선생", "수학"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 비밀번호가_null이면_생성에_실패한다() {
    // when & then
    assertThatThrownBy(() -> Teacher.create("teacher1", null, "김선생", "수학"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 비밀번호가_빈_문자열이면_생성에_실패한다() {
    // when & then
    assertThatThrownBy(() -> Teacher.create("teacher1", "", "김선생", "수학"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 이름이_null이면_생성에_실패한다() {
    // when & then
    assertThatThrownBy(() -> Teacher.create("teacher1", "password", null, "수학"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 이름이_빈_문자열이면_생성에_실패한다() {
    // when & then
    assertThatThrownBy(() -> Teacher.create("teacher1", "password", "", "수학"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 과목이_100자를_초과하면_생성에_실패한다() {
    // given
    String longSubject = "a".repeat(101);

    // when & then
    assertThatThrownBy(() -> Teacher.create("teacher1", "password", "김선생", longSubject))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 비밀번호를_변경한다() {
    // given
    Teacher teacher = Teacher.create("teacher1", "oldPassword", "김선생", "수학");

    // when
    teacher.updatePassword("newPassword");

    // then
    assertThat(teacher.getPassword()).isEqualTo("newPassword");
  }

  @Test
  void 변경할_비밀번호가_null이면_실패한다() {
    // given
    Teacher teacher = Teacher.create("teacher1", "oldPassword", "김선생", "수학");

    // when & then
    assertThatThrownBy(() -> teacher.updatePassword(null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 변경할_비밀번호가_빈_문자열이면_실패한다() {
    // given
    Teacher teacher = Teacher.create("teacher1", "oldPassword", "김선생", "수학");

    // when & then
    assertThatThrownBy(() -> teacher.updatePassword(""))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 프로필을_수정한다() {
    // given
    Teacher teacher = Teacher.create("teacher1", "password", "김선생", "수학");

    // when
    teacher.updateProfile("박선생", "영어");

    // then
    assertThat(teacher.getName()).isEqualTo("박선생");
    assertThat(teacher.getSubject()).isEqualTo("영어");
  }

  @Test
  void 프로필_수정_시_이름이_null이면_실패한다() {
    // given
    Teacher teacher = Teacher.create("teacher1", "password", "김선생", "수학");

    // when & then
    assertThatThrownBy(() -> teacher.updateProfile(null, "영어"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 프로필_수정_시_이름이_빈_문자열이면_실패한다() {
    // given
    Teacher teacher = Teacher.create("teacher1", "password", "김선생", "수학");

    // when & then
    assertThatThrownBy(() -> teacher.updateProfile("", "영어"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 프로필_수정_시_과목이_100자를_초과하면_실패한다() {
    // given
    Teacher teacher = Teacher.create("teacher1", "password", "김선생", "수학");
    String longSubject = "a".repeat(101);

    // when & then
    assertThatThrownBy(() -> teacher.updateProfile("박선생", longSubject))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 프로필_수정_시_과목을_null로_변경할_수_있다() {
    // given
    Teacher teacher = Teacher.create("teacher1", "password", "김선생", "수학");

    // when
    teacher.updateProfile("박선생", null);

    // then
    assertThat(teacher.getSubject()).isNull();
  }
}
