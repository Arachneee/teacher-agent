package com.teacher.agent.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class UserIdTest {

  @Test
  void UserId를_생성한다() {
    // when
    UserId userId = new UserId("teacher1");

    // then
    assertThat(userId.value()).isEqualTo("teacher1");
  }

  @Test
  void null로_생성하면_실패한다() {
    // when & then
    assertThatThrownBy(() -> new UserId(null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 빈_문자열로_생성하면_실패한다() {
    // when & then
    assertThatThrownBy(() -> new UserId(""))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 공백_문자열로_생성하면_실패한다() {
    // when & then
    assertThatThrownBy(() -> new UserId("   "))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void 같은_값의_UserId는_동등하다() {
    // given
    UserId userId1 = new UserId("teacher1");
    UserId userId2 = new UserId("teacher1");

    // then
    assertThat(userId1).isEqualTo(userId2);
    assertThat(userId1.hashCode()).isEqualTo(userId2.hashCode());
  }

  @Test
  void 다른_값의_UserId는_동등하지_않다() {
    // given
    UserId userId1 = new UserId("teacher1");
    UserId userId2 = new UserId("teacher2");

    // then
    assertThat(userId1).isNotEqualTo(userId2);
  }
}
