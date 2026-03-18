package com.teacher.agent.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class StudentTest {

  private static final UserId USER_ID = new UserId("teacher1");

  @Test
  void 이름과_메모로_학생을_생성한다() {
    // when
    Student student = Student.create(USER_ID, "홍길동", "성실한 학생");

    // then
    assertThat(student.getUserId()).isEqualTo(USER_ID);
    assertThat(student.getName()).isEqualTo("홍길동");
    assertThat(student.getMemo()).isEqualTo("성실한 학생");
  }

  @Test
  void 메모_없이_학생을_생성할_수_있다() {
    // when
    Student student = Student.create(USER_ID, "홍길동", null);

    // then
    assertThat(student.getName()).isEqualTo("홍길동");
    assertThat(student.getMemo()).isNull();
  }

  @Test
  void userId가_null이면_생성에_실패한다() {
    // when & then
    assertThatThrownBy(() -> Student.create(null, "홍길동", "메모"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void userId_값이_빈_문자열이면_생성에_실패한다() {
    // when & then
    assertThatThrownBy(() -> Student.create(new UserId(""), "홍길동", "메모"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 이름이_null이면_생성에_실패한다() {
    // when & then
    assertThatThrownBy(() -> Student.create(USER_ID, null, "메모"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 이름이_빈_문자열이면_생성에_실패한다() {
    // when & then
    assertThatThrownBy(() -> Student.create(USER_ID, "", "메모"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 이름이_공백이면_생성에_실패한다() {
    // when & then
    assertThatThrownBy(() -> Student.create(USER_ID, "   ", "메모"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 메모가_500자를_초과하면_생성에_실패한다() {
    // given
    String longMemo = "가".repeat(501);

    // when & then
    assertThatThrownBy(() -> Student.create(USER_ID, "홍길동", longMemo))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 메모가_500자이면_생성에_성공한다() {
    // given
    String maxMemo = "가".repeat(500);

    // when
    Student student = Student.create(USER_ID, "홍길동", maxMemo);

    // then
    assertThat(student.getMemo()).hasSize(500);
  }

  @Test
  void 학생_정보를_수정한다() {
    // given
    Student student = Student.create(USER_ID, "홍길동", "메모");

    // when
    student.update("김철수", "새 메모");

    // then
    assertThat(student.getName()).isEqualTo("김철수");
    assertThat(student.getMemo()).isEqualTo("새 메모");
  }

  @Test
  void 수정_시_이름이_빈_문자열이면_실패한다() {
    // given
    Student student = Student.create(USER_ID, "홍길동", "메모");

    // when & then
    assertThatThrownBy(() -> student.update("", "새 메모"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 수정_시_메모가_500자를_초과하면_실패한다() {
    // given
    Student student = Student.create(USER_ID, "홍길동", "메모");
    String longMemo = "가".repeat(501);

    // when & then
    assertThatThrownBy(() -> student.update("홍길동", longMemo))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
