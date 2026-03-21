package com.teacher.agent.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UpdateScopeTest {

  @Test
  void SINGLE_값이_존재한다() {
    assertThat(UpdateScope.SINGLE).isNotNull();
    assertThat(UpdateScope.SINGLE.name()).isEqualTo("SINGLE");
  }

  @Test
  void THIS_AND_FOLLOWING_값이_존재한다() {
    assertThat(UpdateScope.THIS_AND_FOLLOWING).isNotNull();
    assertThat(UpdateScope.THIS_AND_FOLLOWING.name()).isEqualTo("THIS_AND_FOLLOWING");
  }

  @Test
  void ALL_값이_존재한다() {
    assertThat(UpdateScope.ALL).isNotNull();
    assertThat(UpdateScope.ALL.name()).isEqualTo("ALL");
  }

  @Test
  void 모든_값이_3개이다() {
    assertThat(UpdateScope.values()).hasSize(3);
  }

  @Test
  void 문자열로_변환할_수_있다() {
    assertThat(UpdateScope.valueOf("SINGLE")).isEqualTo(UpdateScope.SINGLE);
    assertThat(UpdateScope.valueOf("THIS_AND_FOLLOWING")).isEqualTo(UpdateScope.THIS_AND_FOLLOWING);
    assertThat(UpdateScope.valueOf("ALL")).isEqualTo(UpdateScope.ALL);
  }
}
