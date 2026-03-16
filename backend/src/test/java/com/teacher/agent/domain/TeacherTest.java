package com.teacher.agent.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TeacherTest {

    @Test
    void 아이디와_비밀번호로_교사를_생성한다() {
        Teacher teacher = Teacher.create("teacher1", "encodedPassword");

        assertThat(teacher.getUsername()).isEqualTo("teacher1");
        assertThat(teacher.getPassword()).isEqualTo("encodedPassword");
    }

    @Test
    void 아이디가_null이면_생성에_실패한다() {
        assertThatThrownBy(() -> Teacher.create(null, "password"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 아이디가_빈_문자열이면_생성에_실패한다() {
        assertThatThrownBy(() -> Teacher.create("", "password"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 비밀번호가_null이면_생성에_실패한다() {
        assertThatThrownBy(() -> Teacher.create("teacher1", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 비밀번호가_빈_문자열이면_생성에_실패한다() {
        assertThatThrownBy(() -> Teacher.create("teacher1", ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 비밀번호를_변경한다() {
        Teacher teacher = Teacher.create("teacher1", "oldPassword");

        teacher.updatePassword("newPassword");

        assertThat(teacher.getPassword()).isEqualTo("newPassword");
    }

    @Test
    void 변경할_비밀번호가_null이면_실패한다() {
        Teacher teacher = Teacher.create("teacher1", "oldPassword");

        assertThatThrownBy(() -> teacher.updatePassword(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 변경할_비밀번호가_빈_문자열이면_실패한다() {
        Teacher teacher = Teacher.create("teacher1", "oldPassword");

        assertThatThrownBy(() -> teacher.updatePassword(""))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
