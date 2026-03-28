package com.teacher.agent.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class UserEventTest {

  @Test
  void 이벤트를_정상_생성한다() {
    UserEvent userEvent = UserEvent.create("admin", "feedback_copy", "{\"feedbackId\": 42}");

    assertThat(userEvent.getUserId()).isEqualTo("admin");
    assertThat(userEvent.getEventType()).isEqualTo("feedback_copy");
    assertThat(userEvent.getMetadata()).isEqualTo("{\"feedbackId\": 42}");
  }

  @Test
  void metadata가_null이어도_정상_생성한다() {
    UserEvent userEvent = UserEvent.create("admin", "feedback_edit", null);

    assertThat(userEvent.getUserId()).isEqualTo("admin");
    assertThat(userEvent.getEventType()).isEqualTo("feedback_edit");
    assertThat(userEvent.getMetadata()).isNull();
  }

  @Test
  void eventType이_blank이면_생성에_실패한다() {
    assertThatThrownBy(() -> UserEvent.create("admin", "  ", null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void eventType이_null이면_생성에_실패한다() {
    assertThatThrownBy(() -> UserEvent.create("admin", null, null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void userId가_blank이면_생성에_실패한다() {
    assertThatThrownBy(() -> UserEvent.create("  ", "feedback_copy", null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void userId가_null이면_생성에_실패한다() {
    assertThatThrownBy(() -> UserEvent.create(null, "feedback_copy", null))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
