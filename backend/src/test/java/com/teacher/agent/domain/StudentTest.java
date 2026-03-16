package com.teacher.agent.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StudentTest {

    @Test
    void 이름과_메모로_학생을_생성한다() {
        Student student = Student.create(1L, "홍길동", "성실한 학생");

        assertThat(student.getTeacherId()).isEqualTo(1L);
        assertThat(student.getName()).isEqualTo("홍길동");
        assertThat(student.getMemo()).isEqualTo("성실한 학생");
    }

    @Test
    void 메모_없이_학생을_생성할_수_있다() {
        Student student = Student.create(1L, "홍길동", null);

        assertThat(student.getName()).isEqualTo("홍길동");
        assertThat(student.getMemo()).isNull();
    }

    @Test
    void 선생님_ID가_0이면_생성에_실패한다() {
        assertThatThrownBy(() -> Student.create(0L, "홍길동", "메모"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 이름이_null이면_생성에_실패한다() {
        assertThatThrownBy(() -> Student.create(1L, null, "메모"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 이름이_빈_문자열이면_생성에_실패한다() {
        assertThatThrownBy(() -> Student.create(1L, "", "메모"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 이름이_공백이면_생성에_실패한다() {
        assertThatThrownBy(() -> Student.create(1L, "   ", "메모"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 메모가_500자를_초과하면_생성에_실패한다() {
        String longMemo = "가".repeat(501);

        assertThatThrownBy(() -> Student.create(1L, "홍길동", longMemo))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 메모가_500자이면_생성에_성공한다() {
        String maxMemo = "가".repeat(500);

        Student student = Student.create(1L, "홍길동", maxMemo);

        assertThat(student.getMemo()).hasSize(500);
    }

    @Test
    void 학생_정보를_수정한다() {
        Student student = Student.create(1L, "홍길동", "메모");

        student.update("김철수", "새 메모");

        assertThat(student.getName()).isEqualTo("김철수");
        assertThat(student.getMemo()).isEqualTo("새 메모");
    }

    @Test
    void 수정_시_이름이_빈_문자열이면_실패한다() {
        Student student = Student.create(1L, "홍길동", "메모");

        assertThatThrownBy(() -> student.update("", "새 메모"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 수정_시_메모가_500자를_초과하면_실패한다() {
        Student student = Student.create(1L, "홍길동", "메모");
        String longMemo = "가".repeat(501);

        assertThatThrownBy(() -> student.update("홍길동", longMemo))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
